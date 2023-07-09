package net.reworlds.modifiedbosses.boss.builder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.boss.dragon.Dragon;
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

    private final LivingEntity boss;
    private final Map<Player, Double> damageByPlayers = new HashMap<>();
    private org.bukkit.boss.BossBar bar;
    private int radius = 200;
    private boolean allowExplodeDamage = true;
    private double maxDamagePerHit = Double.MAX_VALUE;
    private double minimumDamageToReward = 50D;
    private BukkitTask task;
    private long startTime;
    private boolean battleStarted;

    public Boss(LivingEntity boss) {
        this.boss = boss;
    }

    public org.bukkit.boss.BossBar getCustomBossBar(@NotNull String title, BarColor color, BarStyle style) {
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

    public void setAttributes(HashMap<Attribute, Integer> attributes) {
        attributes.forEach((attribute, value) -> {
            AttributeInstance instance = boss.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(value);
            }
        });
    }

    public void isAllowedExplodeDamage(boolean b) {
        allowExplodeDamage = b;
    }

    public void setMaxDamagePerHit(double damage) {
        if (damage < 0) {
            maxDamagePerHit = 0;
            return;
        }
        maxDamagePerHit = damage;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setMinimumDamageToReward(double damage) {
        this.minimumDamageToReward = damage;
    }

    public void damagePlayer(LivingEntity damaged, double damage) {
        if (damaged.isDead()) {
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

    private void addDamage(@NotNull Player player, double damage) {
        damageByPlayers.put(player, damageByPlayers.getOrDefault(player, 0D) + damage);
    }

    private boolean isExplosionCause(@NotNull EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;
    }


    private boolean isNear(@NotNull Player player) {
        return isNear(player.getLocation());
    }

    private boolean isNear(@NotNull Location location) {
        try {
            return boss.getLocation().distance(location) < radius;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    public void startBattle() {
        battleStarted = true;
        startTime = System.currentTimeMillis();
    }

    public void stopBattle() {
        if (!battleStarted) {
            return;
        }

        if (boss.isDead()) {
            bossDeadMessage();
            reward();
            // Не, типа я могу писать метод и НЕ ДЕЛАТЬ НИЧЕ С НИМ
            // УИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИИ
            // это моя реакция на эту штуку

            // ебать абстракции крутая тема

        }
    }

    public void remove() {
        stopBattle();
        boss.remove();
    }

    public void activate() {
        if (task == null || task.isCancelled()) {
            Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
                damageByPlayers.forEach((player, damage) -> {
                    if ((player.getGameMode() == GameMode.SPECTATOR || !isNear(player)) && !boss.isDead()) {
                        damageByPlayers.remove(player);
                        damagePlayer(player, 200);
                    }
                });
                if (damageByPlayers.isEmpty()) {
                    stopBattle();
                } else {
                    startBattle();
                }
            }, 20, 20);
        }
    }

    public void bossDeadMessage() {
        // TODO: переделать

        // я чуток наврал. я изменю кое что в механике босса
        long endTime = System.currentTimeMillis();
        String totalTime = DateFormatter.formatMillis(endTime - startTime);
        TextComponent text = Component.text("§b" + bar.getTitle() + " повержен!");

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(Component.text("§5Эндер Дракон повержен!"));
            player.sendMessage(Component.text("§7Затраченное время: " + totalTime));
        });
        List<Player> topList = Recount.getTop(10);
        SuckingEvent suckEvent = new SuckingEvent(topList);
        Bukkit.getPluginManager().callEvent(suckEvent);
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
                player.sendMessage(Component.text(color + (i + 1) + ". " + top.getName() + " " + colorDamage + damageByPlayers.get(top)));
            }
        });
    }

    public abstract void reward();
    public abstract void

    public List<Player> getTop(int limit) {
        Map<Player, Double> top = damageByPlayers;
        return top.entrySet().stream()
                .sorted(Map.Entry.<Player, Double>comparingByValue().reversed()).limit(limit).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

//    private static void setDefaultSettings() {
//        if (dragon == null || dragon.isDead()) {
//            return;
//        }
//        phase = 0;
//
//        AttributeInstance maxHealth = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH);
//        if (maxHealth != null) {
//            maxHealth.setBaseValue(2000f);
//        }
//
//        if (!dragon.isDead()) {
//            dragon.setHealth(2000f);
//        }
//
//        AttributeInstance attack = dragon.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
//        if (attack != null) {
//            attack.setBaseValue(20);
//        }
//
//        BossBar bossBar = dragon.getBossBar();
//        if (bossBar != null) {
//            bossBar.setColor(BarColor.RED);
//        }
//
//        dragonTeam = TeamUtils.getTeam(ChatColor.DARK_PURPLE, "DragonTeam");
//        dragonTeam.addEntity(dragon);
//        dragon.setGlowing(true);
//    }
    }
