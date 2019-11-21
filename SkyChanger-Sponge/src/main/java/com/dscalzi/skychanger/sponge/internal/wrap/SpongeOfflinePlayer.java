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

package com.dscalzi.skychanger.sponge.internal.wrap;

import com.dscalzi.skychanger.core.internal.wrap.IOfflinePlayer;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public class SpongeOfflinePlayer implements IOfflinePlayer {

    private Player p;

    private SpongeOfflinePlayer(Player p) {
        this.p = p;
    }

    public static SpongeOfflinePlayer of(Player p) {
        return p == null ? null : new SpongeOfflinePlayer(p);
    }

    @Override
    public UUID getUniqueId() {
        return p.getUniqueId();
    }

    @Override
    public boolean isOnline() {
        return p.isOnline();
    }

    @Override
    public IPlayer getPlayer() {
        return SpongePlayer.of(p);
    }

    @Override
    public String getName() {
        return p.getName();
    }

}
