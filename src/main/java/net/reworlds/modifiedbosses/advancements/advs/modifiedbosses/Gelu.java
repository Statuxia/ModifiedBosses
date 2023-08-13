package net.reworlds.modifiedbosses.advancements.advs.modifiedbosses;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import net.reworlds.modifiedbosses.advancements.advs.AdvancementTabNamespaces;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.Material;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;

public class Gelu extends BaseAdvancement  {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.modifiedbosses_NAMESPACE, "gelu");


  public Gelu(Advancement parent) {
    super(KEY.getKey(), new AdvancementDisplay(Material.WITHER_SKELETON_SKULL, "Джелу, Погибель Дьявола", AdvancementFrameType.GOAL, true, true, 1f, 0f , "§aУбейте Джелу, Погибель Дьявола"), parent, 1);
  }
}