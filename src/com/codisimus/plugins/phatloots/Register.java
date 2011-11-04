package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.phatloots.register.payment.Method;

/**
 * Manages payment for buying and selling Chunks
 * Using Nijikokun's Register API
 *
 * @author Codisimus
 */
public class Register {
    public static String economy;
    public static Method econ;

    /**
     * Pays a Player a given amount of money
     * 
     * @param looter The name of the Player to be payed
     * @return The amount of money correctly formatted
     */
    public static String reward(String looter, int reward) {
        econ.getAccount(looter).add(reward);
        return econ.format(reward);
    }
}
