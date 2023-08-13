package net.reworlds.modifiedbosses.respawn;

import net.kyori.adventure.text.Component;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.boss.Boss;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.atomic.AtomicInteger;

public class BossTemplate {

    private final EntityType type;
    private final Location location;
    private final int respawnBossBarRadius;
    private final int respawnMinutes;
    private final String bossName;
    private final Component bossNameComponent;
    private final Class<? extends Boss> bossClass;
    private long firstSpawnTime;
    private LivingEntity bossEntity;
    private Boss boss;
    private long deathTime;
    private ArmorStand dummy;
    private RespawnTimer respawnTimer;

    public BossTemplate(Class<? extends Boss> bossClass, EntityType type, String bossName, Component bossNameComponent, Location location, int respawnMinutes, int respawnBossBarRadius) {
        this.type = type;
        this.bossName = bossName;
        this.bossNameComponent = bossNameComponent;
        this.location = location;
        this.respawnMinutes = respawnMinutes;
        this.respawnBossBarRadius = respawnBossBarRadius;
        this.bossClass = bossClass;
        initializeBoss();
    }

    public BossTemplate(Class<? extends Boss> bossClass, EntityType type, String bossName, Component bossNameComponent, Location location, int respawnMinutes, int respawnBossBarRadius, long firstSpawnTime) {
        this.type = type;
        this.bossName = bossName;
        this.bossNameComponent = bossNameComponent;
        this.location = location;
        this.respawnMinutes = respawnMinutes;
        this.respawnBossBarRadius = respawnBossBarRadius;
        this.bossClass = bossClass;
        this.firstSpawnTime = firstSpawnTime;
        initializeBoss();

    }

    public void spawnBoss() {
        Entity entity = location.getWorld().spawnEntity(location.add(0.5, 0, 0.5), type);
        if (entity instanceof LivingEntity livingEntity) {
            bossEntity = livingEntity;
            try {
                boss = bossClass.getConstructor(LivingEntity.class, String.class, Component.class).newInstance(entity, bossName, bossNameComponent);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            Bukkit.getPluginManager().registerEvents(boss, ModifiedBosses.getINSTANCE());
        }
    }

    public void initializeBoss() {
        Bukkit.getScheduler().runTaskTimer(ModifiedBosses.getINSTANCE(), () -> {
            if (!(bossEntity == null || bossEntity.isDead())) {
                return;
            }
            if (dummy == null) {
                dummy = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, -2.5, 0), EntityType.ARMOR_STAND);
                AtomicInteger shits = new AtomicInteger();
                dummy.getNearbyEntities(1, 1, 1).forEach(entity -> {
                    if (entity instanceof ArmorStand) {
                        shits.getAndIncrement();
                    }
                });
                dummy.setInvisible(true);
                dummy.setInvulnerable(true);
                dummy.setAI(false);
                dummy.setGravity(false);
                dummy.setCollidable(false);
                dummy.setRemoveWhenFarAway(false);
                dummy.getChunk().load();
                if (firstSpawnTime > System.currentTimeMillis()) {
                    respawnTimer = new RespawnTimer(dummy, bossName, (int) ((firstSpawnTime - System.currentTimeMillis()) / 1000L), "" + bossName, BarColor.WHITE, respawnBossBarRadius);
                } else {
                    respawnTimer = new RespawnTimer(dummy, bossName, 60 * respawnMinutes, bossName, BarColor.WHITE, respawnBossBarRadius);
                }
                return;
            }
            dummy.getChunk().load();
            if (deathTime + (1000L * 60 * respawnMinutes) < System.currentTimeMillis() && firstSpawnTime < System.currentTimeMillis()) {
                spawnBoss();
//                Bukkit.getOnlinePlayers().forEach(player -> {
//                    player.sendMessage("§e" + bossName + " появился!");
//                });
                dummy.remove();
                dummy = null;
            }
        }, 0, 20);
    }

    public RespawnTimer getRespawnTimer() {
        return respawnTimer;
    }

    public ArmorStand getDummy() {
        return dummy;
    }

    public LivingEntity getBossEntity() {
        return bossEntity;
    }

    public Boss getBoss() {
        return boss;
    }

    public long getDeathTime() {
        return deathTime;
    }

    public void setDeathTime(long deathTime) {
        this.deathTime = deathTime;
    }
}
