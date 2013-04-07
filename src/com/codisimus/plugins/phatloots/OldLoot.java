package com.codisimus.plugins.phatloots;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.*;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
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
@SerializableAs("Loot")
public class OldLoot implements Comparable, ConfigurationSerializable {
    private static final String ARMOR = "ARMOR";
    private static final String SWORD = "SWORD";
    private static final String AXE = "AXE";
    private static final String BOW = "BOW";
    private static final String DAMAGE = "<dam>";
    private static final String HOLY = "<holy>";
    private static final String FIRE = "<fire>";
    private static final String BUG = "<bug>";
    private static final String THORNS = "<thorns>";
    private static final String DEFENSE = "<def>";
    private static final String FIRE_DEFENSE = "<firedef>";
    private static final String RANGE_DEFENSE = "<rangedef>";
    private static final String BLAST_DEFENSE = "<blastdef>";
    private static final String FALL_DEFENSE = "<falldef>";
    private static final Enchantment[] ARMOR_ENCHANTMENTS = {
        Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE,
        Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE,
        Enchantment.THORNS, Enchantment.DURABILITY
    };
    private static final Enchantment[] SWORD_ENCHANTMENTS = {
        Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD,
        Enchantment.DAMAGE_ARTHROPODS, Enchantment.KNOCKBACK,
        Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS,
        Enchantment.DURABILITY
    };
    private static final Enchantment[] AXE_ENCHANTMENTS = {
        Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD,
        Enchantment.DAMAGE_ARTHROPODS, Enchantment.KNOCKBACK,
        Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS,
        Enchantment.DURABILITY
    };
    private static final Enchantment[] BOW_ENCHANTMENTS = {
        Enchantment.ARROW_DAMAGE, Enchantment.ARROW_KNOCKBACK,
        Enchantment.ARROW_FIRE, Enchantment.ARROW_INFINITE,
        Enchantment.DURABILITY
    };
    static int tierNotify;
    static FileConfiguration loreConfig;
    static FileConfiguration enchantmentConfig;
    static String damageString;
    static String holyString;
    static String bugString;
    static String fireString;
    static String thornsString;
    static String defenseString;
    static String fireDefenseString;
    static String rangeDefenseString;
    static String blastDefenseString;
    static String fallDefenseString;
    private ItemStack item;
    private int amountBonus = 0;
    private int durabilityBonus = 0;
    private double probability;
    boolean autoEnchant;
    private boolean generateName;
    private boolean randomLore;
    private boolean tieredName;
    String name;

    /**
     * Constructs a new Loot with the given Item data
     *
     * @param id The Material id of the item
     * @param amountLower The lower bound of the stack size of the item
     * @param amountUpper The upper bound of the stack size of the item
     */
    public OldLoot(int id, int amountLower, int amountUpper) {
        item = new ItemStack(id, amountLower);
        amountBonus = (amountUpper - amountLower);
    }

    /**
     * Constructs a new Loot with the given ItemStack and bonus amount
     *
     * @param item The given ItemStack
     * @param amountBonus The extra amount for the loot
     */
    public OldLoot(ItemStack item, int amountBonus) {
        this.item = item;
        this.amountBonus = amountBonus;
    }

    public OldLoot(Map<String, Object> map) {
        this.item = (ItemStack) map.get("ItemStack");
        this.amountBonus = (Integer) map.get("BonusAmount");
        this.durabilityBonus = (Integer) map.get("BonusDurability");
        this.probability = (Double) map.get("Probability");
        this.autoEnchant = (Boolean) map.get("AutoEnchant");
        this.generateName = (Boolean) map.get("GenerateName");
        this.randomLore = (Boolean) map.get("RandomLore");
        this.tieredName = (Boolean) map.get("Tiered");
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
     * Sets the Durability value of the Loot Item
     *
     * @param durability The Durability to be set
     */
    public void setDurability(short durability) {
        if (durability >= 0) {
            item.setDurability(durability);
        }
    }

    public void setEnchantments(Map<Enchantment, Integer> enchantments) {
        if (enchantments != null) {
            item.addUnsafeEnchantments(enchantments);
        }
    }

    public boolean rollForLoot(double lootingBonus) {
        return roll() < (probability - lootingBonus);
    }

    private double roll() {
        return PhatLoots.random.nextInt(100) + PhatLoots.random.nextDouble();
    }

    /**
     * Returns the item with the bonus amount, enchantments, etc.
     *
     * @return A clone of the Loot item
     */
    public ItemStack getItem() {
        ItemStack clone = item.clone();

        if (autoEnchant) {
            String type;
            Enchantment[] enchantments;
            switch (clone.getType()) {
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
                String key = type + '.' + enchantment.getName();
                if (enchantmentConfig.contains(key)) {
                    ConfigurationSection config = enchantmentConfig.getConfigurationSection(key);
                    double totalPercent = 0.0D;
                    int level = 0;
                    double roll = roll();
                    for (String string : config.getKeys(false)) {
                        totalPercent += config.getDouble(string);
                        if (totalPercent > roll) {
                            break;
                        }
                        level++;
                    }

                    if (level > 0) {
                        clone.addUnsafeEnchantment(enchantment, level);
                    }
                }
            }
        }

        if (amountBonus > 0) {
            clone.setAmount(clone.getAmount() + PhatLoots.random.nextInt(amountBonus));
        }

        if (durabilityBonus > 0) {
            clone.setDurability((short) (clone.getDurability() + PhatLoots.random.nextInt(durabilityBonus)));
        }

        ItemMeta meta = clone.hasItemMeta()
                        ? clone.getItemMeta().clone()
                        : PhatLoots.server.getItemFactory().getItemMeta(clone.getType());

        if (generateName || tieredName || randomLore) {
            StringBuilder nameBuilder = new StringBuilder();
            if (randomLore) {
                String folder = clone.getType() + clone.getEnchantments().toString();
                File dir = new File(PhatLoots.dataFolder + File.separator + "Item Descriptions" + File.separator + folder);

                File[] files = dir.listFiles();
                Random random = new Random();
                File file = files[random.nextInt(files.length)];

                if (file.exists()) {
                    FileReader fReader = null;
                    BufferedReader bReader = null;
                    try {
                        fReader = new FileReader(file);
                        bReader = new BufferedReader(fReader);

                        String line = bReader.readLine();
                        if (line != null) {
                            if (line.charAt(0) == '&') {
                                line = line.replace('&', '§');
                            }

                            nameBuilder.append(line);

                            List lore = new LinkedList();
                            while ((line = bReader.readLine()) != null) {
                                line = line.replace('&', '§');
                                lore.add(line);
                            }
                            meta.setLore(lore);
                        }
                    } catch (Exception ex) {
                    } finally {
                        try {
                            fReader.close();
                            bReader.close();
                        } catch (Exception ex) {
                        }
                    }
                } else {
                    PhatLoots.logger.severe("The Item Description " + file.getName() + " cannot be read");
                }
            } else {
                nameBuilder.append(WordUtils.capitalizeFully(clone.getType().toString().replace('_', ' ')));
            }

            if (this.generateName) {
                generateName(clone, nameBuilder);
            }

            if (this.tieredName) {
                getTieredName(clone, nameBuilder);
            }

            meta.setDisplayName(nameBuilder.toString());
            clone.setItemMeta(meta);
        }

        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            ListIterator<String> itr = lore.listIterator();
            switch (clone.getType()) {
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
                while (itr.hasNext()) {
                    String string = itr.next();
                    if (string.equals(THORNS)) {
                        if (clone.containsEnchantment(Enchantment.THORNS)) {
                            int lvl = clone.getEnchantments().get(Enchantment.THORNS);
                            itr.set(thornsString.replace("<chance>", String.valueOf(15 * lvl)));
                        } else {
                            itr.remove();
                        }
                    } else if (string.equals(DEFENSE)) {
                        int amount = getBaseArmor(clone.getType());
                        if (clone.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL)) {
                            int lvl = clone.getEnchantments().get(Enchantment.PROTECTION_ENVIRONMENTAL);
                            int epf = (int) Math.floor((6 + lvl * lvl) * 0.75 / 3);
                            int low = amount + (int) Math.ceil(epf / 2);
                            int high = amount + epf;
                            itr.set(defenseString.replace("<amount>", low + "-" + high));
                        } else {
                            itr.set(defenseString.replace("<amount>", String.valueOf(amount)));
                        }
                    } else if (string.equals(FIRE_DEFENSE)) {
                        if (clone.containsEnchantment(Enchantment.PROTECTION_FIRE)) {
                            int lvl = clone.getEnchantments().get(Enchantment.PROTECTION_FIRE);
                            int epf = (int) Math.floor((6 + lvl * lvl) * 1.25 / 3);
                            int low = (int) Math.ceil(epf / 2);
                            int high = epf;
                            itr.set(fireDefenseString.replace("<amount>", low + "-" + high));
                        } else {
                            itr.remove();
                        }
                    } else if (string.equals(RANGE_DEFENSE)) {
                        if (clone.containsEnchantment(Enchantment.PROTECTION_PROJECTILE)) {
                            int lvl = clone.getEnchantments().get(Enchantment.PROTECTION_PROJECTILE);
                            int epf = (int) Math.floor((6 + lvl * lvl) * 1.5 / 3);
                            int low = (int) Math.ceil(epf / 2);
                            int high = epf;
                            itr.set(rangeDefenseString.replace("<amount>", low + "-" + high));
                        } else {
                            itr.remove();
                        }
                    } else if (string.equals(BLAST_DEFENSE)) {
                        if (clone.containsEnchantment(Enchantment.PROTECTION_EXPLOSIONS)) {
                            int lvl = clone.getEnchantments().get(Enchantment.PROTECTION_EXPLOSIONS);
                            int epf = (int) Math.floor((6 + lvl * lvl) * 1.5 / 3);
                            int low = (int) Math.ceil(epf / 2);
                            int high = epf;
                            itr.set(blastDefenseString.replace("<amount>", low + "-" + high));
                        } else {
                            itr.remove();
                        }
                    } else if (string.equals(FALL_DEFENSE)) {
                        if (clone.containsEnchantment(Enchantment.PROTECTION_FALL)) {
                            int lvl = clone.getEnchantments().get(Enchantment.PROTECTION_FALL);
                            int epf = (int) Math.floor((6 + lvl * lvl) * 2.5 / 3);
                            int low = (int) Math.ceil(epf / 2);
                            int high = epf;
                            itr.set(fallDefenseString.replace("<amount>", low + "-" + high));
                        } else {
                            itr.remove();
                        }
                    }
                }
                break;

            case BOW:
                while (itr.hasNext()) {
                    String string = itr.next();
                    if (string.equals(DAMAGE)) {
                        int baseLow = 1;
                        int baseHigh = 10;
                        if (clone.containsEnchantment(Enchantment.DAMAGE_ALL)) {
                            int lvl = clone.getEnchantments().get(Enchantment.ARROW_DAMAGE);
                            double bonus = lvl == 0
                                           ? 0
                                           : 0.25;
                            bonus += (0.25 * lvl);
                            int low = baseLow + (int) (baseLow * bonus);
                            int high = baseHigh + (int) (baseHigh * bonus);
                            itr.set(damageString.replace("<amount>", low + "-" + high));
                        } else {
                            itr.set(damageString.replace("<amount>", baseLow + "-" + baseHigh));
                        }
                    } else if (string.equals(FIRE)) {
                        if (clone.containsEnchantment(Enchantment.ARROW_FIRE)) {
                            itr.set(fireString.replace("<amount>", "4"));
                        } else {
                            itr.remove();
                        }
                    }
                }
                break;

            default:
                while (itr.hasNext()) {
                    String string = itr.next();
                    if (string.equals(DAMAGE)) {
                        int baseLow = getBaseDamage(clone.getType());
                        int baseHigh = (int) (baseLow * 1.5D) + 2;
                        if (clone.containsEnchantment(Enchantment.DAMAGE_ALL)) {
                            int lvl = clone.getEnchantments().get(Enchantment.DAMAGE_ALL);
                            int low = baseLow + lvl;
                            int high = baseHigh + 3 * lvl;
                            itr.set(damageString.replace("<amount>", low + "-" + high));
                        } else {
                            itr.set(damageString.replace("<amount>", baseLow + "-" + baseHigh));
                        }
                    } else if (string.equals(HOLY)) {
                        if (clone.containsEnchantment(Enchantment.DAMAGE_UNDEAD)) {
                            int lvl = clone.getEnchantments().get(Enchantment.DAMAGE_UNDEAD);
                            int low = lvl;
                            int high = 4 * lvl;
                            itr.set(holyString.replace("<amount>", low + "-" + high));
                        } else {
                            itr.remove();
                        }
                    } else if (string.equals(BUG)) {
                        if (clone.containsEnchantment(Enchantment.DAMAGE_ARTHROPODS)) {
                            int lvl = clone.getEnchantments().get(Enchantment.DAMAGE_ARTHROPODS);
                            int low = lvl;
                            int high = 4 * lvl;
                            itr.set(bugString.replace("<amount>", low + "-" + high));
                        } else {
                            itr.remove();
                        }
                    } else if (string.equals(FIRE)) {
                        if (clone.containsEnchantment(Enchantment.FIRE_ASPECT)) {
                            int lvl = clone.getEnchantments().get(Enchantment.FIRE_ASPECT);
                            int amount = 4 * lvl;
                            itr.set(fireString.replace("<amount>", String.valueOf(amount)));
                        } else {
                            itr.remove();
                        }
                    }
                }
                break;
            }
            meta.setLore(lore);
            clone.setItemMeta(meta);
        }

        return clone;
    }

    /**
     * Returns the chance of looting
     *
     * @return The chance of looting
     */
    public double getProbability() {
        return this.probability;
    }

    private void generateName(ItemStack item, StringBuilder nameBuilder) {
        Material mat = item.getType();
        Map enchantments = item.getEnchantments();
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
                nameBuilder.insert(0, ' ');
                nameBuilder.insert(0, lore);
            }
            enchantment = enchantments.containsKey(Enchantment.THORNS)
                          ? Enchantment.THORNS
                          : Enchantment.DURABILITY;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.insert(0, ' ');
                nameBuilder.insert(0, lore);
            }
            enchantment = getTrump(enchantments, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_PROJECTILE, Enchantment.PROTECTION_EXPLOSIONS);
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.append(' ');
                nameBuilder.append(lore);
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
                nameBuilder.replace(nameBuilder.length() - 5, nameBuilder.length(), lore);
            }
            enchantment = enchantments.containsKey(Enchantment.FIRE_ASPECT)
                          ? Enchantment.FIRE_ASPECT
                          : Enchantment.DURABILITY;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.insert(0, ' ');
                nameBuilder.insert(0, lore);
            }
            enchantment = Enchantment.LOOT_BONUS_MOBS;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.append(' ');
                nameBuilder.append(lore);
            }
            enchantment = getTrump(enchantments, Enchantment.KNOCKBACK, Enchantment.DAMAGE_UNDEAD);
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.append(' ');
                nameBuilder.append(lore);
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
                nameBuilder.replace(nameBuilder.length() - 5, nameBuilder.length(), lore);
            }
            enchantment = enchantments.containsKey(Enchantment.FIRE_ASPECT)
                          ? Enchantment.FIRE_ASPECT
                          : Enchantment.DURABILITY;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.insert(0, ' ');
                nameBuilder.insert(0, lore);
            }
            enchantment = Enchantment.LOOT_BONUS_MOBS;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.append(' ');
                nameBuilder.append(lore);
            }
            enchantment = getTrump(enchantments, Enchantment.KNOCKBACK, Enchantment.DAMAGE_UNDEAD);
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.append(' ');
                nameBuilder.append(lore);
            }
            break;

        case BOW:
            type = BOW;
            enchantment = Enchantment.ARROW_DAMAGE;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.replace(0, name.length(), lore);
            }
            enchantment = Enchantment.DURABILITY;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.insert(0, ' ');
                nameBuilder.insert(0, lore);
            }
            enchantment = Enchantment.ARROW_FIRE;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.insert(0, ' ');
                nameBuilder.insert(0, lore);
            }
            enchantment = Enchantment.ARROW_INFINITE;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.append(' ');
                nameBuilder.append(lore);
            }
            enchantment = Enchantment.ARROW_KNOCKBACK;
            level = getLevel(enchantments, enchantment);
            lore = loreConfig.getString(type + '.' + enchantment.getName() + '.' + level);
            if (lore != null) {
                nameBuilder.append(' ');
                nameBuilder.append(lore);
            }
            break;

        default: break;
        }
    }

    private void getTieredName(ItemStack item, StringBuilder nameBuilder) {
        Material mat = item.getType();
        Map<Enchantment, Integer> enchantments = item.getEnchantments();

        int tier = 0;
        for (Integer i : enchantments.values()) {
            tier += i.intValue();
        }
        tier *= 5;

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
        if (tier >= 5) {
            if (tier >= 20) {
                if (tier >= 30) {
                    if (tier >= 50) {
                        if (tier >= 65) {
                            if (tier >= 80) {
                                if (tier >= 100) {
                                    if (tier >= 150) {
                                        if (tier >= 200) {
                                            nameBuilder.insert(0, "§5");
                                            nameBuilder.append(" (Legendary)");
                                        } else {
                                            nameBuilder.insert(0, "§4");
                                            nameBuilder.append(" (Mythic)");
                                        }
                                    } else {
                                        nameBuilder.insert(0, "§2");
                                        nameBuilder.append(" (Epic)");
                                    }
                                } else {
                                    nameBuilder.insert(0, "§1");
                                    nameBuilder.append(" (Ultra Rare)");
                                }
                            } else {
                                nameBuilder.insert(0, "§9");
                                nameBuilder.append(" (Super Rare)");
                            }
                        } else {
                            nameBuilder.insert(0, "§3");
                            nameBuilder.append(" (Very Rare)");
                        }
                    } else {
                        nameBuilder.insert(0, "§b");
                        nameBuilder.append(" (Rare)");
                    }
                } else {
                    nameBuilder.insert(0, "§f");
                    nameBuilder.append(" (Uncommon)");
                }
            } else {
                nameBuilder.insert(0, "§7");
                nameBuilder.append(" (Common)");
            }
        } else {
            nameBuilder.insert(0, "§8");
            nameBuilder.append(" (Poor)");
        }

        if (tier > tierNotify) {
            PhatLoots.logger.info(nameBuilder.toString() + " [Tier " + tier + "] has been generated");
        }
    }

    private int getBaseDamage(Material type) {
        switch (type) {
        case WOOD_SPADE: return 1;
        case WOOD_PICKAXE: return 2;
        case WOOD_AXE: return 3;
        case WOOD_SWORD: return 4;
        case GOLD_SPADE: return 1;
        case GOLD_PICKAXE: return 2;
        case GOLD_AXE: return 3;
        case GOLD_SWORD: return 4;
        case STONE_SPADE: return 2;
        case STONE_PICKAXE: return 3;
        case STONE_AXE: return 4;
        case STONE_SWORD: return 5;
        case IRON_SPADE: return 3;
        case IRON_PICKAXE: return 4;
        case IRON_AXE: return 5;
        case IRON_SWORD: return 6;
        case DIAMOND_SPADE: return 4;
        case DIAMOND_PICKAXE: return 5;
        case DIAMOND_AXE: return 6;
        case DIAMOND_SWORD: return 7;
        default: return 1;
        }
    }

    private int getBaseArmor(Material type) {
        switch (type) {
        case LEATHER_BOOTS: return 1;
        case LEATHER_LEGGINGS: return 2;
        case LEATHER_CHESTPLATE: return 3;
        case LEATHER_HELMET: return 1;
        case GOLD_BOOTS: return 1;
        case GOLD_LEGGINGS: return 3;
        case GOLD_CHESTPLATE: return 5;
        case GOLD_HELMET: return 1;
        case CHAINMAIL_BOOTS: return 2;
        case CHAINMAIL_LEGGINGS: return 4;
        case CHAINMAIL_CHESTPLATE: return 5;
        case CHAINMAIL_HELMET: return 1;
        case IRON_BOOTS: return 2;
        case IRON_LEGGINGS: return 5;
        case IRON_CHESTPLATE: return 6;
        case IRON_HELMET: return 2;
        case DIAMOND_BOOTS: return 3;
        case DIAMOND_LEGGINGS: return 6;
        case DIAMOND_CHESTPLATE: return 8;
        case DIAMOND_HELMET: return 3;
        default: return 0;
        }
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
        for (Enchantment enchantment : enchantments.keySet()) {
            string += "&" + enchantment.getName();

            int level = enchantments.get(enchantment);
            if (level != enchantment.getStartLevel()) {
                string += "(" + enchantments.get(enchantment) + ")";
            }
        }
        return string.substring(1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int amount = item.getAmount();
        sb.append(amount);
        if (amountBonus > 0) {
            sb.append('-');
            sb.append(amount + amountBonus);
        }

        sb.append(" of ");
        if (tieredName) {
            sb.append("tiered ");
        }
        sb.append(PhatLoot.getItemName(item));

        sb.append(" @ ");
        sb.append(Math.floor(probability) == probability ? String.valueOf((int) probability) : String.valueOf(probability));

        sb.append("%");

        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof OldLoot)) {
            return false;
        }

        OldLoot loot = (OldLoot) object;
        return (loot.item.equals(item))
                && (loot.amountBonus == amountBonus)
                && (loot.durabilityBonus == durabilityBonus)
                && (loot.autoEnchant == autoEnchant)
                && (loot.generateName == generateName)
                && (loot.randomLore == randomLore)
                && (loot.tieredName == tieredName);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (item != null ? item.hashCode() : 0);
        hash = 37 * hash + amountBonus;
        hash = 37 * hash + durabilityBonus;
        hash = 37 * hash + (int) (Double.doubleToLongBits(probability) ^ Double.doubleToLongBits(probability) >>> 32);
        hash = 37 * hash + (autoEnchant ? 1 : 0);
        hash = 37 * hash + (generateName ? 1 : 0);
        hash = 37 * hash + (randomLore ? 1 : 0);
        hash = 37 * hash + (tieredName ? 1 : 0);
        return hash;
    }

    @Override
    public int compareTo(Object object) {
        if ((object instanceof OldLoot)) {
            OldLoot loot = (OldLoot) object;
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
        return level == null ? 0 : level;
    }

    @Override
    public Map<String, Object> serialize() {
        Map map = new TreeMap();
        map.put("ItemStack", item);
        map.put("BonusAmount", Integer.valueOf(amountBonus));
        map.put("BonusDurability", Integer.valueOf(durabilityBonus));
        map.put("Probability", Double.valueOf(probability));
        map.put("AutoEnchant", Boolean.valueOf(autoEnchant));
        map.put("GenerateName", Boolean.valueOf(generateName));
        map.put("RandomLore", Boolean.valueOf(randomLore));
        map.put("Tiered", Boolean.valueOf(tieredName));
        return map;
    }

    /* OLD */

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

    public void updateItemStack() {
        if (!this.name.isEmpty()) {
            ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : PhatLoots.server.getItemFactory().getItemMeta(item.getType());

            if (this.item.getType() == Material.WRITTEN_BOOK) {
                Properties book = new Properties();
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new StringBuilder().append(PhatLoots.dataFolder).append(File.separator).append("Books").append(File.separator).append(this.name).append(".properties").toString());

                    book.load(fis);
                } catch (Exception e) {
                    PhatLoots.logger.severe(new StringBuilder().append("Failed to load Book").append(this.name).toString());
                    e.printStackTrace();
                } finally {
                    try {
                        fis.close();
                    } catch (Exception e) {
                    }
                }
                BookMeta bookMeta = (BookMeta) meta;
                bookMeta.setTitle(book.getProperty("TITLE"));
                bookMeta.setAuthor(book.getProperty("AUTHOR"));

                for (int i = 1; book.containsKey(new StringBuilder().append("PAGE").append(i).toString()); i++) {
                    bookMeta.addPage(new String[]{book.getProperty(new StringBuilder().append("PAGE").append(i).toString())});
                }

                this.item.setItemMeta(bookMeta);
            } else {
                if (this.name.equals("Random")) {
                    this.randomLore = true;
                    return;
                }
                if (this.name.equals("Auto")) {
                    this.generateName = true;
                    this.tieredName = true;
                    return;
                }
                File file = new File(new StringBuilder().append(PhatLoots.dataFolder).append(File.separator).append("Item Descriptions").append(File.separator).append(this.name).append(".txt").toString());

                if (file.exists()) {
                    FileReader fReader = null;
                    BufferedReader bReader = null;
                    try {
                        fReader = new FileReader(file);
                        bReader = new BufferedReader(fReader);

                        String line = bReader.readLine();
                        if (line != null) {
                            if (line.charAt(0) == '&') {
                                line = line.replace('&', '§');
                            }

                            meta.setDisplayName(line);

                            List lore = new LinkedList();
                            while ((line = bReader.readLine()) != null) {
                                line = line.replace('&', '§');
                                lore.add(line);
                            }
                            meta.setLore(lore);
                        }
                    } catch (Exception ex) {
                    } finally {
                        try {
                            fReader.close();
                            bReader.close();
                        } catch (Exception ex) {
                        }
                    }
                } else {
                    PhatLoots.logger.severe(new StringBuilder().append("The ").append(this.name).append(" Item Description File cannot be found").toString());
                }

                this.item.setItemMeta(meta);
            }
        }
    }
}
