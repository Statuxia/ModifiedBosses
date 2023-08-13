package net.reworlds.modifiedbosses.utils;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdvancementUtils {

    public static List<Advancement> getCategoryAdvancements(Category category) {
        List<Advancement> categoryAdvancements = new ArrayList<>();
        Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
        while (it.hasNext()) {
            Advancement advancement = it.next();
            String string = advancement.getRoot().getKey().toString();
            if (string.contains("minecraft:recipes/")) {
                continue;
            }
            if (string.contains(category.getValue())) {
                categoryAdvancements.add(advancement);
            }
        }
        return categoryAdvancements;
    }

    public static boolean isCompletedCategory(Player player, Category category) {
        List<Advancement> categoryAdvancements = getCategoryAdvancements(category);
        AtomicBoolean completed = new AtomicBoolean(true);
        categoryAdvancements.forEach(advancement -> {
            AdvancementProgress advancementProgress = player.getAdvancementProgress(advancement);
            if (!advancementProgress.isDone()) {
                completed.set(false);
            }
        });
        return completed.get();
    }

    public enum Category {
        END("end"),
        NETHER("nether"),
        ADVENTURE("adventure"),
        HUSBANDRY("husbandry"),
        STORY("story");

        private final String value;

        Category(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
