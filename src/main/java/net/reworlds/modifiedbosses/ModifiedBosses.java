package net.reworlds.modifiedbosses;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.reworlds.modifiedbosses.advancements.AdvancementManager;
import net.reworlds.modifiedbosses.advancements.advs.AdvancementListener;
import net.reworlds.modifiedbosses.charms.Charms;
import net.reworlds.modifiedbosses.commands.BestiaryCommand;
import net.reworlds.modifiedbosses.commands.BossDamage;
import net.reworlds.modifiedbosses.commands.RPCommand;
import net.reworlds.modifiedbosses.items.SpecialItems;
import net.reworlds.modifiedbosses.respawn.Bosses;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class ModifiedBosses extends JavaPlugin {

    private static ModifiedBosses INSTANCE;
    private static BukkitTask task;
    private static AdvancementManager advancementManager;

    public static ModifiedBosses getINSTANCE() {
        return INSTANCE;
    }

    public static BukkitTask getTask() {
        return task;
    }

    public static void setTask(BukkitTask task) {
        ModifiedBosses.task = task;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // BOSSES
        Bosses.initializeDragon();
        Bosses.initializeGelu();


        advancementManager = new AdvancementManager();
        advancementManager.modifiedbosses.automaticallyShowToPlayers();
        Bukkit.getOnlinePlayers().forEach(player -> {
            advancementManager.modifiedbosses.showTab(player);
            advancementManager.modifiedbosses.grantRootAdvancement(player);
        });

        // Charms
        Charms.activate();

        // Commands
        Bukkit.getPluginCommand("rp").setExecutor(new RPCommand());
        Bukkit.getPluginCommand("bestiary").setExecutor(new BestiaryCommand());
        Bukkit.getPluginCommand("bossDamage").setExecutor(new BossDamage());
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginManager().registerEvents(new SpecialItems(), this);
        Bukkit.getPluginManager().registerEvents(new AdvancementListener(), this);
        ModifiedBosses.setTask(task);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.WORLD_EVENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int effectId = event.getPacket().getIntegers().read(0);
                if (effectId == 1028 || effectId == 1023 || effectId == 1038) {
                    event.setCancelled(true);
                }
            }
        });
    }

    @Override
    public void onDisable() {
        Bosses.getBosses().forEach((bossName, boss) -> {
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
            try {
                boss.getBoss().getBoss().remove();
                boss.getBoss().getBar().setVisible(false);
            } catch (Exception ignored) {
            }
        });
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (SpecialItems.lockedYamatoTo.contains(player.getUniqueId())) {
                player.setInvulnerable(false);
                player.setCollidable(true);
            }
            if (player.getOpenInventory().getOriginalTitle().equals("§0Дроп")) {
                player.closeInventory();
            }
        });
    }

    public static AdvancementManager getAdvancementManager() {
        return advancementManager;
    }
}
