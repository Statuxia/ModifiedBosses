package net.reworlds.modifiedbosses.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class TeamUtils {
    private static final HashMap<ChatColor, Team> teams = new HashMap<>();

    public static @NotNull Team getTeam(@NotNull ChatColor teamColor, @NotNull String tag) {
        Team team = teams.get(teamColor);
        if (team != null) {
            return team;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        team = scoreboard.getTeam(2 + tag);
        if (team == null) {
            team = scoreboard.registerNewTeam(2 + tag);
            team.setAllowFriendlyFire(true);
            team.setCanSeeFriendlyInvisibles(false);
//            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        team.setColor(teamColor);
        teams.put(teamColor, team);
        return team;
    }

    public static @NotNull Team getTeam(@NotNull ChatColor teamColor) {
        return getTeam(teamColor, "team");
    }
}
