/*
 * This file is part of SkyChanger, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2021 Daniel D. Scalzi <https://github.com/dscalzi/SkyChanger>
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

package com.dscalzi.skychanger.sponge.internal;

import com.dscalzi.skychanger.core.internal.util.IWildcardPermissionUtil;
import com.dscalzi.skychanger.core.internal.wrap.IPermissible;
import com.dscalzi.skychanger.core.internal.wrap.IWorld;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.service.permission.Subject;

import java.util.Map;
import java.util.function.Predicate;

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

        Subject p = (Subject)ip.getOriginal();

        if(p instanceof SystemSubject) {
            return true;
        }
        for (Map<String, Boolean> d : p.subjectData().allPermissions().values()) {
            for(Map.Entry<String, Boolean> s : d.entrySet()) {
                if(s.getKey().toLowerCase().startsWith(perm)) {
                    if(s.getValue()) {
                        return true;
                    }
                }
            }
        }
        return p.hasPermission(perm);
    }

    @Override
    public boolean hasChangeskyWorldPerm(IPermissible p, IWorld w) {
        return hasWorldPerm(p, w, CWORLDPERM);
    }
    @Override
    public boolean hasFreezeWorldPerm(IPermissible p, IWorld w) {
        return hasWorldPerm(p, w, FWORLDPERM);
    }
    
    private static boolean hasWorldPerm(IPermissible p, IWorld w, String perm) {
        
        return hasPerm(p, (s) ->  s.getKey().substring(perm.length() + 1).equals(w.getName()), perm)
                || p.hasPermission(perm);
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
        return hasPerm(p, (s) -> {
            try {
                double radiusLimit = Double.parseDouble(s.getKey().substring(perm.length() + 1));
                return radius <= radiusLimit;
            } catch (NumberFormatException e) {
                // Malformed permission.
                return false;
            }
        }, perm) || p.hasPermission(perm);
    }

    private static boolean hasPerm(IPermissible ip, Predicate<Map.Entry<String, Boolean>> hasSpecificPermissionTest, String perm) {
        Subject p = (Subject)ip.getOriginal();
        if(p instanceof SystemSubject) {
            return true;
        }
        boolean canByRight = false;
        for (Map<String, Boolean> d : p.subjectData().allPermissions().values()) {
            for(Map.Entry<String, Boolean> s : d.entrySet()) {
                final String effective = s.getKey().toLowerCase();
                if (effective.equals(perm)) {
                    canByRight = s.getValue();
                } else if (effective.contains(perm) && hasSpecificPermissionTest.test(s)) {
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
