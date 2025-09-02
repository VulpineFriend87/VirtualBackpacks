package top.vulpine.virtualBackpacks;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import top.vulpine.virtualBackpacks.command.MainCommand;
import top.vulpine.virtualBackpacks.config.MainConfig;
import top.vulpine.virtualBackpacks.instance.Backpack;
import top.vulpine.virtualBackpacks.instance.StorageMethod;
import top.vulpine.virtualBackpacks.manager.BackpackManager;
import top.vulpine.virtualBackpacks.manager.StorageManager;
import top.vulpine.virtualBackpacks.util.ActionParser;
import top.vulpine.virtualBackpacks.util.logger.Logger;
import top.vulpine.virtualBackpacks.listener.InventoryListener;
import top.vulpine.virtualBackpacks.listener.JoinQuitListener;

public final class VirtualBackpacks extends JavaPlugin {

    @Getter
    private ActionParser actionParser;

    @Getter
    private StorageManager storageManager;

    @Getter
    private BackpackManager backpackManager;

    @Getter
    private MainConfig mainConfig;

    @Override
    public void onEnable() {

        mainConfig = new MainConfig(this, "config");

        String[] ascii = {
                "",
                "&5 _____ _     _           _ _____         _               _   ",
                "&5|  |  |_|___| |_ _ _ ___| | __  |___ ___| |_ ___ ___ ___| |_ ",
                "&5|  |  | |  _|  _| | | .'| | __ -| .'|  _| '_| . | .'|  _| '_|",
                "&5 \\___/|_|_| |_| |___|__,|_|_____|__,|___|_,_|  _|__,|___|_,_|",
                "",
                ""
        };

        for (String line : ascii) {
            Logger.system(line);
        }

        actionParser = new ActionParser(this);

        storageManager = new StorageManager(this,
                StorageMethod.fromString(mainConfig.getString("storage.method"))
        );

        backpackManager = new BackpackManager(this);

        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);

        getCommand("backpack").setExecutor(new MainCommand(this));

        Logger.system("&aPlugin enabled successfully!");

    }

    @Override
    public void onDisable() {
        if (backpackManager != null) {
            try {
                for (Backpack backpack : backpackManager.getBackpacks()) {
                    backpackManager.updateBackpack(backpack).thenRun(() -> {
                        backpackManager.unloadBackpack(backpack.getOwner());
                    });
                }
            } catch (Exception ignored) {}
        }
        if (storageManager != null) storageManager.close();
    }
}
