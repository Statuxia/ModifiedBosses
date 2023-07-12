package net.reworlds.modifiedbosses.commands.boss.dragon;

import net.reworlds.modifiedbosses.bestiary.boss.dragon.DragonDrop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BestiaryDragonDropCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (commandSender instanceof Player player) {
            DragonDrop.open(player);
        }
        return false;
    }
}
