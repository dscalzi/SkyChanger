/*
 * SkyChanger
 * Copyright (C) 2017-2018 Daniel D. Scalzi
 * See LICENSE for license information.
 */
package com.dscalzi.skychanger.api;

import org.bukkit.entity.Player;

public interface SkyAPI {

    /**
     * Change the sky for a player.
     * 
     * @param player
     *            The target of the sky change.
     * @param number
     *            The packet number which will determine the type of sky.
     * @return True if the sky change was successful, otherwise false.
     * 
     * @since 1.4.0
     */
    public boolean changeSky(Player player, float number);

    /**
     * Freeze a player.
     * 
     * @param player
     *            The targer of the freeze.
     * @return True if the freeze was successful, otherwise false.
     * 
     * @since 1.4.0
     */
    public boolean freeze(Player player);

    /**
     * Unfreeze a player.
     * 
     * @param player
     *            The target of the unfreeze.
     * @return True if the unfreeze was successful, otherwise false.
     * 
     * @since 1.4.0
     */
    public boolean unfreeze(Player player);

}
