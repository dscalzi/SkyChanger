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

package com.dscalzi.skychanger.bukkit.internal.wrap;

import com.dscalzi.skychanger.core.internal.wrap.IOfflinePlayer;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class BukkitOfflinePlayer implements IOfflinePlayer {

    private OfflinePlayer op;

    private BukkitOfflinePlayer(OfflinePlayer offlinePlayer) {
        this.op = offlinePlayer;
    }

    public static BukkitOfflinePlayer of(OfflinePlayer offlinePlayer) {
        return offlinePlayer == null ? null : new BukkitOfflinePlayer(offlinePlayer);
    }

    @Override
    public UUID getUniqueId() {
        return op.getUniqueId();
    }

    @Override
    public boolean isOnline() {
        return op.isOnline();
    }

    @Override
    public IPlayer getPlayer() {
        return BukkitPlayer.of(op.getPlayer());
    }

    @Override
    public String getName() {
        return op.getName();
    }
}
