package com.harriswebstudio.router.config;

import com.harriswebstudio.router.config.type.JsonConfiguration;
import com.harriswebstudio.router.config.type.YamlConfiguration;

import java.io.File;

public class ConfigHandler {

    public static File CONFIG_DIR = new File("config");

    public static <T extends AbstractConfiguration> T loadConfig(T config) {
        if (!ConfigHandler.CONFIG_DIR.exists())
            ConfigHandler.CONFIG_DIR.mkdirs();

        return loadConfig(ConfigHandler.CONFIG_DIR, config);
    }

    public static <T extends AbstractConfiguration> T loadConfig(File directory, T config) {
        if (config instanceof YamlConfiguration)
            return new YamlConfiguration(config.getFileName()).load(directory, config);

        return new JsonConfiguration(config.getFileName()).load(directory, config);
    }

}