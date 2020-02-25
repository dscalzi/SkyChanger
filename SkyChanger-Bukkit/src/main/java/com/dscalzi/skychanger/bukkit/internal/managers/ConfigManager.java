/*
 * This file is part of SkyChanger, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2020 Daniel D. Scalzi <https://github.com/dscalzi/SkyChanger>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dscalzi.skychanger.bukkit.internal.managers;

import java.io.File;

import com.dscalzi.skychanger.core.internal.manager.IConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

import com.dscalzi.skychanger.bukkit.SkyChangerPlugin;

public class ConfigManager implements IConfigManager {

    private static boolean initialized;
    private static ConfigManager instance;

    // TODO Will be implemented in a later version
    private final double configVersion = 1.0;
    private SkyChangerPlugin plugin;
    private FileConfiguration config;

    private ConfigManager(SkyChangerPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        verifyFile();
        this.plugin.reloadConfig();
        this.config = this.plugin.getConfig();
    }

    public void verifyFile() {
        File file = new File(this.plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            this.plugin.saveDefaultConfig();
        }
    }

    public static void initialize(SkyChangerPlugin plugin) {
        if (!initialized) {
            instance = new ConfigManager(plugin);
            initialized = true;
        }
    }

    public static boolean reloadStatic() {
        if (!initialized)
            return false;
        return getInstance().reload();
    }

    @Override
    public boolean reload() {
        try {
            loadConfig();
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

    @Override
    public float getUpperLimit() {
        return Float.parseFloat(config.getString("general_settings.upper_limit", "50.0"));
    }

    @Override
    public float getLowerLimit() {
        return Float.parseFloat(config.getString("general_settings.lower_limit", "-50.0"));
    }

    @Override
    public String getLanguage() {
        return config.getString("general_settings.language", "en_US");
    }

    @Override
    public double getSystemConfigVersion() {
        return this.configVersion;
    }

    @Override
    public double getConfigVersion() {
        return config.getDouble("ConfigVersion", getSystemConfigVersion());
    }

}
