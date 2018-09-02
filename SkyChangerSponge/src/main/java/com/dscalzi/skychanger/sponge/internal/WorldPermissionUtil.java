package com.dscalzi.skychanger.sponge.internal;

import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.world.World;

public class WorldPermissionUtil {

    private static final String CWORLDPERM = "skychanger.changesky.world";
    private static final String FWORLDPERM = "skychanger.freeze.world";

    public static boolean hasGeneralChangeskyPerm(PermissionDescription p) {
        return hasGeneralPerm(p, CWORLDPERM);
    }

    public static boolean hasGeneralFreezePerm(PermissionDescription p) {
        return hasGeneralPerm(p, FWORLDPERM);
    }

    private static boolean hasGeneralPerm(PermissionDescription p, String perm) {
        if (p.getId().toLowerCase().startsWith(perm)) {
            return true;
        }
        return false;
    }

    public static boolean hasChangeskyPerm(PermissionDescription p, World w) {
        return hasWorldPerm(p, w, CWORLDPERM);
    }

    public static boolean hasFreezePerm(PermissionDescription p, World w) {
        return hasWorldPerm(p, w, FWORLDPERM);
    }

    private static boolean hasWorldPerm(PermissionDescription p, World w, String perm) {
        final String effective = p.getId().toLowerCase();
        if (effective.equals(perm + ".*")) {
            return true;
        } else if (effective.equals(perm + "." + w.getName().toLowerCase())) {
            return true;
        }
        return false;
    }

    public static String changeskyBasePerm() {
        return CWORLDPERM;
    }

    public static String freezeBasePerm() {
        return FWORLDPERM;
    }

}
