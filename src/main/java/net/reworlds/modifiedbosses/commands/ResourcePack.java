package net.reworlds.modifiedbosses.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResourcePack implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player player) {
            player.setResourcePack("https://download.mc-packs.net/pack/c92ff8f159fa394467ae935c785aaf98ea2fd042.zip", "c92ff8f159fa394467ae935c785aaf98ea2fd042", false);
        }
        return false;
    }
}
