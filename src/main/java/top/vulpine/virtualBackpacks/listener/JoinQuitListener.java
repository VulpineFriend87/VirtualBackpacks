package top.vulpine.virtualBackpacks.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.vulpine.virtualBackpacks.VirtualBackpacks;
import top.vulpine.virtualBackpacks.instance.Backpack;

public class JoinQuitListener implements Listener {

    private final VirtualBackpacks plugin;

    public JoinQuitListener(VirtualBackpacks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getBackpackManager().loadBackpack(event.getPlayer().getUniqueId(), true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Backpack backpack = plugin.getBackpackManager().getBackpack(event.getPlayer().getUniqueId());
        if (backpack == null) return;

        plugin.getBackpackManager().updateBackpack(backpack).thenRun(() -> {

            plugin.getBackpackManager().unloadBackpack(event.getPlayer().getUniqueId());

        });
    }
}

