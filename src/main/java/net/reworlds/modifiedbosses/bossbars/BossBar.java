package net.reworlds.modifiedbosses.bossbars;

import net.reworlds.modifiedbosses.ModifiedBosses;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BossBar {

    private String title;
    private BarColor color = BarColor.RED;
    private BarStyle style = BarStyle.SOLID;
    private LivingEntity boss;
    private boolean showToNearPlayers;
    private int radius = 200;
    private BukkitTask task;

    public BossBar setTitle(@NotNull String title) {
        this.title = title;
        return this;
    }

    public BossBar setColor(@NotNull BarColor color) {
        this.color = color;
        return this;
    }

    public BossBar setStyle(@NotNull BarStyle style) {
        this.style = style;
        return this;
    }

    public BossBar setEntity(@NotNull LivingEntity boss, boolean showToNearPlayers) {
        this.boss = boss;
        this.showToNearPlayers = showToNearPlayers;
        return this;
    }

    public BossBar setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public org.bukkit.boss.BossBar build() {
        org.bukkit.boss.BossBar bossBar = Bukkit.createBossBar(title, color, style);

        if (boss == null || boss.isDead() || !showToNearPlayers) {
            return bossBar;
        }
        setUpBossBar(bossBar);
        return bossBar;
    }

    private void setUpBossBar(org.bukkit.boss.BossBar bossBar) {
        double maxHealth;
        AttributeInstance attribute = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute == null) {
            maxHealth = boss.getHealth();
        } else {
            maxHealth = attribute.getBaseValue();
        }

        task = Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
            if (boss.isDead() || boss.getHealth() <= 0) {
                task.cancel();
                task = null;
                bossBar.setVisible(false);
                bossBar.removeAll();
                return;
            }

            List<Player> players = new ArrayList<>();
            boss.getNearbyEntities(radius, radius, radius).forEach(entity -> {
                if (entity instanceof Player player) {
                    players.add(player);
                }
            });

            List<Player> bossBarPlayers = bossBar.getPlayers();
            for (Player player : bossBarPlayers) {
                if (!players.remove(player)) {
                    bossBar.removePlayer(player);
                }
            }
            players.forEach(bossBar::addPlayer);

            bossBar.setProgress(boss.getHealth() / maxHealth);
            bossBar.setTitle(title + "§r: " + (int) boss.getHealth() + "/" + (int) maxHealth);
        }, 0, 20);
    }
}
