package net.reworlds.modifiedbosses.advancements;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.advancements.advs.AdvancementTabNamespaces;
import net.reworlds.modifiedbosses.advancements.advs.modifiedbosses.Dragon;
import net.reworlds.modifiedbosses.advancements.advs.modifiedbosses.Gelu;
import net.reworlds.modifiedbosses.advancements.advs.modifiedbosses.Solo_gelu;
import org.bukkit.Material;

public class AdvancementManager {
    public static UltimateAdvancementAPI api;
    public AdvancementTab modifiedbosses;
    public Gelu gelu;
    public Dragon dragon;
    public Solo_gelu solo_gelu;

    public AdvancementManager() {
        initializeTabs();
    }

    private void initializeTabs() {
        try {
            api = UltimateAdvancementAPI.getInstance(ModifiedBosses.getINSTANCE());
            modifiedbosses = api.createAdvancementTab(AdvancementTabNamespaces.modifiedbosses_NAMESPACE);
            RootAdvancement start = new RootAdvancement(modifiedbosses, "start", new AdvancementDisplay(Material.KNOWLEDGE_BOOK, "ModifiedBosses", AdvancementFrameType.TASK, true, false, 0f, 0f, "§aТеперь с достижениями!"), "textures/block/black_concrete.png", 1);
            gelu = new Gelu(start);
            dragon = new Dragon(start);
            solo_gelu = new Solo_gelu(gelu);
            modifiedbosses.registerAdvancements(start, gelu, dragon, solo_gelu);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
}
