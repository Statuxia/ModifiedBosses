package net.reworlds.modifiedbosses;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.reworlds.modifiedbosses.boss.dragon.Dragon;
import net.reworlds.modifiedbosses.charms.Charm;
import net.reworlds.modifiedbosses.charms.CharmsEffects;
import net.reworlds.modifiedbosses.utils.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public final class ModifiedBosses extends JavaPlugin {

    @Getter
    private static ModifiedBosses INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Dragon.setBattleWorld(Bukkit.getWorld("world_the_end"));

        getServer().getPluginManager().registerEvents(new Events(), this);

        Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
            ImmutableList<Player> players = ImmutableList.copyOf(Bukkit.getOnlinePlayers());
            int groupSize = players.size() / 20;

            for (int index = 0; index < 20; index++) {
                int startIndex = index * groupSize;
                int endIndex = (index + 1) * groupSize;

                if (index == 19) {
                    endIndex = players.size();
                }

                List<Player> group = players.subList(startIndex, endIndex);

                Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                    group.forEach(player -> {
                        // Adding Charm Effects
                        player.getInventory().forEach(itemStack -> {
                            if (itemStack == null || itemStack.lore() == null) {
                                return;
                            }
                            String lore = ComponentUtils.plainText(itemStack.lore().get(0));
                            int level = Charm.getLevel(lore);
                            PotionEffectType type = Charm.getPotionEffectType(lore, level);
                            if (level == -1 || type == null) {
                                return;
                            }
                            String typeText = type.getName().toLowerCase();
                            String key = typeText + level;
                            PotionEffect effect = CharmsEffects.getEffects().get(key);
                            if (effect == null) {
                                int duration = typeText.contains("night_vision") ? 1100 : 110;
                                effect = new PotionEffect(type, duration, level - 1);
                                CharmsEffects.getEffects().put(key, effect);
                            }
                            player.addPotionEffect(effect);
                        });

                        // Finding Dragon
                        if (player.getGameMode() == GameMode.SPECTATOR || !Dragon.isSameWorld(player)
                                || !Dragon.isNearCenter(player)) {
                            return;
                        }

                        if (Dragon.findDragon()) {
                            return;
                        }

                        int range = getServer().getTPS()[0] > 13 ? 200 : 100;
                        player.getNearbyEntities(range, range, range).forEach(entity -> {
                            if (entity instanceof EnderDragon dragon && player.getGameMode() != GameMode.SPECTATOR) {
                                Dragon.selectDragon(dragon);
                            }
                        });
                    });
                }, index);
            }
        }, 0, 100);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
