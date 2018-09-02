package com.dscalzi.skychanger.sponge.internal;

import java.util.Map;

import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

public class WorldPermissionUtil {

    private static final String CWORLDPERM = "skychanger.changesky.world";
    private static final String FWORLDPERM = "skychanger.freeze.world";

    public static boolean hasGeneralChangeskyPerm(Subject p) {
        return hasGeneralPerm(p, CWORLDPERM);
    }

    public static boolean hasGeneralFreezePerm(Subject p) {
        return hasGeneralPerm(p, FWORLDPERM);
    }

    private static boolean hasGeneralPerm(Subject p, String perm) {
        for (Map<String, Boolean> d : p.getSubjectData().getAllPermissions().values()) {
            for(Map.Entry<String, Boolean> s : d.entrySet()) {
                if(s.getKey().toLowerCase().startsWith(perm)) {
                    if(s.getValue()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasChangeskyPerm(Subject p, World w) {
        return hasWorldPerm(p, w, CWORLDPERM);
    }

    public static boolean hasFreezePerm(Subject p, World w) {
        return hasWorldPerm(p, w, FWORLDPERM);
    }

    private static boolean hasWorldPerm(Subject p, World w, String perm) {
        boolean canByRight = false;
        for (Map<String, Boolean> d : p.getSubjectData().getAllPermissions().values()) {
            for(Map.Entry<String, Boolean> s : d.entrySet()) {
                final String effective = s.getKey().toLowerCase();
                if (effective.equals(perm)) {
                    canByRight = s.getValue();
                } else if (effective.equals(perm + "." + w.getName().toLowerCase())) {
                    return s.getValue();
                }
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
