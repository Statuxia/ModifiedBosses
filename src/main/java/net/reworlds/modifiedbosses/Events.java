package net.reworlds.modifiedbosses;

import net.reworlds.modifiedbosses.charms.CharmsEffects;
import net.reworlds.modifiedbosses.items.SpecialItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Method;

public class Events implements Listener {

    public static final double minX = 905;
    public static final double maxX = 1080;
    public static final double minZ = 313;
    public static final double maxZ = 432;

//    private final double minX = -75;
//    private final double maxX = 100;
//    private final double minZ = -232;
//    private final double maxZ = -113;

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryView view = event.getView();

        try {
            Method getOriginalTitle = view.getClass().getMethod("getOriginalTitle");
            String genericString = getOriginalTitle.toGenericString();
            if (!genericString.contains("abstract") && "§0Дроп".equals(view.getOriginalTitle())) {
                event.setCancelled(true);
            }
        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void stopDragonDamage(EntityExplodeEvent event) {
        Entity e = event.getEntity();
        if (e instanceof EnderDragonPart || e instanceof EnderDragon) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        if (!location.getWorld().getName().equalsIgnoreCase("world_the_end")) {
            return;
        }


        if (inZone(location.getX(), minX, maxX) && inZone(location.getZ(), minZ, maxZ)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location location = event.getBlock().getLocation();
        if (!location.getWorld().getName().equals("world_the_end")) {
            return;
        }

        if (inZone(location.getX(), minX, maxX) && inZone(location.getZ(), minZ, maxZ)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Location location = event.getLocation();
        if (!location.getWorld().getName().equals("world_the_end")) {
            return;
        }

        if (inZone(location.getX(), minX, maxX) && inZone(location.getZ(), minZ, maxZ)) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Location location = event.getBlock().getLocation();
        if (!location.getWorld().getName().equals("world_the_end")) {
            return;
        }

        if (inZone(location.getX(), minX, maxX) && inZone(location.getZ(), minZ, maxZ)) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onPlayerPlaceBucket(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        Location location = event.getClickedBlock().getLocation();

        if (!location.getWorld().getName().equals("world_the_end")) {
            return;
        }
        if (!(inZone(location.getX(), minX, maxX) && inZone(location.getZ(), minZ, maxZ))) {
            return;
        }

        if (event.getItem() != null && event.getItem().getType().toString().toLowerCase().contains("bucket")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        Location location = event.getEntity().getLocation();
        if (!location.getWorld().getName().equals("world_the_end")) {
            return;
        }
        if (inZone(location.getX(), minX, maxX) && inZone(location.getZ(), minZ, maxZ)) {
            event.getDrops().clear();
        }
    }

    @EventHandler
    public void onPistonMove(BlockPistonRetractEvent event) {
        Location location = event.getBlock().getLocation();
        if (!location.getWorld().getName().equals("world_the_end")) {
            return;
        }

        if (inZone(location.getX(), minX, maxX) && inZone(location.getZ(), minZ, maxZ)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonMove(BlockPistonExtendEvent event) {
        Location location = event.getBlock().getLocation();
        if (!location.getWorld().getName().equals("world_the_end")) {
            return;
        }

        if (inZone(location.getX(), minX, maxX) && inZone(location.getZ(), minZ, maxZ)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        Location location = event.getLocation();
        if (!location.getWorld().getName().equals("world_the_end")) {
            return;
        }

        if (inZone(location.getX(), minX, maxX) && inZone(location.getZ(), minZ, maxZ)) {
            if (event.getEntity() instanceof Enderman || event.getEntity() instanceof Endermite) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDebuff(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (SpecialItems.isSpecialItem(itemInOffHand, SpecialItems.ankhShieldLore[0]) || SpecialItems.isSpecialItem(itemInMainHand, SpecialItems.ankhShieldLore[0])) {
            PotionEffect newEffect = event.getNewEffect();
            if (newEffect == null) {
                return;
            }

            if (CharmsEffects.getNegativeEffects().contains(newEffect.getType())) {
                event.setCancelled(true);
            }
        }
    }

    public static boolean inZone(double coordinate, double min, double max) {
        return coordinate > min && coordinate < max;
    }
}
