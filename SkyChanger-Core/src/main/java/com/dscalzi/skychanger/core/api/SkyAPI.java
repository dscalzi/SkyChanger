/*
 * This file is part of SkyChanger, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2020 Daniel D. Scalzi <https://github.com/dscalzi/SkyChanger>
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

package com.dscalzi.skychanger.core.api;

import com.dscalzi.skychanger.core.internal.wrap.IPlayer;

public interface SkyAPI {

    /**
     * Change the sky for a player. Uses {@link SkyPacket#FADE_VALUE FADE_VALUE}
     * as the packet type.
     * 
     * @param player
     *            The target of the sky change.
     * @param number
     *            The packet number which will determine the type of sky.
     * @return True if the sky change was successful, otherwise false.
     * 
     * @since 1.4.0
     */
    boolean changeSky(IPlayer player, float number);

    /**
     * Change the sky for the player.
     *
     * @param player
     *            The target of the sky change.
     * @param packet
     *            The packet type to send.
     * @param number
     *            The packet number which will determine the type of sky.
     * @return True if the sky change was successful, otherwise false.
     *
     * @since 3.0.0
     */
    boolean changeSky(IPlayer player, SkyPacket packet, float number);

    /**
     * Freeze a player.
     * 
     * @param player
     *            The target of the freeze.
     * @return True if the freeze was successful, otherwise false.
     * 
     * @since 1.4.0
     */
    boolean freeze(IPlayer player);

    /**
     * Unfreeze a player.
     * 
     * @param player
     *            The target of the unfreeze.
     * @return True if the unfreeze was successful, otherwise false.
     * 
     * @since 1.4.0
     */
    boolean unfreeze(IPlayer player);

}
