package net.reworlds.modifiedbosses.bossbars;

import net.reworlds.modifiedbosses.ModifiedBosses;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Timer {

    public static final Map<String, Timer> TIMERS = new HashMap<>();

    private BossBar bar;
    private int seconds;
    private double percent;
    private AtomicInteger current;

    private BukkitTask task;

    public Timer(Player player, String id, int seconds, String title, BarColor color) {
        this.bar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        this.seconds = seconds;
        this.percent = seconds / 100.0D;
        this.current = new AtomicInteger(seconds);
        bar.addPlayer(player);
        bar.setVisible(true);

        task = Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
            if (current.get() <= 0) {
                stop();
                return;
            }
            current.decrementAndGet();
            calc();
        }, 0, 20);

        TIMERS.put(id, this);
    }

    public static Optional<Timer> get(String id) {
        return Optional.ofNullable(TIMERS.get(id));
    }

    public static Timer of(Player player, String id, int seconds, String title, BarColor color) {
        return new Timer(player, id, seconds, title, color);
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
    }

    private void calc() {
        double percents = current.get() / percent;
        bar.setProgress(percents / 100.0);
    }
}
