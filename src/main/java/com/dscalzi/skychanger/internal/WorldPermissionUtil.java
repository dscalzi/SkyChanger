/*
 * SkyChanger
 * Copyright (C) 2017-2018 Daniel D. Scalzi
 * See LICENSE for license information.
 */
package com.dscalzi.skychanger.internal;

import org.bukkit.World;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class WorldPermissionUtil {

	private static final String CWORLDPERM = "skychanger.changesky.world";
	private static final String FWORLDPERM = "skychanger.freeze.world";
	
	public static boolean hasGeneralChangeskyPerm(Permissible p) {
		return hasGeneralPerm(p, CWORLDPERM);
	}
	
	public static boolean hasGeneralFreezePerm(Permissible p) {
		return hasGeneralPerm(p, FWORLDPERM);
	}
	
	private static boolean hasGeneralPerm(Permissible p, String perm) {
		for(PermissionAttachmentInfo i : p.getEffectivePermissions()) {
			if(i.getPermission().toLowerCase().startsWith(perm)) {
				if(i.getValue()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean hasChangeskyPerm(Permissible p, World w) {
		return hasWorldPerm(p, w, CWORLDPERM);
	}
	
	public static boolean hasFreezePerm(Permissible p, World w) {
		return hasWorldPerm(p, w, FWORLDPERM);
	}
	
	private static boolean hasWorldPerm(Permissible p, World w, String perm) {
		boolean canByRight = false;
		for(PermissionAttachmentInfo i : p.getEffectivePermissions()) {
			final String effective = i.getPermission().toLowerCase();
			if(effective.equals(perm + ".*")) {
				canByRight = i.getValue();
			} else if(effective.equals(perm + "." + w.getName().toLowerCase())) {
				return i.getValue();
			}
		}
		return canByRight;
	}
	
	public static String changeskyBasePerm() {
		return CWORLDPERM;
	}
	
	public static String freezeBasePerm() {
		return FWORLDPERM;
	}
	
}
