package com.codisimus.plugins.phatloots;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import net.minecraft.server.v1_4_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A Loot is a ItemStack and with a probability of looting
 *
 * @author Codisimus
 */
public class Loot {
    private final String TITLE = "title";
    private final String AUTHOR = "author";
    private final String PAGE = "page";
    private final String ITEM_DESCRIPTION = "item_description";
    private final String SET = "set";
    private final String CLASS = "class";
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
        net.minecraft.server.v1_4_R1.ItemStack mis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = mis.getTag();
        if (tag != null) {
            name = tag.getString(ITEM_DESCRIPTION);
        }
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
            Properties book = new Properties();
            BookMeta bookMeta = (BookMeta) item.getItemMeta();

            //Store the Title and Author of the Book
            String title = bookMeta.getTitle();
            book.setProperty(TITLE, title);
            book.setProperty(AUTHOR, bookMeta.getAuthor());

            //Store all the Pages
            for (int i = 1; i < bookMeta.getPageCount() + 1; i++) {
                book.setProperty(PAGE + i, bookMeta.getPage(i));
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
            } else if (name.equals("Random")) {
                String folder = item.getType() + item.getEnchantments().toString();
                File dir = new File(PhatLoots.dataFolder
                        + "/Item Descriptions/" + folder);
                if (!dir.isDirectory()) {
                    dir.mkdir();
                }
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
            ItemMeta meta = clone.hasItemMeta()
                            ? clone.getItemMeta()
                            : PhatLoots.server.getItemFactory().getItemMeta(clone.getType());

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
                BookMeta bookMeta = (BookMeta) meta;
                bookMeta.setTitle(book.getProperty(TITLE));
                bookMeta.setAuthor(book.getProperty(AUTHOR));

                //Set all the Pages
                for (int i = 1; book.containsKey(PAGE + i); i++) {
                    bookMeta.addPage(book.getProperty(PAGE + i));
                }

                clone.setItemMeta(bookMeta);
            } else {
                String className = null;
                String set = null;

                File file;
                if (name.equals("Random")) {
                    String folder = clone.getType() + clone.getEnchantments().toString();
                    File dir = new File(PhatLoots.dataFolder
                            + "/Item Descriptions/" + folder);
                    File[] files = dir.listFiles();
                    Random random = new Random();
                    file = files[random.nextInt(files.length)];
                    //String fileName = file.getName();
                    //tag.setString(ITEM_DESCRIPTION, fileName.substring(0, fileName.length() - 4));
                } else {
                    file = new File(PhatLoots.dataFolder
                            + "/Item Descriptions/" + name + ".txt");
                }
                if (file.exists()) {
                    FileReader fReader = null;
                    BufferedReader bReader = null;
                    try {
                        fReader = new FileReader(file);
                        bReader = new BufferedReader(fReader);

                        String line = bReader.readLine();
                        if (line != null) {
                            //Add color to the Name line
                            if (line.charAt(0) == '&') {
                                line = line.replace('&', '\u00a7');
                            }

                            //Set the Name of the Item
                            meta.setDisplayName(line);

                            //Add each remaining line of the File
                            List<String> lore = new LinkedList<String>();
                            while ((line = bReader.readLine()) != null) {
                                line = line.replace('&', '§');
                                lore.add(line);

                                //Check if part of a Class or Set
                                if (line.matches("§[0-9a-flno][0-9a-zA-Z]+ Class")) { //Color ClassNameOfLettersAndNumbersOfAnyLength Class
                                    className = line.substring(2, line.length() - 6);
                                } else if (line.matches("§[0-9a-flno][0-9a-zA-Z]+ Set")) { //Color SetNameOfLettersAndNumbersOfAnyLength Set
                                    set = line.substring(2, line.length() - 4);
                                }
                            }
                            meta.setLore(lore);
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

                clone.setItemMeta(meta);

                net.minecraft.server.v1_4_R1.ItemStack mis = CraftItemStack.asNMSCopy(clone);
                NBTTagCompound tag = mis.getTag();
                if (tag == null) {
                    tag = new NBTTagCompound();
                }

                tag.setString(ITEM_DESCRIPTION, name);
                if (className != null && !className.isEmpty()) {
                    tag.setString(CLASS, className);
                }
                if (set != null && !set.isEmpty()) {
                    tag.setString(SET, set);
                }

                mis.setTag(tag);

                clone = CraftItemStack.asCraftMirror(mis);
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

//    /**
//    * Returns the name of the given ItemStack
//    * Returns null if the ItemStack provided is null
//    * Returns the name from the NBTTag if present
//    * Otherwise returns the name of the ItemStack Material
//    *
//    * @param item The given ItemStack (may be null)
//    * @return The name of the item
//    */
//    public static String getName(ItemStack item) {
//        String name = null;
//        if (item != null) {
//            if (!(item instanceof CraftItemStack)) {
//                item = new CraftItemStack(item);
//            }
//            CraftItemStack cis = (CraftItemStack) item;
//
//            NBTTagCompound tag = cis.getHandle().getTag();
//            NBTTagCompound display = null;
//            if (tag != null) {
//                display = tag.getCompound("display");
//            }
//
//            if (display == null) {
//                name = item.getType().name().toLowerCase();
//            } else {
//                name = display.getString("Name");
//                if (name.isEmpty()) {
//                    name = item.getType().name().toLowerCase();
//                }
//            }
//        }
//        return name;
//    }
}
