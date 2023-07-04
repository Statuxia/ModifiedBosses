package net.reworlds.modifiedbosses.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class Damage {

    public static void damage(LivingEntity damaged, Entity damager, double damage) {
        double health = damaged.getHealth();
        if (damage >= health) {
            damaged.damage(0.01, damager);
            damaged.setHealth(0);
            return;
        }

        damaged.damage(0.01, damager);
        try {
            damaged.setHealth(health - damage);
        } catch (IllegalArgumentException exception) {
            damaged.setHealth(0);
        }
    }
}
