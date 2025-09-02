package top.vulpine.virtualBackpacks.config;

import org.bukkit.configuration.file.YamlConfiguration;
import top.vulpine.virtualBackpacks.VirtualBackpacks;
import top.vulpine.virtualBackpacks.manager.ConfigManager;

import java.util.List;

public class MainConfig extends ConfigManager {

    public MainConfig(VirtualBackpacks plugin, String name) {
        super(plugin, name, plugin.getDataFolder().getPath());

        YamlConfiguration yml = getYml();

        yml.options().setHeader(List.of("VirtualBackpacks Configuration File", "By VulpineFriend87"));
        yml.addDefault("storage.method", "H2");
        yml.addDefault("storage.mysql.host", "localhost");
        yml.addDefault("storage.mysql.port", 3306);
        yml.addDefault("storage.mysql.database", "backpacks");
        yml.addDefault("storage.mysql.username", "root");
        yml.addDefault("storage.mysql.password", "password");

        yml.addDefault("messages.only_players", "&7[&5VB&7] &cThis command can only be executed by players.");
        yml.addDefault("messages.no_permission", "&7[&5VB&7] &cYou do not have permission to execute this command.");
        yml.addDefault("messages.backpack_not_loaded", "&7[&5VB&7] &cYour backpack is not loaded. Please re-join or contact an administrator.");

        save();

    }
}
