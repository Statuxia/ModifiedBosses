package net.reworlds.modifiedbosses.boss;

import lombok.AccessLevel;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.bossbars.BossBar;
import net.reworlds.modifiedbosses.event.vekster.SuckingEvent;
import net.reworlds.modifiedbosses.utils.DateFormatter;
import net.reworlds.modifiedbosses.utils.Recount;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Boss implements Listener {

    protected final LivingEntity boss;
    protected final Map<Player, Double> damageByPlayers = new HashMap<>();
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
    @Setter(AccessLevel.PROTECTED)
    protected boolean clearPotionEffects = true;
    protected org.bukkit.boss.BossBar bar;

    public Boss(LivingEntity boss) {
        this.boss = boss;
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

    protected void setAttributes(@NotNull HashMap<@NotNull Attribute, @NotNull Integer> attributes) {
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

    protected void damagePlayer(@NotNull LivingEntity damaged, double damage) {
        if (damaged.isDead() || boss.isDead()) {
            return;
        }

        double health = damaged.getHealth();

        if (damage >= health && health < 0.01) {
            damaged.damage(damage, boss);
            if (!damaged.isDead()) {
                damaged.setHealth(0);
            }
            return;
        }

        damaged.damage(0.01, boss);
        damaged.setHealth(health - (damage - 0.01));
    }

    protected void addDamage(@NotNull Player player, double damage) {
        damageByPlayers.put(player, damageByPlayers.getOrDefault(player, 0D) + damage);
    }

    /*
     * Utils Part
     */

    public boolean isNear(@NotNull Player player) {
        return isNear(player.getLocation());
    }

    public boolean isNear(@NotNull Location location) {
        try {
            return boss.getLocation().distance(location) < radius;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public List<Player> getTop(int limit) {
        return damageByPlayers.entrySet().stream()
                .sorted(Map.Entry.<Player, Double>comparingByValue().reversed()).limit(limit).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    protected boolean isExplosionCause(@NotNull EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;
    }

    /*
     * Battle Part
     */

    protected void startBattle() {
        battleStarted = true;
        startTime = System.currentTimeMillis();
    }

    protected void stopBattle() {
        if (!battleStarted) {
            return;
        }

        if (boss.isDead()) {
            bossDeadMessage();
            rewardPlayers();
            return;
        }

        AttributeInstance attribute = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute != null) {
            boss.setHealth(attribute.getBaseValue());
        }
    }

    public void remove() {
        stopBattle();
        boss.remove();
    }

    protected abstract void rewardPlayers();

    protected void bossDeadMessage() {
        String totalTime = DateFormatter.formatMillis(System.currentTimeMillis() - startTime);
        TextComponent bossDefeatedText = Component.text(bar.getTitle() + "§а был повержен!");
        TextComponent timeSpentText = Component.text("§7Затраченное время: " + totalTime);

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(bossDefeatedText);
            player.sendMessage(timeSpentText);
        });

        List<Player> topList = Recount.getTop(10);
        Bukkit.getPluginManager().callEvent(new SuckingEvent(topList));
        damageByPlayers.forEach((player, damage) -> {
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
                player.sendMessage(Component.text(color + (i + 1) + ". " + top.getName() + " " + colorDamage + damageByPlayers.get(top).longValue()));
            }
        });
    }

    /*
     * Loop Part
     */

    public void activate() {
        if ((task == null || task.isCancelled()) && !boss.isDead()) {
            task = Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
                damageByPlayers.forEach((player, damage) -> {
                    if ((player.getGameMode() == GameMode.SPECTATOR || !isNear(player)) && !boss.isDead()) {
                        damageByPlayers.remove(player);
                        damagePlayer(player, 200);
                        if (clearPotionEffects) {
                            player.clearActivePotionEffects();
                        }
                    }
                });
                if (damageByPlayers.isEmpty()) {
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
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getDamager() instanceof Player player) {
            addDamage(player, event.getDamage());
            return;
        }
        if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player player) {
            addDamage(player, event.getDamage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!event.getEntity().equals(boss)) {
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
    public void onBossDamage(EntityDamageByBlockEvent event) {
        if (!event.getEntity().equals(boss)) {
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
        if (!isNear(player)) {
            damagePlayer(player, 200D);
            damageByPlayers.remove(player);
        }
    }
}
