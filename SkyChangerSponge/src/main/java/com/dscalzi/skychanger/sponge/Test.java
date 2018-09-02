package com.dscalzi.skychanger.sponge;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChangeGameState;

@Plugin(id = "skychanger")
public class Test {

    @Inject Game game;
    @Inject Logger logger;
    
    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        logger.info("sending packet to : " + event.getTargetEntity().getName());
        sendPacket(event.getTargetEntity());
    }

    public void sendPacket(Player player) {
        EntityPlayerMP p = (EntityPlayerMP)player;
        for(Field f : p.getClass().getFields()) {
            System.out.println(f.getName());
        }
        p.connection.sendPacket(new SPacketChangeGameState(7, 3F));
    }
    
}
