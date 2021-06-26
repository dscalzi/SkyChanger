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

package com.dscalzi.skychanger.sponge.internal;

import com.dscalzi.skychanger.core.api.SkyAPI;
import com.dscalzi.skychanger.core.api.SkyPacket;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.entity.living.player.Player;

public class SkyChangeImpl implements SkyAPI {

    @Override
    public boolean changeSky(IPlayer p, float number) {
        return changeSky(p, SkyPacket.RAIN_LEVEL_CHANGE, number);
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
        ClientboundGameEventPacket packet = new ClientboundGameEventPacket(new ClientboundGameEventPacket.Type(stateIn), number);
        ((ServerPlayer)player).connection.send(packet);
        return true;
    }

    private boolean sendFreezePacket(Player player) {

        ServerPlayer sp = ((ServerPlayer)player);

        ClientboundRespawnPacket packet = new ClientboundRespawnPacket(
                sp.level.dimensionType(),
                sp.level.dimension(),
                player.world().seed(),
                sp.gameMode.getGameModeForPlayer(),
                sp.gameMode.getPreviousGameModeForPlayer(),
                false,
                false,
                true
        );

        NonNullList<ItemStack> nonNullList = NonNullList.create();
        for(int i = 0; i < sp.inventoryMenu.slots.size(); ++i) {
            nonNullList.add(sp.inventoryMenu.slots.get(i).getItem());
        }

        sp.connection.send(packet);
        sp.connection.send(new ClientboundContainerSetContentPacket(sp.inventoryMenu.containerId, nonNullList));
        sp.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, sp.inventory.getCarried()));
        return true;
    }

}
