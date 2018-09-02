package com.dscalzi.skychanger.sponge.internal;

import org.spongepowered.api.entity.living.player.Player;

import com.dscalzi.skychanger.sponge.api.SkyAPI;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

public class SkyChangeImpl implements SkyAPI {

    @Override
    public boolean changeSky(Player p, float number) {
        return sendPacket(p, number);
    }

    @Override
    public boolean freeze(Player p) {
        return sendFreezePacket(p);
    }

    @Override
    public boolean unfreeze(Player p) {
        return p.setLocation(p.getLocation());
    }

    protected boolean sendPacket(Player player, float number) {
        ((EntityPlayerMP)player).connection.sendPacket(new SPacketChangeGameState(7, number));
        return true;
    }

    protected boolean sendFreezePacket(Player player) {
        World w = (World)player.getLocation().getExtent();
        ((EntityPlayerMP)player).connection.sendPacket(new SPacketRespawn(w.provider.getDimensionType().getId(), w.getDifficulty(), WorldType.DEFAULT, ((EntityPlayerMP)player).interactionManager.getGameType()));
        return true;
    }

}
