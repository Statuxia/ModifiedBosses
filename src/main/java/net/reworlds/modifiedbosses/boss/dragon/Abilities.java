package net.reworlds.modifiedbosses.boss.dragon;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.utils.Particles;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Abilities {

    @Getter
    private static Team soulBombTeam;
    @Getter
    private static Team boilingBloodTeam;
    @Getter
    private static Team plagueSurfaceTeam;

    public static long activate() {
        if (Dragon.getLastAbility() + (1000L * ThreadLocalRandom.current().nextInt(20, 30)) > System.currentTimeMillis()) {
            return Dragon.getLastAbility();
        }
        if (Dragon.getDragon() == null || Dragon.getDragon().isDead() || Dragon.getPhase() == 0) {
            return Dragon.getLastAbility();
        }
        int ability = ThreadLocalRandom.current().nextInt(Dragon.getPhase() == 2 ? 7 : 3);
        Bukkit.getLogger().info(" - " + ability);
        switch (ability) {
            case 0 -> dragonRoar();
            case 1 -> voidView();
            case 2 -> cosmicRay();
            case 3 -> soulBomb();
            case 4 -> boilingBlood();
            case 5 -> plagueSurface();
        }
        return System.currentTimeMillis();
    }

    private static void dragonRoar() {
        Dragon.getNearPlayers().forEach(player -> {
            player.sendMessage(Component.text("§eВы чувствуете, что земля начинает трястись под вашими ногами."));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);

        });
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            Dragon.getNearPlayers().forEach(player -> {
                if (player.getWorld().equals(Dragon.getBattleWorld()) && player.getLocation().distance(Dragon.getDragon().getLocation()) < 300) {
                    player.setVelocity(new Vector(0, 2, 0));
                    player.playSound(player, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1, 1);
                    player.playSound(player, Sound.BLOCK_POWDER_SNOW_HIT, 0.5f, 1);
                }
            });
        }, 20 * 3);
    }

    private static void voidView() {
        PotionEffect effect = new PotionEffect(PotionEffectType.DARKNESS, 200, 1);
        PotionEffect effect1 = new PotionEffect(PotionEffectType.BLINDNESS, 200, 1);
        Dragon.getNearPlayers().forEach(player -> {
            player.sendMessage(Component.text("§eВаш взор застилает §0тьма..."));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
        });
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            Dragon.getNearPlayers().forEach(player -> {
                player.addPotionEffect(effect);
                player.addPotionEffect(effect1);
                player.playSound(player, Sound.ENTITY_GHAST_WARN, 0.5f, 1);
            });


        }, 20 * 3);
    }

    private static void cosmicRay() {
        List<Location> locations = new ArrayList<>();
        List<Player> players = new ArrayList<>();

        Dragon.getNearPlayers().forEach(player -> {
            player.sendMessage(Component.text("§eДракон нацелен на вас! §cБегите!"));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
            players.add(player);
        });

        for (int i = 0; i < 9; i++) {
            int finalI = i;
            Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                players.forEach(player -> {
                    if (player.getWorld().equals(Dragon.getBattleWorld()) && player.getLocation().distance(Dragon.getDragon().getLocation()) < 300) {
                        Location location = player.getLocation();
                        Particles.particleLine(Dragon.getDragon().getLocation().clone().add(0.5, -0.5, 0.5), location.clone().add(0.5, 0, 0.5), Color.RED);
                        Particles.particleLine(Dragon.getDragon().getLocation().clone().add(0.5, -0.5, 0.5), location.clone().add(-0.5, 0, -0.5), Color.RED);
                        Particles.particleLine(Dragon.getDragon().getLocation().clone().add(0.5, -0.5, 0.5), location.clone().add(-0.5, 0, 0.5), Color.RED);
                        Particles.particleLine(Dragon.getDragon().getLocation().clone().add(0.5, -0.5, 0.5), location.clone().add(0.5, 0, -0.5), Color.RED);
                        Particles.particleLine(Dragon.getDragon().getLocation(), location, Color.PURPLE);
                        Particles.circle(location.clone(), Color.PURPLE, 6);
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
                location.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, location.clone().add(0, 1, 0), 1);
                location.getNearbyEntities(6, 6, 6).forEach(entity -> {
                    if (entity instanceof Player player) {
                        player.damage(18, Dragon.getDragon());
                    }
                });
            });
            Dragon.getNearPlayers().forEach(player -> {
                player.playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            });
        }, 110);
    }

    private static void soulBomb() {
        List<Player> targets = getTargets();

        targets.forEach(player -> {
            player.sendMessage("§eВас отметели §bбомбой души§e! §aПодойдите к другим игрокам, чтобы распределить урон!");
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
            Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            try {
                soulBombTeam = mainScoreboard.registerNewTeam(ChatColor.BLUE + "team");
            } catch (IllegalArgumentException ignored) {
                soulBombTeam = mainScoreboard.getTeam(ChatColor.BLUE + "team");
            }
            soulBombTeam.setColor(ChatColor.BLUE);
            soulBombTeam.addEntity(player);
            player.setGlowing(true);
        });

        for (int i = 0; i < 40; i++) {
            for (Player target : targets) {
                if (target.getWorld().equals(Dragon.getBattleWorld()) && target.getLocation().distance(Dragon.getDragon().getLocation()) < 300) {
                    Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                        Particles.circle(target.getLocation(), Particle.FALLING_WATER, 5);
                        target.playSound(target, Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                    }, 5 * i);
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            targets.forEach(target -> {
                if (target.getWorld().equals(Dragon.getBattleWorld()) && target.getLocation().distance(Dragon.getDragon().getLocation()) < 300) {
                    Collection<LivingEntity> nearbyEntities = target.getLocation().getWorld()
                            .getNearbyLivingEntities(target.getLocation(), 5, 5, 5);
                    int damage = 70 / nearbyEntities.size();
                    nearbyEntities.forEach(entity -> {
                        entity.damage(damage, Dragon.getDragon());
                    });
                    target.setGlowing(false);
                    soulBombTeam.removeEntity(target);
                }
            });
        }, 200);
    }

    private static void boilingBlood() {
        List<Player> targets = getTargets();

        targets.forEach(player -> {
            player.sendMessage(Component.text("§eВас отметели §4кипящей кровью§e! §aОтбегите от других, чтобы избежать увеличения урона."));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
            Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            try {
                boilingBloodTeam = mainScoreboard.registerNewTeam(ChatColor.RED + "team");
            } catch (IllegalArgumentException ignored) {
                boilingBloodTeam = mainScoreboard.getTeam(ChatColor.RED + "team");
            }
            boilingBloodTeam.setColor(ChatColor.RED);
            boilingBloodTeam.addEntity(player);
            player.setGlowing(true);
        });

        for (int i = 0; i < 40; i++) {
            for (Player target : targets) {
                if (target.getWorld().equals(Dragon.getBattleWorld()) && target.getLocation().distance(Dragon.getDragon().getLocation()) < 300) {
                    Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                        Particles.circle(target.getLocation(), Particle.FALLING_LAVA, 5);
                        target.playSound(target, Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                    }, 5 * i);
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            targets.forEach(target -> {
                if (target.getWorld().equals(Dragon.getBattleWorld()) && target.getLocation().distance(Dragon.getDragon().getLocation()) < 300) {
                    Collection<LivingEntity> nearbyEntities = target.getLocation().getWorld()
                            .getNearbyLivingEntities(target.getLocation(), 5, 5, 5);
                    int damage = 15 * nearbyEntities.size();
                    nearbyEntities.forEach(entity -> {
                        entity.damage(damage, Dragon.getDragon());
                    });
                    target.setGlowing(false);
                    boilingBloodTeam.removeEntity(target);
                }
            });
        }, 200);
    }

    private static void plagueSurface() {
        List<Player> targets = getTargets();

        Dragon.getNearPlayers().forEach(player -> {
            if (!targets.contains(player)) {
                player.sendMessage(Component.text("§eВас отметили §2Чумой порченой крови§e! §aНайдите союзников способных защитить от дебаффа!"));
                player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
            } else {
                player.sendMessage(Component.text("§eВы получили §2Метку иммунитета§e! §aЗащитите других игроков!"));
                player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
                Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                try {
                    plagueSurfaceTeam = mainScoreboard.registerNewTeam(ChatColor.GREEN + "team");
                } catch (IllegalArgumentException ignored) {
                    plagueSurfaceTeam = mainScoreboard.getTeam(ChatColor.GREEN + "team");
                }
                plagueSurfaceTeam.setColor(ChatColor.GREEN);
                plagueSurfaceTeam.addEntity(player);
                player.setGlowing(true);
            }
        });

        for (int i = 0; i < 40; i++) {
            for (Player target : targets) {
                if (target.getWorld().equals(Dragon.getBattleWorld()) && target.getLocation().distance(Dragon.getDragon().getLocation()) < 300) {
                    Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                        Particles.circle(target.getLocation(), Particle.VILLAGER_HAPPY, 7);
                        target.playSound(target, Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                    }, 5 * i);
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            Set<Player> damageTo = Dragon.getNearPlayers();
            targets.forEach(damageTo::remove);
            targets.forEach(target -> {
                if (target.getWorld().equals(Dragon.getBattleWorld()) && target.getLocation().distance(Dragon.getDragon().getLocation()) < 300) {
                    Collection<LivingEntity> nearbyEntities = target.getLocation().getWorld()
                            .getNearbyLivingEntities(target.getLocation(), 7, 7, 7);
                    nearbyEntities.forEach(livingEntity -> {
                        if (livingEntity instanceof Player player) {
                            damageTo.remove(player);
                        }
                    });
                    damageTo.forEach(player -> {
                        player.damage(70);
                        Particles.circle(player.getLocation().clone().add(0, 1, 0), Color.PURPLE, 2);
                    });
                    target.setGlowing(false);
                    plagueSurfaceTeam.removeEntity(target);
                }
            });
        }, 200);
    }

    private static List<Player> getTargets() {
        List<Player> players = new ArrayList<>(Dragon.getNearPlayers().stream().toList());
        List<Player> targets = new ArrayList<>();
        int size = players.size();
        for (int i = 0; i < size / 6 + 1; i++) {
            Player temp = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            players.remove(temp);
            targets.add(temp);
        }
        return targets;
    }
}
