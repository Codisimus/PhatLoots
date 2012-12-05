package com.codisimus.plugins.phatloots;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * A Loot is a ItemStack and with a probability of looting
 *
 * @author Codisimus
 */
public class Loot {
    private final String TITLE = "title";
    private final String AUTHOR = "author";
    private final String PAGES = "pages";
    private final String PAGE = "page";
    private ItemStack item;
    private int bonus = 0;
    private double probability;
    protected String name;

    /**
     * Constructs a new Loot with the given Item data and probability
     *
     * @param id The Material id of the item
     * @param amountLower The lower bound of the stack size of the item
     * @param amountUpper The upper bound of the stack size of the item
     * @param probability The chance of looting the item
     */
    public Loot(int id, int amountLower, int amountUpper) {
        item = new ItemStack(id, amountLower);
        bonus = amountUpper - amountLower;
    }

    /**
     * Constructs a new Loot with the given ItemStack and probability
     *
     * @param item The ItemStack that will be looted
     * @param bonus The amount of extra Items that may be looted
     * @param probability The chance of looting the item
     */
    public Loot(ItemStack item, int bonus) {
        this.item = item;
        this.bonus = bonus;
    }

    /**
     * Sets the Probability of Looting the Item
     *
     * @param probability The Probability to be set
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }

    /**
     * Sets the Data/Durability value of the Loot Item
     *
     * @param durability The Durability to be set
     */
    public void setDurability(short durability) {
        if (durability >= 0) {
            item.setDurability(durability);
        }
    }

    /**
     * Adds Enchantments to the Loot Item
     *
     * @param enchantments The Enchantments to be added and their levels
     */
    public void setEnchantments(Map<Enchantment, Integer> enchantments) {
        if (enchantments != null) {
            item.addUnsafeEnchantments(enchantments);
        }
    }

    /**
     * Set the name of the description file for the Loot
     *
     * @param name The Name of the Item Description File
     * @return False if the description file still needs to be created
     */
    public boolean setName(String name) {
        //Check if the Loot is a WrittenBook
        if (item.getType() == Material.WRITTEN_BOOK) {
            if (!(item instanceof CraftItemStack)) {
                item = new CraftItemStack(item);
            }
            CraftItemStack cis = (CraftItemStack) item;
            Properties book = new Properties();
            NBTTagCompound tag = cis.getHandle().getTag();

            //Store the Title and Author of the Book
            String title = tag.getString(TITLE);
            book.setProperty(TITLE, title);
            book.setProperty(AUTHOR, tag.getString(AUTHOR));

            //Store all the Pages
            NBTTagList pages = tag.getList(PAGES);
            for (int i = 0; i < pages.size(); i++) {
                NBTTagString page = (NBTTagString) pages.get(i);
                book.setProperty(PAGE + i, page.toString());
            }

            //Write the Book Properties to file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(PhatLoots.dataFolder
                        + "/Books/" + title + ".properties");
                book.store(fos, null);
                fos.close();
            } catch (Exception saveFailed) {
                PhatLoots.logger.severe("Failed to write Book to File");
                saveFailed.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }

            this.name = title;
            return true;
        } else {
            this.name = name;
            if (name.isEmpty()) {
                return true;
            } else {
                return new File(PhatLoots.dataFolder
                        + "/Item Descriptions/" + name + ".txt").exists();
            }
        }
    }

    /**
     * Returns the item with the bonus amount
     *
     * @return The item with the bonus amount
     */
    public ItemStack getItem() {
        ItemStack clone = item.clone();

        if (bonus > 0) {
            clone.setAmount(clone.getAmount() + PhatLoots.random.nextInt(bonus));
        }

        if (!name.isEmpty()) {
            if (!(clone instanceof CraftItemStack)) {
                clone = new CraftItemStack(clone);
            }
            CraftItemStack cis = (CraftItemStack) clone;

            NBTTagCompound tag = cis.getHandle().getTag();
            //If the tag doesnt exist, create one.
            if (tag == null) {
                cis.getHandle().setTag(new NBTTagCompound());
                tag = cis.getHandle().getTag();
            }

            //Check if the Loot is a WrittenBook
            if (clone.getType() == Material.WRITTEN_BOOK) {
                //Load the contents of the Book from it's File
                Properties book = new Properties();
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(PhatLoots.dataFolder
                            + "/Books/" + name + ".properties");
                    book.load(fis);
                } catch (Exception loadFailed) {
                    PhatLoots.logger.severe("Failed to load Book" + name);
                    loadFailed.printStackTrace();
                } finally {
                    try {
                        fis.close();
                    } catch (Exception e) {
                    }
                }

                //Set the Title and Author of the Book
                tag.setString(TITLE, book.getProperty(TITLE));
                tag.setString(AUTHOR, book.getProperty(AUTHOR));

                if (!tag.hasKey(PAGES)) {
                    tag.set(PAGES, new NBTTagList());
                }
                NBTTagList pages = tag.getList(PAGES);

                //Set all the Pages
                for (int i = 0; book.containsKey(PAGE + i); i++) {
                    NBTTagString page = new NBTTagString("", book.getProperty(PAGE + i));
                    pages.add(page);
                }
            } else {
                File file = new File(PhatLoots.dataFolder
                        + "/Item Descriptions/" + name + ".txt");
                if (file.exists()) {
                    FileReader fReader = null;
                    BufferedReader bReader = null;
                    try {
                        fReader = new FileReader(file);
                        bReader = new BufferedReader(fReader);

                        if (!tag.hasKey("display")) {
                            tag.set("display", new NBTTagCompound());
                        }
                        NBTTagCompound display = tag.getCompound("display");

                        if (!display.hasKey("Lore")) {
                            display.set("Lore", new NBTTagList());
                        }
                        NBTTagList loreList = display.getList("Lore");

                        String line = bReader.readLine();
                        if (line != null) {
                            //Add color to the Name line
                            if (line.charAt(0) == '&') {
                                line = line.replace('&', '\u00a7');
                            }

                            //Set the Name of the Item
                            display.setString("Name", line);

                            //Add each remaining line of the File
                            while ((line = bReader.readLine()) != null) {
                                line = line.replace('&', '§');
                                loreList.add(new NBTTagString("", line));
                            }
                        }
                    } catch (Exception e) {
                    } finally {
                        try {
                            fReader.close();
                            bReader.close();
                        } catch (Exception ex) {
                        }
                    }
                } else {
                    PhatLoots.logger.severe("The " + name
                            + " Item Description File cannot be found");
                }
            }
        }

        return clone;
    }

    /**
     * Returns the chance of looting
     *
     * @return The chance of looting
     */
    public double getProbability() {
        return probability;
    }

    /**
     * Returns the Enchantments of this Loot as a String in the following format
     * Enchantment1(level)&Enchantment2(level)&Enchantment3(level)...
     *
     * @return The String representation of this Loot's Enchantments
     */
    public String enchantmentsToString() {
        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        String string = "";
        for (Enchantment enchantment: enchantments.keySet()) {
            string += "&" + enchantment.getName();

            int level = enchantments.get(enchantment);
            if (level != enchantment.getStartLevel()) {
                string += "(" + enchantments.get(enchantment) + ")";
            }
        }
        return string.substring(1);
    }

    /**
     * Returns the info of this Loot as a String in the following format
     * [ ] indicates an optional additional field
     * Amount[-Amount] of Name [with data Durability] [with enchantments Enchantment1(level)&Enchantment2(level)...] @ Probability%
     *
     * @return The info of this Loot as a String that is readable by humans
     */
    public String toInfoString() {
        int amount = item.getAmount();
        String string = String.valueOf(amount);

        if (bonus != 0) {
            string += "-" + (amount + bonus);
        }

        string += " of " + (name.isEmpty()
                            ? item.getType().name()
                            : name);

        short durability = item.getDurability();
        if (durability > 0) {
            string += " with data " + durability;
        }

        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (!enchantments.isEmpty()) {
            string += " with enchantments " + enchantmentsToString();
        }

        string += " @ " + (Math.floor(probability) == probability
                           ? String.valueOf((int) probability)
                           : String.valueOf(probability))
                + "%";

        return string;
    }

    /**
     * Returns the String representation of this Loot in the following format
     * [ ] indicates an optional additional field
     * MaterialID[+Name]'Durability[+Enchantment1(level)&Enchantment2(level)...]'Amount[-Amount]'Probability
     *
     * @return The String representation of this Loot
     */
    @Override
    public String toString() {
        String string = String.valueOf(item.getTypeId()); //MaterialID

        //Check if Item has a Description
        if (!name.isEmpty()) {
            string += "+" + name; //:Name
        }

        string += "'" + item.getDurability(); //'Durability

        //Check if Item has Enchantments
        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (!enchantments.isEmpty()) {
            string += "+" + enchantmentsToString(); //+Enchantment1(level)&Enchantment2(level)...
        }

        string += "'" + item.getAmount(); //'Amount

        //Check if Amount is a range
        if (bonus != 0) {
            string += "-" + (item.getAmount() + bonus); //-Amount
        }

        string += "'"; //'

        //Check if Probability is an int
        String prob = Math.floor(probability) == probability
                      ? String.valueOf((int) probability) //Remove ".0"
                      : String.valueOf(probability);

        string += prob; //Probability

        return string;
    }

    /**
     * Compares the String representations of each Loot
     *
     * @param object The given Loot
     * @return true if both of the Loot Objects represent the same Loot
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Loot)) {
            return false;
        }
        return ((Loot) object).toString().equals(toString());
    }

    /**
    * Returns the name of the given ItemStack
    * Returns null if the ItemStack provided is null
    * Returns the name from the NBTTag if present
    * Otherwise returns the name of the ItemStack Material
    *
    * @param item The given ItemStack (may be null)
    * @return The name of the item
    */
    public static String getName(ItemStack item) {
        String name = null;
        if (item != null) {
            if (!(item instanceof CraftItemStack)) {
                item = new CraftItemStack(item);
            }
            CraftItemStack cis = (CraftItemStack) item;

            NBTTagCompound tag = cis.getHandle().getTag();
            NBTTagCompound display = null;
            if (tag != null) {
                display = tag.getCompound("display");
            }

            if (display == null) {
                name = item.getType().name().toLowerCase();
            } else {
                name = display.getString("Name");
                if (name.isEmpty()) {
                    name = item.getType().name().toLowerCase();
                }
            }
        }
        return name;
    }
}
