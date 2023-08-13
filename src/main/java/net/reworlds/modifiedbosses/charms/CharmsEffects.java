package net.reworlds.modifiedbosses.charms;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;

public class CharmsEffects {

    private static final HashMap<String, PotionEffect> effects = new HashMap<>();
    private static final List<PotionEffectType> positiveEffects = List.of(
            PotionEffectType.ABSORPTION,
            PotionEffectType.CONDUIT_POWER,
            PotionEffectType.DAMAGE_RESISTANCE,
            PotionEffectType.DOLPHINS_GRACE,
            PotionEffectType.FAST_DIGGING,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.INCREASE_DAMAGE,
            PotionEffectType.JUMP,
            PotionEffectType.REGENERATION,
            PotionEffectType.SATURATION,
            PotionEffectType.SLOW_FALLING,
            PotionEffectType.WATER_BREATHING,
            PotionEffectType.SPEED
    );

    private static final List<PotionEffectType> negativeEffects = List.of(
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.CONFUSION,
            PotionEffectType.BLINDNESS,
            PotionEffectType.DARKNESS,
            PotionEffectType.WITHER,
            PotionEffectType.POISON,
            PotionEffectType.HUNGER,
            PotionEffectType.WEAKNESS
    );

    public static HashMap<String, PotionEffect> getEffects() {
        return effects;
    }

    public static List<PotionEffectType> getPositiveEffects() {
        return positiveEffects;
    }

    public static List<PotionEffectType> getNegativeEffects() {
        return negativeEffects;
    }
}
