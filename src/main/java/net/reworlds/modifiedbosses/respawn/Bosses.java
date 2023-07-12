package net.reworlds.modifiedbosses.respawn;

import lombok.Getter;
import net.reworlds.modifiedbosses.boss.dragon.Dragon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class Bosses {

    @Getter
    private static final Map<String, BossTemplate> bosses = new HashMap<>();

    public static void initializeDragon() {
        World world = Bukkit.getWorld("world_the_end");
        Location location = new Location(world, 0, 100, 0);
        String bossName = "Эндер-Дракон";
        int respawnMinutes = 15;
        int respawnBossBarRadius = 200;
        BossTemplate template = new BossTemplate(Dragon.class, EntityType.ENDER_DRAGON, bossName, location, respawnMinutes, respawnBossBarRadius);
        bosses.put(bossName, template);
    }
}
