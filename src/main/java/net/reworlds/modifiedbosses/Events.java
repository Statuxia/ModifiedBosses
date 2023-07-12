package net.reworlds.modifiedbosses;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;

public class Events implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        if (view.getOriginalTitle().equals("§0Дроп")) {
            event.setCancelled(true);
        }
    }
}
