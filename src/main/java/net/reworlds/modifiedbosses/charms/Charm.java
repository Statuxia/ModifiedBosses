package net.reworlds.modifiedbosses.charms;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.potion.PotionEffectType.*;

public class Charm {

    private final int level;
    private ItemStack charm;
    private String rawName;
    private String name;

    public Charm(PotionEffectType type, int level) {
        this.level = Math.max(0, Math.min(2, level));
        makeItem();
        addName(type);
        addLore();
    }

    public static int getLevel(String name) {
        return name.contains("0") ? 0 : name.contains("1") ? 1 : name.contains("2") ? 2 : -1;
    }

    public static PotionEffectType getPotionEffectType(String name, int endIndex) {
        String effectName;
        try {
            effectName = name.substring(name.indexOf(" ", 7) + 1, name.indexOf(String.valueOf(endIndex)) - 1);
        } catch (StringIndexOutOfBoundsException exception) {
            return null;
        }
        switch (effectName) {
            case "Силы" -> {
                return INCREASE_DAMAGE;
            }
            case "Сопротивления" -> {
                return DAMAGE_RESISTANCE;
            }
            case "Спешки" -> {
                return FAST_DIGGING;
            }
            case "Грации Дельфина" -> {
                return DOLPHINS_GRACE;
            }
            case "Огнестойкости" -> {
                return FIRE_RESISTANCE;
            }
            case "Прыгучести" -> {
                return JUMP;
            }
            case "Ночного Зрения" -> {
                return NIGHT_VISION;
            }
            case "Регенерации" -> {
                return REGENERATION;
            }
            case "Сытости" -> {
                return SATURATION;
            }
            case "Подводного Дыхания" -> {
                return WATER_BREATHING;
            }
            case "Скорости" -> {
                return SPEED;
            }
            case "Силы Источника" -> {
                return CONDUIT_POWER;
            }
            case "Замедленного Падения" -> {
                return SLOW_FALLING;
            }
            case "Невидимости" -> {
                return INVISIBILITY;
            }
            case "Свечения" -> {
                return GLOWING;
            }
            default -> {
                return null;
            }
        }
    }

    public ItemStack getCharm() {
        return charm;
    }

    private void makeItem() {
        charm = new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1);
        charm.addItemFlags(ItemFlag.values());
    }

    private void addName(PotionEffectType type) {
        rawName = getName(type);
        switch (level) {
            case 0 -> {
                if (rawName.equals("Сытости")) {
                    name = "§d";
                } else {
                    name = "§b";
                }
            }
            case 1 -> name = "§d";
            case 2 -> name = "§6";
        }

        name += "Талисман " + rawName;
        ItemMeta meta = charm.getItemMeta();
        meta.displayName(Component.text(name));
        charm.setItemMeta(meta);
    }

    private void addLore() {
        List<Component> components = new ArrayList<>();
        components.add(Component.text("§fДает эффект " + rawName + " " + (level + 1) + " при ношении в инвентаре"));
        charm.lore(components);
    }

    private String getName(PotionEffectType type) {
        switch (type.getName().toLowerCase()) {
            case "increase_damage" -> {
                return "Силы";
            }
            case "damage_resistance" -> {
                return "Сопротивления";
            }
            case "fast_digging" -> {
                return "Спешки";
            }
            case "dolphins_grace" -> {
                return "Грации Дельфина";
            }
            case "fire_resistance" -> {
                return "Огнестойкости";
            }
            case "jump" -> {
                return "Прыгучести";
            }
            case "night_vision" -> {
                return "Ночного Зрения";
            }
            case "regeneration" -> {
                return "Регенерации";
            }
            case "saturation" -> {
                return "Сытости";
            }
            case "water_breathing" -> {
                return "Подводного Дыхания";
            }
            case "speed" -> {
                return "Скорости";
            }
            case "conduit_power" -> {
                return "Силы Источника";
            }
            case "slow_falling" -> {
                return "Замедленного Падения";
            }
            case "invisibility" -> {
                return "Невидимости";
            }
            case "glowing" -> {
                return "Свечения";
            }
            default -> {
                return "Без Эффекта";
            }
        }
    }
}
