/*
 * SkyChanger
 * Copyright (C) 2017 Daniel D. Scalzi
 * See License.txt for license information.
 */
package com.dscalzi.skychanger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.dscalzi.skychanger.managers.ConfigManager;
import com.dscalzi.skychanger.managers.MessageManager;

public class MainExecutor implements CommandExecutor, TabCompleter{

	private static final Pattern packetNum = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
	private final MessageManager mm;
	
	private SkyChanger plugin;
	
	public MainExecutor(SkyChanger plugin){
		this.plugin = plugin;
		this.mm = MessageManager.getInstance();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if(args.length > 0){
			if(packetNum.matcher(args[0]).matches()){
				this.cmdChangeSky(sender, args);
				return true;
			}
			
			if(args[0].equalsIgnoreCase("help")){
				mm.helpMessage(sender);
				return true;
			}
			
			if(args[0].equalsIgnoreCase("freeze")){
				this.cmdFreeze((Player)sender, false, args);
				return true;
			}
			
			if(args[0].equalsIgnoreCase("unfreeze")){
				this.cmdFreeze((Player)sender, true, args);
				return true;
			}
			
			if(args[0].equalsIgnoreCase("version")){
				this.cmdVersion(sender);
				return true;
			}
			
			if(args[0].equalsIgnoreCase("reload")){
				this.cmdReload(sender);
				return true;
			}
		}
		
		mm.helpMessage(sender);
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private void cmdChangeSky(CommandSender sender, String[] args){
		final String basePerm = "skychanger.changesky";
		if(!sender.hasPermission(basePerm + ".self") && !sender.hasPermission(basePerm + ".others") && !sender.hasPermission(basePerm + ".all")){
			mm.noPermission(sender);
			return;
		}
		float pN;
		try {
			pN = Float.parseFloat(args[0]);
		} catch (NumberFormatException e){
			mm.floatingPointOverflow(sender, args[0]);
			return;
		}
		if(!sender.hasPermission("skychanger.bypasslimit")){
			int upper = ConfigManager.getInstance().getUpperLimit();
			int lower = ConfigManager.getInstance().getLowerLimit();
			if(pN > upper){
				mm.outOfBoundsUpper(sender, upper);
				return;
			}
			if(lower > pN){
				mm.outOfBoundsLower(sender, lower);
				return;
			}
		}
		if(args.length > 1){
			//Check if requested for all
			if(args[1].equalsIgnoreCase("@a")){
				if(!sender.hasPermission("skychanger.changesky.all")){
					mm.noPermission(sender);
					return;
				}
				for(Player p : plugin.getServer().getOnlinePlayers()){
					sendPacket(p, pN);
				}
				mm.packetSent(sender, "@a (everyone)");
				return;
			}
			//Check if param is a player
			if(!sender.hasPermission("skychanger.changesky.others")){
				mm.noPermission(sender);
				return;
			}
			OfflinePlayer target;
			try {
        		target = plugin.getServer().getOfflinePlayer(MessageManager.formatFromInput(args[1]));
        	} catch(IllegalArgumentException e){
        		target = plugin.getServer().getOfflinePlayer(args[1]);
        	}
			if(target == null || !target.isOnline()) {
				mm.playerNotFound(sender, target == null || target.getName() == null ? args[1] : target.getName());
				return;
			}
			if(!(sender instanceof Player) || !target.getUniqueId().equals(((Player)sender).getUniqueId())){
				if(sendPacket(target.getPlayer(), pN))
					mm.packetSent(sender, target.getName());
				else
					mm.packetError(sender, target.getName());
				return;
			}
		}
		
		if(!(sender instanceof Player)){
			MessageManager.getInstance().denyNonPlayer(sender);
			return;
		}
		
		if(!sender.hasPermission("skychanger.changesky.self")){
			mm.noPermission(sender);
			return;
		}
		
		if(sendPacket((Player)sender, pN))
			mm.packetSent(sender);
		else
			mm.packetError(sender);
	}
	
	@SuppressWarnings("deprecation")
	private void cmdFreeze(CommandSender sender, boolean unfreeze, String[] args){
		final String basePerm = "skychanger.freeze";
		if(!sender.hasPermission(basePerm + ".self") && !sender.hasPermission(basePerm + ".others") && !sender.hasPermission(basePerm + ".all")){
			mm.noPermission(sender);
			return;
		}
		if(args.length > 1){
			//Check if requested for all
			if(args[1].equalsIgnoreCase("@a")){
				if(!sender.hasPermission("skychanger.freeze.all")){
					mm.noPermission(sender);
					return;
				}
				for(Player p : plugin.getServer().getOnlinePlayers()){
					if(unfreeze) p.teleport(p.getLocation());
					else sendFreezePacket(p);
				}
				if(unfreeze) mm.packetUnfreeze(sender, "@a (everyone)");
				else mm.packetSent(sender, "@a (everyone)");
				return;
			}
			//Check if param is a player
			if(!sender.hasPermission("skychanger.freeze.others")){
				mm.noPermission(sender);
				return;
			}
			OfflinePlayer target;
			try {
        		target = plugin.getServer().getOfflinePlayer(MessageManager.formatFromInput(args[1]));
        	} catch(IllegalArgumentException e){
        		target = plugin.getServer().getOfflinePlayer(args[1]);
        	}
			if(target == null || !target.isOnline()) {
				mm.playerNotFound(sender, target == null || target.getName() == null ? args[1] : target.getName());
				return;
			}
			if(!(sender instanceof Player) || !target.getUniqueId().equals(((Player)sender).getUniqueId())){
				if((!unfreeze && sendFreezePacket(target.getPlayer())) || (unfreeze && target.getPlayer().teleport(target.getPlayer().getLocation())))
					if(unfreeze) mm.packetUnfreeze(sender, target.getName());
					else mm.packetSent(sender, target.getName());
				else
					mm.packetError(sender, target.getName());
				return;
			}
		}
		
		if(!(sender instanceof Player)){
			MessageManager.getInstance().denyNonPlayer(sender);
			return;
		}
		
		if(!sender.hasPermission("skychanger.freeze.self")){
			mm.noPermission(sender);
			return;
		}
		Player p = (Player)sender;
		if((!unfreeze && sendFreezePacket(p.getPlayer())) || (unfreeze && p.teleport(p.getLocation())))
			if(unfreeze) mm.packetUnfreeze(sender);
			else mm.packetSent(sender);
		else
			mm.packetError(sender);
	}
	
	private void cmdReload(CommandSender sender){
		if(!sender.hasPermission("skychanger.reload")){
			mm.noPermission(sender);
			return;
		}
		if(ConfigManager.reload()){
			plugin.enableMetrics();
			mm.reloadSuccessful(sender);
		}
		else mm.reloadFailed(sender);
	}
	
	private void cmdVersion(CommandSender sender){
		mm.versionMessage(sender);
	}
	
	private Object getConnection(Player player) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		Class<?> ocbPlayer = ReflectionUtil.getOCBClass("entity.CraftPlayer");
		Method getHandle = ReflectionUtil.getMethod(ocbPlayer, "getHandle");
		Object nmsPlayer = getHandle.invoke(player);
		Field conField = nmsPlayer.getClass().getField("playerConnection");
		Object con = conField.get(nmsPlayer);
		return con;
	}
	
	private boolean sendPacket(Player player, float number){
		try	{
			Class<?> packetClass = ReflectionUtil.getNMSClass("PacketPlayOutGameStateChange");
			Constructor<?> packetConstructor = packetClass.getConstructor(int.class, float.class);
			Object packet = packetConstructor.newInstance(7, number);
			Method sendPacket = ReflectionUtil.getNMSClass("PlayerConnection").getMethod("sendPacket", ReflectionUtil.getNMSClass("Packet"));
			sendPacket.invoke(this.getConnection(player), packet);
		} catch (Exception e) {
			mm.getLogger().severe("Packet could not be sent.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private boolean sendFreezePacket(Player player){
		try {
			World w = player.getWorld();
			Class<?> packetClass = ReflectionUtil.getNMSClass("PacketPlayOutRespawn");
			Class<?> diffClass = ReflectionUtil.getNMSClass("EnumDifficulty");
			Class<?> wtClass = ReflectionUtil.getNMSClass("WorldType");
			Class<?> gameModeClass = ReflectionUtil.getNMSClass("EnumGamemode");
			Method diffGetById = ReflectionUtil.getMethod(diffClass, "getById", int.class);
			Method gmGetById = ReflectionUtil.getMethod(gameModeClass, "getById", int.class);
			Constructor<?> packetConstructor = null;
			Object packet = null;
			try{
				packetConstructor = packetClass.getConstructor(int.class, diffClass, wtClass, gameModeClass);
				packet = packetConstructor.newInstance(w.getEnvironment().getId(), diffGetById.invoke(null, w.getDifficulty().getValue()), wtClass.getField("NORMAL").get(null), gmGetById.invoke(null, player.getGameMode().getValue()));
			} catch (NoSuchMethodException e){
				//Try 1.9 method.
				Class<?> worldSettings = ReflectionUtil.getNMSClass("WorldSettings");
				Class<?>[] innerClasses = worldSettings.getDeclaredClasses();
				Class<?> wsGameMode = null;
				for(Class<?> cl : innerClasses)
					if(cl.getSimpleName().equals("EnumGamemode"))
						wsGameMode = cl;
				Method a = ReflectionUtil.getMethod(worldSettings, "a", int.class);
				packetConstructor = packetClass.getConstructor(int.class, diffClass, wtClass, wsGameMode);
				packet =  packetConstructor.newInstance(w.getEnvironment().getId(), diffGetById.invoke(null, w.getDifficulty().getValue()), wtClass.getField("NORMAL").get(null), a.invoke(null, player.getGameMode().getValue()));
			}
			Method sendPacket = ReflectionUtil.getNMSClass("PlayerConnection").getMethod("sendPacket", ReflectionUtil.getNMSClass("Packet"));
			sendPacket.invoke(this.getConnection(player), packet);
			player.updateInventory();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchFieldException | SecurityException | NoSuchMethodException e) {
			mm.getLogger().severe("Packet could not be sent.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		
		List<String> ret = new ArrayList<String>();
		
		boolean b = sender.hasPermission("skychanger.freeze.self") || sender.hasPermission("skychanger.freeze.others") || sender.hasPermission("skychanger.freeze.all");
		
		if(args.length == 1){
			if("help".startsWith(args[0].toLowerCase()))
				ret.add("help");
			if(b && "freeze".startsWith(args[0].toLowerCase()))
				ret.add("freeze");
			if(b && "unfreeze".startsWith(args[0].toLowerCase()))
				ret.add("unfreeze");
			if("version".startsWith(args[0].toLowerCase()))
				ret.add("version");
			if(sender.hasPermission("skychanger.reload") && "reload".startsWith(args[0].toLowerCase()))
				ret.add("reload");
		}
		
		if(args.length == 2 && (packetNum.matcher(args[0]).matches() || args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("unfreeze"))){
			if(sender.hasPermission("skychanger.changesky.others") || sender.hasPermission("skychanger.freeze.others"))
				plugin.getServer().getOnlinePlayers().forEach(player -> {if(player.getName().toLowerCase().startsWith(args[1].toLowerCase())) ret.add(player.getName());});
			if((sender.hasPermission("skychanger.changesky.all") || sender.hasPermission("skychanger.freeze.all")) && "@a".startsWith(args[1].toLowerCase()))
				ret.add("@a");
		}
		
		return ret.size() > 0 ? ret : null;
	}
	
}
