/*
 * SkyChanger
 * Copyright (C) 2017 Daniel D. Scalzi
 * See License.txt for license information.
 */
package com.dscalzi.skychanger.managers;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

import com.dscalzi.skychanger.SkyChanger;

public class ConfigManager {
	
	private static boolean initialized;
	private static ConfigManager instance;
	
	//TODO Will be implemented in a later version
	private final double configVersion = 1.0;
	private SkyChanger plugin;
	private FileConfiguration config;
	
	private ConfigManager(SkyChanger plugin){
		this.plugin = plugin;
		loadConfig();
	}
	
	public void loadConfig(){
    	verifyFile();
    	this.plugin.reloadConfig();
		this.config = this.plugin.getConfig(); 
    }
	
	public void verifyFile(){
    	File file = new File(this.plugin.getDataFolder(), "config.yml");
		if (!file.exists()){
			this.plugin.saveDefaultConfig();
		}
    }
	
	public static void initialize(SkyChanger plugin){
		if(!initialized){
			instance = new ConfigManager(plugin);
			initialized = true;
		}
	}
	
	public static boolean reload(){
		if(!initialized) return false;
		try{
			getInstance().loadConfig();
			return true;
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static ConfigManager getInstance(){
		return ConfigManager.instance;
	}
	
	/* Configuration Accessors */
	
	public float getUpperLimit(){
		return Float.parseFloat(config.getString("general_settings.upper_limit", "50.0"));
	}
	
	public float getLowerLimit(){
		return Float.parseFloat(config.getString("general_settings.lower_limit", "50.0"));
	}
	
	public boolean metricsOptOut(){
		return config.getBoolean("general_settings.metrics_opt_out", false);
	}
	
	public double getSystemConfigVersion(){
		return this.configVersion;
	}
	
	public double getConfigVersion(){
		return config.getDouble("ConfigVersion", getSystemConfigVersion());
	}
	
}
