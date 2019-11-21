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

package com.dscalzi.skychanger.core.internal.manager;

import com.dscalzi.skychanger.core.internal.wrap.ICommandSender;
import com.dscalzi.skychanger.core.internal.wrap.IPlayer;
import com.dscalzi.skychanger.core.internal.util.IWildcardPermissionUtil;
import com.dscalzi.skychanger.core.internal.wrap.IPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class MessageManager {

    private static boolean initialized;
    private static MessageManager instance;
    private static final char b = (char) 8226;
    private static final String BOLD = "§l";
    private static final String RESET = "§r";

    private IPlugin plugin;
    private final String prefix;
    private final String cPrimary;
    private final String cTrim;
    private final String cMessage;
    private final String cSuccess;
    private final String cError;

    private String lang;
    private Properties props;

    private MessageManager(IPlugin plugin) {
        this.plugin = plugin;
        this.cPrimary = "§7"; // GRAY
        this.cTrim = "§4"; // DARK_RED
        this.cMessage = "§e"; // YELLOW
        this.cSuccess = "§a"; // GREEN
        this.cError = "§c"; // RED
        this.prefix = cPrimary + "| " + cTrim + BOLD + "S" + cTrim + "ky" + BOLD + "C" + cTrim
                + "hanger" + cPrimary + " |" + RESET;

        this.loadLanguage();

        this.plugin.info(getString("message.pluginLoading", plugin.getName()));
    }

    private void loadLanguage() {
        String l = plugin.getConfigManager().getLanguage();
        try (InputStream utf8in = plugin.getClass().getResourceAsStream("/lang/Messages_" + l + ".properties");
             Reader reader = new InputStreamReader(utf8in, StandardCharsets.UTF_8)) {
            props = new Properties();
            props.load(reader);
            lang = l;
        } catch (NullPointerException e) {
            severe("Could not find language file for " + l + ". Defaulting to en_US (English).");
            try (InputStream utf8in = plugin.getClass().getResourceAsStream("/lang/Messages_en_US.properties");
                 Reader reader = new InputStreamReader(utf8in, StandardCharsets.UTF_8)) {
                props = new Properties();
                props.load(reader);
                lang = "en_US";
            } catch (IOException | NullPointerException e1) {
                severe("Fatal error, no valid language file found. This may be due to a server"
                        + " reload or an internal error. Please restart the server. Shutting down..");
                e1.printStackTrace();
                plugin.disableSelf();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initialize(IPlugin plugin) {
        if (!initialized) {
            instance = new MessageManager(plugin);
            initialized = true;
        }
    }

    public static void reload() {
        if (!initialized)
            return;
        getInstance().loadLanguage();
    }

    public static MessageManager getInstance() {
        return MessageManager.instance;
    }

    /* Message Distribution */

    public void sendMessage(ICommandSender sender, String message) {
        sender.sendMessage(prefix + cMessage + " " + message);
    }

    public void sendSuccess(ICommandSender sender, String message) {
        sender.sendMessage(prefix + cSuccess + " " + message);
    }

    public void sendError(ICommandSender sender, String message) {
        sender.sendMessage(prefix + cError + " " + message);
    }

    public void sendGlobal(String message, String permission) {
        for (IPlayer p : plugin.getOnlinePlayers()) {
            if (p.hasPermission(permission)) {
                sendMessage(p, message);
            }
        }
    }

    /* Accessors */

    public String getPrefix() {
        return this.prefix;
    }

    public void severe(String s) {
        plugin.severe(s);
    }

    public String getString(String key, Object... args) {
        String s = props.getProperty(key);
        if (args.length > 0) {
            MessageFormat m = new MessageFormat(s);
            s = m.format(args);
        }
        if (s == null) {
            severe("Missing resource " + key + " for " + lang);
            s = "{" + key + "}";
        }
        return s;
    }

    /* Messages */

    public void helpMessage(ICommandSender sender) {
        final IWildcardPermissionUtil wpu = plugin.getWildcardPermissionUtil();
        final String listPrefix = cMessage + " " + b + " ";

        String header = prefix + cMessage + " " + getString("message.commandList");
        List<String> cmds = new ArrayList<>();

        cmds.add(listPrefix + "/SkyChanger help " + RESET + "- " + getString("message.descHelp"));
        if (sender.hasPermission("skychanger.changesky.self") || sender.hasPermission("skychanger.changesky.others")
                || sender.hasPermission("skychanger.changesky.all")
                || wpu.hasGeneralChangeskyWorldPerm(sender)
                || wpu.hasGeneralChangeskyRadiusPerm(sender)) {
            cmds.add(listPrefix + this.generateChangeSkyUsage(sender) + RESET + " - "
                    + getString("message.descChangeSky"));
        }
        if (sender.hasPermission("skychanger.freeze.self") || sender.hasPermission("skychanger.freeze.others")
                || sender.hasPermission("skychanger.freeze.all")
                || wpu.hasGeneralFreezeWorldPerm(sender)
                || wpu.hasGeneralFreezeRadiusPerm(sender)) {
            cmds.add(listPrefix + this.generateFreezeUsage(sender, false) + RESET + " - "
                    + getString("message.descFreeze"));
            cmds.add(listPrefix + this.generateFreezeUsage(sender, true) + RESET + " - "
                    + getString("message.descUnfreeze"));
        }
        if (sender.hasPermission("skychanger.reload"))
            cmds.add(listPrefix + "/SkyChanger reload " + RESET + "- " + getString("message.descReload"));
        cmds.add(listPrefix + "/SkyChanger version " + RESET + "- " + getString("message.descVersion"));

        sender.sendMessage(header);
        for (String s : cmds)
            sender.sendMessage(s);
    }

    public void noPermission(ICommandSender sender) {
        sendError(sender, getString("error.noPermission"));
    }

    public void denyNonPlayer(ICommandSender sender) {
        sendError(sender, getString("error.denyNonPlayer"));
    }

    private String generateChangeSkyUsage(ICommandSender sender) {
        final IWildcardPermissionUtil wpu = plugin.getWildcardPermissionUtil();
        String u = "/SkyChanger <#> [#]";
        boolean o = sender.hasPermission("skychanger.changesky.others"),
                a = sender.hasPermission("skychanger.changesky.all");
        boolean w = wpu.hasGeneralChangeskyWorldPerm(sender);
        boolean r = wpu.hasGeneralChangeskyRadiusPerm(sender);

        return u + genOpti(sender, o, a, w, r);
    }

    private String generateFreezeUsage(ICommandSender sender, boolean unfreeze) {
        final IWildcardPermissionUtil wpu = plugin.getWildcardPermissionUtil();
        String u = "/SkyChanger " + (unfreeze ? "unfreeze" : "freeze");
        boolean o = sender.hasPermission("skychanger.freeze.others"), a = sender.hasPermission("skychanger.freeze.all");
        boolean w = wpu.hasGeneralFreezeWorldPerm(sender);
        boolean r = wpu.hasGeneralFreezeRadiusPerm(sender);

        return u + genOpti(sender, o, a, w, r);
    }

    public String genOpti(ICommandSender sender, boolean o, boolean a, boolean w, boolean r) {
        String opti = "";
        if (o | a | w | r) {
            boolean flowthrough = false;
            opti += " [";
            if (o) {
                opti += getString("message.player");
                flowthrough = true;
            }
            if (a) {
                if (flowthrough) {
                    opti += " | ";
                }
                opti += "-a";
                flowthrough = true;
            }
            if (w) {
                if (flowthrough) {
                    opti += " | ";
                }
                opti += "-w";
                if (!(sender.isPlayer())) {
                    opti += " <" + getString("message.world") + ">";
                } else {
                    opti += " [" + getString("message.world") + "]";
                }
                flowthrough = true;
            }
            if (r) {
                if (flowthrough) {
                    opti += " | ";
                }
                opti += "-r <#>";
                flowthrough = true;
            }
            opti += "]";
        }
        return opti;
    }

    public void floatingPointOverflow(ICommandSender sender, String request) {
        sendError(sender, getString("error.packetOverflow"));
    }

    public void playerNotFound(ICommandSender sender, String name) {
        sendError(sender, getString("error.playerNotFound", name));
    }

    public void packetSent(ICommandSender sender) {
        sendSuccess(sender, getString("success.packetSent"));
    }

    public void packetSent(ICommandSender sender, String name) {
        sendSuccess(sender, getString("success.packetSentTo", name));
    }

    public void packetUnfreeze(ICommandSender sender) {
        sendSuccess(sender, getString("success.packetUnfreezeSent"));
    }

    public void packetUnfreeze(ICommandSender sender, String name) {
        sendSuccess(sender, getString("success.packetUnfreezeSentTo", name));
    }

    public void radiusFormatError(ICommandSender sender) {
        sendError(sender, getString("error.radiusFormat"));
    }

    public void mustSpecifyRadius(ICommandSender sender) {
        sendError(sender, getString("error.specifyRadius"));
    }

    public void mustSpecifyWorld(ICommandSender sender) {
        sendError(sender, getString("error.specifyWorld"));
    }

    public void worldDoesntExist(ICommandSender sender, String name) {
        sendError(sender, getString("error.worldNotFound", name));
    }

    public void packetError(ICommandSender sender) {
        sendError(sender, getString("error.packetError"));
    }

    public void packetError(ICommandSender sender, String name) {
        sendError(sender, getString("error.packetErrorTo", name));
    }

    public void logPacketError() {
        severe(getString("error.logPacketError"));
    }

    public void outOfBoundsUpper(ICommandSender sender, float upper) {
        sendError(sender, getString("error.outOfBoundsUpper", upper));
    }

    public void outOfBoundsLower(ICommandSender sender, float limit) {
        sendError(sender, getString("error.outOfBoundsLower", limit));
    }

    public void reloadSuccessful(ICommandSender sender) {
        sendSuccess(sender, getString("success.reloadSuccess"));
    }

    public void reloadFailed(ICommandSender sender) {
        sendError(sender, getString("error.reloadFail"));
    }

    public void versionMessage(ICommandSender sender) {
        sendMessage(sender, "SkyChanger " + getString("message.version") + " " + plugin.getVersion()
                + "\n" + cPrimary + "| " + cSuccess + getString("message.metrics") + cPrimary + " | " + cMessage
                + plugin.getMetricsURL() + "\n" + cPrimary + "| " + cSuccess
                + getString("message.source") + cPrimary + " | " + cMessage + plugin.getSourceURL());
    }

    /* Static Utility */

    public static UUID formatFromInput(String uuid) throws IllegalArgumentException {
        if (uuid == null)
            throw new IllegalArgumentException();
        uuid = uuid.trim();
        return uuid.length() == 32 ? fromTrimmed(uuid.replaceAll("-", "")) : UUID.fromString(uuid);
    }

    public static UUID fromTrimmed(String trimmedUUID) throws IllegalArgumentException {
        if (trimmedUUID == null)
            throw new IllegalArgumentException();
        StringBuilder builder = new StringBuilder(trimmedUUID.trim());
        /* Backwards adding to avoid index adjustments */
        try {
            builder.insert(20, "-");
            builder.insert(16, "-");
            builder.insert(12, "-");
            builder.insert(8, "-");
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }

        return UUID.fromString(builder.toString());
    }

    public enum Languages {

        en_US("English", "United States"),
        it_IT("Italiano", "Italia"),
        de_DE("Deutsche", "Deutschland"),
        nl_NL("Nederlands", "Nederland"),
        es_EC("Español", "Ecuador"),
        es_AR("Español", "Argentina"),
        no_NO("Norsk", "Norge"),
        iw_IL("עברית", "ישראל"),
        hu_HU("Magyar", "Magyarország"),
        zh_CN("简体中文", "中国"),
        ja_JP("日本語", "日本");

        private String lang;
        private String country;

        private Languages(String lang, String country) {
            this.lang = lang;
            this.country = country;
        }

        public static Languages getByID(String id) {
            for (Languages l : values())
                if (l.name().equals(id))
                    return l;
            // default to English
            return en_US;
        }

        public String getLanguage() {
            return this.lang;
        }

        public String getCountry() {
            return this.country;
        }

        public String getReadable() {
            return lang + " (" + country + ")";
        }
    }

}
