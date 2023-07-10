package net.reworlds.modifiedbosses;

import lombok.Getter;
import lombok.Setter;
import net.reworlds.modifiedbosses.boss.Boss;
import net.reworlds.modifiedbosses.boss.dragon.Dragon;
import net.reworlds.modifiedbosses.charms.Charms;
import net.reworlds.modifiedbosses.commands.ResourcePack;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ModifiedBosses extends JavaPlugin {

    @Getter
    private static ModifiedBosses INSTANCE;
    private static final Map<UUID, Boss> BOSSES = new HashMap<>();
    @Getter
    @Setter
    private static BukkitTask task;

    @Override
    public void onEnable() {
        INSTANCE = this;

//        Dragon.setBattleWorld(Bukkit.getWorld("world_the_end"));
        // BOSSES

        // Charms
        Charms.activate();

        getServer().getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginCommand("rp").setExecutor(new ResourcePack());
    }

    @Override
    public void onDisable() {
        BOSSES.forEach((uuid, boss) -> {
            boss.remove();
        });
    }
}
