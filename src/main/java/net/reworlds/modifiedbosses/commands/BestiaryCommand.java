package net.reworlds.modifiedbosses.commands;

import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.bestiary.BestiaryBook;
import net.reworlds.modifiedbosses.bestiary.boss.dragon.DragonBook;
import net.reworlds.modifiedbosses.bestiary.boss.dragon.DragonDrop;
import net.reworlds.modifiedbosses.bestiary.boss.gelu.GeluBook;
import net.reworlds.modifiedbosses.bestiary.boss.gelu.GeluDrop;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BestiaryCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Только для игроков!"));
            return false;
        }

        if (strings.length == 0) {
            BestiaryBook.open(player);
            return false;
        }

        switch (strings[0]) {
            case "dragon" -> dragon(player, strings.length == 2 ? strings[1] : null);
            case "gelu" -> gelu(player, strings.length == 2 ? strings[1] : null);
            default -> BestiaryBook.open(player);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return getTab(strings.length == 1 ? null : strings[strings.length - 2]);
    }

    private List<String> getTab(String string) {
        if (string == null || string.equals("bestiary")) {
            return List.of("dragon", "gelu");
        }
        switch (string) {
            case "dragon", "gelu" -> {
                return List.of("info", "about", "drop");
            }
            default -> {
                return null;
            }
        }
    }

    private void dragon(Player player, String string) {
        if (string == null || !string.equals("drop")) {
            DragonBook.open(player);
            return;
        }
        DragonDrop.open(player);
    }

    private void gelu(Player player, String string) {
        if (string == null || !string.equals("drop")) {
            GeluBook.open(player);
            return;
        }
        GeluDrop.open(player);
    }
}
