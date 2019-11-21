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

package com.dscalzi.skychanger.sponge.internal;

import com.dscalzi.skychanger.core.api.SkyAPI;
import com.dscalzi.skychanger.core.api.SkyPacket;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import org.spongepowered.api.entity.living.player.Player;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

public class SkyChangeImpl implements SkyAPI {

    @Override
    public boolean changeSky(IPlayer p, float number) {
        return changeSky(p, SkyPacket.FADE_VALUE, number);
    }

    @Override
    public boolean changeSky(IPlayer p, SkyPacket packet, float number) {
        return sendPacket((Player)p.getOriginal(), packet.getValue(), number);
    }

    @Override
    public boolean freeze(IPlayer p) {
        return sendFreezePacket((Player)p.getOriginal());
    }

    @Override
    public boolean unfreeze(IPlayer p) {
        return p.teleport(p.getLocation());
    }

    private boolean sendPacket(Player player, int stateIn, float number) {
        ((EntityPlayerMP)player).connection.sendPacket(new SPacketChangeGameState(stateIn, number));
        return true;
    }

    private boolean sendFreezePacket(Player player) {
        World w = (World)player.getLocation().getExtent();
        EntityPlayerMP xP = ((EntityPlayerMP)player);
        xP.connection.sendPacket(new SPacketRespawn(w.provider.getDimensionType().getId(), w.getDifficulty(), WorldType.DEFAULT, ((EntityPlayerMP)player).interactionManager.getGameType()));
        xP.connection.sendPacket(new SPacketWindowItems(xP.inventoryContainer.windowId, xP.inventoryContainer.getInventory()));
        xP.connection.sendPacket(new SPacketSetSlot(-1, -1, xP.inventory.getCurrentItem()));
        return true;
    }

}
