package net.reworlds.modifiedbosses;

import lombok.Getter;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.reworlds.modifiedbosses.boss.dragon.Dragon;
import net.reworlds.modifiedbosses.commands.GetRunes;
import net.reworlds.modifiedbosses.commands.GiveReward;
import net.reworlds.modifiedbosses.runes.Charm;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class ModifiedBosses extends JavaPlugin {

    @Getter
    private static ModifiedBosses INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Dragon.setBattleWorld(Bukkit.getWorld("world_the_end"));
        Dragon.getBattleWorld().loadChunk(0, 0);
        Dragon.findDragon();
        Dragon.getBattleWorld().playSound(new Location(Dragon.getBattleWorld(), 0, 64, 0), Sound.MUSIC_CREDITS, 0.5f, 1);

        getServer().getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginCommand("charms").setExecutor(new GetRunes());
        Bukkit.getPluginCommand("givereward").setExecutor(new GiveReward());

        Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory().forEach(itemStack -> {
                if (itemStack != null && itemStack.lore() != null) {
                    String lore = PlainTextComponentSerializer.plainText().serialize(itemStack.lore().get(0));
                    int level = Charm.getLevel(lore);
                    PotionEffectType type = Charm.getPotionEffectType(lore, level);
                    if (level != -1 && type != null) {
                        int duration = 110;
                        if (type.getName().toLowerCase().contains("night_vision")) {
                            duration *= 10;
                        }
                        player.addPotionEffect(new PotionEffect(type, duration, level - 1));
                    }
                }
            });

            int range = 100;
            if (getServer().getTPS()[0] > 13) {
                range = 200;
            }
            player.getNearbyEntities(range, range, range).forEach(entity -> {
                if (entity instanceof EnderDragon dragon && player.getGameMode() != GameMode.SPECTATOR) {
                    Dragon.selectDragon(dragon);
                }
            });
        }), 0, 100);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
