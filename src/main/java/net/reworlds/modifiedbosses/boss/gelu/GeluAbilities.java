package net.reworlds.modifiedbosses.boss.gelu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.bossbars.Timer;
import net.reworlds.modifiedbosses.utils.ComponentUtils;
import net.reworlds.modifiedbosses.utils.Particles;
import net.reworlds.modifiedbosses.utils.Random;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class GeluAbilities {

    private final Gelu gelu;

    public GeluAbilities(Gelu gelu) {
        this.gelu = gelu;
    }

    public long activate() {
        long lastAbility = gelu.getLastAbility();
        int phase = gelu.getPhase();
        if (gelu.getDamagers().isEmpty()) {
            return lastAbility;
        }
        if (lastAbility + (1000L * ThreadLocalRandom.current().nextInt(22, 26)) > System.currentTimeMillis()) {
            return lastAbility;
        }
        if (phase < 2) {
            return lastAbility;
        }
        if (gelu.isAbilitiesLocked()) {
            return System.currentTimeMillis();
        }

        int ability = ThreadLocalRandom.current().nextInt(phase == 2 ? 4 : 7);
        if (ability == 0 && gelu.isBossRage()) {
            return lastAbility;
        }
        switch (ability) {
            case 0 -> analyze();
            case 1 -> godStep();
            case 2 -> iceMine();
            case 3 -> energyCrystals();
            case 4 -> helpCall();
            case 5, 6 -> endOfDestiny();
        }

        return System.currentTimeMillis();
    }

    private void analyze() {
        List<Player> damagers = gelu.getDamagers();
        damagers.forEach(player -> {
            Component message1 = ComponentUtils.gradient("#FF1C72", "#FEB781", "⚠ Анализ ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Отбегите от босса!");
            player.showTitle(Title.title(message1, message2));
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
            Timer.of(player, 6, "§6Анализ", BarColor.YELLOW);
            player.playSound(player, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 2);
        });
        LivingEntity boss = gelu.getBoss();
        boss.setAI(false);

        for (int i = 0; i < 20; i++) {
            Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                Particles.circle(boss.getLocation(), Color.PURPLE, 32, true);
                boss.getWorld().playSound(boss.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
            }, 6 * i);
        }
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            boss.getNearbyEntities(32, 32, 32).forEach(entity -> {
                if (entity instanceof Player player && damagers.contains(player) && gelu.calculateDistance(boss.getLocation(), player.getLocation()) <= 32) {
                    atomicBoolean.set(true);
                }
            });
            if (atomicBoolean.get()) {
                gelu.getDamagers().forEach(player -> {
                    Timer.of(player, 60, "§5Усиление", BarColor.PURPLE);
                    player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 2);
                });
                gelu.getBossTeam().setColor(ChatColor.DARK_PURPLE);
                gelu.setBossRage(true);
                AttributeInstance attribute = boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                if (attribute != null) {
                    attribute.setBaseValue(30);
                }
                Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                    if (attribute != null) {
                        attribute.setBaseValue(20);
                    }
                    gelu.getBossTeam().setColor(ChatColor.AQUA);
                    gelu.setBossRage(false);
                }, 20 * 60);
            }
            boss.setAI(true);
        }, 20 * 6);
    }

    private void godStep() {
        List<Player> damagers = gelu.getDamagers();
        damagers.forEach(player -> {
            Component message1 = ComponentUtils.gradient("#8E93FB", "#4E54C8", "⚠ Шаг Бога ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Остерегайтесь подозрительных порталов...");
            player.showTitle(Title.title(message1, message2));
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);

        });

        gelu.setLockShoot(true);
        LivingEntity boss = gelu.getBoss();
        boss.setAI(false);
        ItemStack item = gelu.getBoss().getEquipment().getItemInMainHand();
        gelu.getBoss().getEquipment().setItemInMainHand(null);
        gelu.getBoss().setGlowing(false);
        boss.setInvulnerable(true);
        boss.getWorld().spawnParticle(Particle.SONIC_BOOM, boss.getLocation().clone().add(0, 1, 0), 1);
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            boss.setInvisible(true);
        }, 10);
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            if (damagers.size() == 0) {
                teleportBoss(boss, item);
                return;
            }
            Player target = null;
            List<Player> check = new ArrayList<>(damagers);
            while (check.size() != 0 && target == null) {
                Player checkPlayer = check.get(ThreadLocalRandom.current().nextInt(check.size()));
                if (checkPlayer.getWorld().equals(gelu.getBoss().getWorld())) {
                    target = checkPlayer;
                }
                check.remove(checkPlayer);
            }
            if (target == null) {
                boss.setAI(true);
                gelu.getBoss().setGlowing(true);
                boss.setInvulnerable(false);
                boss.setInvisible(false);
                gelu.setLockShoot(false);
                gelu.getBoss().getEquipment().setItemInMainHand(item);
                return;
            }
            Location location = Random.randomFlatY(target.getLocation(), false, 10);
            Location highest = location.getWorld().getHighestBlockAt(location).getLocation().add(0.5, 1, 0.5);
            boss.teleport(highest);
            teleportBoss(boss, item);

        }, 20 * 5);
    }

    private void teleportBoss(LivingEntity boss, ItemStack itemStack) {
        boss.getWorld().spawnParticle(Particle.SONIC_BOOM, boss.getLocation().clone().add(0, 1, 0), 1);
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            boss.setAI(true);
            gelu.getBoss().setGlowing(true);
            boss.setInvulnerable(false);
            boss.setInvisible(false);
            gelu.setLockShoot(false);
            gelu.getBoss().getEquipment().setItemInMainHand(itemStack);
        }, 10);
    }

    private void iceMine() {
        List<Player> damagers = gelu.getDamagers();
        damagers.forEach(player -> {
            Component message1 = ComponentUtils.gradient("#56CBF2", "#2D7AE2", "⚠ Ледяные мины ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Смотри под ноги!");
            player.showTitle(Title.title(message1, message2));
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
        });

        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            damagers.forEach(player -> {
                if (!player.getLocation().getWorld().equals(gelu.getBoss().getLocation().getWorld())) {
                    return;
                }
                for (int i = 0; i < 8; i++) {
                    Location random;
                    do {
                        random = Random.randomFlatY(player.getLocation(), false, 20);
                    } while (random.distance(player.getLocation()) < 2);
                    int highestBlockYAt = random.getWorld().getHighestBlockYAt(random);
                    random.set(random.getX(), highestBlockYAt + 1.5, random.getZ());
                    if (gelu.getBoss().isDead()) {
                        return;
                    }
                    Entity snowball = random.getWorld().spawnEntity(random, EntityType.SNOWBALL);
                    snowball.setGravity(false);
                    snowball.setInvulnerable(true);
                    snowball.getWorld().playSound(snowball, Sound.ENTITY_ALLAY_HURT, 1f, 1.5f);
                    gelu.getBossTeam().addEntity(snowball);
                    snowball.setGlowing(true);
                    gelu.getSnowBalls().add(snowball);
                }

            });
        }, 20 * 5);
    }

    private void energyCrystals() {
        List<Player> damagers = gelu.getDamagers();
        damagers.forEach(player -> {
            Component message1 = ComponentUtils.gradient("#5B52BE", "#2F2B61", "⚠ Энергетические Кристаллы ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Сломайте их все!");
            player.showTitle(Title.title(message1, message2));
            Timer.of(player, 20, "§bЭнергетические Кристаллы", BarColor.BLUE);
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
        });

        World world = gelu.getBoss().getWorld();
        List<Location> crystalLocations = List.of(
                new Location(world, 950, 89, 396),
                new Location(world, 953, 89, 360),
                new Location(world, 999, 89, 342),
                new Location(world, 980, 89, 386),
                new Location(world, 1030, 89, 368)
        );
//        List<Location> crystalLocations = List.of(
//                new Location(world, -30, 74, -149),
//                new Location(world, -27, 74, -185),
//                new Location(world, 19, 74, -203),
//                new Location(world, 50, 74, -177),
//                new Location(world, 0, 74, -159)
//        );

        crystalLocations.forEach(location -> {
            Entity entity = location.getWorld().spawnEntity(location, EntityType.ENDER_CRYSTAL);
            entity.customName(Component.text("§bЭнергетический кристалл"));
            gelu.getEnderCrystals().put(entity, 0D);
            gelu.getBossTeam().addEntity(entity);
            entity.setGlowing(true);
            Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {

                AtomicBoolean atomicBoolean = new AtomicBoolean(false);
                gelu.getEnderCrystals().forEach((crystal, damage) -> {
                    if (!crystal.isDead()) {
                        atomicBoolean.set(true);
                    }
                });
                if (atomicBoolean.get()) {
                    gelu.getDamagers().forEach(player -> {
                        if (!player.isDead() && gelu.getBoss().getWorld().equals(player.getWorld())) {
                            gelu.damagePlayer(player, 200);
                        }
                    });
                }
                gelu.getEnderCrystals().forEach((crystal, damage) -> crystal.remove());
                gelu.getEnderCrystals().clear();
            }, 20 * 20);
        });
    }

    private void helpCall() {
        List<Player> damagers = gelu.getDamagers();
        damagers.forEach(player -> {
            Component message1 = ComponentUtils.gradient("#FA5C7F", "#6F81F7", "⚠ Призыв помощи ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Приближается подмога босса!");
            player.showTitle(Title.title(message1, message2));
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);
        });

        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            LivingEntity boss = gelu.getBoss();
            Location location = boss.getLocation();
            for (int i = 0; i < damagers.size() + 1; i++) {
                Location location1 = Random.randomFlatY(location, false, 20);
                Location top = location1.getWorld().getHighestBlockAt(location1).getLocation().add(0.5, 1, 0.5);
                LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(top, EntityType.WITHER_SKELETON);
                setAttributes(entity, Map.of(
                        Attribute.GENERIC_MAX_HEALTH, (double) 50,
                        Attribute.GENERIC_ATTACK_DAMAGE, (double) 10,
                        Attribute.GENERIC_MOVEMENT_SPEED, 0.5
                ));
                entity.setCanPickupItems(false);
                entity.setGlowing(true);
                gelu.getBossTeam().addEntity(entity);
            }
        }, 20 * 5);
    }

    protected void setAttributes(@NotNull LivingEntity entity, @NotNull Map<@NotNull Attribute, @NotNull Double> attributes) {
        attributes.forEach((attribute, value) -> {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(value);
            }
        });
    }

    private void endOfDestiny() {
        List<Player> damagers = gelu.getDamagers();
        damagers.forEach(player -> {
            Component message1 = ComponentUtils.gradient("#E85656", "#541F1F", "⚠ Конец Судьбы ⚠");
            Component message2 = ComponentUtils.gradient("#F5A7CA", "#7DF1DC", "Удачи");
            player.showTitle(Title.title(message1, message2));
            player.sendMessage(Component.newline().append(message1).append(Component.newline()).append(message2).append(Component.newline()));
            player.playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1);

        });

        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            damagers.forEach(player -> {
                Timer.of(player, 15, "§6Конец судьбы", BarColor.YELLOW);
            });
            gelu.setAbilitiesLocked(true);
            gelu.setLockShoot(true);
            LivingEntity boss = gelu.getBoss();
            boss.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
                damagers.forEach(target -> {
                    if (target.getGameMode() == GameMode.SURVIVAL) {
                        try {
                            if (boss.isDead()) {
                                return;
                            }
                            Arrow arrow = boss.launchProjectile(Arrow.class, target.getLocation().subtract(boss.getLocation()).toVector().multiply(0.5 + (gelu.isBossRage() ? 0.5 : 0)));
                            arrow.setShooter(boss);
                        } catch (Exception ignored) {
                        }
                    }
                });
            }, 0, 4);
            Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                gelu.setLockShoot(false);
                gelu.setAbilitiesLocked(false);
                gelu.getBoss().getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
                task.cancel();
            }, 20 * 15);
        }, 20 * 5);

    }
}
