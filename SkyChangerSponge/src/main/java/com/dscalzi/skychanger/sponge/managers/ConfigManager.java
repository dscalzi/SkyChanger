/*
 * SkyChanger
 * Copyright (C) 2017-2018 Daniel D. Scalzi
 * See LICENSE for license information.
 */
package com.dscalzi.skychanger.sponge.managers;

import java.io.File;
import java.io.IOException;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;

import com.dscalzi.skychanger.sponge.SkyChangerPlugin;
import com.google.inject.Inject;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class ConfigManager {

    private static boolean initialized;
    private static ConfigManager instance;

    // TODO Will be implemented in a later version
    private final double configVersion = 1.0;
    private SkyChangerPlugin plugin;
    
    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;
    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;
    private CommentedConfigurationNode config;


    private ConfigManager(SkyChangerPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        boolean res = verifyFile();
        if(res) {
            try {
                this.config = this.configManager.load();
            } catch (IOException e) {
                plugin.getLogger().error("Failed to load config.");
                e.printStackTrace();
            }
        } else {
            this.config = null;
        }
    }

    public boolean verifyFile() {
        Asset asset = plugin.getPlugin().getAsset("skychanger.conf").orElse(null);
        File file = new File(configDir, "skychanger.conf");

        if (!file.exists()) {
            if(asset != null) {
                try {
                    asset.copyToFile(file.toPath());
                    return true;
                } catch (IOException e) {
                    plugin.getLogger().error("Failed to save default config.");
                    e.printStackTrace();
                    return false;
                }
            } else {
                plugin.getLogger().error("Failed to locate default config.");
                return false;
            }
        }
        return true;
    }

    public static void initialize(SkyChangerPlugin plugin) {
        if (!initialized) {
            instance = new ConfigManager(plugin);
            initialized = true;
        }
    }

    public static boolean reload() {
        if (!initialized)
            return false;
        try {
            getInstance().loadConfig();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ConfigManager getInstance() {
        return ConfigManager.instance;
    }

    /* Configuration Accessors */

    public float getUpperLimit() {
        if(config == null) {
            return 50.0F;
        } else {
            
        }
        return Float.parseFloat(config.getNode("general_settings", "upper_limit").getString("50.0"));
    }

    public float getLowerLimit() {
        if(config == null) {
            return -50.0F;
        } else {
            
        }
        return Float.parseFloat(config.getNode("general_settings", "lower_limit").getString("-50.0"));
    }

    public String getLanguage() {
        if(config == null) {
            return "en_US";
        } else {
            
        }
        return config.getNode("general_settings", "language").getString("en_US");
    }

    public double getSystemConfigVersion() {
        return this.configVersion;
    }

    public double getConfigVersion() {
        if(config == null) {
            return getSystemConfigVersion();
        } else {
            return config.getNode("ConfigVersion").getDouble(getSystemConfigVersion());
        }
    }

}
