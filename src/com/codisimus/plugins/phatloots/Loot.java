package com.codisimus.plugins.phatloots;

/**
 * A Loot is a ItemStack and with a probability of looting
 *
 * @author Codisimus
 */
public abstract class Loot implements Comparable {
    double probability;

    /**
     * Returns the chance of looting
     *
     * @return The chance of looting
     */
    public double getProbability() {
        return this.probability;
    }

    /**
     * Sets the Probability of Looting the Item
     *
     * @param probability The Probability to be set
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }

    public boolean rollForLoot(double lootingBonus) {
        return roll() < (probability - lootingBonus);
    }

    static double roll() {
        return PhatLoots.random.nextInt(100) + PhatLoots.random.nextDouble();
    }


    @Override
    public int compareTo(Object object) {
        if ((object instanceof Loot)) {
            Loot loot = (Loot) object;
            if (loot.probability < probability) {
                return 1;
            }
            if (loot.probability > probability) {
                return -1;
            }
            if (equals(loot)) {
                return 0;
            }
        }
        return -1;
    }
}
