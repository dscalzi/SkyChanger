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

package com.dscalzi.skychanger.bukkit.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.dscalzi.skychanger.core.api.SkyAPI;
import com.dscalzi.skychanger.core.api.SkyPacket;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import com.dscalzi.skychanger.core.internal.manager.MessageManager;
import org.bukkit.World;
import org.bukkit.entity.Player;

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

    protected Object getConnection(Player player) throws SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        Class<?> ocbPlayer = ReflectionUtil.getOCBClass("entity.CraftPlayer");
        Method getHandle = ReflectionUtil.getMethod(ocbPlayer, "getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        Object con = conField.get(nmsPlayer);
        return con;
    }

    protected boolean sendPacket(Player player, int packetNum, float number) {
        try {
            Class<?> packetClass = ReflectionUtil.getNMSClass("PacketPlayOutGameStateChange");
            Constructor<?> packetConstructor = packetClass.getConstructor(int.class, float.class);
            Object packet = packetConstructor.newInstance(packetNum, number);
            Method sendPacket = ReflectionUtil.getNMSClass("PlayerConnection").getMethod("sendPacket",
                    ReflectionUtil.getNMSClass("Packet"));
            sendPacket.invoke(this.getConnection(player), packet);
        } catch (Exception e) {
            MessageManager.getInstance().logPacketError();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    protected boolean sendFreezePacket(Player player) {
        
        int major = ReflectionUtil.getMajor(), minor = ReflectionUtil.getMinor();
        
        // 1.14+ Method
        if(major == 1 && minor >= 14) {
            
            Class<?> packetClass = ReflectionUtil.getNMSClass("PacketPlayOutRespawn");
            Class<?> dimManClass = ReflectionUtil.getNMSClass("DimensionManager");
            Class<?> worldTypeClass = ReflectionUtil.getNMSClass("WorldType");
            Class<?> gameModeClass = ReflectionUtil.getNMSClass("EnumGamemode");
            Method gmGetById = ReflectionUtil.getMethod(gameModeClass, "getById", int.class);
            
            Class<?> worldServerClass = ReflectionUtil.getNMSClass("WorldServer");
            Method getWorldData = ReflectionUtil.getMethod(worldServerClass, "getWorldData");
            Class<?> worldDataClass = ReflectionUtil.getNMSClass("WorldData");
            Method getType = ReflectionUtil.getMethod(worldDataClass, "getType");
            
            
            Class<?> craftWorldClass = ReflectionUtil.getOCBClass("CraftWorld");
            Method getHandle = ReflectionUtil.getMethod(craftWorldClass, "getHandle");
            
            
            
            try {
                // World is CraftWorld
                Object worldServer = getHandle.invoke(player.getWorld());
                Object worldData = getWorldData.invoke(worldServer);
                
                Class<?> worldProviderClass = ReflectionUtil.getNMSClass("WorldProvider");
                Class<?> worldClass = ReflectionUtil.getNMSClass("World");
                Field worldProviderField = worldClass.getDeclaredField("worldProvider");
                Object worldProvider = worldProviderField.get(worldServer);
                
                Method getDimensionManager = ReflectionUtil.getMethod(worldProviderClass, "getDimensionManager");
                
                Object dimensionManager = getDimensionManager.invoke(worldProvider);
                Object worldType = getType.invoke(worldData);
                
                Constructor<?> packetConstructor = packetClass.getConstructor(dimManClass, worldTypeClass, gameModeClass);
                
                Object packet = packetConstructor.newInstance(dimensionManager, worldType, gmGetById.invoke(null, player.getGameMode().getValue()));
                
                Method sendPacket = ReflectionUtil.getNMSClass("PlayerConnection").getMethod("sendPacket",
                        ReflectionUtil.getNMSClass("Packet"));
                sendPacket.invoke(this.getConnection(player), packet);
                
                return true;
                
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                    InvocationTargetException | NoSuchFieldException | InstantiationException e) {
                e.printStackTrace();
                return false;
            }
            
        } else if(major == 1 && minor == 13) {
            
            // Does not produce desired effect on 1.13
            
            Class<?> packetClass = ReflectionUtil.getNMSClass("PacketPlayOutRespawn");
            Class<?> dimManClass = ReflectionUtil.getNMSClass("DimensionManager");
            Class<?> worldTypeClass = ReflectionUtil.getNMSClass("WorldType");
            Class<?> gameModeClass = ReflectionUtil.getNMSClass("EnumGamemode");
            Method gmGetById = ReflectionUtil.getMethod(gameModeClass, "getById", int.class);
            Class<?> diffClass = ReflectionUtil.getNMSClass("EnumDifficulty");
            Method diffGetById = ReflectionUtil.getMethod(diffClass, "getById", int.class);
            
            Class<?> worldServerClass = ReflectionUtil.getNMSClass("WorldServer");
            Method getWorldData = ReflectionUtil.getMethod(worldServerClass, "getWorldData");
            Class<?> worldDataClass = ReflectionUtil.getNMSClass("WorldData");
            Method getType = ReflectionUtil.getMethod(worldDataClass, "getType");
            
            
            Class<?> craftWorldClass = ReflectionUtil.getOCBClass("CraftWorld");
            Method getHandle = ReflectionUtil.getMethod(craftWorldClass, "getHandle");
            
            
            
            try {
                // World is CraftWorld
                Object worldServer = getHandle.invoke(player.getWorld());
                Object worldData = getWorldData.invoke(worldServer);
                
                Class<?> worldProviderClass = ReflectionUtil.getNMSClass("WorldProvider");
                Class<?> worldClass = ReflectionUtil.getNMSClass("World");
                Field worldProviderField = worldClass.getDeclaredField("worldProvider");
                Object worldProvider = worldProviderField.get(worldServer);
                
                Method getDimensionManager = ReflectionUtil.getMethod(worldProviderClass, "getDimensionManager");
                
                Object dimensionManager = getDimensionManager.invoke(worldProvider);
                Object worldType = getType.invoke(worldData);
                
                Constructor<?> packetConstructor = packetClass.getConstructor(dimManClass, diffClass, worldTypeClass, gameModeClass);
                
                Object packet = packetConstructor.newInstance(dimensionManager, diffGetById.invoke(null, player.getWorld().getDifficulty().getValue()), worldType, gmGetById.invoke(null, player.getGameMode().getValue()));
                
                Method sendPacket = ReflectionUtil.getNMSClass("PlayerConnection").getMethod("sendPacket",
                        ReflectionUtil.getNMSClass("Packet"));
                sendPacket.invoke(this.getConnection(player), packet);
                
                return true;
                
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                    InvocationTargetException | NoSuchFieldException | InstantiationException e) {
                e.printStackTrace();
                return false;
            }
            
        } else {
            
            // 1.12 and Below
            try {
                World w = player.getWorld();
                Class<?> packetClass = ReflectionUtil.getNMSClass("PacketPlayOutRespawn");
                Class<?> diffClass = ReflectionUtil.getNMSClass("EnumDifficulty");
                Class<?> wtClass = ReflectionUtil.getNMSClass("WorldType");
                Class<?> gameModeClass = ReflectionUtil.getNMSClass("EnumGamemode");
                Method diffGetById = ReflectionUtil.getMethod(diffClass, "getById", int.class);
                Method gmGetById = ReflectionUtil.getMethod(gameModeClass, "getById", int.class);
                Constructor<?> packetConstructor = null;
                Object packet = null;
                try {
                    packetConstructor = packetClass.getConstructor(int.class, diffClass, wtClass, gameModeClass);
                    packet = packetConstructor.newInstance(w.getEnvironment().getId(),
                            diffGetById.invoke(null, w.getDifficulty().getValue()), wtClass.getField("NORMAL").get(null),
                            gmGetById.invoke(null, player.getGameMode().getValue()));
                } catch (NoSuchMethodException e) {
                    // Try 1.9 method.
                    Class<?> worldSettings = ReflectionUtil.getNMSClass("WorldSettings");
                    Class<?>[] innerClasses = worldSettings.getDeclaredClasses();
                    Class<?> wsGameMode = null;
                    for (Class<?> cl : innerClasses)
                        if (cl.getSimpleName().equals("EnumGamemode"))
                            wsGameMode = cl;
                    Method a = ReflectionUtil.getMethod(worldSettings, "a", int.class);
                    packetConstructor = packetClass.getConstructor(int.class, diffClass, wtClass, wsGameMode);
                    packet = packetConstructor.newInstance(w.getEnvironment().getId(),
                            diffGetById.invoke(null, w.getDifficulty().getValue()), wtClass.getField("NORMAL").get(null),
                            a.invoke(null, player.getGameMode().getValue()));
                }
                Method sendPacket = ReflectionUtil.getNMSClass("PlayerConnection").getMethod("sendPacket",
                        ReflectionUtil.getNMSClass("Packet"));
                sendPacket.invoke(this.getConnection(player), packet);
                player.updateInventory();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchFieldException | SecurityException | NoSuchMethodException e) {
                MessageManager.getInstance().logPacketError();
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

}
