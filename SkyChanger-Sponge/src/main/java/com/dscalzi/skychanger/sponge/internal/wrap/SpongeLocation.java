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

package com.dscalzi.skychanger.sponge.internal.wrap;

import com.dscalzi.skychanger.core.internal.wrap.ILocation;
import com.dscalzi.skychanger.core.internal.wrap.IWorld;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class SpongeLocation implements ILocation {

    private final Location<World> location;

    private SpongeLocation(Location<World> location) {
        this.location = location;
    }

    public static SpongeLocation of(Location<World> location) {
        return location == null ? null : new SpongeLocation(location);
    }

    @Override
    public Object getOriginal() {
        return location;
    }

    @Override
    public IWorld getWorld() {
        return SpongeWorld.of(location.getExtent());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public double distanceSquared(ILocation o) {
        return location.getPosition().distanceSquared(((Location)o.getOriginal()).getPosition());
    }

}
