package net.reworlds.modifiedbosses.boss;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.bossbars.BossBar;
import net.reworlds.modifiedbosses.event.vekster.SuckingEvent;
import net.reworlds.modifiedbosses.respawn.BossTemplate;
import net.reworlds.modifiedbosses.respawn.Bosses;
import net.reworlds.modifiedbosses.utils.DateFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Boss implements Listener {

    @Getter
    protected final LivingEntity boss;
    @Getter
    protected final String bossName;
    protected final String bossNameColor;
    protected final Map<UUID, Double> damageByPlayers = new HashMap<>();
    protected BukkitTask task;
    protected long startTime;
    protected boolean battleStarted;
    @Setter(AccessLevel.PROTECTED)
    protected int radius = 200;
    @Setter(AccessLevel.PROTECTED)
    protected double maxDamagePerHit = Double.MAX_VALUE;
    @Setter(AccessLevel.PROTECTED)
    protected double minimumDamageToReward = 50D;
    @Setter(AccessLevel.PROTECTED)
    protected boolean allowExplodeDamage = true;
    protected org.bukkit.boss.BossBar bar;
    @Setter(AccessLevel.PROTECTED)
    private double saveDamagePercent;

    public Boss(LivingEntity boss, String bossName, String bossNameColor) {
        this.boss = boss;
        this.bossName = bossName;
        this.bossNameColor = bossNameColor;
    }

    /*
     * Settings Part
     */

    protected org.bukkit.boss.BossBar setBossBar(@NotNull String title, @NotNull BarColor color, @NotNull BarStyle style) {
        if (boss instanceof org.bukkit.entity.Boss instance) {
            bar = instance.getBossBar();
            if (bar != null) {
                bar.setTitle(title);
                bar.setColor(color);
                bar.setStyle(style);
            }
        }
        if (bar == null) {
            return new BossBar().setTitle(title).setColor(color).setStyle(style)
                    .setRadius(radius).setEntity(boss, true)
                    .build();
        }
        return bar;
    }

    protected void setAttributes(@NotNull Map<@NotNull Attribute, @NotNull Integer> attributes) {
        attributes.forEach((attribute, value) -> {
            AttributeInstance instance = boss.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(value);
            }
        });
    }

    protected abstract void setSettings();

    /*
     * Damage Part
     */

    public void damagePlayer(@NotNull LivingEntity damaged, double damage) {
        if (damaged.isDead() || boss.isDead()) {
            return;
        }

        if (!(damaged instanceof Player player)) {
            return;
        }
        if (!getDamagers().contains(player)) {
            return;
        }

        double health = damaged.getHealth();
        if (damage >= health) {
            damaged.damage(0.01, boss);
            damaged.setHealth(0);
            if (boss.getHealth() > boss.getMaxHealth() * 0.1) {
                removeDamager(player);
            }
            return;
        }

        damaged.damage(0.01, boss);
        try {
            damaged.setHealth(health - damage);
        } catch (IllegalArgumentException exception) {
            damaged.setHealth(0);
            if (boss.getHealth() > boss.getMaxHealth() * 0.1) {
                removeDamager(player);
            }
        }
    }

    protected void addDamage(@NotNull Player player, double damage) {
        if (!getDamagers().contains(player)) {
            player.sendMessage("§eВы вступили в бой с боссом " + bossNameColor + bossName + "§e!§r");
            player.sendMessage("§cВ случае побега с поля битвы вы будете убиты!§r");
        }
        putDamager(player, damage);
    }

    /*
     * Utils Part
     */

    public void removeDamager(Player player) {
        damageByPlayers.remove(player.getUniqueId());
    }

    public void clearDamagers() {
        damageByPlayers.clear();
    }

    public List<Player> getDamagers() {
        List<UUID> uuids = new ArrayList<>(damageByPlayers.keySet());
        return getUUIDPlayers(uuids);
    }

    public void putDamager(Player player, double damage) {
        UUID uuid = player.getUniqueId();
        damageByPlayers.put(uuid, damageByPlayers.getOrDefault(uuid, 0D) + damage);
    }

    public boolean isNear(@NotNull Player player) {
        return isNear(player.getLocation());
    }

    public boolean isNear(@NotNull Location location) {
        try {
            return boss.getLocation().distance(location) < radius;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Player> getTop(int limit) {
        List<UUID> collect = damageByPlayers.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed()).limit(limit).map(Map.Entry::getKey)
                .toList();
        return getUUIDPlayers(collect);
    }

    @NotNull
    private List<Player> getUUIDPlayers(List<UUID> collect) {
        List<Player> players = new ArrayList<>();
        collect.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                damageByPlayers.remove(uuid);
                return;
            }
            players.add(player);
        });
        return players;
    }

    protected boolean isExplosionCause(@NotNull EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;
    }

    protected void giveOrDrop(@NotNull Player player, @NotNull ItemStack item) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        } else {
            Item droppedItem = player.getLocation().getWorld().dropItem(player.getLocation(), item);
            droppedItem.addScoreboardTag(player.getName());
        }
    }

    /*
     * Battle Part
     */

    protected void startBattle() {
        if (battleStarted) {
            return;
        }

        battleStarted = true;
        startTime = System.currentTimeMillis();
    }

    protected void stopBattle() {
        if (!battleStarted) {
            return;
        }
        battleStarted = false;

        if (boss.isDead()) {
            BossTemplate template = Bosses.getBosses().get(bossName);
            if (template != null) {
                template.setDeathTime(System.currentTimeMillis());
            }
            bossDeadMessage();
            rewardPlayers();
            clearDamagers();
            task.cancel();
            return;
        }

        AttributeInstance attribute = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute != null) {
            boss.setHealth(attribute.getBaseValue());
        }
        setSettings();
        boss.getNearbyEntities(radius, radius, radius).forEach(entity -> entity.sendMessage("§cВсе атакующие были убиты. Босс восстановлен."));
    }

    public abstract void removeBoss();

    public void remove() {
        stopBattle();
        boss.remove();
    }

    protected abstract void rewardPlayers();

    protected void bossDeadMessage() {
        String totalTime = DateFormatter.formatMillis(System.currentTimeMillis() - startTime);
        TextComponent bossDefeatedText = Component.text("§b" +
                bar.getTitle().substring(0, bar.getTitle().indexOf(':')) + " был повержен!");
        TextComponent timeSpentText = Component.text("§7Затраченное время: " + totalTime);

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(bossDefeatedText);
            player.sendMessage(timeSpentText);
        });

        List<Player> topList = getTop(10);
        Bukkit.getPluginManager().callEvent(new SuckingEvent(topList));
        getDamagers().forEach(player -> {
            player.sendMessage(Component.text("§e===== §2TOP DAMAGE §e====="));
            for (int i = 0; i < topList.size(); i++) {
                String color;
                String colorDamage;
                Player top = topList.get(i);
                if (player.equals(top)) {
                    color = "§a";
                    colorDamage = "§b";
                } else {
                    color = "§7";
                    colorDamage = "§7";
                }
                player.sendMessage(Component.text(color + (i + 1) + ". " + top.getName() + " " + colorDamage + damageByPlayers.get(top.getUniqueId()).longValue()));
            }
        });
    }

    /*
     * Loop Part
     */

    public void activate() {
        if ((task == null || task.isCancelled()) && !boss.isDead()) {
            task = Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
                List<Player> damagers = getDamagers();
                AtomicBoolean isAlived = new AtomicBoolean(false);
                damagers.forEach(player -> {
                    if (!isNear(player) && !boss.isDead() && boss.getHealth() > boss.getMaxHealth() * 0.1) {
                        damagePlayer(player, 200);
                        if (boss.getHealth() > saveDamagePercent) {
                            removeDamager(player);
                        }
                    }
                    if (!player.isDead()) {
                        isAlived.set(true);
                    }
                });

                if (getDamagers().isEmpty() || boss.isDead() || !isAlived.get()) {
                    stopBattle();
                } else {
                    startBattle();
                }
                loopTask();
            }, 20, 20);
        }
    }

    protected abstract void loopTask();

    /*
     * Listeners Part
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        AttributeInstance instance = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (instance != null) {
            if (boss.getHealth() > instance.getBaseValue() * 0.2) {
                removeDamager(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        if (!event.getEntity().equals(boss)) {
            return;
        }

        if (event.getDamager() instanceof AreaEffectCloud) {
            event.setCancelled(true);
        }

        if (boss.isInvulnerable() && event.getEntity().equals(boss)) {
            invulnerableBossDamage();
            event.setCancelled(true);
            return;
        }

        if (!allowExplodeDamage && isExplosionCause(event.getCause()) && event.getEntity().equals(boss)) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamage() > maxDamagePerHit) {
            event.setDamage(maxDamagePerHit);
        }

        if (event.getDamager() instanceof Player player) {
            addDamage(player, event.getDamage());
            if (ThreadLocalRandom.current().nextBoolean()) {
                damagePlayer(player, event.getDamage() / 4);
            }
            return;
        }
        if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player player) {
            addDamage(player, event.getDamage());
            if (ThreadLocalRandom.current().nextBoolean()) {
                damagePlayer(player, event.getDamage() / 4);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBossDamage(EntityDamageByBlockEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        if (!event.getEntity().equals(boss)) {
            return;
        }

        if (boss.isInvulnerable()) {
            invulnerableBossDamage();
            event.setCancelled(true);
            return;
        }

        if (!allowExplodeDamage && isExplosionCause(event.getCause())) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamage() > maxDamagePerHit) {
            event.setDamage(maxDamagePerHit);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        Player player = event.getPlayer();
        if (!boss.isDead()) {
            damagePlayer(player, 200D);
            damageByPlayers.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerEffect(EntityPotionEffectEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof Player player && isNear(player) && !boss.isDead() && isBlackListEffect(event.getNewEffect()) && getDamagers().contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        Player player = event.getPlayer();

        if (item.getScoreboardTags().size() == 0) {
            return;
        }

        if (!item.getScoreboardTags().contains(player.getName())) {
            event.setCancelled(true);
            return;
        }

        item.removeScoreboardTag(player.getName());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().equalsIgnoreCase("/bossdamage")) {
            return;
        }
        if (boss == null || boss.isDead()) {
            return;
        }

        Player player = event.getPlayer();
        double damage = damageByPlayers.getOrDefault(player.getUniqueId(), -1D);
        if (damage == -1) {
            return;
        }

        player.sendMessage("§aВы сражаетесь с боссом " + bossNameColor + bossName);
        player.sendMessage("§aВы нанесли §b" + (long) damage + " §aурона!");
    }

    protected abstract boolean isBlackListEffect(PotionEffect type);

    protected abstract void invulnerableBossDamage();
}
