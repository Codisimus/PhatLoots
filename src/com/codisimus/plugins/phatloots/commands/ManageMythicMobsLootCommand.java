package com.codisimus.plugins.phatloots.commands;

import com.codisimus.plugins.phatloots.commands.CommandHandler.CodCommand;
import com.codisimus.plugins.phatloots.loot.MythicMobsItem;
import org.bukkit.command.CommandSender;

/**
 * Executes Player Commands for configuring MythicMobs Loot
 *
 * @author Codisimus
 */
public class ManageMythicMobsLootCommand {

    @CodCommand(
        command = "add",
        subcommand = "mm",
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
            "§2<command> <tier> [Parameter1] [Parameter2]...",
            "§bex. §6<command> ItemA #2-3 %80",
            "§bex. §6<command> ItemB %10 cWeapon",
            "§bTutorial Video:",
            "§1§nNone yet, Submit yours!"
        },
        permission = "phatloots.manage"
    )
    public boolean addMythicMobsItem(CommandSender sender, String itemId, String[] args) {
        setMythicMobsItem(sender, true, itemId, args);
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
            "§2<command> <tier> [Parameter1] [Parameter2]...",
            "§bex. §6<command> ItemA #2-3 %80",
            "§bex. §6<command> ItemB %10 cWeapon",
            "§bTutorial Video:",
            "§1§nNone yet, Submit yours!"
        },
        permission = "phatloots.manage"
    )
    public boolean removeMythicMobsItem(CommandSender sender, String itemId, String[] args) {
        setMythicMobsItem(sender, false, itemId, args);
        return true;
    }

    /**
     * Generates the MythicDrops Item and sets it on the PhatLoot(s)
     *
     * @param sender The CommandSender who is setting the MythicMobs Item
     * @param add True if the MythicMobs Item should be added to the PhatLoot(s)
     * @param itemId The ID of the MythicMobs Item
     * @param args The arguments of the MythicMobs Item
     */
    private static void setMythicMobsItem(CommandSender sender, boolean add, String itemId, String[] args) {
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
        MythicMobsItem loot = new MythicMobsItem(itemId, amountLower, amountUpper);
        loot.setProbability(percent);

        LootCommandUtil.setLoot(sender, phatLoot, add, coll, loot);
    }
}
