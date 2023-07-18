package net.reworlds.modifiedbosses.boss.dragon;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.boss.Boss;
import net.reworlds.modifiedbosses.charms.Charms;
import net.reworlds.modifiedbosses.charms.CharmsEffects;
import net.reworlds.modifiedbosses.utils.ComponentUtils;
import net.reworlds.modifiedbosses.utils.Particles;
import net.reworlds.modifiedbosses.utils.TeamUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dragon extends Boss {

    private static List<Location> deadLocations;
    private Team bossTeam;
    @Getter
    private long lastAbility;
    @Getter
    private int phase;
    private boolean isSecondPhase = false;

    public Dragon(LivingEntity boss, String bossName, String bossNameColor) {
        super(boss, bossName, bossNameColor);
        setAllowExplodeDamage(false);
        setRadius(200);
        setMaxDamagePerHit(20);
        setMinimumDamageToReward(50);
        setSaveDamagePercent(100);
        setAttributes(Map.of(Attribute.GENERIC_MAX_HEALTH, 2000));
        setSettings();
        bar = setBossBar(bossName, BarColor.RED, BarStyle.SEGMENTED_10);
        activate();
    }

    @Override
    protected void setSettings() {
        phase = 0;
        bossTeam = TeamUtils.getTeam(ChatColor.DARK_PURPLE, bossName + "team");
        bossTeam.addEntity(boss);
        boss.setGlowing(true);
        boss.setCustomName(bossName);
        boss.setHealth(2000);
        boss.setAI(true);
        isSecondPhase = false;

        clearDamagers();
        if (boss instanceof EnderDragon dragon) {
            dragon.setPhase(EnderDragon.Phase.CIRCLING);
        }

        if (deadLocations == null) {
            deadLocations = List.of(
                    new Location(boss.getWorld(), 28, 66, 28),
                    new Location(boss.getWorld(), 28, 66, -29),
                    new Location(boss.getWorld(), -29, 66, -29),
                    new Location(boss.getWorld(), -29, 66, 28)
            );
        }
    }

    public void removeBoss() {
        getDamagers().forEach(player -> {
            if (DragonAbilities.removeEntityFromTeam(player)) {
                TeamUtils.returnTeamBefore(player);
            }
        });

        boss.remove();
        bar.setVisible(false);
    }

    @Override
    public void remove() {
        super.stopBattle();
        getDamagers().forEach(player -> {
            if (DragonAbilities.removeEntityFromTeam(player)) {
                TeamUtils.returnTeamBefore(player);
            }
        });

        bar.setVisible(false);
    }

    @Override
    protected void rewardPlayers() {
        getDamagers().forEach(player -> {
            Double damage = damageByPlayers.get(player.getUniqueId());
            player.giveExp(315);
            if (damage > minimumDamageToReward) {
                specialReward(player, (int) (damage / 50));
            }
        });
    }

    private void specialReward(Player player, int percent) {
        percent = Math.min(percent, 20);
        player.giveExp(1080);
        int applechance = ThreadLocalRandom.current().nextInt(100);
        if (applechance < 40 + percent) {
            giveOrDrop(player, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, ThreadLocalRandom.current().nextInt(2, 4)));
        }

        int charmchance = ThreadLocalRandom.current().nextInt(100);
        if (charmchance < 20 + percent) {
            ItemStack item;
            if (ThreadLocalRandom.current().nextInt(100) < 20) {
                item = Charms.EPIC.get(ThreadLocalRandom.current().nextInt(Charms.EPIC.size())).getCharm();
            } else {
                item = Charms.RARE.get(ThreadLocalRandom.current().nextInt(Charms.RARE.size())).getCharm();
            }
            giveOrDrop(player, item);
            Component itemDisplayName = item.getItemMeta().displayName();
            if (itemDisplayName == null) {
                return;
            }
            Component text = Component.text("§e" + player.getName() + " выбивает ").append(itemDisplayName).append(Component.text("§e!"));
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(text));
        }
    }

    @Override
    protected void loopTask() {
        try {
            if (((EnderDragon) boss).getPhase() == EnderDragon.Phase.HOVER) {
                ((EnderDragon) boss).setPhase(EnderDragon.Phase.CIRCLING);
            }
        } catch (Exception ignored) {
        }

        if (boss.getLocation().getY() < 60) {
            boss.teleport(boss.getLocation().clone().set(0, 100, 0));
        }

        getDamagers().forEach(player -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
            player.setGliding(false);
        });

        deadLocations.forEach(location -> {
            while (location.getWorld().getHighestBlockYAt(location) > 65) {
                location.getWorld().getHighestBlockAt(location).setType(Material.AIR);
            }
        });

        AtomicBoolean isCrystalsInRange = new AtomicBoolean(false);
        boss.getNearbyEntities(radius, radius, radius).forEach(entity -> {
            if (entity instanceof EnderCrystal crystal) {
                if (bossTeam != null) {
                    bossTeam.addEntity(crystal);
                    crystal.setGlowing(true);
                }
                isCrystalsInRange.set(true);
            }
        });
        if (!boss.isDead()) {
            boss.setInvulnerable(isCrystalsInRange.get());
        }

        if (!boss.isInvulnerable()) {
            phase = 1;
        }

        if (boss.getHealth() <= 1000) {
            phase = 2;
            if (!isSecondPhase) {
                getDamagers().forEach(player -> {
                    player.sendMessage(Component.text(bossNameColor + bossName + " §cперешёл на 2 фазу!"));
                });
            }
            isSecondPhase = true;
        }

        if (!boss.isDead() && phase != 0) {
            lastAbility = new DragonAbilities(this).activate();
        }
    }

    @EventHandler
    public void onDragonChangeEvent(EnderDragonChangePhaseEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        if ((event.getNewPhase() == EnderDragon.Phase.FLY_TO_PORTAL
                || event.getNewPhase() == EnderDragon.Phase.LAND_ON_PORTAL)
                && event.getEntity().isInvulnerable()) {
            event.setCancelled(true);
        }
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        super.onPlayerQuit(event);

        if (boss == null || boss.isDead()) {
            return;
        }

        Player player = event.getPlayer();
        if (DragonAbilities.removeEntityFromTeam(player)) {
            TeamUtils.returnTeamBefore(player);
            player.setGlowing(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        if (DragonAbilities.removeEntityFromTeam(player)) {
            player.setGlowing(false);
            TeamUtils.returnTeamBefore(player);
        }
        if (boss.getHealth() > 200) {
            removeDamager(player);
        }
    }

    @EventHandler
    public void onGliding(EntityToggleGlideEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        if (event.getEntity() instanceof Player player) {
            if (isNear(player) && !boss.isDead() && player.isGliding()) {
                player.setGliding(false);
            }
        }
    }

    @EventHandler
    public void onRegenerate(EntityRegainHealthEvent event) {
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.ENDER_CRYSTAL) {
            return;
        }

        if (!event.getEntity().equals(boss)) {
            return;
        }

        event.setCancelled(true);
    }

    @Override
    protected boolean isBlackListEffect(PotionEffect effect) {
        if (effect == null) {
            return false;
        }

        return CharmsEffects.getPositiveEffects().contains(effect.getType());
    }

    @Override
    protected void invulnerableBossDamage() {
        Particles.sphere(boss.getLocation(), Color.PURPLE, 10);
    }
}