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
        for (PermissionAttachmentInfo i : p.getEffectivePermissions()) {
            if (i.getPermission().toLowerCase().startsWith(perm)) {
                if (i.getValue()) {
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
        for (PermissionAttachmentInfo i : p.getEffectivePermissions()) {
            final String effective = i.getPermission().toLowerCase();
            if (effective.equals(perm + ".*")) {
                canByRight = i.getValue();
            } else if (effective.equals(perm + "." + w.getName().toLowerCase())) {
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
