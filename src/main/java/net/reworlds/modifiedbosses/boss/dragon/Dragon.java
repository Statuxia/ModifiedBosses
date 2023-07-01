package net.reworlds.modifiedbosses.boss.dragon;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.runes.Charm;
import net.reworlds.modifiedbosses.runes.Charms;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Dragon {

    @Getter
    private static final HashMap<Player, Integer> attackedBy = new HashMap<>();
    @Getter
    private static final Set<Player> nearPlayers = new HashSet<>();
    @Getter
    @Setter
    private static World battleWorld;
    @Getter
    private static EnderDragon dragon;
    @Getter(AccessLevel.PUBLIC)
    private static boolean isFighting;
    @Getter
    private static int phase;
    private static BukkitTask dragonScheduler;
    private static Team dragonTeam;
    @Getter
    private static long lastAbility;

    public static boolean isSameDragon(EnderDragon dragon) {
        return Dragon.dragon.equals(dragon);
    }

    public static void findDragon() {
        if (Dragon.dragon != null && !Dragon.dragon.isDead()) {
            return;
        }

        if (battleWorld == null) {
            return;
        }

        DragonBattle dragonBattle = battleWorld.getEnderDragonBattle();
        if (dragonBattle == null) {
            return;
        }

        EnderDragon dragon = dragonBattle.getEnderDragon();
        if (dragon != null && !dragon.isDead()) {
            Dragon.dragon = dragon;
            setDefaultSettings();
            activateScheduler();
        }
    }

    public static void selectDragon(EnderDragon dragon) {
        if (Dragon.dragon == null || Dragon.dragon.isDead()) {
            if (dragon.isDead()) {
                findDragon();
            } else {
                Dragon.dragon = dragon;
                setDefaultSettings();
                activateScheduler();
            }
        }
    }

    private static void setDefaultSettings() {
        if (dragon == null || dragon.isDead()) {
            return;
        }
        phase = 0;
        dragon.setInvulnerable(true);

        AttributeInstance maxHealth = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(2000f);
        }
        dragon.setHealth(1020f);

        AttributeInstance attack = dragon.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(20);
        }

        BossBar bossBar = dragon.getBossBar();
        if (bossBar != null) {
            bossBar.setColor(BarColor.RED);
        }

        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        try {
            dragonTeam = mainScoreboard.registerNewTeam(ChatColor.DARK_PURPLE + "team");
        } catch (IllegalArgumentException ignored) {
            dragonTeam = mainScoreboard.getTeam(ChatColor.DARK_PURPLE + "team");
        }
        dragonTeam.setColor(ChatColor.DARK_PURPLE);
        dragonTeam.addEntity(dragon);
        dragon.setGlowing(true);
    }

    private static void activateScheduler() {
        if (dragonScheduler == null || dragonScheduler.isCancelled()) {
            dragonScheduler = Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
                Bukkit.getLogger().info("isFighting -> " + isFighting);
                if (dragon == null || dragon.isDead()) {
                    stopBattle();
                }
                nearPlayers.clear();
                AtomicBoolean isCrystalsInRange = new AtomicBoolean(false);
                dragon.getNearbyEntities(200, 200, 200).forEach(entity -> {
                    if (entity instanceof Player player && player.getGameMode() != GameMode.SPECTATOR) {
                        nearPlayers.add(player);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                    }

                    if (entity instanceof EnderCrystal crystal) {
                        if (dragonTeam != null) {
                            dragonTeam.addEntity(crystal);
                            crystal.setGlowing(true);
                        }
                        isCrystalsInRange.set(true);
                    }
                });
                if (!nearPlayers.isEmpty()) {
                    startBattle();
                }

                dragon.setInvulnerable(isCrystalsInRange.get());

                if (!dragon.isInvulnerable()) {
                    phase = 1;
                }

                if (dragon.getHealth() <= 1000) {
                    phase = 2;
                }

                lastAbility = Abilities.activate();

                if (nearPlayers.isEmpty() && isFighting()) {
                    stopBattle();
                }
            }, 0, 50);
        }
    }

    private static void deactivateScheduler() {
        if (dragonScheduler != null && !dragonScheduler.isCancelled()) {
            dragonScheduler.cancel();
        }
    }


    public static void startBattle() {
        if (isFighting) {
            return;
        }

        findDragon();
        if (dragon == null || dragon.isDead()) {
            return;
        }

        isFighting = true;
        setDefaultSettings();
        activateScheduler();
    }

    public static void stopBattle() {
        if (!isFighting) {
            return;
        }
        isFighting = false;

        if (dragon != null && !dragon.isDead()) {
            setDefaultSettings();
            attackedBy.clear();
            nearPlayers.clear();
            return;
        }

        giveReward();
        attackedBy.clear();
        nearPlayers.clear();
        deactivateScheduler();
        phase = 0;
    }

    private static void giveReward() {
        Bukkit.getLogger().info("giveReward");
        attackedBy.forEach((player, integer) -> {
            Bukkit.getLogger().info(player.getName());
        });
        List<Player> top5 = getTop(5);
        attackedBy.forEach((player, integer) -> {
            player.sendMessage("Топ по урону");
            for (int i = 0; i < top5.size(); i++) {
                Player top = top5.get(i);
                player.sendMessage(Component.text((i + 1) + ". " + top.getName() + " " + attackedBy.get(top)));
            }
        });
        attackedBy.forEach((player, integer) -> {
            if (integer > 50) {
                giveReward(player);
            }
        });
    }

    private static List<Player> getTop(int limit) {
        HashMap<Player, Integer> top = Dragon.getAttackedBy();
        return top.entrySet().stream()
                .sorted(Map.Entry.<Player, Integer>comparingByValue().reversed()).limit(limit).map(Map.Entry::getKey)
                        .collect(Collectors.toList());
    }

    public static void giveReward(Player player) {
        player.giveExp(1395);
        if (ThreadLocalRandom.current().nextInt(100) < 71) {
            player.getInventory().addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, ThreadLocalRandom.current().nextInt(1, 4)));
        }
        if (ThreadLocalRandom.current().nextInt(100) < 31) {
            if (ThreadLocalRandom.current().nextInt(100) < 21) {
                player.getInventory().addItem(Charms.EPIC.get(ThreadLocalRandom.current().nextInt(Charms.EPIC.size())).getRune());
            } else {
                player.getInventory().addItem(Charms.RARE.get(ThreadLocalRandom.current().nextInt(Charms.RARE.size())).getRune());
            }
        }
    }

    public static void addDamage(Player player, double damage) {
        int done = attackedBy.getOrDefault(player, 0) + (int) damage;
        attackedBy.put(player, done);
    }
}
