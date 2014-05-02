package com.codisimus.plugins.phatloots.commands;

import com.codisimus.plugins.phatloots.commands.CommandHandler.CodCommand;
import com.codisimus.plugins.phatloots.loot.MythicDropsItem;
import org.bukkit.command.CommandSender;

/**
 * Executes Player Commands
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
}
