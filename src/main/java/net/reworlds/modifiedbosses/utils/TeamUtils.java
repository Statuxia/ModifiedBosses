package net.reworlds.modifiedbosses.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class TeamUtils {
    private static final HashMap<ChatColor, Team> teams = new HashMap<>();
    private static final HashMap<Player, Team> teamBefore = new HashMap<>();


    public static @NotNull Team getTeam(@NotNull ChatColor teamColor, @NotNull String tag) {
        Team team = teams.get(teamColor);
        if (team != null) {
            return team;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        team = scoreboard.getTeam((1100 + teams.size()) + tag);
        if (team == null) {
            team = scoreboard.registerNewTeam(1100 + (teams.size()) + tag);
            team.setAllowFriendlyFire(true);
            team.setCanSeeFriendlyInvisibles(false);
        }
        team.setColor(teamColor);
        teams.put(teamColor, team);
        return team;
    }

    public static @NotNull Team getTeam(@NotNull ChatColor teamColor) {
        return getTeam(teamColor, "team");
    }

    public static void saveTeamBefore(@NotNull Player player, List<Team> lock) {
        Bukkit.getScoreboardManager().getMainScoreboard().getTeams().forEach(team -> {
            if (team.hasEntity(player) && !lock.contains(team)) {
                teamBefore.put(player, team);
                team.removeEntity(player);
            }
        });
    }

    public static void returnTeamBefore(@NotNull Player player) {
        Team team = teamBefore.remove(player);
        if (team != null) {
            team.addEntity(player);
        }
    }
}
