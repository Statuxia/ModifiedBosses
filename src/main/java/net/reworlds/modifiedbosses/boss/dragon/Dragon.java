package net.reworlds.modifiedbosses.boss.interstellardevourer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.reworlds.modifiedbosses.ModifiedBosses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class InterstellarDevourer {

    private static final HashMap<Player, Integer> attackedBy = new HashMap<>();
    private static final Set<Player> nearPlayers = new HashSet<>();
    @Getter
    @Setter
    private static World battleWorld;
    private static EnderDragon dragon;
    @Getter(AccessLevel.PRIVATE)
    private static boolean isFighting;
    @Getter
    private static int phase;
    private static BukkitTask findNearPlayers;
    private static Team dragonTeam;


    public static boolean isSameDragon(EnderDragon dragon) {
        return InterstellarDevourer.dragon.equals(dragon);
    }

    public static void findDragon() {
        if (InterstellarDevourer.dragon != null && !InterstellarDevourer.dragon.isDead()) {
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
            InterstellarDevourer.dragon = dragon;
            setDefaultSettings();
        }
    }

    public static void selectDragon(EnderDragon dragon) {
        if (InterstellarDevourer.dragon == null || InterstellarDevourer.dragon.isDead()) {
            if (dragon.isDead()) {
                findDragon();
            } else {
                InterstellarDevourer.dragon = dragon;
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
        findNearPlayers = Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
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
            dragon.setInvulnerable(isCrystalsInRange.get());

            Bukkit.getLogger().info(" - " + nearPlayers.size());
            Bukkit.getLogger().info(" - " + nearPlayers.isEmpty());
            Bukkit.getLogger().info(" - " + isFighting);
            if (nearPlayers.isEmpty() && isFighting()) {
                Bukkit.getLogger().info("- stop battle");
                stopBattle();
            }
        }, 0, 50);

    }


    public static void startBattle() {
        Bukkit.getLogger().info("startBattle");
        if (isFighting) {
            Bukkit.getLogger().info("- fighting");
            return;
        }

        findDragon();
        if (dragon == null || dragon.isDead()) {
            Bukkit.getLogger().info("- I.D. is null or dead");
            return;
        }

        isFighting = true;
        Bukkit.getLogger().info("- fighting = " + isFighting);
        phase = 1;
        Bukkit.getLogger().info("- phase = " + phase);
        setDefaultSettings();

        // TODO
    }

    public static void stopBattle() {
        Bukkit.getLogger().info("stopBattle");

        attackedBy.forEach((player, damage) -> Bukkit.getLogger().info(" - " + player.getName() + " " + damage));

        if (!isFighting) {
            Bukkit.getLogger().info("- is fighting = " + isFighting);
            return;
        }
        isFighting = false;

        if (dragon != null && !dragon.isDead()) {
            Bukkit.getLogger().info("- dragon is alive");
            setDefaultSettings();
            attackedBy.clear();
            nearPlayers.clear();
            return;
        }

        Bukkit.getLogger().info("- rewards");
        giveReward();
        attackedBy.clear();
        nearPlayers.clear();
        findNearPlayers.cancel();
    }


    private static void giveReward() {
        // TODO: Prises
    }

    public static void addDamage(Player player, double damage) {
        int done = attackedBy.getOrDefault(player, 0) + (int) damage;
        attackedBy.put(player, done);
    }
}
