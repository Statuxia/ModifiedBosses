package net.reworlds.modifiedbosses.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.boss.Boss;
import net.reworlds.modifiedbosses.bossbars.Timer;
import net.reworlds.modifiedbosses.respawn.Bosses;
import net.reworlds.modifiedbosses.utils.Random;
import net.reworlds.modifiedbosses.utils.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Material.*;

public class SpecialItems implements Listener {

    public static final List<UUID> lockedYamatoTo = new ArrayList<>();
    public static final List<UUID> lockedShieldTo = new ArrayList<>();
    public static final String[] ankhShieldLore = new String[]{"Защищает от большинства дебаффов", "Удваивает время неуязвимости после урона", "По нажатию ПКМ + Shift дает эффект Сопротивления и агрит монстров"};
    public static final ItemStack ankhShield = createAnkhShield();
    private static final List<Material> clickable = List.of(
            TRAPPED_CHEST, HOPPER, DROPPER, DISPENSER, DAYLIGHT_DETECTOR, LEVER, COMPARATOR, REPEATER,
            END_PORTAL_FRAME, DRAGON_EGG, RESPAWN_ANCHOR, ENDER_CHEST, BARREL, CHEST, WARPED_HANGING_SIGN,
            CRIMSON_HANGING_SIGN, BAMBOO_HANGING_SIGN, CHERRY_HANGING_SIGN, MANGROVE_HANGING_SIGN,
            DARK_OAK_HANGING_SIGN, ACACIA_HANGING_SIGN, JUNGLE_HANGING_SIGN, BIRCH_HANGING_SIGN, SPRUCE_HANGING_SIGN,
            OAK_HANGING_SIGN, WARPED_SIGN, CRIMSON_SIGN, BAMBOO_SIGN, CHERRY_SIGN, MANGROVE_SIGN, DARK_OAK_SIGN,
            ACACIA_SIGN, JUNGLE_SIGN, BIRCH_SIGN, SPRUCE_SIGN, OAK_SIGN, LECTERN, CHISELED_BOOKSHELF,
            GLOW_ITEM_FRAME, ITEM_FRAME, ARMOR_STAND, FLOWER_POT, BEEHIVE, LODESTONE, BEACON, BELL, CAULDRON,
            BREWING_STAND, ENCHANTING_TABLE, JUKEBOX, NOTE_BLOCK, COMPOSTER, DAMAGED_ANVIL, CHIPPED_ANVIL, ANVIL,
            SOUL_CAMPFIRE, CAMPFIRE, BLAST_FURNACE, SMOKER, FURNACE, LOOM, GRINDSTONE, SMITHING_TABLE, CARTOGRAPHY_TABLE,
            STONECUTTER, CRAFTING_TABLE, BEE_NEST, PUMPKIN, REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE, RED_BED, ORANGE_BED,
            YELLOW_BED, LIME_BED, GREEN_BED, LIGHT_BLUE_BED, CYAN_BED, BLUE_BED, PURPLE_BED, MAGENTA_BED,
            PINK_BED, BROWN_BED, WHITE_BED, LIGHT_GRAY_BED, GRAY_BED, BLACK_BED, SHULKER_BOX, WHITE_SHULKER_BOX,
            ORANGE_SHULKER_BOX, MAGENTA_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX, YELLOW_SHULKER_BOX, LIME_SHULKER_BOX,
            PINK_SHULKER_BOX, GRAY_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX, CYAN_SHULKER_BOX, PURPLE_SHULKER_BOX,
            BLUE_SHULKER_BOX, BROWN_SHULKER_BOX, GREEN_SHULKER_BOX, RED_SHULKER_BOX, BLACK_SHULKER_BOX, CANDLE,
            WHITE_CANDLE, ORANGE_CANDLE, MAGENTA_CANDLE, LIGHT_BLUE_CANDLE, YELLOW_CANDLE, LIME_CANDLE, PINK_CANDLE,
            GRAY_CANDLE, LIGHT_GRAY_CANDLE, CYAN_CANDLE, PURPLE_CANDLE, BLUE_CANDLE, BROWN_CANDLE, GREEN_CANDLE, RED_CANDLE,
            BLACK_CANDLE, STONE_BUTTON, OAK_BUTTON, SPRUCE_BUTTON, BIRCH_BUTTON, JUNGLE_BUTTON, ACACIA_BUTTON, DARK_OAK_BUTTON,
            CRIMSON_BUTTON, WARPED_BUTTON, POLISHED_BLACKSTONE_BUTTON, CHERRY_BUTTON, MANGROVE_BUTTON, BAMBOO_BUTTON,
            OAK_TRAPDOOR, SPRUCE_TRAPDOOR, BIRCH_TRAPDOOR, JUNGLE_TRAPDOOR, ACACIA_TRAPDOOR, DARK_OAK_TRAPDOOR, MANGROVE_TRAPDOOR,
            BAMBOO_TRAPDOOR, CHERRY_TRAPDOOR, CRIMSON_TRAPDOOR, WARPED_TRAPDOOR, OAK_DOOR, SPRUCE_DOOR, BIRCH_DOOR, JUNGLE_DOOR,
            ACACIA_DOOR, DARK_OAK_DOOR, CRIMSON_DOOR, WARPED_DOOR, MANGROVE_DOOR, CHERRY_DOOR, BAMBOO_DOOR, OAK_FENCE_GATE,
            SPRUCE_FENCE_GATE, BIRCH_FENCE_GATE, JUNGLE_FENCE_GATE, ACACIA_FENCE_GATE, DARK_OAK_FENCE_GATE, CRIMSON_FENCE_GATE,
            WARPED_FENCE_GATE, MANGROVE_DOOR, CHERRY_DOOR, BAMBOO_DOOR
    );
    private static final String[] bowLore = new String[]{"Стреляет быстрее турели", "Это вообще законно?"};
    public static final ItemStack ancientSHBow = createAncientSHBow();
    private static final String[] yamatoLore = new String[]{"I am the storm that is approaching"};
    public static final ItemStack yamato = createYamato();
    private static final Team monsterteam = TeamUtils.getTeam(NamedTextColor.DARK_RED, "monsterteam");

    static {
        monsterteam.setColor(ChatColor.DARK_RED);
    }

    private static ItemStack createAncientSHBow() {
        ItemStack itemStack = new ItemStack(BOW);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(ComponentUtils.gradient("#EC0D46", "#A3E1EE", "Древний Лук Сумеречного Охотника"));
        List<Component> lore = List.of(
                Component.text("§3§o" + bowLore[0]),
                Component.text("§3§o" + bowLore[1])
        );
        itemMeta.addEnchant(Enchantment.MENDING, 1, false);
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack createYamato() {
        ItemStack itemStack = new ItemStack(DIAMOND_SWORD);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(ComponentUtils.gradient("#EC0D46", "#A3E1EE", "Ямато"));
        List<Component> lore = List.of(
                Component.text("§3§o" + yamatoLore[0])
        );
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemStack createAnkhShield() {
        ItemStack itemStack = new ItemStack(SHIELD);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.displayName(ComponentUtils.gradient("#EC0D46", "#A3E1EE", "Щит Анкх"));
        List<Component> lore = List.of(
                Component.text("§3§o" + ankhShieldLore[0]),
                Component.text("§3§o" + ankhShieldLore[1]),
                Component.text("§3§o" + ankhShieldLore[2])
        );
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static boolean isSpecialItem(ItemStack item, String checkLore) {
        return !(item == null || item.lore() == null || item.lore().size() == 0 || !ComponentUtils.plainText(item.lore().get(0)).contains(checkLore));
    }

    @EventHandler
    public void onYamatoUse(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (!isRightClick(action)) {
            return;
        }

        ItemStack yamatoItem = event.getItem();
        if (!isSpecialItem(yamatoItem, yamatoLore[0])) {
            return;
        }

        Player player = event.getPlayer();
        if (lockedYamatoTo.contains(player.getUniqueId())) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && isClickable(clickedBlock)) {
            return;
        }

        if (!AdvancementUtils.isCompletedCategory(player, AdvancementUtils.Category.NETHER)) {
            player.sendMessage("§cВыполните все достижения из категории Незер для открытия всех возможностей этого предмета!");
            return;
        }

        lockedYamatoTo.add(player.getUniqueId());
        player.playSound(player, Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1.7F);
        player.setCollidable(false);
        player.setInvulnerable(true);
        List<Location> locations = new ArrayList<>();

        double radius = 1.5;
        if (player.isSneaking()) {
            radius *= 2;
        } else {
            player.setVelocity(player.getLocation().getDirection().multiply(2));
        }

        double finalRadius = radius;
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
            if (!player.isOnline()) {
                return;
            }
            locations.add(player.getLocation());
            locations.forEach(location -> {
                Location random = Random.random(location, false, 3);
                World world = random.getWorld();
                Particle currentParticle = ThreadLocalRandom.current().nextBoolean() ? Particle.SCRAPE : Particle.WAX_OFF;
                Particles.particleLine(location, finalRadius, currentParticle, true);
                world.playSound(random, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 0.5F, 1.5F);
            });
        }, 0, 1);
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            bukkitTask.cancel();
            locations.forEach(location -> {
                location.getNearbyEntities(finalRadius * 2, finalRadius * 2, finalRadius * 2).forEach(entity -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        if (livingEntity.equals(player)) {
                            return;
                        }
                        livingEntity.damage(20 * (player.isSneaking() ? 2 : 1), player);
                        livingEntity.setNoDamageTicks(0);
                    }
                });
            });
            Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                player.setCollidable(true);
                player.setInvulnerable(false);
            }, 1);
        }, 20);
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            Timer.of(player, 14, ComponentUtils.plainText(yamatoItem.displayName()), BarColor.WHITE);
        }, 20);
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            lockedYamatoTo.remove(player.getUniqueId());
            player.playSound(player, Sound.ITEM_TRIDENT_RETURN, 1, 1);
        }, 20 * 15);
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (lockedYamatoTo.contains(player.getUniqueId())) {
            player.setCollidable(true);
            player.setInvulnerable(false);
        }
    }

    @EventHandler
    public void onBowUse(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (!isRightClick(action)) {
            return;
        }

        ItemStack bowItem = event.getItem();
        if (!isSpecialItem(bowItem, bowLore[0])) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && isClickable(clickedBlock)) {
            return;
        }

        bowItem.getEnchantments().forEach((enchantment, integer) -> {
            if (!enchantment.equals(Enchantment.MENDING)) {
                bowItem.removeEnchantment(enchantment);
            }
        });

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();

        if (!AdvancementUtils.isCompletedCategory(player, AdvancementUtils.Category.NETHER)) {
            player.sendMessage("§cВыполните все достижения из категории Незер для открытия всех возможностей этого предмета!");
            return;
        }

        int first = inventory.first(ARROW);
        if (first == -1) {
            event.setCancelled(true);
            return;
        }

        ItemStack arrowItem = player.getInventory().getItem(first);
        if (arrowItem != null) {
            arrowItem.setAmount(arrowItem.getAmount() - 1);
        }

        event.getItem().damage(1, player);

        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setShooter(player);
        arrow.setHitSound(Sound.ENTITY_CAT_AMBIENT);
        arrow.setVelocity(arrow.getVelocity().multiply(1.3).multiply(1));
        event.setCancelled(true);
    }

    @EventHandler
    public void onBowDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) {
            return;
        }

        ProjectileSource shooter = arrow.getShooter();
        if (!(shooter instanceof Player player)) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isSpecialItem(item, bowLore[0])) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        entity.setNoDamageTicks(0);
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            entity.setNoDamageTicks(0);
        }, 1L);
    }

    @EventHandler
    public void onShieldUse(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        if (player.isBlocking()) {
            return;
        }

        if (!AdvancementUtils.isCompletedCategory(player, AdvancementUtils.Category.NETHER)) {
            return;
        }

        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (isSpecialItem(itemInOffHand, ankhShieldLore[0]) || isSpecialItem(itemInMainHand, ankhShieldLore[0])) {
            Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                player.setNoDamageTicks(player.getNoDamageTicks() * 2);
            }, 1);
        }
    }

//    @EventHandler
//    public void onJoin(PlayerJoinEvent event) {
//        if (event.getPlayer().getName().equals("Statux1a")) {
//            event.getPlayer().getInventory().addItem(yamato);
//            event.getPlayer().getInventory().addItem(ancientSHBow);
//            event.getPlayer().getInventory().addItem(ankhShield);
//        }
//    }

    @EventHandler
    public void onShieldUse(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (!isRightClick(action)) {
            return;
        }

        ItemStack shieldItem = event.getItem();
        if (!isSpecialItem(shieldItem, ankhShieldLore[0])) {
            return;
        }

        Player player = event.getPlayer();

        if (lockedShieldTo.contains(player.getUniqueId())) {
            return;
        }

        if (!player.isSneaking()) {
            return;
        }

        if (!AdvancementUtils.isCompletedCategory(player, AdvancementUtils.Category.NETHER)) {
            player.sendMessage("§cВыполните все достижения из категории Незер для открытия всех возможностей этого предмета!");
            return;
        }

        Map<Monster, Boolean> entities = new HashMap<>();
        List<Boss> aggroBosses = new ArrayList<>();
        player.getNearbyEntities(50, 50, 50).forEach(entity -> {
            if (entity instanceof Monster monster && !monsterteam.hasEntity(monster)) {
                int oldAggro = aggroBosses.size();
                Bosses.getBosses().forEach((s, bossTemplate) -> {
                    Boss boss = bossTemplate.getBoss();
                    if (boss == null) {
                        return;
                    }
                    LivingEntity bossEntity = boss.getBoss();
                    if (bossEntity == null || bossEntity.isDead()) {
                        return;
                    }
                    if (boss.getLockTarget()) {
                        return;
                    }
                    if (!bossEntity.equals(entity)) {
                        return;
                    }
                    aggroBosses.add(boss);
                    if (!boss.getDamagers().contains(player)) {
                        return;
                    }
                    if (boss.calculateDistance(entity.getLocation(), player.getLocation()) <= 5) {
                        boss.setLockTarget(true);
                        TeamUtils.saveTeamBefore(monster);
                        monsterteam.addEntity(monster);
                        monster.setGlowing(true);
                        monster.setTarget(player);
                    }
                });
                if (oldAggro == aggroBosses.size()) {
                    monster.setTarget(player);
                    TeamUtils.saveTeamBefore(monster);
                    entities.put(monster, monster.isGlowing());
                    monsterteam.addEntity(monster);
                    monster.setGlowing(true);
                }
            }
        });
        lockedShieldTo.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            Timer.of(player, 30, ComponentUtils.plainText(ankhShield.displayName()), BarColor.WHITE);
        }, 20);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 15, 3));
        player.playSound(player, Sound.ENTITY_WITHER_AMBIENT, 1, 2);
        Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
            entities.forEach((entity, b) -> {
                if (!b) {
                    entity.setGlowing(false);
                }
                monsterteam.removeEntity(entity);
                TeamUtils.returnTeamBefore(entity);
                entity.setTarget(null);
            });
            aggroBosses.forEach(boss -> {
                monsterteam.removeEntity(boss.getBoss());
                TeamUtils.returnTeamBefore(boss.getBoss());
                boss.setLockTarget(false);
            });
            Bukkit.getScheduler().runTaskLater(ModifiedBosses.getINSTANCE(), () -> {
                lockedShieldTo.remove(player.getUniqueId());
            }, 20 * 15);
        }, 20 * 15);
    }

    public boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }

    public boolean isClickable(Block block) {
        Material type = block.getType();
        return clickable.contains(type);
    }
}
