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

package com.dscalzi.skychanger.bukkit.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dscalzi.skychanger.bukkit.api.SkyChanger;
import com.dscalzi.skychanger.core.api.SkyAPI;
import com.dscalzi.skychanger.core.api.SkyPacket;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import com.dscalzi.skychanger.core.internal.manager.MessageManager;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SkyChangeImpl implements SkyAPI {

    public static final List<String> FREEZE_UNSUPPORTED = Stream.of("1.8", "1.13").collect(Collectors.toList());

    /* API Methods */

    @Override
    public boolean changeSky(IPlayer p, float number) {
        return changeSky(p, SkyPacket.RAIN_LEVEL_CHANGE, number);
    }

    @Override
    public boolean changeSky(IPlayer p, SkyPacket packet, float number) {
        try {
            int major = ReflectionUtil.getMajor(), minor = ReflectionUtil.getMinor();

            Object payload = null;

            if(major == 1) {
                if(minor >= 17) {
                    payload = createPacket_117_plus(packet.getValue(), number);
                } else if(minor >= 16) {
                    payload = createPacket_116(packet.getValue(), number);
                } else {
                    payload = createPacket_18_to_115(packet.getValue(), number);
                }
            }

            if(payload != null) {
                if(minor >= 17) {
                    deliverPacket(payload, (Player)p.getOriginal());
                } else {
                    deliverPacketLegacy(payload, (Player)p.getOriginal());
                }
                return true;
            } else {
                MessageManager.getInstance().logPacketError();
                return false;
            }

        } catch(Throwable t) {
            MessageManager.getInstance().logPacketError();
            t.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean freeze(IPlayer p) {
        return sendFreezePacket((Player)p.getOriginal());
    }

    @Override
    public boolean unfreeze(IPlayer p) {
        return p.teleport(p.getLocation());
    }

    /* NMS Utility */

    protected Object getConnectionLegacy(Player player) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Class<?> ocbPlayer = ReflectionUtil.getOCBClass("entity.CraftPlayer");
        Method getHandle = ReflectionUtil.getMethod(ocbPlayer, "getHandle");
        Object nmsPlayer = Objects.requireNonNull(getHandle).invoke(player);
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        return conField.get(nmsPlayer);
    }

    protected Object getConnection(Player player) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Class<?> ocbPlayer = ReflectionUtil.getOCBClass("entity.CraftPlayer");
        Method getHandle = ReflectionUtil.getMethod(ocbPlayer, "getHandle");
        Object nmsPlayer = Objects.requireNonNull(getHandle).invoke(player);
        Field conField = nmsPlayer.getClass().getField("b");
        return conField.get(nmsPlayer);
    }

    protected void deliverPacketLegacy(Object packet, Player player) throws NoSuchMethodException,
            IllegalAccessException, NoSuchFieldException, InvocationTargetException {
        Method sendPacket = ReflectionUtil.getNMSClassLegacy("PlayerConnection")
                .getMethod( "sendPacket", ReflectionUtil.getNMSClassLegacy("Packet"));
        sendPacket.invoke(this.getConnectionLegacy(player), packet);
    }

    protected void deliverPacket(Object packet, Player player) throws NoSuchMethodException,
            IllegalAccessException, NoSuchFieldException, InvocationTargetException {
        Method sendPacket = ReflectionUtil.getMCClass("server.network.PlayerConnection")
                .getMethod( "sendPacket", ReflectionUtil.getMCClass("network.protocol.Packet"));
        sendPacket.invoke(this.getConnection(player), packet);
    }

    /* Sky Change Packet Creation */

    protected Object createPacket_18_to_115(int packetNum, float number) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> ClientboundGameEventPacket = ReflectionUtil.getNMSClassLegacy("PacketPlayOutGameStateChange");
        Constructor<?> packetConstructor = ClientboundGameEventPacket.getConstructor(int.class, float.class);
        return packetConstructor.newInstance(packetNum, number);
    }

    public Object createPacket_116(int packetNum, float number) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> ClientboundGameEventPacket = ReflectionUtil.getNMSClassLegacy("PacketPlayOutGameStateChange");
        Class<?> packetTypeClass = ReflectionUtil.getDeclaredClass(ClientboundGameEventPacket, "a");
        Constructor<?> packetConstructor = ClientboundGameEventPacket.getConstructor(packetTypeClass, float.class);
        Constructor<?> packetTypeConstructor = Objects.requireNonNull(packetTypeClass).getConstructor(int.class);

        Object packetType = packetTypeConstructor.newInstance(packetNum);
        return packetConstructor.newInstance(packetType, number);
    }

    public Object createPacket_117_plus(int packetNum, float number) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> ClientboundGameEventPacket = ReflectionUtil.getMCClass("network.protocol.game.PacketPlayOutGameStateChange");
        Class<?> packetTypeClass = ReflectionUtil.getDeclaredClass(ClientboundGameEventPacket, "a");
        Constructor<?> packetConstructor = ClientboundGameEventPacket.getConstructor(packetTypeClass, float.class);
        Constructor<?> packetTypeConstructor = Objects.requireNonNull(packetTypeClass).getConstructor(int.class);

        Object packetType = packetTypeConstructor.newInstance(packetNum);
        return packetConstructor.newInstance(packetType, number);
    }

    /* Freeze NMS Utility */

    // 1.16+
    private Object getTypeKey(Class<?> WorldClass, Object world) throws InvocationTargetException, IllegalAccessException {
        Method getTypeKey = Objects.requireNonNull(ReflectionUtil.getMethod(WorldClass, "getTypeKey"));
        return getTypeKey.invoke(world);
    }

    private Object getDimensionManager1162Plus(Class<?> WorldClass, Object world) throws InvocationTargetException, IllegalAccessException {
        Method getDimensionManager = Objects.requireNonNull(ReflectionUtil.getMethod(WorldClass, "getDimensionManager"));
        return getDimensionManager.invoke(world);
    }

    // 1.16+
    private Object getDimensionKey(Class<?> WorldClass, Object world) throws InvocationTargetException, IllegalAccessException {
        Method getDimensionKey = Objects.requireNonNull(ReflectionUtil.getMethod(WorldClass, "getDimensionKey"));
        return getDimensionKey.invoke(world);
    }

    private Object getWorldServer(Player player) throws InvocationTargetException, IllegalAccessException {
        Class<?> craftWorldClass = ReflectionUtil.getOCBClass("CraftWorld");
        Method getHandle = Objects.requireNonNull(ReflectionUtil.getMethod(craftWorldClass, "getHandle"));
        return getHandle.invoke(player.getWorld());
    }

    private Object getDimensionManager(Object worldServer) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Class<?> worldProviderClass = ReflectionUtil.getNMSClassLegacy("WorldProvider");
        Class<?> worldClass = ReflectionUtil.getNMSClassLegacy("World");
        Field worldProviderField = worldClass.getDeclaredField("worldProvider");
        Object worldProvider = worldProviderField.get(worldServer);
        Method getDimensionManager = Objects.requireNonNull(ReflectionUtil.getMethod(worldProviderClass, "getDimensionManager"));
        return getDimensionManager.invoke(worldProvider);
    }

    // 1.13, 1.14, 1.15
    private Object getWorldType(Object worldServer) throws InvocationTargetException, IllegalAccessException {
        Class<?> WorldServerClass = ReflectionUtil.getNMSClassLegacy("WorldServer");
        Method getWorldData = Objects.requireNonNull(ReflectionUtil.getMethod(WorldServerClass, "getWorldData"));
        Object worldData = getWorldData.invoke(worldServer);
        Class<?> worldDataClass = ReflectionUtil.getNMSClassLegacy("WorldData");
        Method getType = Objects.requireNonNull(ReflectionUtil.getMethod(worldDataClass, "getType"));
        return getType.invoke(worldData);
    }

    private int getWorldEnvironmentId(Player player) throws InvocationTargetException, IllegalAccessException {
        Method getId = Objects.requireNonNull(ReflectionUtil.getMethod(World.Environment.class, "getId"));
        return (int) getId.invoke(player.getWorld().getEnvironment());
    }

    private int getGameModeValue(Player player) throws InvocationTargetException, IllegalAccessException {
        Method deprecatedGetValue = Objects.requireNonNull(ReflectionUtil.getMethod(GameMode.class, "getValue"));
        return (int) deprecatedGetValue.invoke(player.getGameMode());
    }

    private Object getEnumGamemode(Class<?> EnumGamemodeClass, Player player) throws InvocationTargetException, IllegalAccessException {
        Method gmGetById = Objects.requireNonNull(ReflectionUtil.getMethod(EnumGamemodeClass, "getById", int.class));
        return gmGetById.invoke(null, getGameModeValue(player));
    }

    private Object getEnumDifficulty(Class<?> EnumDifficultyClass, Player player) throws InvocationTargetException, IllegalAccessException {
        Method diffGetById = Objects.requireNonNull(ReflectionUtil.getMethod(EnumDifficultyClass, "getById", int.class));
        Method deprecatedGetValue = Objects.requireNonNull(ReflectionUtil.getMethod(Difficulty.class, "getValue"));
        return diffGetById.invoke(null, deprecatedGetValue.invoke(player.getWorld().getDifficulty()));
    }

    /* Freeze Packet Creation and Dispatch */

    protected boolean sendFreezePacket(Player player) {
        
        int major = ReflectionUtil.getMajor(), minor = ReflectionUtil.getMinor(), r = ReflectionUtil.getR();

        if(FREEZE_UNSUPPORTED.contains(major + "." + minor)) {
            MessageManager.getInstance().featureUnsupported(SkyChanger.wrapPlayer(player), FREEZE_UNSUPPORTED.toString());
        }

        try {

            Object packet;

            Class<?> ClientboundRespawnPacket = minor >= 17
                    ? ReflectionUtil.getMCClass("network.protocol.game.PacketPlayOutRespawn")
                    : ReflectionUtil.getNMSClassLegacy("PacketPlayOutRespawn");

            if (major == 1) {

                if (minor >= 17) {

                    // 1.17

                    Class<?> EnumGamemodeClass = ReflectionUtil.getMCClass("world.level.EnumGamemode");
                    Object worldServer = getWorldServer(player);
                    Object gameMode = getEnumGamemode(EnumGamemodeClass, player);

                    Class<?> WorldClass = ReflectionUtil.getMCClass("world.level.World");
                    Class<?> ResourceKeyClass = ReflectionUtil.getMCClass("resources.ResourceKey");

                    Class<?> DimensionManagerClass = ReflectionUtil.getMCClass("world.level.dimension.DimensionManager");

                    Constructor<?> packetConstructor = ClientboundRespawnPacket.getConstructor(
                            DimensionManagerClass,            // DimensionManager
                            ResourceKeyClass,                 // DimensionKey
                            long.class,                       // Seed
                            EnumGamemodeClass,                // gameType
                            EnumGamemodeClass,                // previousGameType
                            boolean.class,                    // isDebug
                            boolean.class,                    // isFlat
                            boolean.class);                   // keepAllPlayerData
                    packet = packetConstructor.newInstance(
                            getDimensionManager1162Plus(WorldClass, worldServer),
                            getDimensionKey(WorldClass, worldServer),
                            player.getWorld().getSeed(),
                            gameMode,
                            gameMode,
                            false,
                            false,
                            true);



                } else if (minor >= 16) {

                    // 1.16
                    // Works sometimes so let's just say it works.

                    Class<?> EnumGamemodeClass = ReflectionUtil.getNMSClassLegacy("EnumGamemode");
                    Object worldServer = getWorldServer(player);
                    Object gameMode = getEnumGamemode(EnumGamemodeClass, player);

                    Class<?> WorldClass = ReflectionUtil.getNMSClassLegacy("World");
                    Class<?> ResourceKeyClass = ReflectionUtil.getNMSClassLegacy("ResourceKey");

                    if(r >= 2) {

                        // 1.16.2+

                        Class<?> DimensionManagerClass = ReflectionUtil.getNMSClassLegacy("DimensionManager");

                        Constructor<?> packetConstructor = ClientboundRespawnPacket.getConstructor(
                                DimensionManagerClass,            // DimensionManager
                                ResourceKeyClass,                 // DimensionKey
                                long.class,                       // Seed
                                EnumGamemodeClass,                // gameType
                                EnumGamemodeClass,                // previousGameType
                                boolean.class,                    // isDebug
                                boolean.class,                    // isFlat
                                boolean.class);                   // keepAllPlayerData
                        packet = packetConstructor.newInstance(
                                getDimensionManager1162Plus(WorldClass, worldServer),
                                getDimensionKey(WorldClass, worldServer),
                                player.getWorld().getSeed(),
                                gameMode,
                                gameMode,
                                false,
                                false,
                                true);

                    } else {

                        // 1.16.1

                        Constructor<?> packetConstructor = ClientboundRespawnPacket.getConstructor(
                                ResourceKeyClass,                 // DimensionType
                                ResourceKeyClass,                 // DimensionKey
                                long.class,                       // Seed
                                EnumGamemodeClass,                // gameType
                                EnumGamemodeClass,                // previousGameType
                                boolean.class,                    // isDebug
                                boolean.class,                    // isFlat
                                boolean.class);                   // keepAllPlayerData
                        packet = packetConstructor.newInstance(
                                getTypeKey(WorldClass, worldServer),
                                getDimensionKey(WorldClass, worldServer),
                                player.getWorld().getSeed(),
                                gameMode,
                                gameMode,
                                false,
                                false,
                                true);

                    }

                } else if (minor >= 13) {

                    // 1.13, 1.14, 1.15

                    Class<?> EnumGamemodeClass = ReflectionUtil.getNMSClassLegacy("EnumGamemode");

                    Object worldServer = getWorldServer(player);

                    Class<?> DimensionManagerClass = ReflectionUtil.getNMSClassLegacy("DimensionManager");
                    Class<?> WorldTypeClass = ReflectionUtil.getNMSClassLegacy("WorldType");

                    if (minor == 15) {
                        // 1.15 Constructor

                        Constructor<?> packetConstructor = ClientboundRespawnPacket.getConstructor(
                                DimensionManagerClass,
                                long.class,
                                WorldTypeClass,
                                EnumGamemodeClass);
                        packet = packetConstructor.newInstance(
                                getDimensionManager(worldServer),
                                player.getWorld().getSeed(),
                                getWorldType(worldServer),
                                getEnumGamemode(EnumGamemodeClass, player));
                    } else if (minor == 14) {
                        // 1.14 Constructor

                        Constructor<?> packetConstructor = ClientboundRespawnPacket.getConstructor(
                                DimensionManagerClass,
                                WorldTypeClass,
                                EnumGamemodeClass);
                        packet = packetConstructor.newInstance(
                                getDimensionManager(worldServer),
                                getWorldType(worldServer),
                                getEnumGamemode(EnumGamemodeClass, player));
                    } else {
                        // 1.13 Constructor
                        // Does not produce desired effect on 1.13

                        Class<?> EnumDifficultyClass = ReflectionUtil.getNMSClassLegacy("EnumDifficulty");

                        Constructor<?> packetConstructor = ClientboundRespawnPacket.getConstructor(
                                DimensionManagerClass,
                                EnumDifficultyClass,
                                WorldTypeClass,
                                EnumGamemodeClass);
                        packet = packetConstructor.newInstance(
                                getDimensionManager(worldServer),
                                getEnumDifficulty(EnumDifficultyClass, player),
                                getWorldType(worldServer),
                                getEnumGamemode(EnumGamemodeClass, player));
                    }


                } else {

                    // 1.12 and Below
                    // 1.8, 1.9, 1.10, 1.11, 1.12

                    Class<?> EnumDifficultyClass = ReflectionUtil.getNMSClassLegacy("EnumDifficulty");
                    Class<?> WorldTypeClass = ReflectionUtil.getNMSClassLegacy("WorldType");
                    final Object WorldType_NORMAL = WorldTypeClass.getField("NORMAL").get(null);


                    if(minor >= 10) {
                        // 1.10 - 1.12 Constructor

                        Class<?> EnumGamemodeClass = ReflectionUtil.getNMSClassLegacy("EnumGamemode");

                        Constructor<?> packetConstructor = ClientboundRespawnPacket.getConstructor(int.class, EnumDifficultyClass, WorldTypeClass, EnumGamemodeClass);
                        packet = packetConstructor.newInstance(
                                getWorldEnvironmentId(player),
                                getEnumDifficulty(EnumDifficultyClass, player),
                                WorldType_NORMAL,
                                getEnumGamemode(EnumGamemodeClass, player));
                    } else {
                        // 1.8 - 1.9 Constructor

                        Class<?> WorldSettingsClass = ReflectionUtil.getNMSClassLegacy("WorldSettings");
                        Class<?> EnumGamemodeClass_Declared = ReflectionUtil.getDeclaredClass(WorldSettingsClass, "EnumGamemode");
                        Method getById = Objects.requireNonNull(ReflectionUtil.getMethod(EnumGamemodeClass_Declared, "getById", int.class));

                        Constructor<?> packetConstructor = ClientboundRespawnPacket.getConstructor(int.class, EnumDifficultyClass, WorldTypeClass, EnumGamemodeClass_Declared);
                        packet = packetConstructor.newInstance(
                                getWorldEnvironmentId(player),
                                getEnumDifficulty(EnumDifficultyClass, player),
                                WorldType_NORMAL,
                                getById.invoke(null, getGameModeValue(player)));
                    }

                }


            } else {
                // Minecraft 2? Wow
                MessageManager.getInstance().logPacketError();
                return false;
            }

            if(minor >= 17) {
                deliverPacket(packet, player);
            } else {
                deliverPacketLegacy(packet, player);
            }
            player.updateInventory();

            return true;

        } catch(Throwable t) {
            MessageManager.getInstance().logPacketError();
            t.printStackTrace();
            return false;
        }

    }

}
