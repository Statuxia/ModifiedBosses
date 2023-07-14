package net.reworlds.modifiedbosses.bestiary.boss.dragon;

import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.charms.Charms;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class DragonDrop {


    private static Inventory generateInventory() {
        Inventory inventory = Bukkit.createInventory(null, 27, Component.text("§0Дроп"));

        ItemStack exp = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        ItemMeta expMeta = exp.getItemMeta();
        expMeta.displayName(Component.text("§2Опыт"));
        exp.setItemMeta(expMeta);
        exp.lore(List.of(Component.text("§b100%")));
        inventory.addItem(exp);

        ItemStack apple = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
        apple.lore(List.of(Component.text("§d(2-3) §b50%")));
        inventory.addItem(apple);

        Charms.RARE.forEach(charm -> {
            ItemStack charmClone = charm.getCharm().clone();
            List<Component> lore = charmClone.lore();
            lore.add(Component.text("§7 (Любой Талисман) §b30%"));
            lore.add(Component.text("§7 (Любой Редкий) §b24%"));
            lore.add(Component.text("§6 (Конкретный) §b1.68%"));
            charmClone.lore(lore);
            inventory.addItem(charmClone);
        });

        Charms.EPIC.forEach(charm -> {
            ItemStack charmClone = charm.getCharm().clone();
            List<Component> lore = charmClone.lore();
            lore.add(Component.text("§7 (Любой Талисман) §b30%"));
            lore.add(Component.text("§7 (Любой Эпический) §b6%"));
            lore.add(Component.text("§6 (Конкретный) §b0.84%"));
            charmClone.lore(lore);
            inventory.addItem(charmClone);
        });

        return inventory;
    }

    public static void open(Player player) {
        player.openInventory(generateInventory());
    }
}