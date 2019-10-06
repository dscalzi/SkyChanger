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

package com.dscalzi.skychanger.bukkit.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.dscalzi.skychanger.bukkit.SkyChangerPlugin;
import com.dscalzi.skychanger.bukkit.api.SkyAPI;
import com.dscalzi.skychanger.bukkit.api.SkyChanger;
import com.dscalzi.skychanger.bukkit.managers.ConfigManager;
import com.dscalzi.skychanger.bukkit.managers.MessageManager;

public class MainExecutor implements CommandExecutor, TabCompleter {

    private static final Pattern packetNum = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
    private MessageManager mm;

    private SkyChangerPlugin plugin;

    public MainExecutor(SkyChangerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        this.mm = MessageManager.getInstance();
        
        if (args.length > 0) {
            if (packetNum.matcher(args[0]).matches()) {
                this.cmdChangeSky(sender, args);
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
    private void cmdChangeSky(CommandSender sender, String[] args) {
        final String basePerm = "skychanger.changesky";
        boolean s = sender.hasPermission(basePerm + ".self");
        boolean o = sender.hasPermission(basePerm + ".others");
        boolean a = sender.hasPermission(basePerm + ".all");
        boolean w = WildcardPermissionUtil.hasGeneralChangeskyWorldPerm(sender);
        boolean r = WildcardPermissionUtil.hasGeneralChangeskyRadiusPerm(sender);
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
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    api.changeSky(p, pN);
                }
                mm.packetSent(sender, "-a (" + mm.getString("message.everyone") + ")");
                return;
            }
            // Check if requested for world
            if (args[1].equalsIgnoreCase("-w")) {
                World t = null;
                if (args.length > 2) {
                    t = Bukkit.getWorld(args[2]);
                    if (t == null) {
                        mm.worldDoesntExist(sender, args[2]);
                        return;
                    }
                } else {
                    if (!(sender instanceof Player)) {
                        mm.mustSpecifyWorld(sender);
                        return;
                    }
                    t = ((Player) sender).getWorld();
                }
                if (!WildcardPermissionUtil.hasChangeskyWorldPerm(sender, t)) {
                    mm.noPermission(sender);
                    return;
                }
                for (Player p : t.getPlayers()) {
                    api.changeSky(p, pN);
                }
                mm.packetSent(sender, mm.getString("message.allPlayersIn") + " " + t.getName());
                return;
            }
            // Check if requested for radius
            if (args[1].equalsIgnoreCase("-r")) {
                if (sender instanceof ConsoleCommandSender) {
                    MessageManager.getInstance().denyNonPlayer(sender);
                    return;
                }
                if(args.length > 2) {
                    double radius;
                    double radiusSq;
                    try {
                        radius = Double.parseDouble(args[2]);
                        
                        if (!WildcardPermissionUtil.hasChangeskyRadiusPerm(sender, radius)) {
                            mm.noPermission(sender);
                            return;
                        }
                        
                        radiusSq = Math.pow(radius, 2);
                    } catch (NumberFormatException e) {
                        MessageManager.getInstance().radiusFormatError(sender);
                        return;
                    }
                    Location origin;
                    if (sender instanceof Player) {
                        origin = ((Player)sender).getLocation();
                    } else if (sender instanceof BlockCommandSender) {
                        origin = ((BlockCommandSender)sender).getBlock().getLocation();
                    } else {
                        MessageManager.getInstance().denyNonPlayer(sender);
                        return;
                    }
                    for(Player p : origin.getWorld().getPlayers()) {
                        if(Math.abs(origin.distanceSquared(p.getLocation())) <= radiusSq) {
                            api.changeSky(p, pN);
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
            OfflinePlayer target;
            try {
                target = plugin.getServer().getOfflinePlayer(MessageManager.formatFromInput(args[1]));
            } catch (IllegalArgumentException e) {
                target = plugin.getServer().getOfflinePlayer(args[1]);
            }
            if (target == null || !target.isOnline()) {
                mm.playerNotFound(sender, target == null || target.getName() == null ? args[1] : target.getName());
                return;
            }
            // If a player specified their own name, we run the command as if the player
            // param was not
            // given. The others permission therefore includes the self.
            if (!(sender instanceof Player) || !target.getUniqueId().equals(((Player) sender).getUniqueId())) {
                if (api.changeSky(target.getPlayer(), pN))
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

    @SuppressWarnings("deprecation")
    private void cmdFreeze(CommandSender sender, boolean unfreeze, String[] args) {
        final String basePerm = "skychanger.freeze";
        boolean s = sender.hasPermission(basePerm + ".self");
        boolean o = sender.hasPermission(basePerm + ".others");
        boolean a = sender.hasPermission(basePerm + ".all");
        boolean w = WildcardPermissionUtil.hasGeneralFreezeWorldPerm(sender);
        boolean r = WildcardPermissionUtil.hasGeneralFreezeRadiusPerm(sender);
        if (!s && !o && !a && !w && !r) {
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
                for (Player p : plugin.getServer().getOnlinePlayers()) {
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
                    t = Bukkit.getWorld(args[2]);
                    if (t == null) {
                        mm.worldDoesntExist(sender, args[2]);
                        return;
                    }
                } else {
                    if (!(sender instanceof Player)) {
                        mm.mustSpecifyWorld(sender);
                        return;
                    }
                    t = ((Player) sender).getWorld();
                }
                if (!WildcardPermissionUtil.hasFreezeWorldPerm(sender, t)) {
                    mm.noPermission(sender);
                    return;
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
            // Check if requested for radius
            if (args[1].equalsIgnoreCase("-r")) {
                if (sender instanceof ConsoleCommandSender) {
                    MessageManager.getInstance().denyNonPlayer(sender);
                    return;
                }
                if(args.length > 2) {
                    double radius;
                    double radiusSq;
                    try {
                        radius = Double.parseDouble(args[2]);
                        
                        if (!WildcardPermissionUtil.hasFreezeRadiusPerm(sender, radius)) {
                            mm.noPermission(sender);
                            return;
                        }
                        
                        radiusSq = Math.pow(radius, 2);
                    } catch (NumberFormatException e) {
                        MessageManager.getInstance().radiusFormatError(sender);
                        return;
                    }
                    Location origin;
                    if (sender instanceof Player) {
                        origin = ((Player)sender).getLocation();
                    } else if (sender instanceof BlockCommandSender) {
                        origin = ((BlockCommandSender)sender).getBlock().getLocation();
                    } else {
                        MessageManager.getInstance().denyNonPlayer(sender);
                        return;
                    }
                    for(Player p : origin.getWorld().getPlayers()) {
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
            OfflinePlayer target;
            try {
                target = plugin.getServer().getOfflinePlayer(MessageManager.formatFromInput(args[1]));
            } catch (IllegalArgumentException e) {
                target = plugin.getServer().getOfflinePlayer(args[1]);
            }
            if (target == null || !target.isOnline()) {
                mm.playerNotFound(sender, target == null || target.getName() == null ? args[1] : target.getName());
                return;
            }
            // If a player specified their own name, we run the command as if the player
            // param was not
            // given. The others permission therefore includes the self.
            if (!(sender instanceof Player) || !target.getUniqueId().equals(((Player) sender).getUniqueId())) {
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

        if (!(sender instanceof Player)) {
            MessageManager.getInstance().denyNonPlayer(sender);
            return;
        }

        Player p = (Player) sender;
        if ((!unfreeze && api.freeze(p.getPlayer())) || (unfreeze && api.unfreeze(p)))
            if (unfreeze)
                mm.packetUnfreeze(sender);
            else
                mm.packetSent(sender);
        else
            mm.packetError(sender);
    }

    private void cmdReload(CommandSender sender) {
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

    private void cmdVersion(CommandSender sender) {
        mm.versionMessage(sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> ret = new ArrayList<String>();

        boolean b = sender.hasPermission("skychanger.freeze.self") || sender.hasPermission("skychanger.freeze.others")
                || sender.hasPermission("skychanger.freeze.all") || WildcardPermissionUtil.hasGeneralFreezeWorldPerm(sender)
                || WildcardPermissionUtil.hasGeneralFreezeRadiusPerm(sender);

        if (args.length == 1) {
            if ("help".startsWith(args[0].toLowerCase()))
                ret.add("help");
            if (b && "freeze".startsWith(args[0].toLowerCase()))
                ret.add("freeze");
            if (b && "unfreeze".startsWith(args[0].toLowerCase()))
                ret.add("unfreeze");
            if ("version".startsWith(args[0].toLowerCase()))
                ret.add("version");
            if (sender.hasPermission("skychanger.reload") && "reload".startsWith(args[0].toLowerCase()))
                ret.add("reload");
        }

        if (args.length == 2 && (packetNum.matcher(args[0]).matches() || args[0].equalsIgnoreCase("freeze")
                || args[0].equalsIgnoreCase("unfreeze"))) {
            // Players
            if (sender.hasPermission("skychanger.changesky.others") || sender.hasPermission("skychanger.freeze.others"))
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        ret.add(player.getName());
                    }
                });
            // All flag
            if ((sender.hasPermission("skychanger.changesky.all") || sender.hasPermission("skychanger.freeze.all"))
                    && "-a".startsWith(args[1].toLowerCase())) {
                ret.add("-a");
            }
            // World flag
            if (args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("unfreeze")) {
                if ("-w".startsWith(args[1].toLowerCase()) && WildcardPermissionUtil.hasGeneralFreezeWorldPerm(sender)) {
                    ret.add("-w");
                }
            } else {
                if ("-w".startsWith(args[1].toLowerCase()) && WildcardPermissionUtil.hasGeneralChangeskyWorldPerm(sender)) {
                    ret.add("-w");
                }
            }
            // Radius flag
            if (args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("unfreeze")) {
                if ("-r".startsWith(args[1].toLowerCase()) && WildcardPermissionUtil.hasGeneralFreezeRadiusPerm(sender)) {
                    ret.add("-r");
                }
            } else {
                if ("-r".startsWith(args[1].toLowerCase()) && WildcardPermissionUtil.hasGeneralChangeskyRadiusPerm(sender)) {
                    ret.add("-r");
                }
            }
        }

        // World names
        if (args.length == 3) {
            if(args[1].equalsIgnoreCase("-w")) {
                if (packetNum.matcher(args[0]).matches()) {
                    for (World w : plugin.getServer().getWorlds()) {
                        if (w.getName().toLowerCase().startsWith(args[2].toLowerCase())
                                && WildcardPermissionUtil.hasChangeskyWorldPerm(sender, w)) {
                            ret.add(w.getName());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("unfreeze")) {
                    for (World w : plugin.getServer().getWorlds()) {
                        if (w.getName().toLowerCase().startsWith(args[2].toLowerCase())
                                && WildcardPermissionUtil.hasFreezeWorldPerm(sender, w)) {
                            ret.add(w.getName());
                        }
                    }
                }
            }
        }

        return ret;
    }

}
