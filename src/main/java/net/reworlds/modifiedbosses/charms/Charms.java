package net.reworlds.modifiedbosses.charms;

import java.util.List;

import static org.bukkit.potion.PotionEffectType.*;

public class Charms {
    public static Charm DAMAGE_RESISTANCE_RARE = new Charm(DAMAGE_RESISTANCE, 0);
    public static Charm FAST_DIGGING_RARE = new Charm(FAST_DIGGING, 0);
    public static Charm DOLPHINS_GRACE_RARE = new Charm(DOLPHINS_GRACE, 0);
    public static Charm FIRE_RESISTANCE_RARE = new Charm(FIRE_RESISTANCE, 0);
    public static Charm JUMP_RARE = new Charm(JUMP, 0);
    public static Charm NIGHT_VISION_RARE = new Charm(NIGHT_VISION, 0);
    public static Charm REGENERATION_RARE = new Charm(REGENERATION, 0);
    public static Charm WATER_BREATHING_RARE = new Charm(WATER_BREATHING, 0);
    public static Charm SPEED_RARE = new Charm(SPEED, 0);
    public static Charm INCREASE_DAMAGE_RARE = new Charm(INCREASE_DAMAGE, 0);
    public static Charm CONDUIT_POWER_RARE = new Charm(CONDUIT_POWER, 0);
    public static Charm SLOW_FALLING_RARE = new Charm(SLOW_FALLING, 0);
    public static Charm INVISIBILITY_RARE = new Charm(INVISIBILITY, 0);
    public static Charm GLOWING_RARE = new Charm(GLOWING, 0);
    public static Charm DAMAGE_RESISTANCE_EPIC = new Charm(DAMAGE_RESISTANCE, 1);
    public static Charm FAST_DIGGING_EPIC = new Charm(FAST_DIGGING, 1);
    public static Charm JUMP_EPIC = new Charm(JUMP, 1);
    public static Charm REGENERATION_EPIC = new Charm(REGENERATION, 1);
    public static Charm SPEED_EPIC = new Charm(SPEED, 1);
    public static Charm INCREASE_DAMAGE_EPIC = new Charm(INCREASE_DAMAGE, 1);
    public static Charm SATURATION_EPIC = new Charm(SATURATION, 0);

    public static List<Charm> RARE = List.of(
            DAMAGE_RESISTANCE_RARE,
            FAST_DIGGING_RARE,
            DOLPHINS_GRACE_RARE,
            FIRE_RESISTANCE_RARE,
            JUMP_RARE,
            NIGHT_VISION_RARE,
            REGENERATION_RARE,
            WATER_BREATHING_RARE,
            SPEED_RARE,
            INCREASE_DAMAGE_RARE,
            CONDUIT_POWER_RARE,
            SLOW_FALLING_RARE,
            INVISIBILITY_RARE,
            GLOWING_RARE
    );
    public static List<Charm> EPIC = List.of(
            DAMAGE_RESISTANCE_EPIC,
            FAST_DIGGING_EPIC,
            JUMP_EPIC,
            REGENERATION_EPIC,
            SPEED_EPIC,
            INCREASE_DAMAGE_EPIC,
            SATURATION_EPIC
    );
}
