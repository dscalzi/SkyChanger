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

package com.dscalzi.skychanger.sponge.api;

import com.dscalzi.skychanger.sponge.SkyChangerPlugin;
import com.dscalzi.skychanger.sponge.internal.SkyChangeImpl;

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
     * <code>dependencies = {&#064;Dependency(id = "skychanger")}</code> in
     * your plugin metadata so that this won't return null for you.
     * https://docs.spongepowered.org/stable/en/plugin/plugin-meta.html#plugin-annotation
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
