/*
 * This file is part of SkyChanger, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2019 Daniel D. Scalzi <https://github.com/dscalzi/SkyChanger>
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

package com.dscalzi.skychanger.bukkit;

import com.dscalzi.skychanger.bukkit.api.SkyChanger;
import com.dscalzi.skychanger.bukkit.internal.WildcardPermissionUtil;
import com.dscalzi.skychanger.bukkit.internal.wrap.BukkitOfflinePlayer;
import com.dscalzi.skychanger.bukkit.internal.wrap.BukkitPlayer;
import com.dscalzi.skychanger.bukkit.internal.wrap.BukkitWorld;
import com.dscalzi.skychanger.core.api.SkyAPI;
import com.dscalzi.skychanger.core.internal.wrap.IOfflinePlayer;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import com.dscalzi.skychanger.core.internal.wrap.IWorld;
import com.dscalzi.skychanger.core.internal.manager.IConfigManager;
import com.dscalzi.skychanger.core.internal.manager.MessageManager;
import com.dscalzi.skychanger.core.internal.util.IWildcardPermissionUtil;
import com.dscalzi.skychanger.core.internal.wrap.IPlugin;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.dscalzi.skychanger.bukkit.internal.MainExecutor;
import com.dscalzi.skychanger.bukkit.internal.managers.ConfigManager;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SkyChangerPlugin extends JavaPlugin implements IPlugin {

    private static SkyChangerPlugin inst;

    private IWildcardPermissionUtil wildcardPermissionUtil;
    private Metrics metrics;

    public SkyChangerPlugin() {
        inst = this;
    }

    @Override
    public void onEnable() {
        this.wildcardPermissionUtil = new WildcardPermissionUtil();
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

    @Override
    public SkyAPI getAPI() {
        return SkyChanger.getAPI();
    }

    @Override
    public IWildcardPermissionUtil getWildcardPermissionUtil() {
        return this.wildcardPermissionUtil;
    }

    @Override
    public void disableSelf() {
        this.getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public String getMetricsURL() {
        return "https://bstats.org/plugin/bukkit/SkyChanger";
    }

    @Override
    public String getSourceURL() {
        return "https://github.com/dscalzi/SkyChanger";
    }

    @Override
    public IWorld getWorld(String name) {
        return BukkitWorld.of(Bukkit.getWorld(name));
    }

    @Override
    public IConfigManager getConfigManager() {
        return ConfigManager.getInstance();
    }

    @Override
    public IOfflinePlayer getOfflinePlayer(UUID uuid) {
        return BukkitOfflinePlayer.of(getServer().getOfflinePlayer(uuid));
    }

    @Override
    @SuppressWarnings("deprecation")
    public IOfflinePlayer getOfflinePlayer(String name) {
        return BukkitOfflinePlayer.of(getServer().getOfflinePlayer(name));
    }

    @Override
    public List<IPlayer> getOnlinePlayers() {
        return getServer().getOnlinePlayers().stream().map(BukkitPlayer::of).collect(Collectors.toList());
    }

    @Override
    public List<IWorld> getWorlds() {
        return getServer().getWorlds().stream().map(BukkitWorld::of).collect(Collectors.toList());
    }

    @Override
    public void info(String s) {
        getLogger().info(s);
    }

    @Override
    public void warning(String s) {
        getLogger().warning(s);
    }

    @Override
    public void severe(String s) {
        getLogger().severe(s);
    }

}