package net.reworlds.modifiedbosses.respawn;

import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.boss.dragon.Dragon;
import net.reworlds.modifiedbosses.boss.gelu.Gelu;
import net.reworlds.modifiedbosses.utils.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class Bosses {

    private static final Map<String, BossTemplate> bosses = new HashMap<>();

    public static Map<String, BossTemplate> getBosses() {
        return bosses;
    }

    public static void initializeDragon() {
        World world = Bukkit.getWorld("world_the_end");
        Location location = new Location(world, 0, 100, 0);
        String bossName = "Эндер-Дракон";
        Component bossNameComponent = ComponentUtils.gradient("#FA0C6B", "#594B92", bossName);
        int respawnMinutes = 15;
        int respawnBossBarRadius = 200;
        BossTemplate template = new BossTemplate(Dragon.class, EntityType.ENDER_DRAGON, bossName, bossNameComponent, location, respawnMinutes, respawnBossBarRadius);
        bosses.put(bossName, template);
    }

    public static void initializeGelu() {
        World world = Bukkit.getWorld("world_the_end");
        Location location = new Location(world, 976, 89, 361);
//        World world = Bukkit.getWorld("world");
//        Location location = new Location(world, -4, 74, -184);
        location.getChunk().load();
        String bossName = "Джелу, Погибель Дьявола";
        Component bossNameComponent = ComponentUtils.gradient("#FA0C6B", "#594B92", bossName);
        int respawnMinutes = 15;
        int respawnBossBarRadius = 200;
        BossTemplate template = new BossTemplate(Gelu.class, EntityType.WITHER_SKELETON, bossName, bossNameComponent, location, respawnMinutes, respawnBossBarRadius, 1691416850482L + (1000 * 60 * 20));
//        BossTemplate template = new BossTemplate(Gelu.class, EntityType.WITHER_SKELETON, bossName, bossNameComponent, location, respawnMinutes, respawnBossBarRadius);
        bosses.put(bossName, template);
    }
}
