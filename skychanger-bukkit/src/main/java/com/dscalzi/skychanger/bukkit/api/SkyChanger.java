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

package com.dscalzi.skychanger.bukkit.api;

import com.dscalzi.skychanger.bukkit.SkyChangerPlugin;
import com.dscalzi.skychanger.bukkit.internal.SkyChangeImpl;
import com.dscalzi.skychanger.bukkit.internal.wrap.BukkitPlayer;
import com.dscalzi.skychanger.core.api.SkyAPI;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import org.bukkit.entity.Player;

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
     * <code>softdepend: [SkyChanger]</code> or <code>depend: [SkyChanger]</code> in
     * your plugin.yml so that this won't return null for you.
     *
     * @return the SkyChanger plugin if it is loaded, otherwise null.
     */
    @SuppressWarnings("unused")
    public static SkyChangerPlugin getPlugin() {
        return SkyChangerPlugin.inst();
    }

    /**
     * Get an instance of the SkyChanger API.
     * 
     * @return An instance of the SkyChanger API.
     */
    public static SkyAPI getAPI() {
        return api;
    }

    /**
     * Wrap a player instance to be sent to the API.
     *
     * @param p The player to be wrapped.
     *
     * @return A wrapped IPlayer instance of the provided player.
     */
    @SuppressWarnings("unused")
    public static IPlayer wrapPlayer(Player p) {
        return BukkitPlayer.of(p);
    }

}
