package com.dscalzi.skychanger.sponge;

import java.util.Arrays;
import java.util.Optional;

import org.bstats.sponge.Metrics;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
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

@Plugin(id = "skychanger")
public class SkyChangerPlugin {

    @Inject private PluginContainer plugin;
    @Inject private Logger logger;
    @Inject private Game game;
    @Inject private Metrics metrics;
    
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
    
    public void disable() {
        game.getEventManager().unregisterPluginListeners(this);
        game.getCommandManager().getOwnedBy(this).forEach(game.getCommandManager()::removeMapping);
        game.getScheduler().getScheduledTasks(this).forEach(Task::cancel);
    }
    
    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent e){
        logger.info("Enabling " + plugin.getName() + " version " + plugin.getVersion().orElse("") + ".");

        ConfigManager.initialize(this);
        MessageManager.initialize(this);
        
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("Enable or disable nightvision."))
                .arguments(GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of("args"))))
                .executor(new MainExecutor(this))
                .build(), Arrays.asList("skychanger"));
    }
    
    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        metrics.addCustomChart(new Metrics.SimplePie("used_language",
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
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to changing a specific world's sky color.")).id(plugin.getId() + ".changesky.world").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to changing the sky color of all online players.")).id(plugin.getId() + ".changesky.all").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to every part of the SkyChanger freeze and unfreeze commands.")).id(plugin.getId() + ".freeze").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to freeze/unfreeze yourself.")).id(plugin.getId() + ".freeze.self").register();
                opdb.assign(PermissionDescription.ROLE_USER, true).description(Text.of("Access to freeze/unfreeze a specific person.")).id(plugin.getId() + ".freeze.others").register();
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
