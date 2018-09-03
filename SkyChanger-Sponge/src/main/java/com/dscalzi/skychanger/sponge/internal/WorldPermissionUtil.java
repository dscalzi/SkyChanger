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

package com.dscalzi.skychanger.sponge.internal;

import java.util.Map;

import org.spongepowered.api.command.source.ConsoleSource;
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
        if(p instanceof ConsoleSource) {
            return true;
        }
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
        if(p instanceof ConsoleSource) {
            return true;
        }
        boolean canByRight = false;
        for (Map<String, Boolean> d : p.getSubjectData().getAllPermissions().values()) {
            for(Map.Entry<String, Boolean> s : d.entrySet()) {
                final String effective = s.getKey().toLowerCase();
                if (effective.equals(perm)) {
                    canByRight = s.getValue();
                } else if (effective.indexOf(perm) > -1 && s.getKey().substring(perm.length() + 1).equals(w.getName())) {
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
