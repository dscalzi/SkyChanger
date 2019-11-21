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

package com.dscalzi.skychanger.core.internal.command;

import com.dscalzi.skychanger.core.api.SkyAPI;
import com.dscalzi.skychanger.core.api.SkyPacket;
import com.dscalzi.skychanger.core.internal.wrap.ICommandSender;
import com.dscalzi.skychanger.core.internal.wrap.IOfflinePlayer;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import com.dscalzi.skychanger.core.internal.manager.MessageManager;
import com.dscalzi.skychanger.core.internal.util.IWildcardPermissionUtil;
import com.dscalzi.skychanger.core.internal.wrap.ICommandBlock;
import com.dscalzi.skychanger.core.internal.wrap.ILocation;
import com.dscalzi.skychanger.core.internal.wrap.IPlugin;
import com.dscalzi.skychanger.core.internal.wrap.IWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommandAdapter {

    private static final Pattern packetNum = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
    private MessageManager mm;

    private IPlugin plugin;

    public CommandAdapter(IPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean resolve(ICommandSender sender, String[] args) {

        this.mm = MessageManager.getInstance();

        if (args.length > 0) {
            if (packetNum.matcher(args[0]).matches()) {
                this.cmdChangeSky(sender, SkyPacket.FADE_VALUE, args);
                return true;
            }

            if (args[0].equalsIgnoreCase("tweak")) {
                this.cmdChangeSky(sender, SkyPacket.FADE_TIME, args);
                return true;
            }

            if (args[0].equalsIgnoreCase("help")) {
                mm.helpMessage(sender);
                return true;
            }

            if (args[0].equalsIgnoreCase("freeze")) {
                this.cmdFreeze(sender, false, args);
                return true;
            }

            if (args[0].equalsIgnoreCase("unfreeze")) {
                this.cmdFreeze(sender, true, args);
                return true;
            }

            if (args[0].equalsIgnoreCase("version")) {
                this.cmdVersion(sender);
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                this.cmdReload(sender);
                return true;
            }
        }

        mm.helpMessage(sender);
        return false;
    }

    @SuppressWarnings("deprecation")
    private void cmdChangeSky(ICommandSender sender, SkyPacket packet, String[] args) {
        final IWildcardPermissionUtil wpu = plugin.getWildcardPermissionUtil();
        final String basePerm = "skychanger.changesky";
        boolean s = sender.hasPermission(basePerm + ".self");
        boolean o = sender.hasPermission(basePerm + ".others");
        boolean a = sender.hasPermission(basePerm + ".all");
        boolean w = wpu.hasGeneralChangeskyWorldPerm(sender);
        boolean r = wpu.hasGeneralChangeskyRadiusPerm(sender);
        if (!s && !o && !a && !w && !r) {
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
            float upper = plugin.getConfigManager().getUpperLimit();
            float lower = plugin.getConfigManager().getLowerLimit();
            if (pN > upper) {
                mm.outOfBoundsUpper(sender, upper);
                return;
            }
            if (lower > pN) {
                mm.outOfBoundsLower(sender, lower);
                return;
            }
        }
        final SkyAPI api = plugin.getAPI();
        if (args.length > 1) {
            // Check if requested for all
            if (args[1].equalsIgnoreCase("-a")) {
                if (!a) {
                    mm.noPermission(sender);
                    return;
                }
                for (IPlayer p : plugin.getOnlinePlayers()) {
                    api.changeSky(p, packet, pN);
                }
                mm.packetSent(sender, "-a (" + mm.getString("message.everyone") + ")");
                return;
            }
            // Check if requested for world
            if (args[1].equalsIgnoreCase("-w")) {
                IWorld t = null;
                if (args.length > 2) {
                    t = plugin.getWorld(args[2]);
                    if (t == null) {
                        mm.worldDoesntExist(sender, args[2]);
                        return;
                    }
                } else {
                    if (!(sender.isPlayer())) {
                        mm.mustSpecifyWorld(sender);
                        return;
                    }
                    t = ((IPlayer) sender).getWorld();
                }
                if (!wpu.hasChangeskyWorldPerm(sender, t)) {
                    mm.noPermission(sender);
                    return;
                }
                for (IPlayer p : t.getPlayers()) {
                    api.changeSky(p, packet, pN);
                }
                mm.packetSent(sender, mm.getString("message.allPlayersIn") + " " + t.getName());
                return;
            }
            // Check if requested for radius
            if (args[1].equalsIgnoreCase("-r")) {
                if (sender.isConsole()) {
                    MessageManager.getInstance().denyNonPlayer(sender);
                    return;
                }
                if(args.length > 2) {
                    double radius;
                    double radiusSq;
                    try {
                        radius = Double.parseDouble(args[2]);

                        if (!wpu.hasChangeskyRadiusPerm(sender, radius)) {
                            mm.noPermission(sender);
                            return;
                        }

                        radiusSq = Math.pow(radius, 2);
                    } catch (NumberFormatException e) {
                        MessageManager.getInstance().radiusFormatError(sender);
                        return;
                    }
                    ILocation origin;
                    if (sender.isPlayer()) {
                        origin = ((IPlayer)sender).getLocation();
                    } else if (sender.isCommandBlock()) {
                        origin = ((ICommandBlock)sender).getLocation();
                    } else {
                        MessageManager.getInstance().denyNonPlayer(sender);
                        return;
                    }
                    for(IPlayer p : origin.getWorld().getPlayers()) {
                        if(Math.abs(origin.distanceSquared(p.getLocation())) <= radiusSq) {
                            api.changeSky(p, packet, pN);
                        }
                    }
                    mm.packetSent(sender, mm.getString("message.allPlayersInRadius") + " " + args[2]);
                    return;
                } else {
                    mm.mustSpecifyRadius(sender);
                    return;
                }
            }
            // Check if param is a player
            if (!o) {
                mm.noPermission(sender);
                return;
            }
            IOfflinePlayer target;
            try {
                target = plugin.getOfflinePlayer(MessageManager.formatFromInput(args[1]));
            } catch (IllegalArgumentException e) {
                target = plugin.getOfflinePlayer(args[1]);
            }
            if (target == null || !target.isOnline()) {
                mm.playerNotFound(sender, target == null || target.getName() == null ? args[1] : target.getName());
                return;
            }
            // If a player specified their own name, we run the command as if the player
            // param was not
            // given. The others permission therefore includes the self.
            if (!(sender.isPlayer()) || !target.getUniqueId().equals(((IPlayer) sender).getUniqueId())) {
                if (api.changeSky(target.getPlayer(), packet, pN))
                    mm.packetSent(sender, target.getName());
                else
                    mm.packetError(sender, target.getName());
                return;
            }
        }

        if (!(sender.isPlayer())) {
            MessageManager.getInstance().denyNonPlayer(sender);
            return;
        }

        if (api.changeSky((IPlayer) sender, packet, pN))
            mm.packetSent(sender);
        else
            mm.packetError(sender);
    }

    @SuppressWarnings("deprecation")
    private void cmdFreeze(ICommandSender sender, boolean unfreeze, String[] args) {
        final IWildcardPermissionUtil wpu = plugin.getWildcardPermissionUtil();
        final String basePerm = "skychanger.freeze";
        boolean s = sender.hasPermission(basePerm + ".self");
        boolean o = sender.hasPermission(basePerm + ".others");
        boolean a = sender.hasPermission(basePerm + ".all");
        boolean w = wpu.hasGeneralFreezeWorldPerm(sender);
        boolean r = wpu.hasGeneralFreezeRadiusPerm(sender);
        if (!s && !o && !a && !w && !r) {
            mm.noPermission(sender);
            return;
        }
        final SkyAPI api = plugin.getAPI();
        if (args.length > 1) {
            // Check if requested for all
            if (args[1].equalsIgnoreCase("-a")) {
                if (!a) {
                    mm.noPermission(sender);
                    return;
                }
                for (IPlayer p : plugin.getOnlinePlayers()) {
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
                IWorld t;
                if (args.length > 2) {
                    t = plugin.getWorld(args[2]);
                    if (t == null) {
                        mm.worldDoesntExist(sender, args[2]);
                        return;
                    }
                } else {
                    if (!(sender.isPlayer())) {
                        mm.mustSpecifyWorld(sender);
                        return;
                    }
                    t = ((IPlayer) sender).getWorld();
                }
                if (!wpu.hasFreezeWorldPerm(sender, t)) {
                    mm.noPermission(sender);
                    return;
                }
                for (IPlayer p : t.getPlayers()) {
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
            // Check if requested for radius
            if (args[1].equalsIgnoreCase("-r")) {
                if (sender.isConsole()) {
                    MessageManager.getInstance().denyNonPlayer(sender);
                    return;
                }
                if(args.length > 2) {
                    double radius;
                    double radiusSq;
                    try {
                        radius = Double.parseDouble(args[2]);

                        if (!wpu.hasFreezeRadiusPerm(sender, radius)) {
                            mm.noPermission(sender);
                            return;
                        }

                        radiusSq = Math.pow(radius, 2);
                    } catch (NumberFormatException e) {
                        MessageManager.getInstance().radiusFormatError(sender);
                        return;
                    }
                    ILocation origin;
                    if (sender.isPlayer()) {
                        origin = ((IPlayer)sender).getLocation();
                    } else if (sender.isCommandBlock()) {
                        origin = ((ICommandBlock)sender).getLocation();
                    } else {
                        MessageManager.getInstance().denyNonPlayer(sender);
                        return;
                    }
                    for(IPlayer p : origin.getWorld().getPlayers()) {
                        if(Math.abs(origin.distanceSquared(p.getLocation())) <= radiusSq) {
                            if (unfreeze)
                                api.unfreeze(p);
                            else
                                api.freeze(p);
                        }
                    }
                    if (unfreeze)
                        mm.packetUnfreeze(sender, mm.getString("message.allPlayersInRadius") + " " + args[2]);
                    else
                        mm.packetSent(sender, mm.getString("message.allPlayersInRadius") + " " + args[2]);
                    return;
                } else {
                    mm.mustSpecifyRadius(sender);
                    return;
                }
            }
            // Check if param is a player
            if (!o) {
                mm.noPermission(sender);
                return;
            }
            IOfflinePlayer target;
            try {
                target = plugin.getOfflinePlayer(MessageManager.formatFromInput(args[1]));
            } catch (IllegalArgumentException e) {
                target = plugin.getOfflinePlayer(args[1]);
            }
            if (target == null || !target.isOnline()) {
                mm.playerNotFound(sender, target == null || target.getName() == null ? args[1] : target.getName());
                return;
            }
            // If a player specified their own name, we run the command as if the player
            // param was not
            // given. The others permission therefore includes the self.
            if (!(sender.isPlayer()) || !target.getUniqueId().equals(((IPlayer) sender).getUniqueId())) {
                if ((!unfreeze && api.freeze(target.getPlayer()))
                        || (unfreeze && target.getPlayer().teleport(target.getPlayer().getLocation())))
                    if (unfreeze)
                        mm.packetUnfreeze(sender, target.getName());
                    else
                        mm.packetSent(sender, target.getName());
                else
                    mm.packetError(sender, target.getName());
                return;
            }
        }

        if (!(sender.isPlayer())) {
            MessageManager.getInstance().denyNonPlayer(sender);
            return;
        }

        IPlayer p = (IPlayer) sender;
        if ((!unfreeze && api.freeze(p)) || (unfreeze && api.unfreeze(p)))
            if (unfreeze)
                mm.packetUnfreeze(sender);
            else
                mm.packetSent(sender);
        else
            mm.packetError(sender);
    }

    private void cmdReload(ICommandSender sender) {
        if (!sender.hasPermission("skychanger.reload")) {
            mm.noPermission(sender);
            return;
        }
        if (plugin.getConfigManager().reload()) {
            MessageManager.reload();
            mm.reloadSuccessful(sender);
        } else
            mm.reloadFailed(sender);
    }

    private void cmdVersion(ICommandSender sender) {
        mm.versionMessage(sender);
    }

    public List<String> tabComplete(ICommandSender sender, String[] args) {
        final IWildcardPermissionUtil wpu = plugin.getWildcardPermissionUtil();
        List<String> ret = new ArrayList<>();

        boolean a = sender.hasPermission("skychanger.changesky.self") || sender.hasPermission("skychanger.changesky.others")
                || sender.hasPermission("skychanger.changesky.all") || wpu.hasGeneralChangeskyWorldPerm(sender)
                || wpu.hasGeneralChangeskyRadiusPerm(sender);

        boolean b = sender.hasPermission("skychanger.freeze.self") || sender.hasPermission("skychanger.freeze.others")
                || sender.hasPermission("skychanger.freeze.all") || wpu.hasGeneralFreezeWorldPerm(sender)
                || wpu.hasGeneralFreezeRadiusPerm(sender);

        if (args.length == 1) {
            if ("help".startsWith(args[0].toLowerCase()))
                ret.add("help");
            if (a && "tweak".startsWith(args[0].toLowerCase()))
                ret.add("tweak");
            if (b && "freeze".startsWith(args[0].toLowerCase()))
                ret.add("freeze");
            if (b && "unfreeze".startsWith(args[0].toLowerCase()))
                ret.add("unfreeze");
            if ("version".startsWith(args[0].toLowerCase()))
                ret.add("version");
            if (sender.hasPermission("skychanger.reload") && "reload".startsWith(args[0].toLowerCase()))
                ret.add("reload");
        }

        final boolean isTweak = args.length >= 3 && packetNum.matcher(args[1]).matches();
        final boolean isChangeSkyOrFreeze = args.length >= 2 && (packetNum.matcher(args[0]).matches() || args[0].equalsIgnoreCase("freeze")
                || args[0].equalsIgnoreCase("unfreeze"));
        final int flagPos = isTweak ? 2 : 1;

        if ((args.length == 3 && isTweak) || (args.length == 2 && isChangeSkyOrFreeze)) {

            // Players
            if (sender.hasPermission("skychanger.changesky.others") || sender.hasPermission("skychanger.freeze.others"))
                plugin.getOnlinePlayers().forEach(player -> {
                    if (player.getName().toLowerCase().startsWith(args[flagPos].toLowerCase())) {
                        ret.add(player.getName());
                    }
                });
            // All flag
            if ((sender.hasPermission("skychanger.changesky.all") || sender.hasPermission("skychanger.freeze.all"))
                    && "-a".startsWith(args[flagPos].toLowerCase())) {
                ret.add("-a");
            }
            // World flag
            if (args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("unfreeze")) {
                if ("-w".startsWith(args[flagPos].toLowerCase()) && wpu.hasGeneralFreezeWorldPerm(sender)) {
                    ret.add("-w");
                }
            } else {
                if ("-w".startsWith(args[flagPos].toLowerCase()) && wpu.hasGeneralChangeskyWorldPerm(sender)) {
                    ret.add("-w");
                }
            }
            // Radius flag
            if (args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("unfreeze")) {
                if ("-r".startsWith(args[flagPos].toLowerCase()) && wpu.hasGeneralFreezeRadiusPerm(sender)) {
                    ret.add("-r");
                }
            } else {
                if ("-r".startsWith(args[flagPos].toLowerCase()) && wpu.hasGeneralChangeskyRadiusPerm(sender)) {
                    ret.add("-r");
                }
            }
        }

        // World names
        if ((isChangeSkyOrFreeze && args.length == 3) || (isTweak && args.length == 4)) {
            if(args[flagPos].equalsIgnoreCase("-w")) {
                if (isTweak || packetNum.matcher(args[0]).matches()) {
                    for (IWorld w : plugin.getWorlds()) {
                        if (w.getName().toLowerCase().startsWith(args[flagPos + 1].toLowerCase())
                                && wpu.hasChangeskyWorldPerm(sender, w)) {
                            ret.add(w.getName());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("unfreeze")) {
                    for (IWorld w : plugin.getWorlds()) {
                        if (w.getName().toLowerCase().startsWith(args[flagPos + 1].toLowerCase())
                                && wpu.hasFreezeWorldPerm(sender, w)) {
                            ret.add(w.getName());
                        }
                    }
                }
            }
        }

        return ret;
    }

}
