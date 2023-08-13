package net.reworlds.modifiedbosses.boss.dragon;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.bossbars.Timer;
import net.reworlds.modifiedbosses.utils.ComponentUtils;
import net.reworlds.modifiedbosses.utils.Particles;
import net.reworlds.modifiedbosses.utils.Random;
import net.reworlds.modifiedbosses.utils.TeamUtils;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DragonAbilities {

    private final Dragon dragon;

    public DragonAbilities(Dragon dragon) {
        this.dragon = dragon;
    }

    public static Team getSoulBombTeam() {
        return TeamUtils.getTeam(NamedTextColor.BLUE, "SoulBombTeam");
    }

    public static Team getBoilingBloodTeam() {
        return TeamUtils.getTeam(NamedTextColor.RED, "BoilingBloodTeam");
    }

    public static Team getPlagueSurfaceTeam() {
        return TeamUtils.getTeam(NamedTextColor.GREEN, "PlagueSurfaceTeam");
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
        if (dragon.getDamagers().isEmpty()) {
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
        List<Player> damagers = dragon.getDamagers();
        damagers.forEach(player -> {
            Component message1 = ComponentUtils.gradient("#FF1C72", "#FEB781", "⚠ Драконий Рёв ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Подготовьтесь к падению!");
            player.showTitle(Title.title(message1, message2));
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);

        });
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            damagers.forEach(player -> {
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
        List<Player> damagers = dragon.getDamagers();
        PotionEffect darkness = new PotionEffect(PotionEffectType.DARKNESS, 200, 1);
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 200, 1);
        damagers.forEach(player -> {
            Component message1 = ComponentUtils.gradient("#1F3F86", "#0B162F", "⚠ Взгляд Тьмы ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Эндермены злы на вас!");
            player.showTitle(Title.title(message1, message2));
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
        });
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            damagers.forEach(player -> {
                if (dragon.isNear(player) && !dragon.getBoss().isDead()) {
                    for (int i = 0; i < dragon.getPhase(); i++) {
                        Enderman enderman = (Enderman) player.getWorld().spawnEntity(Random.randomFlatY(player.getLocation(), true, 10), EntityType.ENDERMAN);
                        enderman.setTarget(player);
                    }
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
        List<Player> damagers = dragon.getDamagers();

        damagers.forEach(player -> {
            Component message1 = ComponentUtils.gradient("#E756B2", "#AB1283", "⚠ Космический луч ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Убегайте из зоны поражения!");
            player.showTitle(Title.title(message1, message2));
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
            try {
                Timer.of(player, 5, "§dКосмический Луч", BarColor.PINK);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
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
                        Particles.circle(location.clone(), Color.FUCHSIA, 6, false);
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
                    if (entity instanceof Player player && player.getGameMode() != GameMode.SPECTATOR
                            && dragon.getDamagers().contains(player) && dragon.calculateDistance(location, player.getLocation()) <= 6) {
                        if (dragon.getPhase() == 2) {
                            dragon.damagePlayer(player, 12 * 2);
                        } else {
                            dragon.damagePlayer(player, 12);
                        }
                    }
                });
            });

            damagers.forEach(player -> {
                if (dragon.isNear(player) && !dragon.getBoss().isDead()) {
                    player.playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                }
            });
        }, 105);
    }

    private void shadowOfDeath() {
        List<Player> damagers = dragon.getDamagers();
        List<Player> players = new ArrayList<>();

        damagers.forEach(player -> {
            Component message1 = ComponentUtils.gradient("#594B92", "#981173", "⚠ Дыхание Смерти ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Убегайте из зоны поражения!");
            player.showTitle(Title.title(message1, message2));
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
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
            Component message1 = ComponentUtils.gradient("#0295C9", "#1E3D84", "⚠ Бомба Души ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Подойдите к союзникам!");
            player.showTitle(Title.title(message1, message2));
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
            try {
                Timer.of(player, 10, "§bБомба Души", BarColor.BLUE);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
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
                            || !dragon.getDamagers().contains(player)
                            || dragon.calculateDistance(target.getLocation(), player.getLocation()) > 6
                    );
                    int damage = 56 / (nearbyEntities.size() + 1);
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
            Component message1 = ComponentUtils.gradient("#F13F40", "#A01109", "⚠ Кипящая Кровь ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Отбегите от союзников!");
            player.showTitle(Title.title(message1, message2));
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
            try {
                Timer.of(player, 10, "§4Кипящая Кровь", BarColor.RED);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
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
                            || !dragon.getDamagers().contains(player)
                            || dragon.calculateDistance(target.getLocation(), player.getLocation()) > 6
                    );
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
        List<Player> damagers = dragon.getDamagers();

        damagers.forEach(player -> {
            if (!targets.contains(player)) {
                Component message1 = ComponentUtils.gradient("#47FA1E", "#268510", "⚠ Чума Порченой Крови ⚠");
                Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Подойдите к союзнику с эффектом!");
                player.showTitle(Title.title(message1, message2));
                player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
                try {
                    Timer.of(player, 10, "§2Чума Порченой Крови", BarColor.GREEN);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
            } else {
                Component message1 = ComponentUtils.gradient("#47FA1E", "#268510", "⚠ Метка иммунитета ⚠");
                Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Защитите зараженных союзников!");
                player.showTitle(Title.title(message1, message2));
                player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
                try {
                    Timer.of(player, 10, "§2Метка иммунитета", BarColor.GREEN);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
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
            targets.forEach(damagers::remove);
            targets.forEach(target -> {
                if (dragon.isNear(target) && !dragon.getBoss().isDead()) {
                    Collection<Entity> nearbyEntities = target.getWorld()
                            .getNearbyEntities(target.getLocation(), 2, 2, 2);
                    nearbyEntities.forEach(entity -> {
                        if (entity instanceof Player player
                                && dragon.calculateDistance(target.getLocation(), player.getLocation()) <= 6) {
                            damagers.remove(player);
                        }
                    });
                }
                target.setGlowing(false);
                getPlagueSurfaceTeam().removeEntity(target);
                TeamUtils.returnTeamBefore(target);
            });
            damagers.forEach(player -> {

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
        List<Player> players = dragon.getDamagers();
        if (everyN == 1) {
            return players;
        }

        List<Player> targets = new ArrayList<>();
        int size = players.size();
        for (int i = 0; i < size / everyN + 1; i++) {
            Player temp = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            if (temp.isOnline() && !temp.isDead() && dragon.isNear(temp)) {
                players.remove(temp);
                targets.add(temp);
            }
        }
        return targets;
    }
}
