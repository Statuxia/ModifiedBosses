package net.reworlds.modifiedbosses.bossbars;

import net.reworlds.modifiedbosses.ModifiedBosses;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Timer {

    private BossBar bar;
    private int seconds;
    private double percent;
    private AtomicInteger current;

    private BukkitTask task;

    public Timer(Player player, int seconds, String title, BarColor color) {
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

    }

    public static Timer of(Player player, int seconds, String title, BarColor color) {
        return new Timer(player, seconds, title, color);
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
