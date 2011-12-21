package com.codisimus.plugins.phatloots;

import net.milkbowl.vault.economy.Economy;

/**
 * Manages payment/rewards of using Warps
 * 
 * @author Codisimus
 */
public class Econ {
    static Economy economy;
    
    /**
     * Pays a Player a given amount of money
     * 
     * @param looter The name of the Player to be payed
     * @return The amount of money correctly formatted
     */
    public static String reward(String looter, int reward) {
        economy.depositPlayer(looter, reward);
        return economy.format(reward).replace(".00", "");
    }
}
