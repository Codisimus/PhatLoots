package com.codisimus.plugins.phatloots;

import java.io.*;
import java.util.*;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * A Loot is a ItemStack and with a probability of looting
 *
 * @author Codisimus
 */
public class Loot implements Comparable {
    private final static String TITLE = "title";
    private final static String AUTHOR = "author";
    private final static String PAGE = "page";
    private final static String ARMOR = "ARMOR";
    private final static String SWORD = "SWORD";
    private final static String AXE = "AXE";
    private final static String BOW = "BOW";
    private final static Enchantment[] ARMOR_ENCHANTMENTS = new Enchantment[] {
            Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE,
            Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE,
            Enchantment.THORNS, Enchantment.DURABILITY
    };
    private final static Enchantment[] SWORD_ENCHANTMENTS = new Enchantment[] {
            Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD,
            Enchantment.DAMAGE_ARTHROPODS, Enchantment.KNOCKBACK,
            Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS,
            Enchantment.DURABILITY
    };
    private final static Enchantment[] AXE_ENCHANTMENTS = new Enchantment[] {
            Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD,
            Enchantment.DAMAGE_ARTHROPODS, Enchantment.KNOCKBACK,
            Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS,
            Enchantment.DURABILITY
    };
    private final static Enchantment[] BOW_ENCHANTMENTS = new Enchantment[] {
            Enchantment.ARROW_DAMAGE, Enchantment.ARROW_KNOCKBACK,
            Enchantment.ARROW_FIRE, Enchantment.ARROW_INFINITE,
            Enchantment.DURABILITY
    };
    static int tierNotify;
    private ItemStack item;
    private int bonus = 0;
    private double probability;
    protected String name;
    protected boolean autoEnchant;
    static FileConfiguration loreConfig;
    static FileConfiguration enchantmentConfig;

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
                if (item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta.hasDisplayName()) {
                        name = meta.getDisplayName().replace('§', '&');
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            } else if (name.equalsIgnoreCase("Random")) {
                String folder = item.getType() + item.getEnchantments().toString();
                File dir = new File(PhatLoots.dataFolder
                        + "/Item Descriptions/" + folder);
                if (!dir.isDirectory()) {
                    dir.mkdir();
                }
                return true;
            } else if (name.equalsIgnoreCase("Auto")) {
                return true;
            }

            File file = new File(PhatLoots.dataFolder + "/Item Descriptions/" + name + ".txt");
            if (file.exists()) {
                return true;
            }
            if (!item.hasItemMeta()) {
                return false;
            }
            ItemMeta meta = item.getItemMeta();

            BufferedWriter bWriter = null;
            try {
                file.createNewFile();
                bWriter = new BufferedWriter(new FileWriter(file));
                bWriter.write(meta.getDisplayName().replace('§', '&'));

                List<String> lore = meta.getLore();
                if (lore != null) {
                    for (String line : lore) {
                        bWriter.newLine();
                        bWriter.write(line.replace('§', '&'));
                    }
                }
            } catch (Exception e) {
            } finally {
                try {
                    bWriter.close();
                } catch (Exception e) {
                }
            }

            return true;
        }
    }

    public boolean rollForLoot() {
        return roll() < probability;
    }

    private double roll() {
        return PhatLoots.random.nextInt(100) + PhatLoots.random.nextDouble();
    }

    /**
     * Returns the item with the bonus amount
     *
     * @return The item with the bonus amount
     */
    public ItemStack getItem() {
        ItemStack clone = item.clone();

        if (autoEnchant) {
            String type;
            Enchantment[] enchantments;
            switch (item.getType()) {
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case GOLD_HELMET:
            case GOLD_CHESTPLATE:
            case GOLD_LEGGINGS:
            case GOLD_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
                type = ARMOR;
                enchantments = ARMOR_ENCHANTMENTS;
                break;

            case DIAMOND_SWORD:
            case IRON_SWORD:
            case GOLD_SWORD:
            case STONE_SWORD:
            case WOOD_SWORD:
                type = SWORD;
                enchantments = SWORD_ENCHANTMENTS;
                break;

            case DIAMOND_AXE:
            case IRON_AXE:
            case GOLD_AXE:
            case STONE_AXE:
            case WOOD_AXE:
                type = AXE;
                enchantments = AXE_ENCHANTMENTS;
                break;

            case BOW:
                type = BOW;
                enchantments = BOW_ENCHANTMENTS;
                break;

            default:
                type = "";
                enchantments = new Enchantment[0];
                break;
            }

            for (Enchantment enchantment : enchantments) {
                if (enchantmentConfig.contains(type + '.' + enchantment)) {
                    ConfigurationSection config = enchantmentConfig.getConfigurationSection(type + '.' + enchantment);
                    double totalPercent = 0;
                    int level = 0;
                    double roll = roll();
                    for (String string : config.getKeys(false)) {
                        totalPercent += config.getDouble(string);
                        if (totalPercent > roll) {
                            break;
                        } else {
                            level++;
                        }
                    }
                    if (level > 0) {
                        clone.addUnsafeEnchantment(enchantment, level);
                    }
                }
            }
        }

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
                } else if (name.equals("Auto")) {
                    meta.setDisplayName(getTieredName(clone));
                    clone.setItemMeta(meta);
                    return clone;
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
                                line = line.replace('&', '§');
                            }

                            //Set the Name of the Item
                            meta.setDisplayName(line);

                            //Add each remaining line of the File
                            List<String> lore = new LinkedList<String>();
                            while ((line = bReader.readLine()) != null) {
                                line = line.replace('&', '§');
                                lore.add(line);
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

    private String getTieredName(ItemStack item) {
        Material mat = item.getType();
        StringBuilder nameBuiler = new StringBuilder();
        nameBuiler.append(WordUtils.capitalizeFully(mat.toString().replace('_', ' ')));
        Map<Enchantment, Integer> enchantments = item.getEnchantments();

        String type;
        Enchantment enchantment;
        int level;
        String lore;
        switch (mat) {
        case DIAMOND_HELMET:
        case DIAMOND_CHESTPLATE:
        case DIAMOND_LEGGINGS:
        case DIAMOND_BOOTS:
        case IRON_HELMET:
        case IRON_CHESTPLATE:
        case IRON_LEGGINGS:
        case IRON_BOOTS:
        case GOLD_HELMET:
        case GOLD_CHESTPLATE:
        case GOLD_LEGGINGS:
        case GOLD_BOOTS:
        case CHAINMAIL_HELMET:
        case CHAINMAIL_CHESTPLATE:
        case CHAINMAIL_LEGGINGS:
        case CHAINMAIL_BOOTS:
        case LEATHER_HELMET:
        case LEATHER_CHESTPLATE:
        case LEATHER_LEGGINGS:
        case LEATHER_BOOTS:
            type = ARMOR;
            enchantment = Enchantment.PROTECTION_FIRE;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.insert(0, ' ');
                nameBuiler.insert(0, lore);
            }
            enchantment = enchantments.containsKey(Enchantment.THORNS)
                          ? Enchantment.THORNS
                          : Enchantment.DURABILITY;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.insert(0, ' ');
                nameBuiler.insert(0, lore);
            }
            enchantment = getTrump(enchantments, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_PROJECTILE, Enchantment.PROTECTION_EXPLOSIONS);
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.append(' ');
                nameBuiler.append(lore);
            }
            break;

        case DIAMOND_SWORD:
        case IRON_SWORD:
        case GOLD_SWORD:
        case STONE_SWORD:
        case WOOD_SWORD:
            type = SWORD;
            enchantment = getTrump(enchantments, Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_ALL);
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.replace(nameBuiler.length() - 5, nameBuiler.length(), lore);
            }
            enchantment = enchantments.containsKey(Enchantment.FIRE_ASPECT)
                          ? Enchantment.FIRE_ASPECT
                          : Enchantment.DURABILITY;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.insert(0, ' ');
                nameBuiler.insert(0, lore);
            }
            enchantment = Enchantment.LOOT_BONUS_MOBS;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.append(' ');
                nameBuiler.append(lore);
            }
            enchantment = getTrump(enchantments, Enchantment.KNOCKBACK, Enchantment.DAMAGE_UNDEAD);
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.append(' ');
                nameBuiler.append(lore);
            }
            break;

        case DIAMOND_AXE:
        case IRON_AXE:
        case GOLD_AXE:
        case STONE_AXE:
        case WOOD_AXE:
            type = AXE;
            enchantment = getTrump(enchantments, Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_ALL);
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.replace(nameBuiler.length() - 5, nameBuiler.length(), lore);
            }
            enchantment = enchantments.containsKey(Enchantment.FIRE_ASPECT)
                          ? Enchantment.FIRE_ASPECT
                          : Enchantment.DURABILITY;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.insert(0, ' ');
                nameBuiler.insert(0, lore);
            }
            enchantment = Enchantment.LOOT_BONUS_MOBS;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.append(' ');
                nameBuiler.append(lore);
            }
            enchantment = getTrump(enchantments, Enchantment.KNOCKBACK, Enchantment.DAMAGE_UNDEAD);
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.append(' ');
                nameBuiler.append(lore);
            }
            break;

        case BOW:
            type = BOW;
            enchantment = Enchantment.ARROW_DAMAGE;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.replace(0, name.length(), lore);
            }
            enchantment = Enchantment.DURABILITY;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.insert(0, ' ');
                nameBuiler.insert(0, lore);
            }
            enchantment = Enchantment.ARROW_FIRE;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.insert(0, ' ');
                nameBuiler.insert(0, lore);
            }
            enchantment = Enchantment.ARROW_INFINITE;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.append(' ');
                nameBuiler.append(lore);
            }
            enchantment = Enchantment.ARROW_KNOCKBACK;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuiler.append(' ');
                nameBuiler.append(lore);
            }
            break;

        default: break;
        }

        int tier = 0;
        for (Integer i : enchantments.values()) {
            tier += i;
        }
        tier = tier * 5;

        switch (mat) {
        case DIAMOND_SWORD:
        case DIAMOND_AXE:
        case DIAMOND_HELMET:
        case DIAMOND_CHESTPLATE:
        case DIAMOND_LEGGINGS:
        case DIAMOND_BOOTS:
            tier += 30;
            break;

        case IRON_SWORD:
        case IRON_AXE:
        case IRON_HELMET:
        case IRON_CHESTPLATE:
        case IRON_LEGGINGS:
        case IRON_BOOTS:
            tier += 20;
            break;

        case GOLD_SWORD:
        case GOLD_AXE:
        case GOLD_HELMET:
        case GOLD_CHESTPLATE:
        case GOLD_LEGGINGS:
        case GOLD_BOOTS:
            tier += 20;
            break;

        case STONE_SWORD:
        case STONE_AXE:
        case CHAINMAIL_HELMET:
        case CHAINMAIL_CHESTPLATE:
        case CHAINMAIL_LEGGINGS:
        case CHAINMAIL_BOOTS:
            tier += 10;
            break;

        case BOW:
            tier = tier * 3;
            break;

        default: break;
        }

        String name = nameBuiler.toString();

        if (tier >= 5) {
            if (tier >= 20) {
                if (tier >= 30) {
                    if (tier >= 50) {
                        if (tier >= 65) {
                            if (tier >= 80) {
                                if (tier >= 100) {
                                    if (tier >= 150) {
                                        if (tier >= 200) {
                                            name = "§5" + name + " (Legendary)";
                                        } else {
                                            name = "§4" + name + " (Mythic)";
                                        }
                                    } else {
                                        name = "§2" + name + " (Epic)";
                                    }
                                } else {
                                    name = "§1" + name + " (Ultra Rare)";
                                }
                            } else {
                                name = "§9" + name + " (Super Rare)";
                            }
                        } else {
                            name = "§3" + name + " (Very Rare)";
                        }
                    } else {
                        name = "§b" + name + " (Rare)";
                    }
                } else {
                    name = "§f" + name + " (Uncommon)";
                }
            } else {
                name = "§7" + name + " (Common)";
            }
        } else {
            name = "§8" + name + " (Poor)";
        }

        if (tier > tierNotify) {
            PhatLoots.logger.info(name + " [Tier " + tier + "] has been generated");
        }
        return name;
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
     * [(DyedColor|SkullOwner)]MaterialID[+Name]'Durability[+Enchantment1(level)&Enchantment2(level)...]'Amount[-Amount]'Probability
     *
     * @return The String representation of this Loot
     */
    @Override
    public String toString() {
        String string = String.valueOf(item.getTypeId()); //MaterialID
        int id = item.getTypeId();
        if (id >= 298 && id <= 301) {
            if (item.hasItemMeta()) {
                string = "(" + ((LeatherArmorMeta) item.getItemMeta()).getColor().asRGB() + ")" + string; //:DyedColor
            }
        } else if (id == 144) {
            if (item.hasItemMeta()) {
                string = "(" + ((SkullMeta) item.getItemMeta()).getOwner() + ")" + string; //:SkullOwner
            }
        }

        //Check if Item has a Description
        if (!name.isEmpty()) {
            string += "+" + name; //+Name
        }

        string += "'" + item.getDurability(); //'Durability

        //Check if Item has Enchantments
        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (!enchantments.isEmpty()) {
            string += "+" + enchantmentsToString(); //+Enchantment1(level)&Enchantment2(level)...
            if (autoEnchant) {
                string += "&auto";
            }
        } else if (autoEnchant) {
            string += "+auto";
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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Loot)) {
            return false;
        }

        Loot loot = (Loot) object;
        return loot.name.equals(name)
                && loot.item.getAmount() == item.getAmount()
                && loot.item.getDurability() == item.getDurability()
                && loot.item.getTypeId() == item.getTypeId()
                && loot.bonus == bonus
                && loot.probability == probability
                && loot.item.getEnchantments().entrySet().equals(item.getEnchantments().entrySet());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.item != null ? this.item.hashCode() : 0);
        hash = 97 * hash + this.bonus;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.probability) ^ (Double.doubleToLongBits(this.probability) >>> 32));
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    public void setColor(Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
    }

    public void setSkullOwner(String owner) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(owner);
        item.setItemMeta(meta);
    }

    @Override
    public int compareTo(Object object) {
        if (object instanceof Loot) {
            Loot loot = (Loot) object;
            if (loot.probability < probability) {
                return 1;
            } else if (loot.probability > probability) {
                return -1;
            } else {
                if (this.equals(loot)) {
                    return 0;
                }
            }
        }
        return -1;
    }

    private Enchantment getTrump(Map<Enchantment, Integer> enchantments, Enchantment... enchants) {
        int highestLevel = -1;
        Enchantment trump = null;
        for (Enchantment enchant : enchants) {
            int level = getLevel(enchantments, enchant);
            if (level > highestLevel) {
                trump = enchant;
                highestLevel = level;
            }
        }
        return trump;
    }

    private int getLevel(Map<Enchantment, Integer> enchantments, Enchantment enchantment) {
        Integer level = enchantments.get(enchantment);
        return level == null
               ? 0
               : level;
    }
}
