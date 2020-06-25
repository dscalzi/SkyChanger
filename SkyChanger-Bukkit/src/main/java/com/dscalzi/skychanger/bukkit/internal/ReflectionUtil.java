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

package com.dscalzi.skychanger.bukkit.internal;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

public final class ReflectionUtil {

    // Caches
    private static String version;
    private static int major;
    private static int minor;

    private final static Map<String, Class<?>> nmsClasses;
    private final static Map<String, Class<?>> ocbClasses;
    private final static Map<Class<?>, Map<String, Class<?>>> declaredClasses;

    private final static Map<Class<?>, Map<String, Method>> cachedMethods;

    static {
        nmsClasses = new HashMap<>();
        ocbClasses = new HashMap<>();
        declaredClasses = new HashMap<>();
        cachedMethods = new HashMap<>();
    }

    public static String getVersion() {
        if (version == null) {
            String declaration = Bukkit.getServer().getClass().getPackage().getName();
            version = declaration.substring(declaration.lastIndexOf('.') + 1) + ".";
            String[] pts = version.substring(1).split("_");
            major = Integer.parseInt(pts[0]);
            minor = Integer.parseInt(pts[1]);
        }
        return version;
    }
    
    public static int getMajor() {
        if(version == null) {
            getVersion();
        }
        return major;
    }
    
    public static int getMinor() {
        if(version == null) {
            getVersion();
        }
        return minor;
    }

    public static Class<?> getNMSClass(String localPackage) {

        if (nmsClasses.containsKey(localPackage))
            return nmsClasses.get(localPackage);

        String declaration = "net.minecraft.server." + getVersion() + localPackage;
        Class<?> clazz;

        try {
            clazz = Class.forName(declaration);
        } catch (Throwable e) {
            e.printStackTrace();
            return nmsClasses.put(localPackage, null);
        }

        nmsClasses.put(localPackage, clazz);
        return clazz;
    }

    public static Class<?> getOCBClass(String localPackage) {

        if (ocbClasses.containsKey(localPackage))
            return ocbClasses.get(localPackage);

        String declaration = "org.bukkit.craftbukkit." + getVersion() + localPackage;
        Class<?> clazz;

        try {
            clazz = Class.forName(declaration);
        } catch (Throwable e) {
            e.printStackTrace();
            return ocbClasses.put(localPackage, null);
        }

        ocbClasses.put(localPackage, clazz);
        return clazz;
    }

    public static Class<?> getDeclaredClass(Class<?> origin, String className) {
        if (!declaredClasses.containsKey(origin))
            declaredClasses.put(origin, new HashMap<>());

        Map<String, Class<?>> classMap = declaredClasses.get(origin);

        if (classMap.containsKey(className))
            return classMap.get(className);

        for(Class<?> clazz : origin.getDeclaredClasses()) {
            if(clazz.getSimpleName().equals(className)) {
                classMap.put(className, clazz);
                declaredClasses.put(origin, classMap);
                return clazz;
            }
        }

        classMap.put(className, null);
        declaredClasses.put(origin, classMap);
        return null;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        if (!cachedMethods.containsKey(clazz))
            cachedMethods.put(clazz, new HashMap<>());

        Map<String, Method> methods = cachedMethods.get(clazz);

        if (methods.containsKey(methodName))
            return methods.get(methodName);

        try {
            Method method = clazz.getMethod(methodName, params);
            methods.put(methodName, method);
            cachedMethods.put(clazz, methods);
            return method;
        } catch (Throwable e) {
            e.printStackTrace();
            methods.put(methodName, null);
            cachedMethods.put(clazz, methods);
            return null;
        }
    }

}
