package top.vulpine.virtualBackpacks.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.vulpine.virtualBackpacks.VirtualBackpacks;
import top.vulpine.virtualBackpacks.instance.Backpack;
import top.vulpine.virtualBackpacks.util.Colorize;

public class MainCommand implements CommandExecutor {

    private final VirtualBackpacks plugin;

    public MainCommand(VirtualBackpacks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Colorize.color(
                    plugin.getMainConfig().getString("messages.only_players")
            ));
            return true;
        }

        Backpack backpack = plugin.getBackpackManager().getBackpack(player.getUniqueId());
        if (backpack == null) {
            sender.sendMessage(Colorize.color(
                    plugin.getMainConfig().getString("messages.backpack_not_loaded")
            ));
            return true;
        }

        backpack.open(player);

        return true;
    }
}

