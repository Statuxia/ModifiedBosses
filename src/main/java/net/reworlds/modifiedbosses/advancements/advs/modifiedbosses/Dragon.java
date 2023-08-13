package net.reworlds.modifiedbosses.advancements.advs.modifiedbosses;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import net.reworlds.modifiedbosses.advancements.advs.AdvancementTabNamespaces;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.bukkit.Material;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;

public class Dragon extends BaseAdvancement  {

  public static AdvancementKey KEY = new AdvancementKey(AdvancementTabNamespaces.modifiedbosses_NAMESPACE, "dragon");


  public Dragon(Advancement parent) {
    super(KEY.getKey(), new AdvancementDisplay(Material.DRAGON_EGG, "Эндер-Дракон", AdvancementFrameType.GOAL, true, true, 1f, 1f , "§aУбейте обновленного Эндер-Дракона"), parent, 1);
  }
}