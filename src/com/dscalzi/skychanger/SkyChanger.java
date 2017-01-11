package com.dscalzi.skychanger;

import org.bukkit.plugin.java.JavaPlugin;

import com.dscalzi.skychanger.managers.ConfigManager;
import com.dscalzi.skychanger.managers.MessageManager;

public class SkyChanger extends JavaPlugin{

	@Override
	public void onEnable(){
		ConfigManager.initialize(this);
		MessageManager.initialize(this);
		this.getCommand("skychanger").setExecutor(new MainExecutor(this));
	}
	
	@Override
	public void onDisable(){
		//Nothing for now.
	}
	
}
