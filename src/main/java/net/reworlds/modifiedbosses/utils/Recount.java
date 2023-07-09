package net.reworlds.modifiedbosses.utils;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.collect.Lists;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.boss.dragon.Dragon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Recount {

    @Getter
    private Scoreboard board;
    private Objective objective;

    public Recount(String tableName) {
        generateScoreboard(tableName);
    }

    public static List<Player> getTop(int limit) {
        HashMap<Player, Integer> top = Dragon.getAttackedBy();
        return top.entrySet().stream()
                .sorted(Map.Entry.<Player, Integer>comparingByValue().reversed()).limit(limit).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void generateScoreboard(String name) {
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = board.getObjective(name);
        if (objective == null) {
            objective = board.registerNewObjective(name, "dummy");
        }
        objective.displayName(Component.text("§b" + name));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void addAttackers(HashMap<Player, Integer> attackers) {
        clear();
        List<Player> top = getTop(10);
        top.forEach(player -> {
            Score score = objective.getScore(player.getName());
            score.setScore(attackers.get(player));
        });
    }

    private void clear() {
        board.getEntries().forEach(s -> {
            board.resetScores(s);
        });
//        board.clearSlot(DisplaySlot.SIDEBAR);
    }
}
