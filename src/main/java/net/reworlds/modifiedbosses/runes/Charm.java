package net.reworlds.modifiedbosses.runes;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.potion.PotionEffectType.*;

public class Rune {

    private final int level;
    @Getter
    private ItemStack rune;
    private String rawName;
    private String name;

    public Rune(PotionEffectType type, int level) {
        this.level = Math.max(0, Math.min(2, level));
        makeItem();
        addName(type);
        addLore();
    }

    public static PotionEffectType getPotionEffectType(String name, int endIndex) {
        String effectName = name.substring(name.indexOf(" ", 5) + 1, name.indexOf(String.valueOf(endIndex)) - 1);
        switch (effectName) {
            case "сопротивления" -> {
                return DAMAGE_RESISTANCE;
            }
            case "спешки" -> {
                return FAST_DIGGING;
            }
            case "грации дельфина" -> {
                return DOLPHINS_GRACE;
            }
            case "огнестойкости" -> {
                return FIRE_RESISTANCE;
            }
            case "прыгучести" -> {
                return JUMP;
            }
            case "ночного зрения" -> {
                return NIGHT_VISION;
            }
            case "регенерации" -> {
                return REGENERATION;
            }
            case "сытости" -> {
                return SATURATION;
            }
            case "подводного дыхания" -> {
                return WATER_BREATHING;
            }
            case "скорости" -> {
                return SPEED;
            }
            case "силы источника" -> {
                return CONDUIT_POWER;
            }
            case "замедленного падения" -> {
                return SLOW_FALLING;
            }
            case "невидимости" -> {
                return INVISIBILITY;
            }
            case "свечения" -> {
                return GLOWING;
            }
            default -> {
                return null;
            }
        }
    }

    public static int getLevel(String name) {
        return name.contains("0") ? 0 : name.contains("1") ? 1 : name.contains("2") ? 2 : -1;
    }

    private void makeItem() {
        rune = new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1);
        rune.addItemFlags(ItemFlag.values());
    }

    private void addName(PotionEffectType type) {
        switch (level) {
            case 0 -> name = "§b";
            case 1 -> name = "§d";
            case 2 -> name = "§6";
        }
        rawName = getName(type);
        name += "Талисман " + rawName;
        ItemMeta meta = rune.getItemMeta();
        meta.displayName(Component.text(name));
        rune.setItemMeta(meta);
    }

    private void addLore() {
        List<Component> components = new ArrayList<>();
        components.add(Component.text("§fДает эффект " + rawName + " " + (level + 1) + " при ношении в инвентаре"));
        rune.lore(components);
    }

    private String getName(PotionEffectType type) {
        switch (type.getName().toLowerCase()) {
            case "damage_resistance" -> {
                return "сопротивления";
            }
            case "fast_digging" -> {
                return "спешки";
            }
            case "dolphins_grace" -> {
                return "грации дельфина";
            }
            case "fire_resistance" -> {
                return "огнестойкости";
            }
            case "jump" -> {
                return "прыгучести";
            }
            case "night_vision" -> {
                return "ночного зрения";
            }
            case "regeneration" -> {
                return "регенерации";
            }
            case "saturation" -> {
                return "сытости";
            }
            case "water_breathing" -> {
                return "подводного дыхания";
            }
            case "speed" -> {
                return "скорости";
            }
            case "conduit_power" -> {
                return "силы источника";
            }
            case "slow_falling" -> {
                return "замедленного падения";
            }
            case "invisibility" -> {
                return "невидимости";
            }
            case "glowing" -> {
                return "свечения";
            }
            default -> {
                return "без эффекта";
            }
        }
    }
}
