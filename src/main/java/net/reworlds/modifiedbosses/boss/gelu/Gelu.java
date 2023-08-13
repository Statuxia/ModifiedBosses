package net.reworlds.modifiedbosses.boss.gelu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.reworlds.modifiedbosses.Events;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.boss.Boss;
import net.reworlds.modifiedbosses.charms.Charms;
import net.reworlds.modifiedbosses.charms.CharmsEffects;
import net.reworlds.modifiedbosses.items.SpecialItems;
import net.reworlds.modifiedbosses.utils.ComponentUtils;
import net.reworlds.modifiedbosses.utils.TeamUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class Gelu extends Boss {
    private final List<Entity> snowBalls = new ArrayList<>();
    private final Map<Entity, Double> enderCrystals = new HashMap<>();
    private int phase;
    private Team bossTeam;
    private boolean isSecondPhase;
    private boolean isThirdPhase;
    private long lastAbility;
    private BukkitTask shootTask;
    private boolean meleeAttack = false;
    private boolean bossRage = false;
    private boolean abilitiesLocked;
    private boolean lockShoot = false;

    public Gelu(LivingEntity boss, String bossName, Component bossNameComponent) {
        super(boss, bossName, bossNameComponent);
        setMaxHealth(4000);
        setAllowExplodeDamage(false);
        setRadius(200);
        setMaxDamagePerHit(8);
        setMinimumDamageToReward(200);
        setSaveDamageHealth(600);
        setAttributes(Map.of(
                Attribute.GENERIC_MAX_HEALTH, (double) maxHealth,
                Attribute.GENERIC_ATTACK_DAMAGE, (double) 20,
                Attribute.GENERIC_MOVEMENT_SPEED, 0.5
        ));
        setSettings();
        setupBow();
        bar = setBossBar(bossName, BarColor.RED, BarStyle.SEGMENTED_10);
        activate();
    }

    public boolean getLockShoot() {
        return lockShoot;
    }

    public void setLockShoot(boolean b) {
        lockShoot = b;
    }

    public Team getBossTeam() {
        return bossTeam;
    }

    @Override
    protected void setSettings() {
        phase = 0;
        clearDead();
        bossTeam = TeamUtils.getTeam(NamedTextColor.AQUA, bossName + "team");
        bossTeam.addEntity(boss);
        boss.setGlowing(true);
        boss.setCanPickupItems(false);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0));
        boss.setVisualFire(false);
        boss.customName(Component.text(bossName));
        boss.setHealth(maxHealth);
        boss.setAI(true);
        boss.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
        meleeAttack = false;
        boss.setRemoveWhenFarAway(false);
    }

    @Override
    public void removeBoss() {
        if (boss.isDead()) {
            return;
        }
        boss.remove();
        bar.setVisible(false);
        snowBalls.forEach(Entity::remove);
        enderCrystals.forEach((entity, aDouble) -> entity.remove());
        snowBalls.clear();
        enderCrystals.clear();
        deadPlayers.clear();
        deadIPS.clear();
    }

    @Override
    public void remove() {
        stopBattle();
        bar.setVisible(false);
    }

    @Override
    public void stopBattle() {
        super.stopBattle();
        if (boss.isDead()) {
            deadPlayers.clear();
            deadIPS.clear();
            snowBalls.forEach(Entity::remove);
            enderCrystals.forEach((entity, aDouble) -> entity.remove());
            snowBalls.clear();
            enderCrystals.clear();
        }
    }

    @Override
    protected void rewardPlayers() {
        getDamagers().forEach(player -> {
            Double damage = damageByPlayers.get(player.getUniqueId());
            player.giveExp(315);
            net.reworlds.modifiedbosses.advancements.advs.modifiedbosses.Gelu gelu = ModifiedBosses.getAdvancementManager().gelu;
            if (!gelu.isGranted(player)) {
                gelu.grant(player);
            }
            if (damage > minimumDamageToReward) {
                specialReward(player, (int) (damage / 100));
            }
        });
        if (getDamagers().size() == 1 && deadPlayers.size() == 0 && deadIPS.size() == 0) {
            net.reworlds.modifiedbosses.advancements.advs.modifiedbosses.Solo_gelu solo_gelu = ModifiedBosses.getAdvancementManager().solo_gelu;
            Player player = getDamagers().get(0);
            if (!solo_gelu.isGranted(player)) {
                solo_gelu.grant(player);
            }
        }
    }

    private void specialReward(Player player, int percent) {
        percent = Math.min(percent, 20);
        player.giveExp(1080 * 2);
        int applechance = ThreadLocalRandom.current().nextInt(100);
        if (applechance < 50 + percent) {
            giveOrDrop(player, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, ThreadLocalRandom.current().nextInt(3, 6)));
        }

        int charmchance = ThreadLocalRandom.current().nextInt(100);
        if (charmchance < 30 + percent) {
            ItemStack item;
            if (ThreadLocalRandom.current().nextInt(100) < 40) {
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
        int yamatoChance = ThreadLocalRandom.current().nextInt(100);
        if (yamatoChance < 3) {
            giveOrDrop(player, SpecialItems.yamato);
            Component itemDisplayName = SpecialItems.yamato.getItemMeta().displayName();
            if (itemDisplayName == null) {
                return;
            }
            Component text = Component.text("§e" + player.getName() + " выбивает ").append(itemDisplayName).append(Component.text("§e!"));
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(text));
        }

        int ancientSHBow = ThreadLocalRandom.current().nextInt(100);
        if (ancientSHBow < 3) {
            giveOrDrop(player, SpecialItems.ancientSHBow);
            Component itemDisplayName = SpecialItems.ancientSHBow.getItemMeta().displayName();
            if (itemDisplayName == null) {
                return;
            }
            Component text = Component.text("§e" + player.getName() + " выбивает ").append(itemDisplayName).append(Component.text("§e!"));
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(text));
        }

        int ankhShield = ThreadLocalRandom.current().nextInt(100);
        if (ankhShield < 3) {
            giveOrDrop(player, SpecialItems.ankhShield);
            Component itemDisplayName = SpecialItems.ankhShield.getItemMeta().displayName();
            if (itemDisplayName == null) {
                return;
            }
            Component text = Component.text("§e" + player.getName() + " выбивает ").append(itemDisplayName).append(Component.text("§e!"));
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.sendMessage(text));
        }
    }

    @Override
    protected void loopTask() {
        if (boss.getLocation().getY() < 72 || boss.getLocation().distance(spawnLocation) > radius * 0.7) {
            boss.teleport(spawnLocation.clone());
        }

        if (boss.getHealth() > maxHealth * 0.95 && !boss.isDead()) {
            snowBalls.forEach(Entity::remove);
            enderCrystals.forEach((entity, aDouble) -> entity.remove());
            snowBalls.clear();
            enderCrystals.clear();
            deadPlayers.clear();
            deadIPS.clear();
            Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                spawnLocation.getWorld().getNearbyEntities(spawnLocation, radius, radius, radius).forEach(entity -> {
                    if (entity instanceof Snowball) {
                        entity.remove();
                    }
                });
            }, 5);
        }

        ItemStack itemInMainHand = boss.getEquipment().getItemInMainHand();
        if (ThreadLocalRandom.current().nextInt(0, 10) == 0 && !lockShoot) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                meleeAttack = true;
                if (itemInMainHand.getType() != Material.DIAMOND_SWORD) {
                    boss.getWorld().playSound(boss, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 10, 1);
                    boss.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
                }
            } else {
                meleeAttack = false;
                if (itemInMainHand.getType() != Material.BOW) {
                    boss.getWorld().playSound(boss, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 10, 1);
                    boss.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
                }
            }
        }

        if (boss.getHealth() <= maxHealth * 0.9) {
            phase = 2;
            if (!isSecondPhase) {
                getDamagers().forEach(player -> player.sendMessage(Component.newline().append(bossNameComponent).append(ComponentUtils.gradient("#A3E1EE", "#EC0D46", " перешёл на 2 фазу!")).append(Component.newline())));
            }
            isSecondPhase = true;
        }

        if (boss.getHealth() <= maxHealth * 0.5) {
            phase = 3;
            if (!isThirdPhase) {
                getDamagers().forEach(player -> player.sendMessage(Component.newline().append(bossNameComponent).append(ComponentUtils.gradient("#A3E1EE", "#EC0D46", " перешёл на 3 фазу!")).append(Component.newline())));
            }
            isThirdPhase = true;
        }

        if (!boss.isDead() && phase != 0) {
            lastAbility = new GeluAbilities(this).activate();
        }
    }

    private double distance(Player player) {
        return boss.getLocation().distance(player.getLocation());
    }

    @Override
    protected boolean isBlackListEffect(PotionEffect effect, Player player) {
        if (effect == null) {
            return false;
        }
        try {
            net.reworlds.modifiedbosses.advancements.advs.modifiedbosses.Solo_gelu solo_gelu = ModifiedBosses.getAdvancementManager().solo_gelu;
            if (solo_gelu.isGranted(player)) {
                return false;
            }
        } catch (Exception ignored) {
        }

        return CharmsEffects.getPositiveEffects().contains(effect.getType());
    }

    @Override
    protected void invulnerableBossDamage() {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerEffect(EntityPotionEffectEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof Player player && isNear(player) && !boss.isDead() && isBlackListEffect(event.getNewEffect(), player) && getDamagers().contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSnowBallTouch(PlayerMoveEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        Player player = event.getPlayer();
        Location to = event.getTo();

        if (!getDamagers().contains(player)) {
            return;
        }

        List<Entity> delete = new ArrayList<>();
        snowBalls.forEach(snowBall -> {
            Location location = snowBall.getLocation();
            try {
                if (location.distance(to) < 1) {
                    delete.add(snowBall);
                    location.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, location, 1);
                    location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 2, 2);
                    player.setVelocity(Vector.getRandom().multiply(2.3));
                    if (!player.isDead() && boss.getWorld().equals(player.getWorld())) {
                        damagePlayer(player, 6);
                    }
                }
            } catch (Exception ignored) {
            }
        });
        snowBalls.removeIf(delete::contains);
        delete.forEach(Entity::remove);
    }

    @EventHandler
    public void onEnderCrystalDamage(EntityDamageByBlockEvent event) {
        if (event.getEntity() instanceof EnderCrystal && enderCrystals.containsKey(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRodUse(PlayerInteractEvent event) {
        Location location = event.getPlayer().getLocation();
        if (!(Events.inZone(location.getX(), Events.minX, Events.maxX)
                && Events.inZone(location.getZ(), Events.minZ, Events.maxZ))) {
            return;
        }

        if (boss == null || boss.isDead()) {
            return;
        }

        if (event.getAction().isRightClick() && event.getItem() != null && event.getItem().getType() == Material.FISHING_ROD) {
            event.getPlayer().sendMessage("§сВы не можете использовать удочку на территории босса!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEnderCrystalExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (enderCrystals.containsKey(entity)) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onCrystalRemove(EntityDamageByEntityEvent event) {
        if (enderCrystals.containsKey(event.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBossDamagePlayer(EntityDamageByEntityEvent event) {
        if (!event.getDamager().equals(boss) && !(event.getDamager() instanceof Projectile projectile && projectile.getShooter() != null && projectile.getShooter().equals(boss))) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getDamager() instanceof Arrow && !getDamagers().contains(player)) {
            event.setCancelled(true);
            player.setVelocity(Vector.getRandom().multiply(5));
        }

        ItemStack[] armorContents = player.getEquipment().getArmorContents();
        for (ItemStack armorContent : armorContents) {
            if (armorContent == null) {
                continue;
            }
            short durability = armorContent.getDurability();
            if (durability - 5 <= 0) {
                durability = 0;
            } else {
                durability -= 5;
            }
            armorContent.setDurability(durability);
        }
        player.getEquipment().setArmorContents(armorContents);

    }

    @EventHandler
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Location location = event.getEntity().getLocation();
        if (!location.getWorld().equals(spawnLocation.getWorld())) {
            return;
        }
        if (!(Events.inZone(location.getX(), Events.minX, Events.maxX)
                && Events.inZone(location.getZ(), Events.minZ, Events.maxZ))) {
            return;
        }
        if (!(event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player) && !(event.getDamager() instanceof Player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onEnderCrystalDamage(EntityDamageByEntityEvent event) {
        Entity crystal = event.getEntity();
        if (!(crystal instanceof EnderCrystal && enderCrystals.containsKey(crystal))) {
            return;
        }

        if (!(event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player && getDamagers().contains(player)) && !(event.getDamager() instanceof Player player1 && getDamagers().contains(player1))) {
            event.setCancelled(true);
            return;
        }

        double totalDamage = enderCrystals.get(crystal) + event.getDamage();
        if (totalDamage >= 20) {
            enderCrystals.remove(crystal);
            return;
        }

        enderCrystals.put(crystal, totalDamage);
        event.setCancelled(true);
    }

    public void setupBow() {
        shootTask = Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
            if (boss != null) {
                boss.getChunk().load();
            }
            if ((boss == null || boss.isDead())) {
                if (shootTask != null) {
                    shootTask.cancel();
                }
                return;
            }

            Location location = boss.getLocation();
            if (!(Events.inZone(location.getX(), Events.minX, Events.maxX)
                    && Events.inZone(location.getZ(), Events.minZ, Events.maxZ))) {
                boss.teleport(spawnLocation);
            }
            getDamagers().forEach(player -> {
                Location location1 = player.getLocation();
                if (!location1.getWorld().equals(spawnLocation.getWorld())) {
                    return;
                }
                if ((Events.inZone(location1.getX(), Events.minX + 10, Events.maxX - 21)
                        && Events.inZone(location1.getZ(), Events.minZ + 10, Events.maxZ - 12))) {
                    return;
                }
                player.damage(4);
            });

            AtomicReference<Player> nearbyPlayer = new AtomicReference<>(null);
            List<Player> nearPlayers = new ArrayList<>();
            boss.getNearbyEntities(3, 3, 3).forEach(entity -> {
                if (entity instanceof Player player) {
                    nearPlayers.add(player);
                }
            });
            if (nearPlayers.size() > 4) {
                nearPlayers.forEach(player -> {
                    if (!player.getWorld().equals(spawnLocation.getWorld())) {
                        return;
                    }
                    player.setVelocity(player.getLocation().subtract(location).toVector().multiply(0.5 + (bossRage ? 0.5 : 0)));
                });
            }
            boss.getNearbyEntities(radius, radius, radius).forEach(entity -> {
                if (entity.getType() == boss.getType()) {
                    return;
                }
                if (!(entity instanceof Player player)) {
                    if (!(entity instanceof Item) && !(entity instanceof Projectile) && !(entity instanceof ArmorStand) && !(entity instanceof EnderCrystal)) {
                        if (Events.inZone(location.getX(), Events.minX - 5, Events.maxX + 5) && Events.inZone(location.getZ(), Events.minZ - 5, Events.maxZ + 5)) {
                            entity.remove();
                        }
                    }
                    return;
                }

                if (!getDamagers().contains(player)) {
                    return;
                }

                if (player.isDead()) {
                    return;
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                player.setGliding(false);
                if ((nearbyPlayer.get() == null || distance(player) < distance(nearbyPlayer.get())) && !player.isDead()
                        && player.getGameMode() == GameMode.SURVIVAL && !lockTarget) {
                    nearbyPlayer.set(player);
                }
            });
            if (boss instanceof Mob mob && nearbyPlayer.get() != null) {
                if (!lockTarget) {
                    mob.setTarget(nearbyPlayer.get());
                }
            }

            if (boss.hasAI() && boss instanceof Mob mob) {
                LivingEntity target = mob.getTarget();
                if (target == null || target.isDead() || !isNear(target)) {
                    mob.setTarget(null);
                    return;
                }
                if (meleeAttack) {
                    return;
                }
                if (target instanceof Player player && player.getGameMode() == GameMode.SURVIVAL && target.getWorld().equals(spawnLocation.getWorld())) {
                    Arrow arrow = boss.launchProjectile(Arrow.class, player.getLocation().subtract(location).toVector().multiply(0.9 + (bossRage ? 0.9 : 0)));
                    arrow.setShooter(boss);
                } else {
                    if (lockTarget && !target.getWorld().equals(spawnLocation.getWorld())) {
                        lockTarget = false;
                    }
                    if (!lockTarget) {
                        mob.setTarget(null);
                    }
                }
            }
        }, 0, 4);
    }

    @EventHandler
    public void onGliding(EntityToggleGlideEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        if (event.getEntity() instanceof Player player) {
            if (isNear(player) && (getDamagers().contains(player) || containsDamagers(player))) {
                if (event.isGliding()) {
                    player.setGliding(false);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onTarget(EntityShootBowEvent event) {
        if (!event.getEntity().equals(boss)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (boss == null || boss.isDead()) {
            return;
        }

        Location location = event.getEntity().getLocation();
        if (!location.getWorld().getName().equals("world_the_end")) {
            return;
        }

        if (Events.inZone(location.getX(), Events.minX, Events.maxX)
                && Events.inZone(location.getZ(), Events.minZ, Events.maxZ)) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }

        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        if (getDamagers().contains(player)) {
            deadPlayers.add(player.getUniqueId());
            if (player.getAddress() != null) {
                deadIPS.add(player.getAddress().getHostString());
            }
        }

        if (boss.getHealth() > saveDamageHealth) {
            if (getDamagers().contains(player)) {
                player.sendMessage("§cУ босса более " + saveDamageHealth + " ХП. Урон сброшен.");
            }
            removeDamager(player);
        }
    }

    public long getLastAbility() {
        return lastAbility;
    }

    public int getPhase() {
        return phase;
    }

    public boolean isBossRage() {
        return bossRage;
    }

    public void setBossRage(boolean bossRage) {
        this.bossRage = bossRage;
    }

    public boolean isAbilitiesLocked() {
        return abilitiesLocked;
    }

    public void setAbilitiesLocked(boolean abilitiesLocked) {
        this.abilitiesLocked = abilitiesLocked;
    }

    public List<Entity> getSnowBalls() {
        return snowBalls;
    }

    public Map<Entity, Double> getEnderCrystals() {
        return enderCrystals;
    }
}
