/*
 * This file is part of SkyChanger, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2018 Daniel D. Scalzi <https://github.com/dscalzi/SkyChanger>
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

package com.dscalzi.bukkit.skychanger.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.dscalzi.bukkit.skychanger.api.SkyAPI;
import com.dscalzi.bukkit.skychanger.managers.MessageManager;

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
        return p.teleport(p.getLocation());
    }

    protected Object getConnection(Player player) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        Class<?> ocbPlayer = ReflectionUtil.getOCBClass("entity.CraftPlayer");
        Method getHandle = ReflectionUtil.getMethod(ocbPlayer, "getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        Object con = conField.get(nmsPlayer);
        return con;
    }

    protected boolean sendPacket(Player player, float number) {
        try {
            Class<?> packetClass = ReflectionUtil.getNMSClass("PacketPlayOutGameStateChange");
            Constructor<?> packetConstructor = packetClass.getConstructor(int.class, float.class);
            Object packet = packetConstructor.newInstance(7, number);
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
