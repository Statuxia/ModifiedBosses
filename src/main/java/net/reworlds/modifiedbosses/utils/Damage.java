package net.reworlds.modifiedbosses.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class Damage {

    public static void damage(LivingEntity damaged, Entity damager, double damage) {
        double health = damaged.getHealth();
        health -= damage;
        damaged.damage(0.1, damager);
        if (health < 0) {
            damaged.setHealth(0);
        } else {
            damaged.setHealth(health);
        }
    }
}
