/*
 * This file is part of SkyChanger, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2018 Daniel D. Scalzi <https://github.com/dscalzi/SkyChanger>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dscalzi.skychanger.bukkit.internal;

import java.util.function.Predicate;

import org.bukkit.World;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class WildcardPermissionUtil {

    private static final String CWORLDPERM = "skychanger.changesky.world";
    private static final String FWORLDPERM = "skychanger.freeze.world";
    
    private static final String CRADIUSPERM = "skychanger.changesky.radius";
    private static final String FRADIUSPERM = "skychanger.freeze.radius";

    public static boolean hasGeneralChangeskyWorldPerm(Permissible p) {
        return hasGeneralPerm(p, CWORLDPERM);
    }

    public static boolean hasGeneralFreezeWorldPerm(Permissible p) {
        return hasGeneralPerm(p, FWORLDPERM);
    }
    
    public static boolean hasGeneralChangeskyRadiusPerm(Permissible p) {
        return hasGeneralPerm(p, CRADIUSPERM);
    }
    
    public static boolean hasGeneralFreezeRadiusPerm(Permissible p) {
        return hasGeneralPerm(p, FRADIUSPERM);
    }

    private static boolean hasGeneralPerm(Permissible p, String perm) {
        for (PermissionAttachmentInfo i : p.getEffectivePermissions()) {
            if (i.getPermission().toLowerCase().startsWith(perm)) {
                if (i.getValue()) {
                    return true;
                }
            }
        }
        return p.hasPermission(perm + ".*");
    }

    public static boolean hasChangeskyWorldPerm(Permissible p, World w) {
        return hasWorldPerm(p, w, CWORLDPERM);
    }

    public static boolean hasFreezeWorldPerm(Permissible p, World w) {
        return hasWorldPerm(p, w, FWORLDPERM);
    }

    private static boolean hasWorldPerm(Permissible p, World w, String perm) {
        return hasPerm(p, (i) ->  i.getPermission().substring(perm.length() + 1).equals(w.getName()), perm)
                || p.hasPermission(perm + ".*");
    }
    
    public static boolean hasChangeskyRadiusPerm(Permissible p, double radius) {
        return hasRadiusPerm(p, radius, CRADIUSPERM);
    }
    
    public static boolean hasFreezeRadiusPerm(Permissible p, double radius) {
        return hasRadiusPerm(p, radius, FRADIUSPERM);
    }

    public static boolean hasRadiusPerm(Permissible p, double radius, String perm) {
        return hasPerm(p, (i) -> {
            try {
                double radiusLimit = Double.parseDouble(i.getPermission().substring(perm.length() + 1));
                return radius <= radiusLimit;
            } catch (NumberFormatException e) {
                // Malformed permission.
                return false;
            }
        }, perm) || p.hasPermission(perm + ".*");
    }
    
    public static boolean hasPerm(Permissible p, Predicate<PermissionAttachmentInfo> hasSpecificPermissionTest, String perm) {
        boolean canByRight = false;
        for (PermissionAttachmentInfo i : p.getEffectivePermissions()) {
            final String effective = i.getPermission().toLowerCase();
            if (effective.equals(perm + ".*")) {
                canByRight = i.getValue();
            } else if (effective.indexOf(perm + '.') > -1 && hasSpecificPermissionTest.test(i)) {
                return i.getValue();
            }
        }
        return canByRight;
    }
    
    public static String changeskyWorldBasePerm() {
        return CWORLDPERM;
    }

    public static String freezeWorldBasePerm() {
        return FWORLDPERM;
    }
    
    public static String changeskyRadiusBasePerm() {
        return CRADIUSPERM;
    }
    
    public static String freezeRadiusBasePerm() {
        return FRADIUSPERM;
    }

}
