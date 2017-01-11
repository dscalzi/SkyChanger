package com.dscalzi.skychanger.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dscalzi.skychanger.SkyChanger;

public class MessageManager {

	private static boolean initialized;
	private static MessageManager instance;
	
	private SkyChanger plugin;
	private final Logger logger;
	private final String prefix;
	private final ChatColor cPrimary;
	private final ChatColor cTrim;
	private final ChatColor cMessage;
	private final ChatColor cSuccess;
	private final ChatColor cError;
	
	private MessageManager(SkyChanger plugin){
		this.plugin = plugin;
		this.logger = plugin.getLogger();
		this.cPrimary = ChatColor.GRAY;
		this.cTrim = ChatColor.DARK_RED;
		this.cMessage = ChatColor.YELLOW;
		this.cSuccess = ChatColor.GREEN;
		this.cError = ChatColor.RED;
		this.prefix = cPrimary + "| " + cTrim + ChatColor.BOLD + "S" + cTrim + "ky" + ChatColor.BOLD + "C" + cTrim + "hanger" + cPrimary + " |" + ChatColor.RESET;
		
		this.plugin.getLogger().info(plugin.getDescription().getName() + " is loading.");
	}
	
	public static void initialize(SkyChanger plugin){
		if(!initialized){
			instance = new MessageManager(plugin);
			initialized = true;
		}
	}
	
	public static MessageManager getInstance(){
		return MessageManager.instance;
	}
	
	/* Message Distribution */
	
	public void sendMessage(CommandSender sender, String message){
		sender.sendMessage(prefix + cMessage + " " + message);
	}
	
	public void sendSuccess(CommandSender sender, String message){
		sender.sendMessage(prefix + cSuccess + " " + message);
	}
	
	public void sendError(CommandSender sender, String message){
		sender.sendMessage(prefix + cError + " " + message);
	}
	
	public void sendGlobal(String message, String permission){
		for(Player p : plugin.getServer().getOnlinePlayers()){
			if(p.hasPermission(permission)){
				sendMessage(p, message);
			}
		}
	}
	
	/* Accessors */
	
	public String getPrefix(){
		return this.prefix;
	}
	
	public Logger getLogger(){
		return this.logger;
	}
	
	/* Messages */
	
	public void helpMessage(CommandSender sender){
		final String listPrefix = cMessage + " • ";
		
		String header = prefix + cMessage + " Command List - <Required> [Optional]";
		List<String> cmds = new ArrayList<String>();
		
		cmds.add(listPrefix + "/SkyChanger help " + ChatColor.RESET + "- View the command list.");
		if(sender.hasPermission("skychanger.changesky.self") || sender.hasPermission("skychanger.changesky.others") || sender.hasPermission("skychanger.changesky.all")){
			cmds.add(listPrefix + this.generateUsage(sender) + ChatColor.RESET + " - Change the sky.");
		}
		if(sender.hasPermission("skychanger.reload"))
			cmds.add(listPrefix + "/SkyChanger reload " + ChatColor.RESET + "- Reload the configuration.");
		cmds.add(listPrefix + "/SkyChanger version " + ChatColor.RESET + "- View version information.");
		
		sender.sendMessage(header);
		for(String s : cmds) sender.sendMessage(s);
	}
	
	public void noPermission(CommandSender sender){
		sendError(sender, "You do not have permission to do this.");
	}
	
	public void denyNonPlayer(CommandSender sender){
		sendError(sender, "Only players may use this command.");
	}
	
	private String generateUsage(CommandSender sender){
		String u = "/SkyChanger <#>";
		String b = sender.hasPermission("skychanger.changesky.self") ? "[]" : "<>";
		boolean o = sender.hasPermission("skychanger.changesky.others"), a = sender.hasPermission("skychanger.changesky.all");
		
		String opti = (o|a) ? " " + b.charAt(0) + (o ? "player" + (a ? " | @a" + b.charAt(1) : b.charAt(1)) : "@a" + b.charAt(1)): "";
		
		return u+opti;
	}
	
	public void playerNotFound(CommandSender sender, String name){
		sendError(sender, "Could not find player " + name + ". Are they online?");
	}
	
	public void packetSent(CommandSender sender){
		sendSuccess(sender, "Packet sent.");
	}
	
	public void packetSent(CommandSender sender, String name){
		sendSuccess(sender, "Sent packet to " + name + ".");
	}
	
	public void outOfBoundsUpper(CommandSender sender, int limit){
		sendError(sender, "The packet number must not exceed " + limit + ".");
	}
	
	public void outOfBoundsLower(CommandSender sender, int limit){
		sendError(sender, "The packet number must be larger than " + limit + ".");
	}
	
	public void reloadSuccessful(CommandSender sender){
		sendSuccess(sender, "Plugin successfully reloaded.");
	}
	
	public void reloadFailed(CommandSender sender){
		sendError(sender, "Plugin failed to reload, see console for details..");
	}
	
	/* Static Utility */
	
	public static UUID formatFromInput(String uuid) throws IllegalArgumentException {
		if(uuid == null) throw new IllegalArgumentException();
		uuid = uuid.trim();
		return uuid.length() == 32 ? fromTrimmed(uuid.replaceAll("-", "")) : UUID.fromString(uuid);
	}
	
	public static UUID fromTrimmed(String trimmedUUID) throws IllegalArgumentException {
		if(trimmedUUID == null) throw new IllegalArgumentException();
		StringBuilder builder = new StringBuilder(trimmedUUID.trim());
		/* Backwards adding to avoid index adjustments */
		try {
			builder.insert(20, "-");
			builder.insert(16, "-");
			builder.insert(12, "-");
			builder.insert(8, "-");
		} catch (StringIndexOutOfBoundsException e){
			throw new IllegalArgumentException();
		}
		
		return UUID.fromString(builder.toString());
	}
	
}
