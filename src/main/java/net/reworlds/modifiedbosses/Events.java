package net.reworlds.modifiedbosses;

import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent;
import net.reworlds.modifiedbosses.boss.dragon.Abilities;
import net.reworlds.modifiedbosses.boss.dragon.Dragon;
import net.reworlds.modifiedbosses.utils.Particles;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Events implements Listener {

    @EventHandler
    public void onEntityResurrect(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player player) {
            player.setCooldown(Material.TOTEM_OF_UNDYING, 6000);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getLocation().getWorld().equals(Dragon.getBattleWorld())) {
            Dragon.startBattle();

            Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            for (ChatColor value : ChatColor.values()) {
                Team team;
                try {
                    team = mainScoreboard.registerNewTeam(value + "team");
                } catch (IllegalArgumentException ignored) {
                    try {
                        team = mainScoreboard.getTeam(value + "team");
                    } catch (IllegalArgumentException ignored1) {
                        return;
                    }
                }
                team.removeEntity(event.getPlayer());
                event.getPlayer().setGlowing(false);
            }
        }
    }

    @EventHandler
    public void onPlayerLeft(PlayerQuitEvent event) {
        if (event.getPlayer().getLocation().getWorld().equals(Dragon.getBattleWorld())) {
            event.getPlayer().setGlowing(false);
            if (Abilities.getBoilingBloodTeam() != null) {
                Abilities.getBoilingBloodTeam().removeEntity(event.getPlayer());
            }
            if (Abilities.getSoulBombTeam() != null) {
                Abilities.getSoulBombTeam().removeEntity(event.getPlayer());
            }
            if (Abilities.getPlagueSurfaceTeam() != null) {
                Abilities.getPlagueSurfaceTeam().removeEntity(event.getPlayer());
            }
            event.getPlayer().damage(200, Dragon.getDragon());
            Dragon.getAttackedBy().remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onTeleportToEnd(PlayerPortalEvent event) {
        if (event.getTo().getWorld().equals(Dragon.getBattleWorld())) {
            Dragon.startBattle();
        }
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Player && event.getTo() != null
                && event.getTo().getWorld().equals(Dragon.getBattleWorld())) {
            Dragon.startBattle();
        }
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEndGatewayEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            event.setCancelled(true);
        }

        if (event.getEntity() instanceof Player && event.getTo() != null
                && event.getTo().getWorld().equals(Dragon.getBattleWorld())) {
            Dragon.startBattle();
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
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
                player.damage(event.getDamage() / 3, Dragon.getDragon());
                return;
            }
            if (event.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player player) {
                Dragon.addDamage(player, event.getDamage());
                player.damage(event.getDamage() / 3, Dragon.getDragon());
            }
        }
    }
}
