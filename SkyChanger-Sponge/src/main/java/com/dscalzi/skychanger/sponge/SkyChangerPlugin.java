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

package com.dscalzi.skychanger.sponge;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import com.dscalzi.skychanger.core.api.SkyAPI;
import com.dscalzi.skychanger.core.internal.manager.IConfigManager;
import com.dscalzi.skychanger.core.internal.manager.MessageManager;
import com.dscalzi.skychanger.core.internal.util.IWildcardPermissionUtil;
import com.dscalzi.skychanger.core.internal.wrap.IOfflinePlayer;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import com.dscalzi.skychanger.core.internal.wrap.IPlugin;
import com.dscalzi.skychanger.core.internal.wrap.IWorld;
import com.dscalzi.skychanger.sponge.api.SkyChanger;
import com.dscalzi.skychanger.sponge.internal.WildcardPermissionUtil;
import com.dscalzi.skychanger.sponge.internal.wrap.SpongeOfflinePlayer;
import com.dscalzi.skychanger.sponge.internal.wrap.SpongePlayer;
import com.dscalzi.skychanger.sponge.internal.wrap.SpongeWorld;
import org.bstats.sponge.Metrics2;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import com.dscalzi.skychanger.sponge.internal.MainExecutor;
import com.dscalzi.skychanger.sponge.internal.managers.ConfigManager;
import com.google.inject.Inject;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "skychanger")
public class SkyChangerPlugin implements IPlugin {

    @Inject private PluginContainer plugin;
    @Inject private Logger logger;
    @Inject private Game game;
    
    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;
    
    private static SkyChangerPlugin inst;

    private WildcardPermissionUtil wildcardPermissionUtil;

    private Metrics2 metrics;

    @Inject
    public SkyChangerPlugin(Metrics2.Factory metricsFactory) {
        inst = this;
        metrics = metricsFactory.make(3228);
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
    public void disableSelf() {
        this.disable();
    }

    @Override
    public String getName() {
        return plugin.getName();
    }

    @Override
    public String getVersion() {
        return plugin.getVersion().orElse("dev");
    }

    @Override
    public String getMetricsURL() {
        return "https://bstats.org/plugin/sponge/SkyChanger/3228";
    }

    @Override
    public String getSourceURL() {
        return "https://github.com/dscalzi/SkyChanger";
    }

    @Override
    public void info(String s) {
        logger.info(s);
    }

    @Override
    public void warning(String s) {
        logger.warn(s);
    }

    @Override
    public void severe(String s) {
        logger.error(s);
    }

    @Override
    public IWorld getWorld(String name) {
        return this.game.getServer().getWorld(name).map(SpongeWorld::of).orElse(null);
    }

    @Override
    public IConfigManager getConfigManager() {
        return ConfigManager.getInstance();
    }

    @Override
    public IWildcardPermissionUtil getWildcardPermissionUtil() {
        return this.wildcardPermissionUtil;
    }

    @Override
    public IOfflinePlayer getOfflinePlayer(UUID uuid) {
        return this.game.getServer().getPlayer(uuid).map(SpongeOfflinePlayer::of).orElse(null);
    }

    @Override
    public IOfflinePlayer getOfflinePlayer(String name) {
        return this.game.getServer().getPlayer(name).map(SpongeOfflinePlayer::of).orElse(null);
    }

    @Override
    public List<IPlayer> getOnlinePlayers() {
        return this.game.getServer().getOnlinePlayers().stream().map(SpongePlayer::of).collect(Collectors.toList());
    }

    @Override
    public List<IWorld> getWorlds() {
        return this.game.getServer().getWorlds().stream().map(SpongeWorld::of).collect(Collectors.toList());
    }

    @Override
    public SkyAPI getAPI() {
        return SkyChanger.getAPI();
    }

    public PluginContainer getPlugin() {
        return plugin;
    }
    
    public ConfigurationLoader<CommentedConfigurationNode> getConfigLoader(){
        return configLoader;
    }
    
    public File getConfigDir() {
        return configDir;
    }
    
    private void disable() {
        game.getEventManager().unregisterPluginListeners(this);
        game.getCommandManager().getOwnedBy(this).forEach(game.getCommandManager()::removeMapping);
        game.getScheduler().getScheduledTasks(this).forEach(Task::cancel);
    }
    
    @Listener
    @SuppressWarnings("unused")
    public void onGamePreInitialization(GamePreInitializationEvent e){
        logger.info("Enabling " + plugin.getName() + " version " + plugin.getVersion().orElse("dev") + ".");

        this.wildcardPermissionUtil = new WildcardPermissionUtil();

        ConfigManager.initialize(this);
        MessageManager.initialize(this);
        
        Sponge.getCommandManager().register(this, new MainExecutor(this), Collections.singletonList("skychanger"));
    }
    
    @Listener
    @SuppressWarnings("unused")
    public void onServerStart(GameStartedServerEvent event) {
        metrics.addCustomChart(new Metrics2.SimplePie("used_language",
                () -> MessageManager.Languages.getByID(ConfigManager.getInstance().getLanguage()).getReadable()));
    }

    
    @Listener
    @SuppressWarnings("unused")
    public void onPostInit(GamePostInitializationEvent event) {
        Optional<PermissionService> ops = Sponge.getServiceManager().provide(PermissionService.class);
        if (ops.isPresent()) {
            Builder opdb = ops.get().newDescriptionBuilder(this);
            if (opdb != null) {
                opdb.assign(PermissionDescription.ROLE_ADMIN, true).description(Text.of("Access to all SkyChanger commands.")).id(plugin.getId()).register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to every part of the main SkyChanger command.")).id(plugin.getId() + ".changesky").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to change your personal sky color.")).id(plugin.getId() + ".changesky.self").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to changing a specific person's sky color.")).id(plugin.getId() + ".changesky.others").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to change the sky color for players within a radius.")).id(plugin.getId() + ".changesky.radius").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to changing a specific world's sky color.")).id(plugin.getId() + ".changesky.world").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to changing the sky color of all online players.")).id(plugin.getId() + ".changesky.all").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to every part of the SkyChanger freeze and unfreeze commands.")).id(plugin.getId() + ".freeze").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to freeze/unfreeze yourself.")).id(plugin.getId() + ".freeze.self").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to freeze/unfreeze a specific person.")).id(plugin.getId() + ".freeze.others").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to freeze/unfreeze a players within a radius.")).id(plugin.getId() + ".freeze.radius").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to freeze/unfreeze a specific world.")).id(plugin.getId() + ".freeze.world").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to freeze/unfreeze all online players.")).id(plugin.getId() + ".freeze.all").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Bypass the packet range limits set in the config.yml.")).id(plugin.getId() + ".bypasslimit").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to reload the configuration.")).id(plugin.getId() + ".reload").register();
            }
        }
    }

    
    @Listener
    @SuppressWarnings("unused")
    public void onReload(GameReloadEvent e){
        ConfigManager.reloadStatic();
        MessageManager.reload();
    }

    
}
