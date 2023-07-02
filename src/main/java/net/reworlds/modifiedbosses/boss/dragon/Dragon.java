package net.reworlds.modifiedbosses.boss.dragon;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.charms.Charms;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Item;
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
    @Getter
    private static boolean activated;
    private static long startFightTime;
    @Getter
    private static int phase;
    private static BukkitTask dragonScheduler;
    private static Team dragonTeam;
    @Getter
    private static long lastAbility;

    public static boolean findDragon() {
        return findDragon(0);
    }

    public static boolean findDragon(int numTry) {
        if (numTry == 5) {
            return false;
        }

        if (Dragon.dragon != null && !Dragon.dragon.isDead()) {
            return findDragon(++numTry);
        }

        if (battleWorld == null) {
            return findDragon(++numTry);
        }

        DragonBattle dragonBattle = battleWorld.getEnderDragonBattle();
        if (dragonBattle == null) {
            return findDragon(++numTry);
        }

        EnderDragon dragon = dragonBattle.getEnderDragon();
        if (dragon == null || dragon.isDead()) {
            return findDragon(++numTry);
        }
        Dragon.dragon = dragon;
        setDefaultSettings();
        activateScheduler();
        return true;
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
        dragon.setHealth(2000f);

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
                if (dragon == null || dragon.isDead()) {
                    stopBattle();
                }

                nearPlayers.clear();
                AtomicBoolean isCrystalsInRange = new AtomicBoolean(false);
                dragon.getNearbyEntities(200, 200, 200).forEach(entity -> {
                    if (entity instanceof Player player && player.getGameMode() != GameMode.SPECTATOR && !player.isDead()) {
                        nearPlayers.add(player);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                        player.setGliding(false);
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
                    activateDragon();
                }

                dragon.setInvulnerable(isCrystalsInRange.get());

                if (!dragon.isInvulnerable()) {
                    phase = 1;
                }

                if (dragon.getHealth() <= 1000) {
                    phase = 2;
                }

                lastAbility = Abilities.activate();

                if (nearPlayers.isEmpty() && isActivated()) {
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

    public static void activateDragon() {
        if (activated) {
            return;
        }

        findDragon();
        if (dragon == null || dragon.isDead()) {
            return;
        }

        activated = true;
        setDefaultSettings();
        activateScheduler();
    }

    public static void stopBattle() {
        if (!activated) {
            return;
        }
        activated = false;

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
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(Component.text("§5Эндер Дракон повержен!")));
        List<Player> top5 = getTop(10);
        attackedBy.forEach((player, integer) -> {
            player.sendMessage(Component.text("§e===== §2TOP DAMAGE §e====="));
            for (int i = 0; i < top5.size(); i++) {
                String color;
                switch (i) {
                    case 0 -> color = "§6§l";
                    case 1, 2 -> color = "§a";
                    default -> color = "§7";
                }
                Player top = top5.get(i);
                player.sendMessage(Component.text("§e" + (i + 1) + ". " + color + top.getName() + " §b" + attackedBy.get(top)));
            }
        });
        attackedBy.forEach((player, integer) -> {
            player.giveExp(315);
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
        player.giveExp(1080);
        player.getInventory().addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, ThreadLocalRandom.current().nextInt(2, 8)));
        if (ThreadLocalRandom.current().nextInt(100) < 31) {
            ItemStack item;
            if (ThreadLocalRandom.current().nextInt(100) < 21) {
                item = Charms.EPIC.get(ThreadLocalRandom.current().nextInt(Charms.EPIC.size())).getRune();
            } else {
                item = Charms.RARE.get(ThreadLocalRandom.current().nextInt(Charms.RARE.size())).getRune();
            }
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                Item droppedItem = player.getLocation().getWorld().dropItem(player.getLocation(), item);
                droppedItem.addScoreboardTag(player.getName());
            }
            Component itemDisplayName = item.getItemMeta().displayName();
            if (itemDisplayName == null) {
                return;
            }
            Component text = Component.text("§e" + player.getName() + " выбивает ").append(itemDisplayName);
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(text));
        }
    }

    public static void addDamage(Player player, double damage) {
        int done = attackedBy.getOrDefault(player, 0) + (int) damage;
        attackedBy.put(player, done);
    }

    public static boolean isSameWorld(Player player) {
        return isSameWorld(player.getLocation());
    }

    public static boolean isSameWorld(Location location) {
        return isSameWorld(location.getWorld());
    }

    public static boolean isSameWorld(World world) {
        return world.equals(battleWorld);
    }

    public static boolean isNearCenter(Player player) {
        return isNearCenter(player.getLocation());
    }

    public static boolean isNearCenter(Location location) {
        return location.distance(new Location(battleWorld, 0, 64, 0)) <= 300;
    }

    public static boolean isNearDragon(Player player) {
        return isNearDragon(player.getLocation());
    }

    public static boolean isNearDragon(Location location) {
        if (dragon == null || dragon.isDead()) {
            return false;
        }
        return location.distance(dragon.getLocation()) <= 300;
    }

    public static void removeAttackedBy(Player player) {
        attackedBy.remove(player);
    }
}
