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

package com.dscalzi.skychanger.core.internal.util;

import com.dscalzi.skychanger.core.internal.wrap.IPermissible;
import com.dscalzi.skychanger.core.internal.wrap.IWorld;

public abstract class IWildcardPermissionUtil {

    protected static final String CWORLDPERM = "skychanger.changesky.world";
    protected static final String FWORLDPERM = "skychanger.freeze.world";

    protected static final String CRADIUSPERM = "skychanger.changesky.radius";
    protected static final String FRADIUSPERM = "skychanger.freeze.radius";

    public abstract boolean hasGeneralChangeskyWorldPerm(IPermissible p);
    public abstract boolean hasGeneralFreezeWorldPerm(IPermissible p);

    public abstract boolean hasGeneralChangeskyRadiusPerm(IPermissible p);
    public abstract boolean hasGeneralFreezeRadiusPerm(IPermissible p);

    public abstract boolean hasChangeskyWorldPerm(IPermissible p, IWorld w);
    public abstract boolean hasFreezeWorldPerm(IPermissible p, IWorld w);

    public abstract boolean hasChangeskyRadiusPerm(IPermissible p, double radius);
    public abstract boolean hasFreezeRadiusPerm(IPermissible p, double radius);

    
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
