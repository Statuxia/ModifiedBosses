package net.reworlds.modifiedbosses;

import lombok.Getter;
import lombok.Setter;
import net.reworlds.modifiedbosses.charms.Charms;
import net.reworlds.modifiedbosses.commands.BestiaryCommand;
import net.reworlds.modifiedbosses.commands.BossDamage;
import net.reworlds.modifiedbosses.commands.RPCommand;
import net.reworlds.modifiedbosses.commands.boss.dragon.BestiaryDragonCommand;
import net.reworlds.modifiedbosses.commands.boss.dragon.BestiaryDragonDropCommand;
import net.reworlds.modifiedbosses.respawn.Bosses;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class ModifiedBosses extends JavaPlugin {

    @Getter
    private static ModifiedBosses INSTANCE;
    @Getter
    @Setter
    private static BukkitTask task;

    @Override
    public void onEnable() {
        INSTANCE = this;

        // BOSSES
        Bosses.initializeDragon();

        // Charms
        Charms.activate();

        // Commands
        Bukkit.getPluginCommand("rp").setExecutor(new RPCommand());
        Bukkit.getPluginCommand("bestiary").setExecutor(new BestiaryCommand());
        Bukkit.getPluginCommand("bDragon").setExecutor(new BestiaryDragonCommand());
        Bukkit.getPluginCommand("bDragonDrop").setExecutor(new BestiaryDragonDropCommand());
        Bukkit.getPluginCommand("bossDamage").setExecutor(new BossDamage());
        Bukkit.getPluginManager().registerEvents(new Events(), this);
    }

    @Override
    public void onDisable() {
        Bosses.getBosses().forEach((uuid, boss) -> {
            try {
                boss.getDummy().remove();
            } catch (Exception ignored) {
            }
            try {
                boss.getRespawnTimer().stop();
            } catch (Exception ignored) {
            }
            try {
                boss.getBoss().removeBoss();
            } catch (Exception ignored) {
            }
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getOpenInventory().getOriginalTitle().equals("§0Дроп")) {
                    player.closeInventory();
                }
            });
        });
    }
}
