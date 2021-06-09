/*
 * This file is part of SkyChanger, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2021 Daniel D. Scalzi <https://github.com/dscalzi/SkyChanger>
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

import com.dscalzi.skychanger.core.api.SkyAPI;
import com.dscalzi.skychanger.core.internal.manager.IConfigManager;
import com.dscalzi.skychanger.core.internal.manager.MessageManager;
import com.dscalzi.skychanger.core.internal.util.IWildcardPermissionUtil;
import com.dscalzi.skychanger.core.internal.wrap.IOfflinePlayer;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import com.dscalzi.skychanger.core.internal.wrap.IPlugin;
import com.dscalzi.skychanger.core.internal.wrap.IWorld;
import com.dscalzi.skychanger.sponge.api.SkyChanger;
import com.dscalzi.skychanger.sponge.internal.MainExecutor;
import com.dscalzi.skychanger.sponge.internal.WildcardPermissionUtil;
import com.dscalzi.skychanger.sponge.internal.managers.ConfigManager;
import com.dscalzi.skychanger.sponge.internal.wrap.SpongeOfflinePlayer;
import com.dscalzi.skychanger.sponge.internal.wrap.SpongePlayer;
import com.dscalzi.skychanger.sponge.internal.wrap.SpongeWorld;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.bstats.charts.SimplePie;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Plugin("skychanger")
public class SkyChangerPlugin implements IPlugin {

    private final PluginContainer plugin;
    private final Logger logger;
    private final Game game;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;
    
    private static SkyChangerPlugin inst;

    private WildcardPermissionUtil wildcardPermissionUtil;

//    private final Metrics metrics;

    @Inject
    public SkyChangerPlugin(
            final PluginContainer container,
            final Logger logger,
            final Game game//,
//            Metrics.Factory metricsFactory
    ) {
        this.plugin = container;
        this.logger = logger;
        this.game = game;
        inst = this;
//        metrics = metricsFactory.make(3228);
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
        return plugin.metadata().name().orElseThrow();
    }

    @Override
    public String getVersion() {
        return plugin.metadata().version();
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
        return this.game.server().worldManager().world(ResourceKey.resolve(name)).map(SpongeWorld::of).orElse(null);
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
        return this.game.server().userManager().find(GameProfile.of(uuid)).map(SpongeOfflinePlayer::of).orElse(null);
    }

    @Override
    public IOfflinePlayer getOfflinePlayer(String name) {
        return this.game.server().userManager().find(name).map(SpongeOfflinePlayer::of).orElse(null);
    }

    @Override
    public List<IPlayer> getOnlinePlayers() {
        return this.game.server().onlinePlayers().stream().map(SpongePlayer::of).collect(Collectors.toList());
    }

    @Override
    public List<IWorld> getWorlds() {
        return this.game.server().worldManager().worlds().stream().map(SpongeWorld::of).collect(Collectors.toList());
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
    
    public Path getConfigDir() {
        return configDir;
    }

    // We "disable" the plugin by not initializing it.
    boolean markedForDisable = false;
    private void disable() {
        // No-op
        this.markedForDisable = true;
//        game.eventManager().unregisterPluginListeners(this.plugin);
//        game.server().scheduler().tasks(this.plugin).forEach(ScheduledTask::cancel);
    }
    
    @Listener
    @SuppressWarnings("unused")
    public void onConstructPlugin(final ConstructPluginEvent event){
        logger.info("Enabling " + plugin.metadata().name().orElseThrow() + " version " + plugin.metadata().version() + ".");

        this.wildcardPermissionUtil = new WildcardPermissionUtil();

        ConfigManager.initialize(this);
        MessageManager.initialize(this);
    }

    @Listener
    @SuppressWarnings("unused")
    public void onRegisterCommands(final RegisterCommandEvent<Command.Raw> event) {
        if(!this.markedForDisable) {
            event.register(this.plugin, new MainExecutor(this), "skychanger");
        }
    }

//    @Listener
//    @SuppressWarnings("unused")
//    public void onServerStarting(final StartingEngineEvent<Server> event) {
//        metrics.addCustomChart(new SimplePie("used_language",
//                () -> MessageManager.Languages.getByID(ConfigManager.getInstance().getLanguage()).getReadable()));
//    }
    
    @Listener
    @SuppressWarnings("unused")
    public void onPostInit(final StartedEngineEvent<Server> event) {
        Optional<PermissionService> ops = Sponge.serviceProvider().provide(PermissionService.class);
        if (ops.isPresent()) {
            Builder opdb = ops.get().newDescriptionBuilder(this.plugin);
            if (opdb != null) {
                opdb.assign(PermissionDescription.ROLE_ADMIN, true).description(Component.text("Access to all SkyChanger commands.")).id(plugin.metadata().id()).register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to every part of the main SkyChanger command.")).id(plugin.metadata().id() + ".changesky").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to change your personal sky color.")).id(plugin.metadata().id() + ".changesky.self").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to changing a specific person's sky color.")).id(plugin.metadata().id() + ".changesky.others").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to change the sky color for players within a radius.")).id(plugin.metadata().id() + ".changesky.radius").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to changing a specific world's sky color.")).id(plugin.metadata().id() + ".changesky.world").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to changing the sky color of all online players.")).id(plugin.metadata().id() + ".changesky.all").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to every part of the SkyChanger freeze and unfreeze commands.")).id(plugin.metadata().id() + ".freeze").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to freeze/unfreeze yourself.")).id(plugin.metadata().id() + ".freeze.self").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to freeze/unfreeze a specific person.")).id(plugin.metadata().id() + ".freeze.others").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to freeze/unfreeze a players within a radius.")).id(plugin.metadata().id() + ".freeze.radius").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to freeze/unfreeze a specific world.")).id(plugin.metadata().id() + ".freeze.world").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to freeze/unfreeze all online players.")).id(plugin.metadata().id() + ".freeze.all").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Bypass the packet range limits set in the config.yml.")).id(plugin.metadata().id() + ".bypasslimit").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Component.text("Access to reload the configuration.")).id(plugin.metadata().id() + ".reload").register();
            }
        }
    }

    
    @Listener
    @SuppressWarnings("unused")
    public void onReload(RefreshGameEvent e){
        ConfigManager.reloadStatic();
        MessageManager.reload();
    }

}
