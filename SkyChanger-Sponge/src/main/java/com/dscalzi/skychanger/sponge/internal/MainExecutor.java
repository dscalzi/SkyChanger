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

package com.dscalzi.skychanger.sponge.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.dscalzi.skychanger.sponge.SkyChangerPlugin;
import com.dscalzi.skychanger.sponge.api.SkyAPI;
import com.dscalzi.skychanger.sponge.api.SkyChanger;
import com.dscalzi.skychanger.sponge.internal.managers.ConfigManager;
import com.dscalzi.skychanger.sponge.internal.managers.MessageManager;

public class MainExecutor implements CommandCallable {

    private static final Pattern packetNum = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
    private MessageManager mm;
    private SkyChangerPlugin plugin;
    
    public MainExecutor(SkyChangerPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public CommandResult process(CommandSource src, String arguments) throws CommandException {
        final String[] args = arguments.isEmpty() ? new String[0] : arguments.replaceAll("\\s{2,}", " ").split(" ");
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
                this.cmdFreeze(src, false, args);
                return CommandResult.success();
            }

            if (args[0].equalsIgnoreCase("unfreeze")) {
                this.cmdFreeze(src, true, args);
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
                if (sender instanceof ConsoleSource) {
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
                    Location<World> origin;
                    if (sender instanceof Player) {
                        origin = ((Player)sender).getLocation();
                    } else if (sender instanceof CommandBlockSource) {
                        origin = ((CommandBlockSource)sender).getLocation();
                    } else {
                        MessageManager.getInstance().denyNonPlayer(sender);
                        return;
                    }
                    for(Player p : origin.getExtent().getPlayers()) {
                        if(Math.abs(origin.getPosition().distanceSquared(p.getPosition())) <= radiusSq) {
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
                if (sender instanceof ConsoleSource) {
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
                    Location<World> origin;
                    if (sender instanceof Player) {
                        origin = ((Player)sender).getLocation();
                    } else if (sender instanceof CommandBlockSource) {
                        origin = ((CommandBlockSource)sender).getLocation();
                    } else {
                        MessageManager.getInstance().denyNonPlayer(sender);
                        return;
                    }
                    for(Player p : origin.getExtent().getPlayers()) {
                        if(Math.abs(origin.getPosition().distanceSquared(p.getPosition())) <= radiusSq) {
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

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, Location<World> targetPosition)
            throws CommandException {
        String[] argsDirty = arguments.replaceAll("\\s{2,}", " ").split(" ");
        String[] args = arguments.endsWith(" ") ? new String[argsDirty.length + 1] : argsDirty;
        if(args != argsDirty) {
            System.arraycopy(argsDirty, 0, args, 0, argsDirty.length);
            args[args.length-1] = new String();
        }

        List<String> ret = new ArrayList<String>();

        boolean b = source.hasPermission("skychanger.freeze.self") || source.hasPermission("skychanger.freeze.others")
                || source.hasPermission("skychanger.freeze.all") || WildcardPermissionUtil.hasGeneralFreezeWorldPerm(source)
                || WildcardPermissionUtil.hasGeneralFreezeRadiusPerm(source);

        if (args.length == 1) {
            if ("help".startsWith(args[0].toLowerCase()))
                ret.add("help");
            if (b && "freeze".startsWith(args[0].toLowerCase()))
                ret.add("freeze");
            if (b && "unfreeze".startsWith(args[0].toLowerCase()))
                ret.add("unfreeze");
            if ("version".startsWith(args[0].toLowerCase()))
                ret.add("version");
            if (source.hasPermission("skychanger.reload") && "reload".startsWith(args[0].toLowerCase()))
                ret.add("reload");
        }

        if (args.length == 2 && (packetNum.matcher(args[0]).matches() || args[0].equalsIgnoreCase("freeze")
                || args[0].equalsIgnoreCase("unfreeze"))) {
            // Players
            if (source.hasPermission("skychanger.changesky.others") || source.hasPermission("skychanger.freeze.others"))
                plugin.getGame().getServer().getOnlinePlayers().forEach(player -> {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        ret.add(player.getName());
                    }
                });
            // All flag
            if ((source.hasPermission("skychanger.changesky.all") || source.hasPermission("skychanger.freeze.all"))
                    && "-a".startsWith(args[1].toLowerCase())) {
                ret.add("-a");
            }
            // World flag
            if (args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("unfreeze")) {
                if ("-w".startsWith(args[1].toLowerCase()) && WildcardPermissionUtil.hasGeneralFreezeWorldPerm(source)) {
                    ret.add("-w");
                }
            } else {
                if ("-w".startsWith(args[1].toLowerCase()) && WildcardPermissionUtil.hasGeneralChangeskyWorldPerm(source)) {
                    ret.add("-w");
                }
            }
            // Radius flag
            if (args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("unfreeze")) {
                if ("-r".startsWith(args[1].toLowerCase()) && WildcardPermissionUtil.hasGeneralFreezeRadiusPerm(source)) {
                    ret.add("-r");
                }
            } else {
                if ("-r".startsWith(args[1].toLowerCase()) && WildcardPermissionUtil.hasGeneralChangeskyRadiusPerm(source)) {
                    ret.add("-r");
                }
            }
        }

        // World names
        if (args.length == 3) {
            if(args[1].equalsIgnoreCase("-w")) {
                if (packetNum.matcher(args[0]).matches()) {
                    for (World w : plugin.getGame().getServer().getWorlds()) {
                        if (w.getName().toLowerCase().startsWith(args[2].toLowerCase())
                                && WildcardPermissionUtil.hasChangeskyWorldPerm(source, w)) {
                            ret.add(w.getName());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("unfreeze")) {
                    for (World w : plugin.getGame().getServer().getWorlds()) {
                        if (w.getName().toLowerCase().startsWith(args[2].toLowerCase())
                                && WildcardPermissionUtil.hasFreezeWorldPerm(source, w)) {
                            ret.add(w.getName());
                        }
                    }
                }
            }
        }

        return ret;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return true;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Change the color of the sky."));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
    	Text t = Text.of("Run /SkyChanger to view usage.");
        return Optional.of(t);
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("/SkyChanger <args>");
    }

}
