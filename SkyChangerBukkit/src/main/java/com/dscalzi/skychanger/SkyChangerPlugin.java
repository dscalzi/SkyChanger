/*
 * SkyChanger
 * Copyright (C) 2017-2018 Daniel D. Scalzi
 * See LICENSE for license information.
 */
package com.dscalzi.skychanger;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import com.dscalzi.skychanger.internal.MainExecutor;
import com.dscalzi.skychanger.managers.ConfigManager;
import com.dscalzi.skychanger.managers.MessageManager;

public class SkyChangerPlugin extends JavaPlugin {

    private static SkyChangerPlugin inst;

    private Metrics metrics;

    public SkyChangerPlugin() {
        inst = this;
    }

    @Override
    public void onEnable() {
        ConfigManager.initialize(this);
        MessageManager.initialize(this);
        this.getCommand("skychanger").setExecutor(new MainExecutor(this));
        metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("used_language",
                () -> MessageManager.Languages.getByID(ConfigManager.getInstance().getLanguage()).getReadable()));
    }

    /**
     * Get the current instance of SkyChanger.
     * 
     * @return SkyChangerPlugin instance.
     */
    public static SkyChangerPlugin inst() {
        return inst;
    }

}