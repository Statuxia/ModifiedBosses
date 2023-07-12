package net.reworlds.modifiedbosses.commands.boss.dragon;

import net.reworlds.modifiedbosses.bestiary.boss.dragon.DragonBook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BestiaryDragonCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (commandSender instanceof Player player) {
            DragonBook.open(player);
        }
        return false;
    }
}
