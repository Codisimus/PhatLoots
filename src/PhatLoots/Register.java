
package PhatLoots;

import com.codisimus.phatloots.register.payment.Method;

/**
 *
 * @author Codisimus
 */
public class Register {
    protected static String economy;
    protected static Method econ;

    /**
     * Pays a Player a given amount of money
     * 
     * @param looter The name of the Player to be payed
     * @return The amount of money correctly formatted
     */
    protected static String reward(String looter, int reward) {
        econ.getAccount(looter).add(reward);
        return econ.format(reward);
    }
}
