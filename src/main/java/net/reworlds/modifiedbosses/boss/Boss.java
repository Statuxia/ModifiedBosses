package net.reworlds.modifiedbosses.boss;

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
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Boss implements Listener {


    protected final LivingEntity boss;
    protected final String bossName;
    protected final Component bossNameComponent;
    protected final Map<UUID, Double> damageByPlayers = new HashMap<>();
    protected final Set<UUID> deadPlayers = new HashSet<>();
    protected final Set<String> deadIPS = new HashSet<>();
    protected final Location spawnLocation;
    protected BukkitTask task;
    protected long startTime;
    protected boolean battleStarted;
    protected int radius = 200;
    protected double maxDamagePerHit = Double.MAX_VALUE;
    protected double minimumDamageToReward = 50D;
    protected boolean allowExplodeDamage = true;
    protected org.bukkit.boss.BossBar bar;
    protected int maxHealth = 2000;
    protected double saveDamageHealth;
    protected boolean lockTarget;

    public Boss(LivingEntity boss, String bossName, Component bossNameComponent) {
        this.boss = boss;
        this.spawnLocation = boss.getLocation();
        this.bossName = bossName;
        this.bossNameComponent = bossNameComponent;
    }

    public void setLockTarget(boolean b) {
        lockTarget = b;
    }

    public boolean getLockTarget() {
        return lockTarget;
    }

    public org.bukkit.boss.BossBar getBar() {
        return bar;
    }

    /*
     * Settings Part
     */

    protected void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

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


    protected void setAttributes(@NotNull Map<@NotNull Attribute, @NotNull Double> attributes) {
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
        damagePlayer(damaged, damage, boss);
    }

    public void damagePlayer(@NotNull LivingEntity damaged, double damage, LivingEntity damager) {
        if (damaged.isDead() || boss.isDead()) {
            return;
        }

        if (!(damaged instanceof Player player)) {
            return;
        }
        if (!damageByPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        double health = damaged.getHealth();
        if (damage >= health) {
            damaged.damage(0.01, damager);
            damaged.setHealth(0);
            if (boss.getHealth() > saveDamageHealth) {
                if (getDamagers().contains(player)) {
                    player.sendMessage("§cУ босса более " + saveDamageHealth + " ХП. Урон сброшен.");
                }
                removeDamager(player);
            }
            return;
        }

        damaged.damage(0.01, damager);
        try {
            damaged.setHealth(health - damage);
        } catch (IllegalArgumentException exception) {
            damaged.setHealth(0);
            if (boss.getHealth() > saveDamageHealth) {
                if (getDamagers().contains(player)) {
                    player.sendMessage("§cУ босса более " + saveDamageHealth + " ХП. Урон сброшен.");
                }
                removeDamager(player);
            }
        }
    }

    protected void addDamage(@NotNull Player player, double damage) {
        if (!getDamagers().contains(player)) {

            player.sendMessage(Component.newline().append(Component.text("§eВы вступили в бой с боссом ")).append(bossNameComponent).append(Component.text("§e!§r")));
            player.sendMessage(Component.text("§cВ случае побега с поля битвы вы будете убиты!§r").append(Component.newline()));
        }
        putDamager(player, damage);
    }

    /*
     * Utils Part
     */

    public void removeDamager(Player player) {
        if (damageByPlayers.containsKey(player.getUniqueId())) {
            deadPlayers.add(player.getUniqueId());
            if (player.getAddress() != null) {
                deadIPS.add(player.getAddress().getHostString());
            }
        }
        damageByPlayers.remove(player.getUniqueId());
    }

    public void clearDamagers() {
        damageByPlayers.clear();
    }

    public void clearDead() {
        deadPlayers.clear();
        deadIPS.clear();
    }

    public List<Player> getDamagers() {
        List<UUID> uuids = new ArrayList<>(damageByPlayers.keySet());
        uuids.removeIf(deadPlayers::contains);
        List<Player> uuidPlayers = getUUIDPlayers(uuids);
        uuidPlayers.removeIf(player -> {
            InetSocketAddress address = player.getAddress();
            if (address == null) {
                return false;
            }
            return deadIPS.contains(address.getHostString());
        });
        return uuidPlayers;
    }

    public boolean containsDamagers(Player player) {
        return getDamagers().contains(player);
    }

    public void putDamager(Player player, double damage) {
        UUID uuid = player.getUniqueId();
        damageByPlayers.put(uuid, damageByPlayers.getOrDefault(uuid, 0D) + damage);
    }

    public boolean isNear(@NotNull LivingEntity entity) {
        return isNear(entity.getLocation());
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
            deadIPS.clear();
            deadPlayers.clear();
            clearDamagers();
            clearDead();
            task.cancel();
            return;
        }
        deadIPS.clear();
        deadPlayers.clear();
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
        damageByPlayers.forEach((uuid, damage) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                return;
            }
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
                AtomicBoolean isAlived = new AtomicBoolean(false);
                getDamagers().forEach(player -> {
                    if (!isNear(player) && !boss.isDead() && boss.getHealth() > boss.getMaxHealth() * 0.1) {
                        if (!player.isDead() && !boss.getWorld().equals(player.getWorld())) {
                            damagePlayer(player, 200);
                        }
                        if (boss.getHealth() > saveDamageHealth) {
                            if (getDamagers().contains(player)) {
                                player.sendMessage("§cУ босса более " + saveDamageHealth + " ХП. Урон сброшен.");
                            }
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

    @EventHandler
    public void potionSplashEvent(LingeringPotionSplashEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        if (isNear(event.getAreaEffectCloud().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void potionSplashEvent(PotionSplashEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        if (isNear(event.getPotion().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        if (getDamagers().contains(player)) {
            deadPlayers.add(player.getUniqueId());
            if (player.getAddress() != null) {
                deadIPS.add(player.getAddress().getHostString());
            }
        }

        AttributeInstance instance = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (instance != null) {
            if (boss.getHealth() > saveDamageHealth) {
                if (getDamagers().contains(player)) {
                    player.sendMessage("§cУ босса более " + saveDamageHealth + " ХП. Урон сброшен.");
                }
                removeDamager(player);
            }
        }
    }

    @EventHandler
    public void onBossDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity().equals(boss)) {
            event.setCancelled(true);
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
            playerTryDamage(event, player);
            return;
        }
        if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player player) {
            playerTryDamage(event, player);
            return;
        }
        event.setCancelled(true);
    }

    private void playerTryDamage(EntityDamageByEntityEvent event, Player player) {
        InetSocketAddress address = player.getAddress();
        if (address != null && deadIPS.contains(address.getHostString())) {
            player.sendMessage("§cВы погибли и не можете наносить урон боссу!");
            event.setCancelled(true);
            return;
        }
        if (deadPlayers.contains(player.getUniqueId())) {
            player.sendMessage("§cВы погибли и не можете наносить урон боссу!");
            event.setCancelled(true);
            return;
        }
        if (boss.getHealth() < (double) maxHealth / 2 && !getDamagers().contains(player)) {
            player.sendMessage("§cВы не можете вступить в битву, когда у босса меньше 50% ХП!");
            event.setCancelled(true);
            return;
        }
        addDamage(player, event.getDamage());
        if (!player.isDead() && boss.getWorld().equals(player.getWorld())) {
            damagePlayer(player, event.getDamage() / 4);
        }
    }

    @EventHandler
    public void onChorusEat(PlayerTeleportEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            return;
        }
        if (isNear(event.getPlayer())) {
            event.setCancelled(true);
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
            if (!player.isDead() && !boss.getWorld().equals(player.getWorld())) {
                damagePlayer(player, 200D);
            }

            if (damageByPlayers.containsKey(player.getUniqueId())) {
                deadPlayers.add(player.getUniqueId());
                if (player.getAddress() != null) {
                    deadIPS.add(player.getAddress().getHostString());
                }
            }
            damageByPlayers.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerEffect(EntityPotionEffectEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof Player player && isNear(player) && !boss.isDead() && isBlackListEffect(event.getNewEffect(), player) && getDamagers().contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTarget(EntityTargetEvent event) {
        if (!event.getEntity().equals(boss)) {
            return;
        }

        if (!(event.getTarget() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }

        if (!getDamagers().contains(player)) {
            event.setCancelled(true);
            return;
        }
        if (lockTarget) {
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

        player.sendMessage(Component.text("§aВы сражаетесь с боссом ").append(bossNameComponent));
        player.sendMessage("§aВы нанесли §b" + (long) damage + " §aурона!");
    }

    protected abstract boolean isBlackListEffect(PotionEffect type, Player player);

    protected abstract void invulnerableBossDamage();

    protected void setSaveDamageHealth(double saveDamageHealth) {
        this.saveDamageHealth = saveDamageHealth;
    }

    public LivingEntity getBoss() {
        return boss;
    }

    public String getBossName() {
        return bossName;
    }

    protected void setRadius(int radius) {
        this.radius = radius;
    }

    protected void setMaxDamagePerHit(int maxDamagePerHit) {
        this.maxDamagePerHit = maxDamagePerHit;
    }

    protected void setMinimumDamageToReward(int minimumDamageToReward) {
        this.minimumDamageToReward = minimumDamageToReward;
    }

    protected void setAllowExplodeDamage(boolean allowExplodeDamage) {
        this.allowExplodeDamage = allowExplodeDamage;
    }

    public double calculateDistance(Location l1, Location l2) {
        return Math.pow(Math.pow(l2.getX() - l1.getX(), 2) + Math.pow(l2.getY() - l1.getY(), 2) + Math.pow(l2.getZ() - l1.getZ(), 2), 0.5);
    }
}
