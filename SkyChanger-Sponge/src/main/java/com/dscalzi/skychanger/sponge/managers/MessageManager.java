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

package com.dscalzi.skychanger.sponge.managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.dscalzi.skychanger.sponge.SkyChangerPlugin;
import com.dscalzi.skychanger.sponge.internal.WorldPermissionUtil;

public class MessageManager {

    private static boolean initialized;
    private static MessageManager instance;
    private static final char b = (char) 8226;

    private SkyChangerPlugin plugin;
    private final Logger logger;
    private final Marker severe;
    private final Text prefix;
    private final TextColor cPrimary;
    private final TextColor cTrim;
    private final TextColor cMessage;
    private final TextColor cSuccess;
    private final TextColor cError;

    private String lang;
    private Properties props;

    private MessageManager(SkyChangerPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.severe = MarkerFactory.getMarker("Severe");
        this.cPrimary = TextColors.GRAY;
        this.cTrim = TextColors.DARK_RED;
        this.cMessage = TextColors.YELLOW;
        this.cSuccess = TextColors.GREEN;
        this.cError = TextColors.RED;
        this.prefix = Text.of(cPrimary, "| ", cTrim, TextStyles.BOLD, "S", TextStyles.RESET, cTrim, "ky", TextStyles.BOLD, "C", TextStyles.RESET, cTrim
                , "hanger", cPrimary, " |", TextStyles.RESET);

        this.loadLanguage();
        
        this.plugin.getLogger().info(getString("message.pluginLoading", plugin.getPlugin().getName()));
    }

    private void loadLanguage() {
        String l = ConfigManager.getInstance().getLanguage();
        try (InputStream utf8in = plugin.getClass().getResourceAsStream("/lang/Messages_" + l + ".properties");
                Reader reader = new InputStreamReader(utf8in, "UTF-8");) {
            props = new Properties();
            props.load(reader);
            lang = l;
        } catch (NullPointerException e) {
            getLogger().error(severe, "Could not find language file for " + l + ". Defaulting to en_US (English).");
            try (InputStream utf8in = plugin.getClass().getResourceAsStream("/lang/Messages_en_US.properties");
                    Reader reader = new InputStreamReader(utf8in, "UTF-8");) {
                props = new Properties();
                props.load(reader);
                lang = "en_US";
            } catch (IOException | NullPointerException e1) {
                getLogger().error(severe, "Fatal error, no valid language file found. This may be due to a server"
                        + " reload or an internal error. Please restart the server. Shutting down..");
                e1.printStackTrace();
                plugin.disable();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initialize(SkyChangerPlugin plugin) {
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

    public void sendMessage(CommandSource sender, String message) {
        sender.sendMessage(Text.of(prefix, cMessage, " " + message));
    }
    
    public void sendMessage(CommandSource sender, Text message) {
        sender.sendMessage(Text.of(prefix, cMessage, " ", message));
    }

    public void sendSuccess(CommandSource sender, String message) {
        sender.sendMessage(Text.of(prefix, cSuccess, " " + message));
    }

    public void sendError(CommandSource sender, String message) {
        sender.sendMessage(Text.of(prefix, cError, " " + message));
    }

    public void sendGlobal(String message, String permission) {
        for (Player p : plugin.getGame().getServer().getOnlinePlayers()) {
            if (p.hasPermission(permission)) {
                sendMessage(p, message);
            }
        }
    }

    /* Accessors */

    public Text getPrefix() {
        return this.prefix;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public String getString(String key, Object... args) {
        String s = props.getProperty(key);
        if (args.length > 0) {
            MessageFormat m = new MessageFormat(s);
            s = m.format(args);
        }
        if (s == null) {
            getLogger().error(severe, "Missing resource " + key + " for " + lang);
            s = "{" + key + "}";
        }
        return s;
    }

    /* Messages */
    
    public void helpMessage(CommandSource sender) {
        final Text listPrefix = Text.of(cMessage, " " + b + " ");

        Text header = Text.of(prefix, cMessage, " " + getString("message.commandList"));
        List<Text> cmds = new ArrayList<Text>();

        cmds.add(Text.of(listPrefix, cMessage, "/SkyChanger help ", TextColors.NONE, "- " + getString("message.descHelp")));
        if (sender.hasPermission("skychanger.changesky.self") || sender.hasPermission("skychanger.changesky.others")
                || sender.hasPermission("skychanger.changesky.all")
                || WorldPermissionUtil.hasGeneralChangeskyPerm(sender)) {
            cmds.add(Text.of(listPrefix, cMessage, this.generateChangeSkyUsage(sender), TextColors.NONE, " - "
                    + getString("message.descChangeSky")));
        }
        if (sender.hasPermission("skychanger.freeze.self") || sender.hasPermission("skychanger.freeze.others")
                || sender.hasPermission("skychanger.freeze.all") || WorldPermissionUtil.hasGeneralFreezePerm(sender)) {
            cmds.add(Text.of(listPrefix, cMessage, this.generateFreezeUsage(sender, false), TextColors.NONE, " - "
                    + getString("message.descFreeze")));
            cmds.add(Text.of(listPrefix, cMessage, this.generateFreezeUsage(sender, true), TextColors.NONE, " - "
                    + getString("message.descUnfreeze")));
        }
        if (sender.hasPermission("skychanger.reload"))
            cmds.add(Text.of(listPrefix, cMessage, "/SkyChanger reload ", TextColors.NONE, "- " + getString("message.descReload")));
        cmds.add(Text.of(listPrefix, cMessage, "/SkyChanger version ", TextColors.NONE, "- " + getString("message.descVersion")));

        sender.sendMessage(header);
        for (Text t : cmds)
            sender.sendMessage(t);
    }

    public void noPermission(CommandSource sender) {
        sendError(sender, getString("error.noPermission"));
    }

    public void denyNonPlayer(CommandSource sender) {
        sendError(sender, getString("error.denyNonPlayer"));
    }

    private String generateChangeSkyUsage(CommandSource sender) {
        String u = "/SkyChanger <#>";
        boolean o = sender == null ? true : sender.hasPermission("skychanger.changesky.others"),
                a = sender == null ? true : sender.hasPermission("skychanger.changesky.all");
        boolean w = sender == null ? true : WorldPermissionUtil.hasGeneralChangeskyPerm(sender);

        return u + genOpti(sender, o, a, w);
    }

    private String generateFreezeUsage(CommandSource sender, boolean unfreeze) {
        String u = "/SkyChanger " + (unfreeze ? "unfreeze" : "freeze");
        boolean o = sender == null ? true : sender.hasPermission("skychanger.freeze.others"), a = sender == null ? true : sender.hasPermission("skychanger.freeze.all");
        boolean w = sender == null ? true : WorldPermissionUtil.hasGeneralFreezePerm(sender);

        return u + genOpti(sender, o, a, w);
    }

    public String genOpti(CommandSource sender, boolean o, boolean a, boolean w) {
        String opti = "";
        if (o | a | w) {
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
                if (!(sender instanceof Player)) {
                    opti += " <" + getString("message.world") + ">";
                } else {
                    opti += " [" + getString("message.world") + "]";
                }
            }
            opti += "]";
        }
        return opti;
    }

    public void floatingPointOverflow(CommandSource sender, String request) {
        sendError(sender, getString("error.packetOverflow"));
    }

    public void playerNotFound(CommandSource sender, String name) {
        sendError(sender, getString("error.playerNotFound", name));
    }

    public void packetSent(CommandSource sender) {
        sendSuccess(sender, getString("success.packetSent"));
    }

    public void packetSent(CommandSource sender, String name) {
        sendSuccess(sender, getString("success.packetSentTo", name));
    }

    public void packetUnfreeze(CommandSource sender) {
        sendSuccess(sender, getString("success.packetUnfreezeSent"));
    }

    public void packetUnfreeze(CommandSource sender, String name) {
        sendSuccess(sender, getString("success.packetUnfreezeSentTo", name));
    }

    public void mustSpecifyWorld(CommandSource sender) {
        sendError(sender, getString("error.specifyWorld"));
    }

    public void worldDoesntExist(CommandSource sender, String name) {
        sendError(sender, getString("error.worldNotFound", name));
    }

    public void packetError(CommandSource sender) {
        sendError(sender, getString("error.packetError"));
    }

    public void packetError(CommandSource sender, String name) {
        sendError(sender, getString("error.packetErrorTo", name));
    }

    public void logPacketError() {
        getLogger().error(severe, getString("error.logPacketError"));
    }

    public void outOfBoundsUpper(CommandSource sender, float upper) {
        sendError(sender, getString("error.outOfBoundsUpper", upper));
    }

    public void outOfBoundsLower(CommandSource sender, float limit) {
        sendError(sender, getString("error.outOfBoundsLower", limit));
    }

    public void reloadSuccessful(CommandSource sender) {
        sendSuccess(sender, getString("success.reloadSuccess"));
    }

    public void reloadFailed(CommandSource sender) {
        sendError(sender, getString("error.reloadFail"));
    }

    public void versionMessage(CommandSource sender) {
        sendMessage(sender, Text.of("SkyChanger " + getString("message.version") + " " + plugin.getPlugin().getVersion().orElse("dev")
                + "\n", cPrimary, "| ", cSuccess, getString("message.metrics"), cPrimary, " | ", cMessage
                , "https://bstats.org/plugin/bukkit/SkyChanger" + "\n", cPrimary, "| ", cSuccess
                , getString("message.source"), cPrimary, " | ", cMessage, "https://github.com/dscalzi/SkyChanger"));
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

        en_US("English", "United States"), it_IT("Italiano", "Italia"), de_DE("Deutsche", "Deutschland"), nl_NL(
                "Nederlands", "Nederland"), es_EC("Español", "Ecuador"), es_AR("Español", "Argentina"), no_NO("Norsk",
                        "Norge"), iw_IL("עברית", "ישר�?ל"), hu_HU("Magyar", "Magyarország"), zh_CN("简体中文", "中国");

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
