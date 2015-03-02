package com.codisimus.plugins.phatloots.commands;

import com.codisimus.plugins.phatloots.commands.CommandHandler.CodCommand;
import com.codisimus.plugins.phatloots.loot.Gem;
import com.codisimus.plugins.phatloots.loot.MythicDropsItem;
import com.codisimus.plugins.phatloots.loot.UnidentifiedItem;
import org.bukkit.command.CommandSender;

/**
 * Executes Player Commands for configuring MythicDrops Loot
 *
 * @author Codisimus
 */
public class ManageMythicDropsLootCommand {

    @CodCommand(
        command = "add",
        subcommand = "md",
        weight = 193.1,
        aliases = {"+"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2#§f: §5The amount to be looted ex. §6#10 §5or §6#1-64",
            "§2d§f: §5The data/durability value of the item ex. §6d5",
            "§2<command> <tier> [Parameter1] [Parameter2]...",
            "§bex. §6<command> Common_Armor #2-3 %80",
            "§bex. §6<command> Rare_Weapon %10 cWeapon",
            "§bTutorial Video:",
            "§1§nNone yet, Submit yours!"
        },
        permission = "phatloots.manage"
    )
    public boolean addMythicDropsItem(CommandSender sender, String tier, String[] args) {
        setMythicDropsItem(sender, true, tier, args);
        return true;
    }

    @CodCommand(
        command = "remove",
        subcommand = "md",
        weight = 198.1,
        aliases = {"-"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2#§f: §5The amount to be looted ex. §6#10 §5or §6#1-64",
            "§2d§f: §5The data/durability value of the item ex. §6d5",
            "§2<command> <tier> [Parameter1] [Parameter2]...",
            "§bex. §6<command> Common_Armor #2-3 %80",
            "§bex. §6<command> Rare_Weapon %10 cWeapon",
            "§bTutorial Video:",
            "§1§nNone yet, Submit yours!"
        },
        permission = "phatloots.manage"
    )
    public boolean removeMythicDropsItem(CommandSender sender, String tier, String[] args) {
        setMythicDropsItem(sender, false, tier, args);
        return true;
    }

    /**
     * Generates the MythicDrops Item and sets it on the PhatLoot(s)
     *
     * @param sender The CommandSender who is setting the MythicDrops Item
     * @param add True if the MythicDrops Item should be added to the PhatLoot(s)
     * @param tier The name of the MythicDrops Tier to apply
     * @param args The arguments of the MythicDrops Item
     */
    private static void setMythicDropsItem(CommandSender sender, boolean add, String tier, String[] args) {
        String phatLoot = null; //The name of the PhatLoot
        double percent = 100; //The chance of receiving the Loot (defaulted to 100)
        String coll = null; //The Collection to add the Loot to
        int amountLower = 1; //Least amount of loot items to possibly generate (defaulted to 1)
        int amountUpper = 1; //Most amount of loot items to possibly generate(defaulted to 1)
        int durabilityLower = 0; //Least amount of damage for the loot item to spawn with (defaulted to 0)
        int durabilityUpper = 0; //Most amount of damage for the loot item to spawn with (defaulted to 0)

        //Check each parameter
        int i = 0;
        while (i < args.length) {
            char c = args[i].charAt(0);
            String s = args[i].substring(1);
            switch (c) {
            case 'p': //PhatLoot Name
                phatLoot = s;
                break;

            case '%': //Probability
                percent = LootCommandUtil.getPercent(sender, s);
                if (percent == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a percent");
                    return;
                }
                break;

            case 'c': //Collection Name
                coll = s;
                break;

            case '#': //Amount
                amountLower = LootCommandUtil.getLowerBound(s);
                amountUpper = LootCommandUtil.getUpperBound(s);
                if (amountLower == -1 || amountUpper == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a valid number or range");
                    return;
                }
                break;

            case 'd': //Durability
                durabilityLower = LootCommandUtil.getLowerBound(s);
                durabilityUpper = LootCommandUtil.getUpperBound(s);
                if (durabilityLower == -1 || durabilityUpper == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a valid number or range");
                    return;
                }
                break;

            default: //Invalid Parameter
                sender.sendMessage("§6" + c + "§4 is not a valid parameter ID");
                return;
            }

            i++;
        }

        //Construct the Loot
        MythicDropsItem loot = new MythicDropsItem(tier, amountLower, amountUpper, durabilityLower, durabilityUpper);
        loot.setProbability(percent);

        LootCommandUtil.setLoot(sender, phatLoot, add, coll, loot);
    }

    @CodCommand(
        command = "add",
        subcommand = "?",
        weight = 193.2,
        aliases = {"+"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2#§f: §5The amount to be looted ex. §6#10 §5or §6#1-64",
            "§2d§f: §5The data/durability value of the item ex. §6d5",
            "§2<command> <tier> [Parameter1] [Parameter2]...",
            "§bex. §6<command> RANDOM #2-3 %80",
            "§bex. §6<command> Rare_Weapon %10 cWeapon",
            "§bTutorial Video:",
            "§1§nNone yet, Submit yours!"
        },
        permission = "phatloots.manage"
    )
    public boolean addUnidentifiedItem(CommandSender sender, String tier, String[] args) {
        setUnidentifiedItem(sender, true, tier, args);
        return true;
    }

    @CodCommand(
        command = "remove",
        subcommand = "?",
        weight = 198.2,
        aliases = {"-"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2#§f: §5The amount to be looted ex. §6#10 §5or §6#1-64",
            "§2d§f: §5The data/durability value of the item ex. §6d5",
            "§2<command> <tier> [Parameter1] [Parameter2]...",
            "§bex. §6<command> RANDOM #2-3 %80",
            "§bex. §6<command> Rare_Weapon %10 cWeapon",
            "§bTutorial Video:",
            "§1§nNone yet, Submit yours!"
        },
        permission = "phatloots.manage"
    )
    public boolean removeUnidentifiedItem(CommandSender sender, String tier, String[] args) {
        setUnidentifiedItem(sender, false, tier, args);
        return true;
    }

    /**
     * Generates the Unidentified Item and sets it on the PhatLoot(s)
     *
     * @param sender The CommandSender who is setting the Unidentified Item
     * @param add True if the Unidentified Item should be added to the PhatLoot(s)
     * @param tier The name of the MythicDrops Tier to apply
     * @param args The arguments of the Unidentified Item
     */
    private static void setUnidentifiedItem(CommandSender sender, boolean add, String tier, String[] args) {
        String phatLoot = null; //The name of the PhatLoot
        double percent = 100; //The chance of receiving the Loot (defaulted to 100)
        String coll = null; //The Collection to add the Loot to
        int amountLower = 1; //Least amount of loot items to possibly generate (defaulted to 1)
        int amountUpper = 1; //Most amount of loot items to possibly generate(defaulted to 1)
        int durabilityLower = 0; //Least amount of damage for the loot item to spawn with (defaulted to 0)
        int durabilityUpper = 0; //Most amount of damage for the loot item to spawn with (defaulted to 0)

        //Check each parameter
        int i = 0;
        while (i < args.length) {
            char c = args[i].charAt(0);
            String s = args[i].substring(1);
            switch (c) {
            case 'p': //PhatLoot Name
                phatLoot = s;
                break;

            case '%': //Probability
                percent = LootCommandUtil.getPercent(sender, s);
                if (percent == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a percent");
                    return;
                }
                break;

            case 'c': //Collection Name
                coll = s;
                break;

            case '#': //Amount
                amountLower = LootCommandUtil.getLowerBound(s);
                amountUpper = LootCommandUtil.getUpperBound(s);
                if (amountLower == -1 || amountUpper == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a valid number or range");
                    return;
                }
                break;

            case 'd': //Durability
                durabilityLower = LootCommandUtil.getLowerBound(s);
                durabilityUpper = LootCommandUtil.getUpperBound(s);
                if (durabilityLower == -1 || durabilityUpper == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a valid number or range");
                    return;
                }
                break;

            default: //Invalid Parameter
                sender.sendMessage("§6" + c + "§4 is not a valid parameter ID");
                return;
            }

            i++;
        }

        //Construct the Loot
        UnidentifiedItem loot = new UnidentifiedItem(tier, amountLower, amountUpper, durabilityLower, durabilityUpper);
        loot.setProbability(percent);

        LootCommandUtil.setLoot(sender, phatLoot, add, coll, loot);
    }

    @CodCommand(
        command = "add",
        subcommand = "gem",
        weight = 193.3,
        aliases = {"+"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2#§f: §5The amount to be looted ex. §6#10 §5or §6#1-64",
            "§2<command> <tier> [Parameter1] [Parameter2]...",
            "§bex. §6<command> RANDOM #2-3 %80",
            "§bex. §6<command> Rare_Gem %10 cWeapon",
            "§bTutorial Video:",
            "§1§nNone yet, Submit yours!"
        },
        permission = "phatloots.manage"
    )
    public boolean addGem(CommandSender sender, String tier, String[] args) {
        setGem(sender, true, tier, args);
        return true;
    }

    @CodCommand(
        command = "remove",
        subcommand = "gem",
        weight = 198.3,
        aliases = {"-"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2#§f: §5The amount to be looted ex. §6#10 §5or §6#1-64",
            "§2<command> <tier> [Parameter1] [Parameter2]...",
            "§bex. §6<command> RANDOM #2-3 %80",
            "§bex. §6<command> Rare_Gem %10 cWeapon",
            "§bTutorial Video:",
            "§1§nNone yet, Submit yours!"
        },
        permission = "phatloots.manage"
    )
    public boolean removeGem(CommandSender sender, String tier, String[] args) {
        setGem(sender, false, tier, args);
        return true;
    }

    /**
     * Generates the Loot Gem and sets it on the PhatLoot(s)
     *
     * @param sender The CommandSender who is setting the Loot Gem
     * @param add True if the Loot Gem should be added to the PhatLoot(s)
     * @param tier The name of the MythicDrops Tier to apply
     * @param args The arguments of the Loot Gem
     */
    private static void setGem(CommandSender sender, boolean add, String tier, String[] args) {
        String phatLoot = null; //The name of the PhatLoot
        double percent = 100; //The chance of receiving the Loot (defaulted to 100)
        String coll = null; //The Collection to add the Loot to
        int amountLower = 1; //Least amount of loot items to possibly generate (defaulted to 1)
        int amountUpper = 1; //Most amount of loot items to possibly generate(defaulted to 1)

        //Check each parameter
        int i = 0;
        while (i < args.length) {
            char c = args[i].charAt(0);
            String s = args[i].substring(1);
            switch (c) {
            case 'p': //PhatLoot Name
                phatLoot = s;
                break;

            case '%': //Probability
                percent = LootCommandUtil.getPercent(sender, s);
                if (percent == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a percent");
                    return;
                }
                break;

            case 'c': //Collection Name
                coll = s;
                break;

            case '#': //Amount
                amountLower = LootCommandUtil.getLowerBound(s);
                amountUpper = LootCommandUtil.getUpperBound(s);
                if (amountLower == -1 || amountUpper == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a valid number or range");
                    return;
                }
                break;

            default: //Invalid Parameter
                sender.sendMessage("§6" + c + "§4 is not a valid parameter ID");
                return;
            }

            i++;
        }

        //Construct the Loot
        Gem loot = new Gem(tier, amountLower, amountUpper);
        loot.setProbability(percent);

        LootCommandUtil.setLoot(sender, phatLoot, add, coll, loot);
    }
}
