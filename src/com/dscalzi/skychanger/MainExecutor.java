package com.dscalzi.skychanger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.dscalzi.skychanger.managers.ConfigManager;
import com.dscalzi.skychanger.managers.MessageManager;

public class MainExecutor implements CommandExecutor, TabCompleter{

	private final MessageManager mm;
	
	private SkyChanger plugin;
	
	public MainExecutor(SkyChanger plugin){
		this.plugin = plugin;
		this.mm = MessageManager.getInstance();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if(args.length > 0){
			if(args[0].matches("(\\d+|-\\d+)")){
				this.cmdChangeSky(sender, args);
				return true;
			}
			
			if(args[0].equalsIgnoreCase("help")){
				mm.helpMessage(sender);
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
		int pN = Integer.parseInt(args[0]);
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
				sendPacket(target.getPlayer(), pN);
				mm.packetSent(sender, target.getName());
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
		
		sendPacket((Player)sender, pN);
		mm.packetSent(sender);
	}
	
	private void cmdReload(CommandSender sender){
		if(!sender.hasPermission("skychanger.reload")){
			mm.noPermission(sender);
			return;
		}
		if(ConfigManager.reload()) mm.reloadSuccessful(sender);
		else mm.reloadFailed(sender);
	}
	
	private void cmdVersion(CommandSender sender){
		mm.sendMessage(sender, "SkyChanger Version " + plugin.getDescription().getVersion());
	}
	
	private Object getConnection(Player player) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		Class<?> ocbPlayer = ReflectionUtil.getOCBClass("entity.CraftPlayer");
		Method getHandle = ReflectionUtil.getMethod(ocbPlayer, "getHandle");
		Object nmsPlayer = getHandle.invoke(player);
		Field conField = nmsPlayer.getClass().getField("playerConnection");
		Object con = conField.get(nmsPlayer);
		return con;
	}
	
	private void sendPacket(Player player, int number){
		try	{
			Class<?> packetClass = ReflectionUtil.getNMSClass("PacketPlayOutGameStateChange");
			Constructor<?> packetConstructor = packetClass.getConstructor(int.class, float.class);
			Object packet = packetConstructor.newInstance(7, number);
			Method sendPacket = ReflectionUtil.getNMSClass("PlayerConnection").getMethod("sendPacket", ReflectionUtil.getNMSClass("Packet"));
			sendPacket.invoke(this.getConnection(player), packet);
		} catch (Exception e) {
			mm.getLogger().severe("Packet could not be sent.");
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		
		List<String> ret = new ArrayList<String>();
		
		if(args.length == 1){
			if("help".startsWith(args[0].toLowerCase()))
				ret.add("help");
			if("version".startsWith(args[0].toLowerCase()))
				ret.add("version");
			if(sender.hasPermission("skychanger.reload") && "reload".startsWith(args[0].toLowerCase()))
				ret.add("reload");
		}
		
		if(args.length == 2){
			if(sender.hasPermission("skychanger.others"))
				plugin.getServer().getOnlinePlayers().forEach(player -> {if(player.getName().toLowerCase().startsWith(args[1].toLowerCase())) ret.add(player.getName());});
			if(sender.hasPermission("skychanger.all") && "@a".startsWith(args[1].toLowerCase()))
				ret.add("@a");
		}
		
		return ret.size() > 0 ? ret : null;
	}
	
}
