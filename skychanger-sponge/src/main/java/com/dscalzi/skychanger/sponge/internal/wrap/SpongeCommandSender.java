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

package com.dscalzi.skychanger.sponge.internal.wrap;

import com.dscalzi.skychanger.core.internal.wrap.ICommandSender;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.block.entity.CommandBlock;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.Subject;

public class SpongeCommandSender extends SpongePermissible implements ICommandSender {

    private final Audience audience;

    protected SpongeCommandSender(Subject subject, Audience audience) {
        super(subject);
        this.audience = audience;
    }

    public static SpongeCommandSender of(CommandCause commandSource) {
        if(commandSource == null) {
            return null;
        }
        Subject subject = commandSource.subject();
        if(subject instanceof Player) {
            return SpongePlayer.of((ServerPlayer) subject);
        } else if(subject instanceof CommandBlock) {
            return SpongeCommandBlock.of((CommandBlock) subject, commandSource.audience());
        } else {
            return new SpongeCommandSender(subject, commandSource.audience());
        }
    }

    @Override
    public boolean isConsole() {
        return s instanceof SystemSubject;
    }

    @Override
    public boolean isCommandBlock() {
        return s instanceof CommandBlock;
    }

    @Override
    public boolean isPlayer() {
        return s instanceof Player;
    }

    @Override
    public void sendMessage(String msg) {
        if(audience != null) {
            audience.sendMessage(Identity.nil(), LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
        }
    }
}
