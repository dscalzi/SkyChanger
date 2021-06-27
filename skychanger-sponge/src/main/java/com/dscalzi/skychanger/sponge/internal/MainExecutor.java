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

import com.dscalzi.skychanger.core.internal.command.CommandAdapter;
import com.dscalzi.skychanger.sponge.SkyChangerPlugin;
import com.dscalzi.skychanger.sponge.internal.wrap.SpongeCommandSender;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.parameter.ArgumentReader;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainExecutor implements Command.Raw {

    private final CommandAdapter adapter;
    
    public MainExecutor(SkyChangerPlugin plugin) {
        this.adapter = new CommandAdapter(plugin);
    }
    
    @Override
    public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) {
        String argStr = arguments.remaining();
        final String[] args = argStr.isEmpty() ? new String[0] : argStr.replaceAll("\\s{2,}", " ").split(" ");

        adapter.resolve(SpongeCommandSender.of(cause), args);

        return CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) {
        String argStr = arguments.remaining();
        String[] argsDirty = argStr.replaceAll("\\s{2,}", " ").split(" ");
        String[] args = argStr.endsWith(" ") ? new String[argsDirty.length + 1] : argsDirty;
        if(args != argsDirty) {
            System.arraycopy(argsDirty, 0, args, 0, argsDirty.length);
            args[args.length-1] = "";
        }

        return adapter.tabComplete(SpongeCommandSender.of(cause), args).stream().map(CommandCompletion::of).collect(Collectors.toList());
    }

    @Override
    public boolean canExecute(CommandCause cause) {
        return true;
    }

    @Override
    public Optional<Component> shortDescription(CommandCause cause) {
        return Optional.of(Component.text("Change the color of the sky."));
    }

    @Override
    public Optional<Component> extendedDescription(CommandCause cause) {
        return Optional.of(Component.text("Change the color of the sky."));
    }

    @Override
    public Optional<Component> help(@SuppressWarnings("NullableProblems") CommandCause cause) {
        Component t = Component.text("Run /SkyChanger to view usage.");
        return Optional.of(t);
    }

    @Override
    public Component usage(CommandCause cause) {
        return Component.text("/SkyChanger <args>");
    }

}
