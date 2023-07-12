package net.reworlds.modifiedbosses.boss.dragon;

import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.bossbars.Timer;
import net.reworlds.modifiedbosses.utils.Particles;
import net.reworlds.modifiedbosses.utils.TeamUtils;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DragonAbilities {

    private final Dragon dragon;

    public DragonAbilities(Dragon dragon) {
        this.dragon = dragon;
    }

    public static Team getSoulBombTeam() {
        return TeamUtils.getTeam(ChatColor.BLUE, "SoulBombTeam");
    }

    public static Team getBoilingBloodTeam() {
        return TeamUtils.getTeam(ChatColor.RED, "BoilingBloodTeam");
    }

    public static Team getPlagueSurfaceTeam() {
        return TeamUtils.getTeam(ChatColor.GREEN, "PlagueSurfaceTeam");
    }

    public static List<Team> getAbilitiesTeam() {
        return List.of(getSoulBombTeam(), getBoilingBloodTeam(), getPlagueSurfaceTeam());
    }

    public static boolean removeEntityFromTeam(Entity entity) {
        return getSoulBombTeam().removeEntity(entity)
                || getBoilingBloodTeam().removeEntity(entity)
                || getPlagueSurfaceTeam().removeEntity(entity);
    }

    public long activate() {
        long lastAbility = dragon.getLastAbility();
        int phase = dragon.getPhase();
        if (dragon.getDamageByPlayers().isEmpty()) {
            return lastAbility;
        }
        if (lastAbility + (1000L * ThreadLocalRandom.current().nextInt(18, 26)) > System.currentTimeMillis()) {
            return lastAbility;
        }
        int ability = ThreadLocalRandom.current().nextInt(phase == 2 ? 7 : 4);
        switch (ability) {
            case 0 -> dragonRoar();
            case 1 -> voidView();
            case 2 -> cosmicRay();
            case 3 -> shadowOfDeath();
            case 4 -> soulBomb();
            case 5 -> boilingBlood();
            case 6 -> plagueSurface();
        }
        return System.currentTimeMillis();
    }

    private void dragonRoar() {
        Map<Player, Double> damageByPlayers = dragon.getDamageByPlayers();
        damageByPlayers.forEach((player, damage) -> {
            player.sendTitle("§c⚠ §eДраконий Рёв §c⚠§r", "§eПодготовьтесь к падению!§r");
            player.sendMessage(Component.text("§c⚠ §eДраконий Рёв §c⚠§ \n§eПодготовьтесь к падению!§r"));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);

        });
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            damageByPlayers.forEach((player, damage) -> {
                if (dragon.isNear(player) && !dragon.getBoss().isDead()) {
                    if (dragon.getPhase() == 2) {
                        player.setVelocity(new Vector(0, 2 * 2, 0));
                    } else {
                        player.setVelocity(new Vector(0, 2, 0));
                    }

                    player.playSound(player, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1, 1);
                }
            });
        }, 20 * 3);
    }

    private void voidView() {
        Map<Player, Double> damageByPlayers = dragon.getDamageByPlayers();
        PotionEffect darkness = new PotionEffect(PotionEffectType.DARKNESS, 200, 1);
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 200, 1);
        damageByPlayers.forEach((player, damage) -> {
            player.sendTitle("§c⚠ §8Взгляд Тьмы §c⚠§r", "");
            player.sendMessage(Component.text("§c⚠ §8Взгляд Тьмы §c⚠§r"));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
        });
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            damageByPlayers.forEach((player, damage) -> {
                if (dragon.isNear(player) && !dragon.getBoss().isDead()) {
                    player.addPotionEffect(darkness);
                    player.addPotionEffect(blindness);
                    player.playSound(player, Sound.ENTITY_GHAST_WARN, 0.5f, 1);
                }
            });
        }, 20 * 3);
    }

    private void cosmicRay() {
        List<Location> locations = new ArrayList<>();
        List<Player> players = new ArrayList<>();
        Map<Player, Double> damageByPlayers = dragon.getDamageByPlayers();

        damageByPlayers.forEach((player, damage) -> {
            player.sendTitle("§c⚠ §dКосмический Луч §c⚠§r", "§eУбегайте из зоны поражения!§r");
            player.sendMessage(Component.text("§c⚠ §dКосмический Луч §c⚠ \n§eУбегайте из зоны поражения!§r"));
            Timer.of(player, "cosmicRay", 5, "§dКосмический Луч", BarColor.PINK);
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
            players.add(player);
        });

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                players.forEach(player -> {
                    if (dragon.isNear(player) && !dragon.getBoss().isDead()) {
                        Location location = player.getLocation();
                        Particles.particleLine(dragon.getBoss().getLocation().clone().add(0, -0.5, 0), location.clone().add(0.5, 0, 0.5), Color.RED);
                        Particles.particleLine(dragon.getBoss().getLocation().clone().add(0, -0.5, 0), location.clone().add(-0.5, 0, -0.5), Color.RED);
                        Particles.particleLine(dragon.getBoss().getLocation().clone().add(0, -0.5, 0), location.clone().add(-0.5, 0, 0.5), Color.RED);
                        Particles.particleLine(dragon.getBoss().getLocation().clone().add(0, -0.5, 0), location.clone().add(0.5, 0, -0.5), Color.RED);
                        Particles.particleLine(dragon.getBoss().getLocation(), location, Color.FUCHSIA);
                        Particles.circle(location.clone(), Color.FUCHSIA, 6);
                        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                        if (finalI == 8) {
                            locations.add(player.getLocation().clone());
                        }
                    }
                });
            }, 10 * i);
        }

        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            locations.forEach(location -> {
                location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location.clone().add(0, 1, 0), 1);
                location.getNearbyEntities(6, 6, 6).forEach(entity -> {
                    if (entity instanceof Player player && player.getGameMode() != GameMode.SPECTATOR) {
                        if (dragon.getPhase() == 2) {
                            dragon.damagePlayer(player, 12 * 2);
                        } else {
                            dragon.damagePlayer(player, 12);
                        }
                    }
                });
            });

            damageByPlayers.forEach((player, damage) -> {
                if (dragon.isNear(player) && !dragon.getBoss().isDead()) {
                    player.playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                }
            });
        }, 105);
    }

    private void shadowOfDeath() {
        Map<Player, Double> damageByPlayers = dragon.getDamageByPlayers();
        List<Player> players = new ArrayList<>();

        damageByPlayers.forEach((player, damage) -> {
            player.sendTitle("§c⚠ §5Дыхание Смерти §c⚠§r", "§eУбегайте из зоны поражения!§r");
            player.sendMessage(Component.text("§c⚠ §5Дыхание Смерти §c⚠ \n§eУбегайте из зоны поражения!§r"));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
            players.add(player);
        });

        for (int i = 0; i < 10; i++) {
            Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                players.forEach(player -> {
                    if (dragon.isNear(player) && !dragon.getBoss().isDead()) {
                        Location dragonLocation = dragon.getBoss().getLocation();
                        DragonFireball fireball = dragonLocation.getWorld().spawn(dragonLocation, DragonFireball.class);
                        fireball.setDirection(player.getLocation().subtract(dragonLocation).toVector());
                        fireball.setVelocity(fireball.getVelocity().multiply(6));
                        player.playSound(player, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1, 1);
                    }
                });
            }, 10 * i);
        }
    }

    private void soulBomb() {
        List<Player> targets = getTargets(8);

        targets.forEach(player -> {
            player.sendTitle("§b⚠ §bБомба Души §c⚠§r", "§eПодойдите к союзникам!§r");
            player.sendMessage("§b⚠ §bБомба Души §c⚠ \n§eПодойдите к союзникам!§r");
            Timer.of(player, "soulBomb", 10, "§bБомба Души", BarColor.BLUE);
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
            TeamUtils.saveTeamBefore(player, getAbilitiesTeam());
            getSoulBombTeam().addEntity(player);
            player.setGlowing(true);
        });

        for (int i = 0; i < 40; i++) {
            for (Player target : targets) {
                if (dragon.isNear(target) && !dragon.getBoss().isDead()) {
                    Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                        Particles.circle(target.getLocation(), Particle.FALLING_WATER, 3);
                        target.playSound(target, Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                    }, 5 * i);
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            targets.forEach(target -> {
                if (dragon.isNear(target) && !dragon.getBoss().isDead()) {
                    Collection<LivingEntity> nearbyEntities = target.getLocation().getWorld()
                            .getNearbyLivingEntities(target.getLocation(), 3, 3, 3);
                    nearbyEntities.removeIf(livingEntity -> (!(livingEntity instanceof Player player)
                            || player.getGameMode() == GameMode.SPECTATOR)
                            || !dragon.getDamageByPlayers().containsKey(player));
                    int damage = 50 / (nearbyEntities.size() + 1);
                    nearbyEntities.forEach(entity -> {
                        dragon.damagePlayer(entity, damage);
                    });
                }
                target.setGlowing(false);
                getSoulBombTeam().removeEntity(target);
                TeamUtils.returnTeamBefore(target);
            });
        }, 200);
    }

    private void boilingBlood() {
        List<Player> targets = getTargets(1);

        targets.forEach(player -> {
            player.sendTitle("§b⚠ §4Кипящая Кровь §c⚠§r", "§eОтбегите от союзников!§r");
            player.sendMessage("§b⚠ §4Кипящая Кровь §c⚠ \n§eОтбегите от союзников!§r");
            Timer.of(player, "boilingBlood", 10, "§4Кипящая Кровь", BarColor.RED);
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
            TeamUtils.saveTeamBefore(player, getAbilitiesTeam());
            getBoilingBloodTeam().addEntity(player);
            player.setGlowing(true);
        });

        for (int i = 0; i < 40; i++) {
            for (Player target : targets) {
                if (dragon.isNear(target) && !dragon.getBoss().isDead()) {
                    Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                        Particles.circle(target.getLocation(), Particle.FALLING_LAVA, 14);
                        target.playSound(target, Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                    }, 5 * i);
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            targets.forEach(target -> {
                if (dragon.isNear(target) && !dragon.getBoss().isDead()) {
                    Collection<LivingEntity> nearbyEntities = target.getLocation().getWorld()
                            .getNearbyLivingEntities(target.getLocation(), 14, 14, 14);
                    nearbyEntities.removeIf(livingEntity -> (!(livingEntity instanceof Player player)
                            || player.getGameMode() == GameMode.SPECTATOR)
                            || !dragon.getDamageByPlayers().containsKey(player));
                    int damage = 10 * nearbyEntities.size();
                    nearbyEntities.forEach(entity -> {
                        if (entity instanceof Player) {
                            dragon.damagePlayer(entity, damage);
                        }
                    });
                }
                target.setGlowing(false);
                getBoilingBloodTeam().removeEntity(target);
                TeamUtils.returnTeamBefore(target);
            });
        }, 200);
    }

    private void plagueSurface() {
        List<Player> targets = getTargets(9);

        dragon.getDamageByPlayers().forEach((player, damage) -> {
            if (!targets.contains(player)) {
                player.sendTitle("§b⚠ §2Чума Порченой Крови §c⚠§r", "§eПодойдите к союзнику с эффектом!§r");
                player.sendMessage("§b⚠ §2Чума Порченой Крови §c⚠ \n§eПодойдите к союзнику с эффектом!§r");
                Timer.of(player, "plagueSurface2", 10, "§2Чума Порченой Крови", BarColor.GREEN);
                player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
            } else {
                player.sendTitle("§b⚠ §2Метка иммунитета §c⚠§r", "§eЗащитите зараженных союзников!§r");
                player.sendMessage("§b⚠ §2Метка иммунитета §c⚠ \n§eПодойдите к союзнику с эффектом!§r");
                Timer.of(player, "plagueSurface", 10, "§2Метка иммунитета", BarColor.GREEN);
                player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);

                TeamUtils.saveTeamBefore(player, getAbilitiesTeam());
                getPlagueSurfaceTeam().addEntity(player);
                player.setGlowing(true);
            }
        });

        for (int i = 0; i < 40; i++) {
            for (Player target : targets) {
                Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                    Particles.circle(target.getLocation(), Particle.VILLAGER_HAPPY, 2);
                    target.playSound(target, Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                }, 5 * i);
            }
        }

        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            List<Player> damageTo = new ArrayList<>(dragon.getDamageByPlayers().keySet());
            targets.forEach(damageTo::remove);
            targets.forEach(target -> {
                if (dragon.isNear(target) && !dragon.getBoss().isDead()) {
                    Collection<Entity> nearbyEntities = target.getWorld()
                            .getNearbyEntities(target.getLocation(), 2, 2, 2);
                    nearbyEntities.forEach(entity -> {
                        if (entity instanceof Player player) {
                            damageTo.remove(player);
                        }
                    });
                }
                target.setGlowing(false);
                getPlagueSurfaceTeam().removeEntity(target);
                TeamUtils.returnTeamBefore(target);
            });
            damageTo.forEach(player -> {
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    return;
                }
                dragon.damagePlayer(player, 200);
                for (float i = 0; i < 2; i += 0.1) {
                    Particles.circle(player.getLocation().clone().add(0, i, 0), Particle.DRAGON_BREATH, 1);
                }
            });
        }, 200);
    }

    private List<Player> getTargets(int everyN) {
        List<Player> players = new ArrayList<>(dragon.getDamageByPlayers().keySet());

        if (everyN == 1) {
            return players;
        }

        List<Player> targets = new ArrayList<>();
        int size = players.size();
        for (int i = 0; i < size / everyN + 1; i++) {
            Player temp = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            players.remove(temp);
            targets.add(temp);
        }
        return targets;
    }
}
