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

import com.dscalzi.skychanger.core.internal.wrap.ILocation;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import com.dscalzi.skychanger.core.internal.wrap.IWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitPlayer extends BukkitCommandSender implements IPlayer {

    private final Player p;

    private BukkitPlayer(Player player) {
        super(player);
        this.p = player;
    }

    public static BukkitPlayer of(Player player) {
        return player == null ? null : new BukkitPlayer(player);
    }

    @Override
    public Object getOriginal() {
        return p;
    }

    @Override
    public IWorld getWorld() {
        return BukkitWorld.of(p.getWorld());
    }

    @Override
    public ILocation getLocation() {
        return BukkitLocation.of(p.getLocation());
    }

    @Override
    public boolean teleport(ILocation loc) {
        return p.teleport((Location)loc.getOriginal());
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
        return this;
    }

    @Override
    public String getName() {
        return p.getName();
    }
}
