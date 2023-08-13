package net.reworlds.modifiedbosses.advancements.advs;

import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;
import net.reworlds.modifiedbosses.ModifiedBosses;
import net.reworlds.modifiedbosses.advancements.AdvancementManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AdvancementListener implements Listener {

    @EventHandler
    private void onPlayerLoading(PlayerLoadingCompletedEvent event) {
        AdvancementManager advancementManager = ModifiedBosses.getAdvancementManager();
        Player player = event.getPlayer();
        advancementManager.modifiedbosses.showTab(player);
        advancementManager.modifiedbosses.grantRootAdvancement(player);
    }
}
