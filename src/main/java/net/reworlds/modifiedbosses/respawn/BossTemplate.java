package net.reworlds.modifiedbosses.respawn;

import lombok.Getter;
import lombok.Setter;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.boss.Boss;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class BossTemplate {

    private final EntityType type;
    private final Location location;
    private final int respawnBossBarRadius;
    private final int respawnMinutes;
    private final String bossName;
    private final Class<? extends Boss> bossClass;
    @Getter
    private LivingEntity bossEntity;
    @Getter
    private Boss boss;
    @Getter
    @Setter
    private long deathTime;
    @Getter
    private ArmorStand dummy;
    @Getter
    private RespawnTimer respawnTimer;

    public BossTemplate(Class<? extends Boss> bossClass, EntityType type, String bossName, Location location, int respawnMinutes, int respawnBossBarRadius) {
        this.type = type;
        this.bossName = bossName;
        this.location = location;
        this.respawnMinutes = respawnMinutes;
        this.respawnBossBarRadius = respawnBossBarRadius;
        this.bossClass = bossClass;
        initializeBoss();
    }

    public void spawnBoss() {
        Entity entity = location.getWorld().spawnEntity(location, type);
        if (entity instanceof LivingEntity livingEntity) {
            bossEntity = livingEntity;
            try {
                boss = bossClass.getConstructor(LivingEntity.class, String.class).newInstance(entity, bossName);
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
            if (dummy == null || dummy.isDead()) {
                dummy = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
                dummy.setInvisible(true);
                dummy.setInvulnerable(true);
                dummy.setAI(false);
                dummy.setGravity(false);
                respawnTimer = new RespawnTimer(dummy, bossName, 60 * 15, bossName, BarColor.WHITE, respawnBossBarRadius);
                return;
            }
            if (deathTime + (1000L * 60 * respawnMinutes) < System.currentTimeMillis()) {
                spawnBoss();
//                Bukkit.getOnlinePlayers().forEach(player -> {
//                    player.sendMessage("§e" + bossName + " появился!");
//                });
                dummy.remove();
            }
        }, 0, 20);
    }
}
