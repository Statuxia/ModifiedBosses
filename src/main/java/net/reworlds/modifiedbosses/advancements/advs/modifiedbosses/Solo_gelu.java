package net.reworlds.modifiedbosses.advancements.advs.modifiedbosses;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import net.reworlds.modifiedbosses.advancements.advs.AdvancementTabNamespaces;
import org.bukkit.Material;

public class Solo_gelu extends BaseAdvancement {

    public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.modifiedbosses_NAMESPACE, "solo_gelu");


    public Solo_gelu(Advancement parent) {
        super(KEY.getKey(), new AdvancementDisplay(Material.NETHER_STAR, "Погибель Дьявола", AdvancementFrameType.CHALLENGE, true, true, 2f, 0f, "§aУбейте Джелу, Погибель Дьявола", "в одиночку", "", "§aНаграда: включение положительных", "§aэффектов на боссе Джелу"), parent, 1);
    }
}