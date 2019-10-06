/*
 * This file is part of SkyChanger, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2018 Daniel D. Scalzi <https://github.com/dscalzi/SkyChanger>
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
import java.util.Arrays;
import java.util.Optional;

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

import com.dscalzi.skychanger.sponge.managers.ConfigManager;
import com.dscalzi.skychanger.sponge.managers.MessageManager;
import com.dscalzi.skychanger.sponge.internal.MainExecutor;
import com.google.inject.Inject;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "skychanger")
public class SkyChangerPlugin {

    @Inject private PluginContainer plugin;
    @Inject private Logger logger;
    @Inject private Game game;
    @Inject private Metrics2 metrics;
    
    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;
    
    private static SkyChangerPlugin inst;

    public SkyChangerPlugin() {
        inst = this;
    }
    
    /**
     * Get the current instance of SkyChanger.
     * 
     * @return SkyChangerPlugin instance.
     */
    public static SkyChangerPlugin inst() {
        return inst;
    }
    
    public Logger getLogger(){
        return logger;
    }
    
    public PluginContainer getPlugin() {
        return plugin;
    }
    
    public Game getGame() {
        return game;
    }
    
    public ConfigurationLoader<CommentedConfigurationNode> getConfigLoader(){
        return configLoader;
    }
    
    public File getConfigDir() {
        return configDir;
    }
    
    public void disable() {
        game.getEventManager().unregisterPluginListeners(this);
        game.getCommandManager().getOwnedBy(this).forEach(game.getCommandManager()::removeMapping);
        game.getScheduler().getScheduledTasks(this).forEach(Task::cancel);
    }
    
    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent e){
        logger.info("Enabling " + plugin.getName() + " version " + plugin.getVersion().orElse("dev") + ".");

        ConfigManager.initialize(this);
        MessageManager.initialize(this);
        
        Sponge.getCommandManager().register(this, new MainExecutor(this), Arrays.asList("skychanger"));
    }
    
    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        metrics.addCustomChart(new Metrics2.SimplePie("used_language",
                () -> MessageManager.Languages.getByID(ConfigManager.getInstance().getLanguage()).getReadable()));
    }

    
    @Listener
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
    public void onReload(GameReloadEvent e){
        ConfigManager.reload();
        MessageManager.reload();
    }

    
}
