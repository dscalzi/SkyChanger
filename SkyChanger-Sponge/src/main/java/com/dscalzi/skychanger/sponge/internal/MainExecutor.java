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

package com.dscalzi.skychanger.sponge.internal;

import com.dscalzi.skychanger.core.internal.command.CommandAdapter;
import com.dscalzi.skychanger.sponge.SkyChangerPlugin;
import com.dscalzi.skychanger.sponge.internal.wrap.SpongeCommandSender;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public class MainExecutor implements CommandCallable {

    private CommandAdapter adapter;
    
    public MainExecutor(SkyChangerPlugin plugin) {
        this.adapter = new CommandAdapter(plugin);
    }
    
    @Override
    public CommandResult process(CommandSource src, String arguments) throws CommandException {
        final String[] args = arguments.isEmpty() ? new String[0] : arguments.replaceAll("\\s{2,}", " ").split(" ");

        adapter.resolve(SpongeCommandSender.of(src), args);

        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, Location<World> targetPosition)
            throws CommandException {
        String[] argsDirty = arguments.replaceAll("\\s{2,}", " ").split(" ");
        String[] args = arguments.endsWith(" ") ? new String[argsDirty.length + 1] : argsDirty;
        if(args != argsDirty) {
            System.arraycopy(argsDirty, 0, args, 0, argsDirty.length);
            args[args.length-1] = new String();
        }

        return adapter.tabComplete(SpongeCommandSender.of(source), args);
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return true;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Change the color of the sky."));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
    	Text t = Text.of("Run /SkyChanger to view usage.");
        return Optional.of(t);
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("/SkyChanger <args>");
    }

}
