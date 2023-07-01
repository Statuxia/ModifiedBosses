package net.reworlds.modifiedbosses.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Runes implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ItemStack stack = new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1);
        stack.addItemFlags(ItemFlag.values());
        List<Component> components = new ArrayList<>();
        components.add(Component.text("Талисман силы"));
        components.add(Component.text("Дает эффект силы 1 при ношении в инвентаре"));
        stack.lore(components);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text("Талисман силы"));
        stack.getItemMeta().displayName(Component.text("Талисман силы"));

        if (commandSender instanceof Player player) {
            player.getInventory().addItem(stack);
            stack.setItemMeta(meta);
            player.getInventory().addItem(stack);
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
