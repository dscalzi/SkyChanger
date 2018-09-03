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

package com.dscalzi.skychanger.sponge.internal;

import java.util.Optional;
import java.util.regex.Pattern;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import com.dscalzi.skychanger.sponge.SkyChangerPlugin;
import com.dscalzi.skychanger.sponge.api.SkyAPI;
import com.dscalzi.skychanger.sponge.api.SkyChanger;
import com.dscalzi.skychanger.sponge.managers.ConfigManager;
import com.dscalzi.skychanger.sponge.managers.MessageManager;

public class MainExecutor implements CommandExecutor {

    private static final Pattern packetNum = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
    private MessageManager mm;
    private SkyChangerPlugin plugin;
    
    public MainExecutor(SkyChangerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public CommandResult execute(CommandSource src, CommandContext cc) throws CommandException {
        final String argStr = ((String)cc.getOne(Text.of("args")).orElse(null));
        final String[] args = argStr != null ? argStr.split(" ") : new String[0];
        this.mm = MessageManager.getInstance();
        
        if (args.length > 0) {
            if (packetNum.matcher(args[0]).matches()) {
                this.cmdChangeSky(src, args);
                return CommandResult.success();
            }

            if (args[0].equalsIgnoreCase("help")) {
                mm.helpMessage(src);
                return CommandResult.success();
            }

            if (args[0].equalsIgnoreCase("freeze")) {
                this.cmdFreeze((Player) src, false, args);
                return CommandResult.success();
            }

            if (args[0].equalsIgnoreCase("unfreeze")) {
                this.cmdFreeze((Player) src, true, args);
                return CommandResult.success();
            }

            if (args[0].equalsIgnoreCase("version")) {
                this.cmdVersion(src);
                return CommandResult.success();
            }

            if (args[0].equalsIgnoreCase("reload")) {
                this.cmdReload(src);
                return CommandResult.success();
            }
        }

        mm.helpMessage(src);
        return CommandResult.success();
    }
    
    private void cmdChangeSky(CommandSource sender, String[] args) {
        final String basePerm = "skychanger.changesky";
        boolean s = sender.hasPermission(basePerm + ".self");
        boolean o = sender.hasPermission(basePerm + ".others");
        boolean a = sender.hasPermission(basePerm + ".all");
        boolean w = WorldPermissionUtil.hasGeneralChangeskyPerm(sender);
        if (!s && !o && !a && !w) {
            mm.noPermission(sender);
            return;
        }
        float pN;
        try {
            pN = Float.parseFloat(args[0]);
        } catch (NumberFormatException e) {
            mm.floatingPointOverflow(sender, args[0]);
            return;
        }
        if (!sender.hasPermission("skychanger.bypasslimit")) {
            float upper = ConfigManager.getInstance().getUpperLimit();
            float lower = ConfigManager.getInstance().getLowerLimit();
            if (pN > upper) {
                mm.outOfBoundsUpper(sender, upper);
                return;
            }
            if (lower > pN) {
                mm.outOfBoundsLower(sender, lower);
                return;
            }
        }
        final SkyAPI api = SkyChanger.getAPI();
        if (args.length > 1) {
            // Check if requested for all
            if (args[1].equalsIgnoreCase("-a")) {
                if (!a) {
                    mm.noPermission(sender);
                    return;
                }
                for (Player p : plugin.getGame().getServer().getOnlinePlayers()) {
                    api.changeSky(p, pN);
                }
                mm.packetSent(sender, "-a (" + mm.getString("message.everyone") + ")");
                return;
            }
            // Check if requested for world
            if (args[1].equalsIgnoreCase("-w")) {
                World t = null;
                if (args.length > 2) {
                    Optional<World> tOpt = plugin.getGame().getServer().getWorld(args[2]);
                    if (!tOpt.isPresent()) {
                        mm.worldDoesntExist(sender, args[2]);
                        return;
                    }
                    t = tOpt.get();
                } else {
                    if (!(sender instanceof Player)) {
                        mm.mustSpecifyWorld(sender);
                        return;
                    }
                    t = ((Player) sender).getWorld();
                    if (!WorldPermissionUtil.hasChangeskyPerm(sender, t)) {
                        mm.noPermission(sender);
                        return;
                    }
                }
                for (Player p : t.getPlayers()) {
                    api.changeSky(p, pN);
                }
                mm.packetSent(sender, mm.getString("message.allPlayersIn") + " " + t.getName());
                return;
            }
            // Check if param is a player
            if (!o) {
                mm.noPermission(sender);
                return;
            }
            Optional<Player> targetOpt;
            try {
                targetOpt = plugin.getGame().getServer().getPlayer(MessageManager.formatFromInput(args[1]));
            } catch (IllegalArgumentException e) {
                targetOpt = plugin.getGame().getServer().getPlayer(args[1]);
            }
            if (!targetOpt.isPresent() || !targetOpt.get().isOnline()) {
                mm.playerNotFound(sender, !targetOpt.isPresent() || targetOpt.get().getName() == null ? args[1] : targetOpt.get().getName());
                return;
            }
            // If a player specified their own name, we run the command as if the player
            // param was not
            // given. The others permission therefore includes the self.
            Player target = targetOpt.get();
            if (!(sender instanceof Player) || !target.getUniqueId().equals(((Player) sender).getUniqueId())) {
                if (api.changeSky(target, pN))
                    mm.packetSent(sender, target.getName());
                else
                    mm.packetError(sender, target.getName());
                return;
            }
        }

        if (!(sender instanceof Player)) {
            MessageManager.getInstance().denyNonPlayer(sender);
            return;
        }

        if (api.changeSky((Player) sender, pN))
            mm.packetSent(sender);
        else
            mm.packetError(sender);
    }

    private void cmdFreeze(CommandSource sender, boolean unfreeze, String[] args) {
        final String basePerm = "skychanger.freeze";
        boolean s = sender.hasPermission(basePerm + ".self");
        boolean o = sender.hasPermission(basePerm + ".others");
        boolean a = sender.hasPermission(basePerm + ".all");
        boolean w = WorldPermissionUtil.hasGeneralFreezePerm(sender);
        if (!s && !o && !a && !w) {
            mm.noPermission(sender);
            return;
        }
        final SkyAPI api = SkyChanger.getAPI();
        if (args.length > 1) {
            // Check if requested for all
            if (args[1].equalsIgnoreCase("-a")) {
                if (!a) {
                    mm.noPermission(sender);
                    return;
                }
                for (Player p : plugin.getGame().getServer().getOnlinePlayers()) {
                    if (unfreeze)
                        api.unfreeze(p);
                    else
                        api.freeze(p);
                }
                if (unfreeze)
                    mm.packetUnfreeze(sender, "-a (" + mm.getString("message.everyone") + ")");
                else
                    mm.packetSent(sender, "@a (" + mm.getString("message.everyone") + ")");
                return;
            }
            // Check if requested for world
            if (args[1].equalsIgnoreCase("-w")) {
                World t = null;
                if (args.length > 2) {
                    Optional<World> tOpt = plugin.getGame().getServer().getWorld(args[2]);
                    if (!tOpt.isPresent()) {
                        mm.worldDoesntExist(sender, args[2]);
                        return;
                    }
                    t = tOpt.get();
                } else {
                    if (!(sender instanceof Player)) {
                        mm.mustSpecifyWorld(sender);
                        return;
                    }
                    t = ((Player) sender).getWorld();
                    if (!WorldPermissionUtil.hasFreezePerm(sender, t)) {
                        mm.noPermission(sender);
                        return;
                    }
                }
                for (Player p : t.getPlayers()) {
                    if (unfreeze)
                        api.unfreeze(p);
                    else
                        api.freeze(p);
                }
                if (unfreeze)
                    mm.packetUnfreeze(sender, mm.getString("message.allPlayersIn") + " " + t.getName());
                else
                    mm.packetSent(sender, mm.getString("message.allPlayersIn") + " " + t.getName());
                return;
            }
            // Check if param is a player
            if (!o) {
                mm.noPermission(sender);
                return;
            }
            Optional<Player> targetOpt;
            try {
                targetOpt = plugin.getGame().getServer().getPlayer(MessageManager.formatFromInput(args[1]));
            } catch (IllegalArgumentException e) {
                targetOpt = plugin.getGame().getServer().getPlayer(args[1]);
            }
            if (!targetOpt.isPresent() || !targetOpt.get().isOnline()) {
                mm.playerNotFound(sender, !targetOpt.isPresent() || targetOpt.get().getName() == null ? args[1] : targetOpt.get().getName());
                return;
            }
            // If a player specified their own name, we run the command as if the player
            // param was not
            // given. The others permission therefore includes the self.
            Player target = targetOpt.get();
            if (!(sender instanceof Player) || !target.getUniqueId().equals(((Player) sender).getUniqueId())) {
                if ((!unfreeze && api.freeze(target))
                        || (unfreeze && api.unfreeze(target)))
                    if (unfreeze)
                        mm.packetUnfreeze(sender, target.getName());
                    else
                        mm.packetSent(sender, target.getName());
                else
                    mm.packetError(sender, target.getName());
                return;
            }
        }

        if (!(sender instanceof Player)) {
            MessageManager.getInstance().denyNonPlayer(sender);
            return;
        }

        Player p = (Player) sender;
        if ((!unfreeze && api.freeze(p)) || (unfreeze && api.unfreeze(p)))
            if (unfreeze)
                mm.packetUnfreeze(sender);
            else
                mm.packetSent(sender);
        else
            mm.packetError(sender);
    }

    private void cmdReload(CommandSource sender) {
        if (!sender.hasPermission("skychanger.reload")) {
            mm.noPermission(sender);
            return;
        }
        if (ConfigManager.reload()) {
            MessageManager.reload();
            mm.reloadSuccessful(sender);
        } else
            mm.reloadFailed(sender);
    }

    private void cmdVersion(CommandSource sender) {
        mm.versionMessage(sender);
    }

}
