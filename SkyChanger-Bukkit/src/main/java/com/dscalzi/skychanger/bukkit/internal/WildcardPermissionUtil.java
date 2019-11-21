/*
 * This file is part of SkyChanger, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2019 Daniel D. Scalzi <https://github.com/dscalzi/SkyChanger>
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

import com.dscalzi.skychanger.core.internal.wrap.IPermissible;
import com.dscalzi.skychanger.core.internal.wrap.IWorld;
import com.dscalzi.skychanger.core.internal.util.IWildcardPermissionUtil;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class WildcardPermissionUtil extends IWildcardPermissionUtil {

    @Override
    public boolean hasGeneralChangeskyWorldPerm(IPermissible p) {
        return hasGeneralPerm(p, CWORLDPERM);
    }
    @Override
    public boolean hasGeneralFreezeWorldPerm(IPermissible p) {
        return hasGeneralPerm(p, FWORLDPERM);
    }

    @Override
    public boolean hasGeneralChangeskyRadiusPerm(IPermissible p) {
        return hasGeneralPerm(p, CRADIUSPERM);
    }
    @Override
    public boolean hasGeneralFreezeRadiusPerm(IPermissible p) {
        return hasGeneralPerm(p, FRADIUSPERM);
    }

    private static boolean hasGeneralPerm(IPermissible ip, String perm) {
        Permissible p = (Permissible)ip.getOriginal();
        for (PermissionAttachmentInfo i : p.getEffectivePermissions()) {
            if (i.getPermission().toLowerCase().startsWith(perm)) {
                if (i.getValue()) {
                    return true;
                }
            }
        }
        return p.hasPermission(perm + ".*");
    }

    @Override
    public boolean hasChangeskyWorldPerm(IPermissible p, IWorld w) {
        return hasWorldPerm(p, w, CWORLDPERM);
    }
    @Override
    public boolean hasFreezeWorldPerm(IPermissible p, IWorld w) {
        return hasWorldPerm(p, w, FWORLDPERM);
    }

    private boolean hasWorldPerm(IPermissible p, IWorld w, String perm) {
        return hasPerm(p, (i) ->  i.getPermission().substring(perm.length() + 1).equals(w.getName()), perm)
                || p.hasPermission(perm + ".*");
    }

    @Override
    public boolean hasChangeskyRadiusPerm(IPermissible p, double radius) {
        return hasRadiusPerm(p, radius, CRADIUSPERM);
    }
    @Override
    public boolean hasFreezeRadiusPerm(IPermissible p, double radius) {
        return hasRadiusPerm(p, radius, FRADIUSPERM);
    }

    public static boolean hasRadiusPerm(IPermissible p, double radius, String perm) {
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

    public static boolean hasPerm(IPermissible ip, Predicate<PermissionAttachmentInfo> hasSpecificPermissionTest, String perm) {
        Permissible p = (Permissible) ip.getOriginal();
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

}
