package net.reworlds.modifiedbosses;

import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent;
import net.reworlds.modifiedbosses.boss.dragon.Abilities;
import net.reworlds.modifiedbosses.boss.dragon.Dragon;
import net.reworlds.modifiedbosses.utils.Damage;
import net.reworlds.modifiedbosses.utils.Particles;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ThreadLocalRandom;

public class Events implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!Dragon.isActivated() || !Dragon.isSameWorld(player) || !Dragon.isNearDragon(player)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        if (Dragon.isSameWorld(player) && Abilities.removeEntityFromTeam(player)) {
            player.setGlowing(false);
        }
    }

    @EventHandler
    public void onPlayerLeft(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (Dragon.isSameWorld(player)) {
            if (Abilities.removeEntityFromTeam(player)) {
                player.setGlowing(false);
            }
            if (Dragon.isActivated() && Dragon.isNearDragon(player)) {
                Damage.damage(player, Dragon.getDragon(), 200);
                Dragon.removeAttackedBy(player);
            }
        }
    }

    @EventHandler
    public void onGliding(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (Dragon.isSameWorld(player) && Dragon.isNearDragon(player)) {
                event.setCancelled(true);
                player.setGliding(false);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
//        if (Dragon.isActivated() && Dragon.isSameWorld(player)) {
//            event.deathMessage(null);
//        }

        if (Dragon.isSameWorld(player) && Abilities.removeEntityFromTeam(player)) {
            player.setGlowing(false);
        }
        Dragon.removeAttackedBy(player);
    }

    @EventHandler
    public void onTeleportToEnd(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        if (Dragon.isSameWorld(player) && Dragon.isNearCenter(player) && !Dragon.isActivated()) {
            Dragon.activateDragon();
        }
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof EnderDragon || event.getEntity() instanceof EnderDragonPart) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Player player && Dragon.isSameWorld(player)
                && Dragon.isNearCenter(player) && !Dragon.isActivated()) {
            Dragon.activateDragon();
        }
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEndGatewayEvent event) {
        if (event.getEntity() instanceof EnderDragon || event.getEntity() instanceof EnderDragonPart) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Player player && Dragon.isSameWorld(player)
                && Dragon.isNearCenter(player) && !Dragon.isActivated()) {
            Dragon.activateDragon();
        }
    }

    @EventHandler
    public void onDragonSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof EnderDragon dragon) {
            Dragon.selectDragon(dragon);
        }
    }

    @EventHandler
    public void onDragonChangeEvent(EnderDragonChangePhaseEvent event) {
        if ((event.getNewPhase() == EnderDragon.Phase.FLY_TO_PORTAL || event.getNewPhase() == EnderDragon.Phase.LAND_ON_PORTAL) && event.getEntity().isInvulnerable()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDragonDamage(EntityDamageByBlockEvent event) {
        if (event.getEntity() instanceof EnderDragon dragon) {
            if (dragon.isInvulnerable()) {
                Particles.sphere(dragon.getLocation(), Color.PURPLE, 10);
                event.setCancelled(true);
                return;
            }

            if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                    || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
                return;
            }

            if (event.getDamage() > 35) {
                event.setDamage(35);
            }
        }
    }

    @EventHandler
    public void onDragonDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof EnderDragon dragon) {
            if (dragon.isInvulnerable()) {
                Particles.sphere(dragon.getLocation(), Color.PURPLE, 10);
                event.setCancelled(true);
                return;
            }

            if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                    || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
                return;
            }

            if (event.getDamage() > 35) {
                event.setDamage(35);
            }

            if (event.getDamager() instanceof Player player) {
                Dragon.addDamage(player, event.getDamage());
                if (ThreadLocalRandom.current().nextBoolean()) {
                    Damage.damage(player, Dragon.getDragon(), event.getDamage() / 3);
                }
                return;
            }
            if (event.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player player) {
                Dragon.addDamage(player, event.getDamage());
                if (ThreadLocalRandom.current().nextBoolean()) {
                    Damage.damage(player, Dragon.getDragon(), event.getDamage() / 3);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRessurectOnBattle(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player player && Dragon.isSameWorld(player)
                && Dragon.isNearCenter(player) && Dragon.isActivated()) {
            player.setCooldown(Material.TOTEM_OF_UNDYING, 6000);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        Player player = event.getPlayer();

        if (item.getScoreboardTags().size() == 0) {
            return;
        }

        if (!item.getScoreboardTags().contains(player.getName())) {
            event.setCancelled(true);
            return;
        }

        item.removeScoreboardTag(player.getName());
    }
}
