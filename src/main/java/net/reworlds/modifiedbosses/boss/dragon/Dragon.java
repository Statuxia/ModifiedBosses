package net.reworlds.modifiedbosses.boss.dragon;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.charms.Charms;
import net.reworlds.modifiedbosses.event.vekster.SuckingEvent;
import net.reworlds.modifiedbosses.utils.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
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
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dragon {

    @Getter
    private static final HashMap<Player, Integer> attackedBy = new HashMap<>();
    @Getter
    private static final Set<Player> nearPlayers = new HashSet<>();
    @Getter
    @Setter
    private static World battleWorld;
    private static final List<Location> deadLocations = List.of(
            new Location(battleWorld, 28, 66, 28),
            new Location(battleWorld, 28, 66, -29),
            new Location(battleWorld, -29, 66, -29),
            new Location(battleWorld, -29, 66, 28)
    );
    @Getter
    private static EnderDragon dragon;
    @Getter
    private static BossBar bossbar;
    @Getter
    private static boolean activated;
    @Getter
    private static int phase;
    private static BukkitTask dragonScheduler;
    private static Team dragonTeam;
    @Getter
    private static long lastAbility;
    private static long startTime;

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

        AttributeInstance maxHealth = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(2000f);
        }

        if (!dragon.isDead()) {
            dragon.setHealth(2000f);
        }

        dragon.getBossBar().setStyle(BarStyle.SEGMENTED_10);
        dragon.getBossBar().setColor(BarColor.RED);

        AttributeInstance attack = dragon.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(20);
        }

        dragonTeam = TeamUtils.getTeam(ChatColor.DARK_PURPLE, "DragonTeam");
        dragonTeam.addEntity(dragon);
        dragon.setGlowing(true);
    }

    private static void activateScheduler() {
        if (dragonScheduler == null || dragonScheduler.isCancelled()) {
            if (1 != 2) {
                return;
            }
            dragonScheduler = Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
                if (dragon == null || dragon.isDead()) {
                    stopBattle();
                }

                nearPlayers.clear();
                AtomicBoolean isCrystalsInRange = new AtomicBoolean(false);
                dragon.getNearbyEntities(200, 200, 200).forEach(entity -> {
                    if (entity instanceof Player player) {
                        if (player.getGameMode() != GameMode.SPECTATOR && !player.isDead()) {
                            nearPlayers.add(player);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                            player.setGliding(false);
                        }
                    }
                    if (entity instanceof EnderCrystal crystal) {
                        if (dragonTeam != null) {
                            dragonTeam.addEntity(crystal);
                            crystal.setGlowing(true);
                        }
                        isCrystalsInRange.set(true);
                    }
                });
                dragon.setInvulnerable(isCrystalsInRange.get());

                AtomicBoolean isFighting = new AtomicBoolean(false);
                attackedBy.forEach((player, integer) -> {
                    if (isSameWorld(player) && isNearDragon(player)) {
                        isFighting.set(true);
                    }
                });

                if (!isFighting.get() && !dragon.isDead()) {
                    dragon.setHealth(2000f);
                    startTime = System.currentTimeMillis();
                }

                if (!nearPlayers.isEmpty()) {
                    activateDragon();
                }

                deadLocations.forEach(location -> {
                    while (battleWorld.getHighestBlockYAt(location) > 65) {
                        battleWorld.getHighestBlockAt(location).setType(Material.AIR);
                    }
                });

//                oldNearPlayer.forEach(player -> {
//                    if (!nearPlayers.contains(player) && player.getScoreboard().equals(recount.getBoard())) {
//                        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
//                    }
//                });
//                recount.addAttackers(attackedBy);
//                nearPlayers.forEach(player -> {
//                    try {
//                        player.setScoreboard(recount.getBoard());
//                    } catch (Exception ignored) {
//                    }
//                });
//                oldNearPlayer.clear();
//                oldNearPlayer.addAll(nearPlayers);

                Set<Player> toRemove = new HashSet<>();
                attackedBy.forEach((player, damage) -> {
                    if (!nearPlayers.contains(player) && Dragon.getDragon().getHealth() > 100) {
                        toRemove.add(player);
                    }
                });

                toRemove.forEach(player -> {
                    Dragon.removeAttackedBy(player);
                    Damage.damage(player, Dragon.getDragon(), 200);
                });
                toRemove.clear();

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
        if (1 != 2) {
            return;
        }
        if (activated) {
            return;
        }

        findDragon();
        if (dragon == null || dragon.isDead()) {
            return;
        }

        activated = true;
        startTime = System.currentTimeMillis();
        setDefaultSettings();
        activateScheduler();
    }

    public static void stopBattle() {
        if (!activated) {
            return;
        }
        activated = false;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }

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
        bossbar = null;
    }

    private static void giveReward() {
        long endTime = System.currentTimeMillis();
        String totalTime = DateFormatter.formatMillis(endTime - startTime);
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(Component.text("§5Эндер Дракон повержен!"));
            player.sendMessage(Component.text("§7Затраченное время: " + totalTime));
        });
        List<Player> topList = Recount.getTop(10);
        SuckingEvent suckEvent = new SuckingEvent(topList);
        Bukkit.getPluginManager().callEvent(suckEvent);
        attackedBy.forEach((player, integer) -> {
            player.sendMessage(Component.text("§e===== §2TOP DAMAGE §e====="));
            for (int i = 0; i < topList.size(); i++) {
                String color;
                String colorDamage;
                Player top = topList.get(i);
                if (player.equals(top)) {
                    color = "§a";
                    colorDamage = "§b";
                } else {
                    color = "§7";
                    colorDamage = "§7";
                }
                player.sendMessage(Component.text(color + (i + 1) + ". " + top.getName() + " " + colorDamage + attackedBy.get(top)));
            }
        });
        attackedBy.forEach((player, integer) -> {
            player.giveExp(315);
            if (integer > 50) {
                giveReward(player);
            }
        });
    }

    public static void giveReward(Player player) {
        player.giveExp(1080);
        if (ThreadLocalRandom.current().nextInt(100) < 51) {
            player.getInventory().addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, ThreadLocalRandom.current().nextInt(2, 4)));
        }
        if (ThreadLocalRandom.current().nextInt(100) < 26) {
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
            Bukkit.getLogger().info(ComponentUtils.plainText(text));
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
