package net.reworlds.modifiedbosses.respawn;

import net.reworlds.modifiedbosses.ModifiedBosses;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RespawnTimer {

    public static final Map<String, RespawnTimer> TIMERS = new HashMap<>();

    private final BossBar bar;
    private final double percent;
    private final AtomicInteger current;
    private final Entity dummy;

    private BukkitTask task;

    public RespawnTimer(Entity dummy, String id, int seconds, String title, BarColor color, int radius) {
        this.bar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        this.percent = seconds / 100.0D;
        this.current = new AtomicInteger(seconds);
        this.dummy = dummy;
        bar.setVisible(true);

        task = Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
            if (current.get() <= 0 || dummy.isDead()) {
                stop();
                return;
            }
            current.decrementAndGet();
            calc();

            List<Player> players = new ArrayList<>();
            dummy.getNearbyEntities(radius, radius, radius).forEach(entity -> {
                if (entity instanceof Player player) {
                    players.add(player);
                }
            });

            List<Player> bossBarPlayers = bar.getPlayers();
            for (Player player : bossBarPlayers) {
                if (!players.remove(player)) {
                    bar.removePlayer(player);
                }
            }
            players.forEach(bar::addPlayer);
        }, 0, 20);

        TIMERS.put(id, this);
    }

    public static Optional<RespawnTimer> get(String id) {
        return Optional.ofNullable(TIMERS.get(id));
    }

    public static RespawnTimer of(Player player, String id, int seconds, String title, BarColor color, int radius) {
        return new RespawnTimer(player, id, seconds, title, color, radius);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        bar.setVisible(false);
        for (Player player : new HashSet<>(bar.getPlayers())) {
            bar.removePlayer(player);
        }
        dummy.remove();
    }

    private void calc() {
        double percents = current.get() / percent;
        bar.setProgress(percents / 100.0);
    }
}
