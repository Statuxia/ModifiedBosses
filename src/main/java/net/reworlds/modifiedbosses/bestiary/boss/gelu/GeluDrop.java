package net.reworlds.modifiedbosses.bestiary.boss.gelu;

import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.charms.Charms;
import net.reworlds.modifiedbosses.items.SpecialItems;
import net.reworlds.modifiedbosses.utils.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GeluDrop {


    private static Inventory generateInventory() {
        Inventory inventory = Bukkit.createInventory(null, 27, Component.text("§0Дроп"));

        ItemStack exp = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        ItemMeta expMeta = exp.getItemMeta();
        expMeta.displayName(Component.text("§2Опыт"));
        exp.setItemMeta(expMeta);
        exp.lore(List.of(Component.text("--------"), Component.text("§b100%")));
        inventory.addItem(exp);

        ItemStack apple = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
        apple.lore(List.of(Component.text("--------"), Component.text("§d(3-5) §b50%"), Component.text("§7За каждые 100 урона добавляется 1% к шансу на предмет до 20%")));
        inventory.addItem(apple);

        Charms.RARE.forEach(charm -> {
            ItemStack charmClone = charm.getCharm().clone();
            List<Component> lore = charmClone.lore();
            lore.add(Component.text("--------"));
            lore.add(Component.text("§7(Любой Талисман) §b30%"));
            lore.add(Component.text("§7(Любой Редкий) §b18%"));
            lore.add(Component.text("§6(Конкретный) §b1.26%"));
            lore.add(Component.text("§7За каждые 100 урона добавляется 1% к шансу на предмет до 20%"));
            charmClone.lore(lore);
            inventory.addItem(charmClone);
        });

        Charms.EPIC.forEach(charm -> {
            ItemStack charmClone = charm.getCharm().clone();
            List<Component> lore = charmClone.lore();
            lore.add(Component.text("--------"));
            lore.add(Component.text("§7(Любой Талисман) §b30%"));
            lore.add(Component.text("§7(Любой Эпический) §b12%"));
            lore.add(Component.text("§6(Конкретный) §b1.68%"));
            lore.add(Component.text("§7За каждые 100 урона добавляется 1% к шансу на предмет до 20%"));
            charmClone.lore(lore);
            inventory.addItem(charmClone);
        });

        ItemStack yamato = new ItemStack(SpecialItems.yamato);
        setGeluSpecialDropChance(inventory, yamato);

        ItemStack ancientSHBow = new ItemStack(SpecialItems.ancientSHBow);
        setGeluSpecialDropChance(inventory, ancientSHBow);

        ItemStack ankhShield = new ItemStack(SpecialItems.ankhShield);
        setGeluSpecialDropChance(inventory, ankhShield);

        return inventory;
    }

    private static void setGeluSpecialDropChance(Inventory inventory, ItemStack itemStack) {
        List<Component> lore = itemStack.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(Component.text("--------"));
        lore.add(Component.text("§b3%"));
        lore.add(Component.text("§cУрон не увеличивает шанс на дроп!"));
        itemStack.lore(lore);
        inventory.addItem(itemStack);
    }

    public static void open(Player player) {
        player.openInventory(generateInventory());
    }
}
