package top.vulpine.virtualBackpacks.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import top.vulpine.virtualBackpacks.VirtualBackpacks;
import top.vulpine.virtualBackpacks.util.logger.Logger;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    @Getter
    private YamlConfiguration yml;

    private File config;

    @Getter @Setter
    private String name;

    @Getter
    private boolean firstTime = false;

    public ConfigManager(VirtualBackpacks plugin, String name, String dir) {

        File file = new File(dir);

        if (!file.exists()) {
            if (!file.mkdirs()) {

                Logger.error("Could not create directory: " + file.getPath());
                return;

            }
        }

        config = new File(dir, name + ".yml");
        if (!config.exists()) {
            firstTime = true;
            Logger.info("Creating new config: " + config.getPath());

            try {

                if (!config.createNewFile()) {

                    Logger.error("Could not create file: " + config.getPath());
                    return;

                }

            } catch (IOException e) {

                Logger.error("Could not create file: " + config.getPath());
                e.printStackTrace();

            }

        }

        yml = YamlConfiguration.loadConfiguration(config);
        yml.options().copyDefaults(true);
        this.name = name;

    }

    public void reload() {
        yml = YamlConfiguration.loadConfiguration(config);
    }

    public void set(String path, Object value) {
        yml.set(path, value);
        save();
    }

    public void save() {

        try {
            yml.save(config);
        } catch (IOException e) {
            Logger.error("Could not save config: " + name + ".yml");
            e.printStackTrace();
        }

    }

    public boolean getBoolean(String path) {
        return yml.getBoolean(path);
    }

    public String getString(String path) {
        return yml.getString(path);
    }

    public int getInt(String path) {
        return yml.getInt(path);
    }

    public double getDouble(String path) {
        return yml.getDouble(path);
    }

    public long getLong(String path) {
        return yml.getLong(path);
    }

    public Object get(String path) {
        return yml.get(path);
    }

}
