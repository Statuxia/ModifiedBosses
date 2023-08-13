package net.reworlds.modifiedbosses.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RPCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player player) {
            player.sendMessage("§cДля отображения текстур вам необходим мод OptiFine или CITResewn!");
            player.setResourcePack(
                    "https://download.mc-packs.net/pack/4c8aea24ce5532d7a216e5dbfb809f6f7c366dc5.zip",
                    "4c8aea24ce5532d7a216e5dbfb809f6f7c366dc5",
                    false);
        }
        return false;
    }
}
