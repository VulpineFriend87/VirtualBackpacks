package top.vulpine.virtualBackpacks.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import top.vulpine.virtualBackpacks.VirtualBackpacks;
import top.vulpine.virtualBackpacks.instance.Backpack;

public class InventoryListener implements Listener {

    private final VirtualBackpacks plugin;

    public InventoryListener(VirtualBackpacks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Backpack backpack = plugin.getBackpackManager().getBackpack(player.getUniqueId());
        if (backpack == null || event.getInventory().getHolder() != null || !event.getInventory().getHolder().equals(backpack)) return;

        plugin.getBackpackManager().updateBackpack(backpack);

    }
}

