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

package com.dscalzi.skychanger.sponge.internal.wrap;

import com.dscalzi.skychanger.core.internal.wrap.ICommandBlock;
import com.dscalzi.skychanger.core.internal.wrap.ILocation;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.block.entity.CommandBlock;

public class SpongeCommandBlock extends SpongeCommandSender implements ICommandBlock {

    private final CommandBlock cbs;

    private SpongeCommandBlock(CommandBlock commandBlock) {
        this(commandBlock, null);
    }

    private SpongeCommandBlock(CommandBlock commandBlock, Audience audience) {
        super(commandBlock, audience);
        this.cbs = commandBlock;
    }

    public static SpongeCommandBlock of(CommandBlock commandBlock) {
        return commandBlock == null ? null : new SpongeCommandBlock(commandBlock);
    }

    public static SpongeCommandBlock of(CommandBlock commandBlock, Audience audience) {
        return commandBlock == null ? null : new SpongeCommandBlock(commandBlock, audience);
    }

    @Override
    public ILocation getLocation() {
        return SpongeLocation.of(cbs.location());
    }
}
