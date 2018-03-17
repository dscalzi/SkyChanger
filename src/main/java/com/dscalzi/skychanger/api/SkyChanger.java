/*
 * SkyChanger
 * Copyright (C) 2017-2018 Daniel D. Scalzi
 * See LICENSE for license information.
 */
package com.dscalzi.skychanger.api;

import com.dscalzi.skychanger.SkyChangerPlugin;
import com.dscalzi.skychanger.internal.SkyChangeImpl;

/**
 * Utility class to obtain references to components of SkyChanger.
 */
public class SkyChanger {

	private static final SkyAPI api = new SkyChangeImpl();
	
	/**
     * Get the SkyChanger plugin. If SkyChanger is not loaded yet, then this will
     * return null.
     * <p>
     * If you are depending on SkyChanger in your plugin, you should place
     * <code>softdepend: [SkyChanger]</code> or <code>depend: [SkyChanger]</code>
     * in your plugin.yml so that this won't return null for you.
     *
     * @return the SkyChanger plugin if it is loaded, otherwise null.
     */
	public static final SkyChangerPlugin getPlugin() {
		return SkyChangerPlugin.inst();
	}
	
	/**
	 * Get an instance of the SkyChanger API.
	 * 
	 * @return An instance of the SkyChanger API.
	 */
	public static final SkyAPI getAPI() {
		return api;
	}
	
}
