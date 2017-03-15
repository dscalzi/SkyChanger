/*
 * SkyChanger
 * Copyright (C) 2017 Daniel D. Scalzi
 * See License.txt for license information.
 */
package com.dscalzi.skychanger.managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dscalzi.skychanger.SkyChanger;

public class MessageManager {

	private static boolean initialized;
	private static MessageManager instance;
	private static final char b = (char)8226;
	
	private SkyChanger plugin;
	private final Logger logger;
	private final String prefix;
	private final ChatColor cPrimary;
	private final ChatColor cTrim;
	private final ChatColor cMessage;
	private final ChatColor cSuccess;
	private final ChatColor cError;
	
	private String lang;
	private Properties props;
	
	private MessageManager(SkyChanger plugin){
		this.plugin = plugin;
		this.logger = plugin.getLogger();
		this.cPrimary = ChatColor.GRAY;
		this.cTrim = ChatColor.DARK_RED;
		this.cMessage = ChatColor.YELLOW;
		this.cSuccess = ChatColor.GREEN;
		this.cError = ChatColor.RED;
		this.prefix = cPrimary + "| " + cTrim + ChatColor.BOLD + "S" + cTrim + "ky" + ChatColor.BOLD + "C" + cTrim + "hanger" + cPrimary + " |" + ChatColor.RESET;
		
		this.loadLanguage();
		
		this.plugin.getLogger().info(getString("message.pluginLoading", plugin.getDescription().getName()));
	}
	
	private void loadLanguage(){
		String l = ConfigManager.getInstance().getLanguage();
		try(InputStream utf8in = plugin.getClass().getResourceAsStream("lang/Messages_"+l+".properties");
			Reader reader = new InputStreamReader(utf8in, "UTF-8");){
			props = new Properties();
			props.load(reader);
			lang = l;
		} catch (NullPointerException e){
			getLogger().severe("Could not find language file for " + l + ". Defaulting to en_US (English).");
			try(InputStream utf8in = plugin.getClass().getResourceAsStream("lang/Messages_en_US.properties");
					Reader reader = new InputStreamReader(utf8in, "UTF-8");){
					props = new Properties();
					props.load(reader);
					lang = "en_US";
			} catch (IOException | NullPointerException e1) {
				getLogger().severe("Fatal error, no valid language file found (this may be due to a server"
						+ "reload). Shutting down..");
				e1.printStackTrace();
				plugin.getServer().getPluginManager().disablePlugin(plugin);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void initialize(SkyChanger plugin){
		if(!initialized){
			instance = new MessageManager(plugin);
			initialized = true;
		}
	}
	
	public static void reload(){
		if(!initialized) return;
		getInstance().loadLanguage();
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
	
	public String getString(String key, Object... args){
		try {
			String s = props.getProperty(key);
			if(args.length > 0){
				MessageFormat m = new MessageFormat(s);
				s = m.format(args);
			}
			return s;
		} catch (MissingResourceException e){
			getLogger().severe("Missing resource " + key + " for " + lang);
			return "{" + key + "}";
		}
	}
	
	/* Messages */
	
	public void helpMessage(CommandSender sender){
		final String listPrefix = cMessage + " "+b+" ";
		
		String header = prefix + cMessage + " " + getString("message.commandList");
		List<String> cmds = new ArrayList<String>();
		
		cmds.add(listPrefix + "/SkyChanger help " + ChatColor.RESET + "- " + getString("message.descHelp"));
		if(sender.hasPermission("skychanger.changesky.self") || sender.hasPermission("skychanger.changesky.others") || sender.hasPermission("skychanger.changesky.all")){
			cmds.add(listPrefix + this.generateUsage(sender) + ChatColor.RESET + " - " + getString("message.descChangeSky"));
		}
		if(sender.hasPermission("skychanger.freeze.self") || sender.hasPermission("skychanger.freeze.others") || sender.hasPermission("skychanger.freeze.all")){
			cmds.add(listPrefix + this.generateFreezeUsage(sender, false) + ChatColor.RESET + " - " + getString("message.descFreeze"));
			cmds.add(listPrefix + this.generateFreezeUsage(sender, true) + ChatColor.RESET + " - " + getString("message.descUnfreeze"));
		}
		if(sender.hasPermission("skychanger.reload"))
			cmds.add(listPrefix + "/SkyChanger reload " + ChatColor.RESET + "- " + getString("message.descReload"));
		cmds.add(listPrefix + "/SkyChanger version " + ChatColor.RESET + "- " + getString("message.descVersion"));
		
		sender.sendMessage(header);
		for(String s : cmds) sender.sendMessage(s);
	}
	
	public void noPermission(CommandSender sender){
		sendError(sender, getString("error.noPermission"));
	}
	
	public void denyNonPlayer(CommandSender sender){
		sendError(sender, getString("error.denyNonPlayer"));
	}
	
	private String generateUsage(CommandSender sender){
		String u = "/SkyChanger <#>";
		String b = sender.hasPermission("skychanger.changesky.self") ? "[]" : "<>";
		boolean o = sender.hasPermission("skychanger.changesky.others"), a = sender.hasPermission("skychanger.changesky.all");
		
		String opti = (o|a) ? " " + b.charAt(0) + (o ? getString("message.player") + (a ? " | @a" + b.charAt(1) : b.charAt(1)) : "@a" + b.charAt(1)): "";
		
		return u+opti;
	}
	
	private String generateFreezeUsage(CommandSender sender, boolean unfreeze){
		String u = "/SkyChanger " + (unfreeze ? "unfreeze" : "freeze");
		String b = sender.hasPermission("skychanger.freeze.self") ? "[]" : "<>";
		boolean o = sender.hasPermission("skychanger.freeze.others"), a = sender.hasPermission("skychanger.freeze.all");
		
		String opti = (o|a) ? " " + b.charAt(0) + (o ? getString("message.player") + (a ? " | @a" + b.charAt(1) : b.charAt(1)) : "@a" + b.charAt(1)): "";
		
		return u+opti;
	}
	
	public void floatingPointOverflow(CommandSender sender, String request){
		sendError(sender, getString("error.packetOverflow"));
	}
	
	public void playerNotFound(CommandSender sender, String name){
		sendError(sender, getString("error.playerNotFound", name));
	}
	
	public void packetSent(CommandSender sender){
		sendSuccess(sender, getString("success.packetSent"));
	}
	
	public void packetSent(CommandSender sender, String name){
		sendSuccess(sender, getString("success.packetSentTo", name));
	}
	
	public void packetUnfreeze(CommandSender sender){
		sendSuccess(sender, getString("success.packetUnfreezeSent"));
	}
	
	public void packetUnfreeze(CommandSender sender, String name){
		sendSuccess(sender, getString("success.packetUnfreezeSentTo", name));
	}
	
	public void packetError(CommandSender sender){
		sendError(sender, getString("error.packetError"));
	}
	
	public void packetError(CommandSender sender, String name){
		sendError(sender, getString("error.packetErrorTo", name));
	}
	
	public void logPacketError(){
		getLogger().severe(getString("error.logPacketError"));
	}
	
	public void outOfBoundsUpper(CommandSender sender, float upper){
		sendError(sender, getString("error.outOfBoundsUpper", upper));
	}
	
	public void outOfBoundsLower(CommandSender sender, float limit){
		sendError(sender, getString("error.outOfBoundsLower", limit));
	}
	
	public void reloadSuccessful(CommandSender sender){
		sendSuccess(sender, getString("success.reloadSuccess"));
	}
	
	public void reloadFailed(CommandSender sender){
		sendError(sender, getString("error.reloadFail"));
	}
	
	public void versionMessage(CommandSender sender){
		sendMessage(sender, "SkyChanger " + getString("message.version") + " " + plugin.getDescription().getVersion() + 
				"\n" + cPrimary + "| " + cSuccess + getString("message.metrics") + cPrimary + " | " + cMessage + "https://bstats.org/plugin/bukkit/SkyChanger" + 
				"\n" + cPrimary + "| " + cSuccess + getString("message.source") + cPrimary + " | " + cMessage + "https://bitbucket.org/AventiumSoftworks/skychanger");
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
