package com.codisimus.plugins.phatloots.commands;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsConfig;
import com.codisimus.plugins.phatloots.commands.CommandHandler.CodCommand;
import com.codisimus.plugins.phatloots.loot.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Executes Player Commands
 *
 * @author Codisimus
 */
public class ManageLootCommand {
    @CodCommand(
        command = "add",
        subcommand = "hand",
        weight = 190,
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
            "§2t§f: §5Tier the Item (tiers.yml) ex. §6t",
            "§2l§f: §5Generate Lore for the Item (lores.yml) ex. §l",
            "§2e§f: §5The item enchantment ex. §6earrow_fire §5or §6eauto",
            "§bEnchantment levels can be added. ex. §6arrow_fire(2)",
            "§2<command> [Parameter1] [Parameter2]...",
            "§bex. §6<command> #1-16 %32",
            "§bTutorial Video:",
            "§1§nwww.youtu.be/7ViP0dEq7nk"
        },
        permission = "phatloots.manage"
    )
    public boolean addHand(Player player, String[] args) {
        setItem(player, true, player.getItemInHand().clone(), args);
        return true;
    }

    @CodCommand(
        command = "add",
        subcommand = "coll",
        weight = 191,
        aliases = {"+"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2#§f: §5The amount to be looted ex. §6#10 §5or §6#1-64",
            "§bUse §6#0 §bif you want each Loot in a collection to be rolled for individually",
            "§2<command> <Name> [Parameter1] [Parameter2]...",
            "§bex. §6<command> Weapon %25",
            "§bTutorial Video:",
            "§1§nwww.youtu.be/7ViP0dEq7nk"
        },
        permission = "phatloots.manage"
    )
    public boolean addColl(CommandSender sender, String name, String[] args) {
        setColl(sender, true, name, args);
        return true;
    }

    @CodCommand(
        command = "add",
        subcommand = "cmd",
        weight = 192,
        aliases = {"+"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2<command> [Parameter1] [Parameter2]... /<Command>",
            "§bex. §6<command> %50 /broadcast &6<player>&r found the treasure!",
            "§bTutorial Video:",
            "§1§nwww.youtu.be/7ViP0dEq7nk"
        },
        permission = "phatloots.manage"
    )
    public boolean addCmd(CommandSender sender, String[] args) {
        setCmd(sender, true, args);
        return true;
    }

    @CodCommand(
        command = "add",
        subcommand = "msg",
        weight = 193,
        aliases = {"+"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2<command> [Parameter1] [Parameter2]... -<Message>",
            "§bex. §6<command> %50 -Congrats &6<player>&r, You found the treasure!",
            "§bTutorial Video:",
            "§1§nwww.youtu.be/7ViP0dEq7nk"
        },
        permission = "phatloots.manage"
    )
    public boolean addMsg(CommandSender sender, String[] args) {
        setMsg(sender, true, args);
        return true;
    }

    @CodCommand(
        command = "add",
        weight = 194,
        aliases = {"+"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2#§f: §5The amount to be looted ex. §6#10 §5or §6#1-64",
            "§bUse §6#0 §bif you want each Loot in a collection to be rolled for individually",
            "§2d§f: §5The data/durability value of the item ex. §6d5",
            "§2t§f: §5Tier the Item (tiers.yml) ex. §6t",
            "§2l§f: §5Generate Lore for the Item (lores.yml) ex. §l",
            "§2e§f: §5The item enchantment ex. §6earrow_fire §5or §6eauto",
            "§bEnchantment levels can be added. ex. §6arrow_fire(2)",
            "§2<command> <Item|ID|hand> [Parameter1] [Parameter2]...",
            "§bex. §6<command> hand #1-16 %32",
            "§bex. §6<command> diamond_sword efire_aspect(2) edamage_all %75 cWeapon",
            "§2<command> coll <Name> [Parameter1] [Parameter2]...",
            "§bex. §6<command> coll Weapon %25",
            "§2<command> cmd [Parameter1] [Parameter2]... /<Command>",
            "§2<command> msg [Parameter1] [Parameter2]... -<Message>",
            "§bTutorial Video:",
            "§1§nwww.youtu.be/7ViP0dEq7nk"
        },
        permission = "phatloots.manage"
    )
    public boolean add(CommandSender sender, Material mat, String[] args) {
        setItem(sender, true, new ItemStack(mat), args);
        return true;
    }

    @CodCommand(
        command = "remove",
        subcommand = "hand",
        weight = 195,
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
            "§2t§f: §5Tier the Item (tiers.yml) ex. §6t",
            "§2l§f: §5Generate Lore for the Item (lores.yml) ex. §l",
            "§2e§f: §5The item enchantment ex. §6earrow_fire §5or §6eauto",
            "§bEnchantment levels can be added. ex. §6arrow_fire(2)",
            "§2<command> [Parameter1] [Parameter2]...",
            "§bex. §6<command> #1-16 %32",
            "§bTutorial Video:",
            "§1§nwww.youtu.be/7ViP0dEq7nk"
        },
        permission = "phatloots.manage"
    )
    public boolean removeHand(Player player, String[] args) {
        setItem(player, false, player.getItemInHand(), args);
        return true;
    }

    @CodCommand(
        command = "remove",
        subcommand = "coll",
        weight = 196,
        aliases = {"+"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2#§f: §5The amount to be looted ex. §6#10 §5or §6#1-64",
            "§bUse §6#0 §bif you want each Loot in a collection to be rolled for individually",
            "§2<command> <Name> [Parameter1] [Parameter2]...",
            "§bex. §6<command> Weapon %25",
            "§bTutorial Video:",
            "§1§nwww.youtu.be/7ViP0dEq7nk"
        },
        permission = "phatloots.manage"
    )
    public boolean removeColl(CommandSender sender, String name, String[] args) {
        setColl(sender, false, name, args);
        return true;
    }

    @CodCommand(
        command = "remove",
        subcommand = "cmd",
        weight = 197,
        aliases = {"+"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2<command> [Parameter1] [Parameter2]... /<Command>",
            "§bex. §6<command> %50 /broadcast &6<player>&r found the treasure!",
            "§bTutorial Video:",
            "§1§nwww.youtu.be/7ViP0dEq7nk"
        },
        permission = "phatloots.manage"
    )
    public boolean removeCmd(CommandSender sender, String[] args) {
        setCmd(sender, false, args);
        return true;
    }

    @CodCommand(
        command = "remove",
        subcommand = "msg",
        weight = 198,
        aliases = {"+"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2<command> [Parameter1] [Parameter2]... -<Message>",
            "§bex. §6<command> %50 -Congrats &6<player>&r, You found the treasure!",
            "§bTutorial Video:",
            "§1§nwww.youtu.be/7ViP0dEq7nk"
        },
        permission = "phatloots.manage"
    )
    public boolean removeMsg(CommandSender sender, String[] args) {
        setMsg(sender, false, args);
        return true;
    }

    @CodCommand(
        command = "remove",
        weight = 199,
        aliases = {"-"},
        usage = {
            "§e     PhatLoots Manage Loot Help Page:",
            "§5A Parameter starts with the 1 character §2id",
            "§2p§f: §5The Name of the PhatLoot ex. §6pEpic",
            "§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected",
            "§2%§f: §5The chance of looting ex. §6%50 §5or §6%0.1 §5(default: §6100§5)",
            "§2c§f: §5The name of the collection to add the loot to ex. §6cFood",
            "§2#§f: §5The amount to be looted ex. §6#10 §5or §6#1-64",
            "§bUse §6#0 §bif you want each Loot in a collection to be rolled for individually",
            "§2d§f: §5The data/durability value of the item ex. §6d5",
            "§2t§f: §5Tier the Item (tiers.yml) ex. §6t",
            "§2l§f: §5Generate Lore for the Item (lores.yml) ex. §l",
            "§2e§f: §5The item enchantment ex. §6earrow_fire §5or §6eauto",
            "§bEnchantment levels can be added. ex. §6arrow_fire(2)",
            "§2<command> <Item|ID|hand> [Parameter1] [Parameter2]...",
            "§bex. §6<command> hand #1-16 %32",
            "§bex. §6<command> diamond_sword efire_aspect(2) edamage_all %75 cWeapon",
            "§2<command> coll <Name> [Parameter1] [Parameter2]...",
            "§bex. §6<command> coll Weapon %25",
            "§2<command> cmd [Parameter1] [Parameter2]... /<Command>",
            "§2<command> msg [Parameter1] [Parameter2]... -<Message>",
            "§bTutorial Video:",
            "§1§nwww.youtu.be/7ViP0dEq7nk"
        },
        permission = "phatloots.manage"
    )
    public boolean remove(CommandSender sender, Material mat, String[] args) {
        setItem(sender, false, new ItemStack(mat), args);
        return true;
    }

    private static void setItem(CommandSender sender, boolean add, ItemStack item, String[] args) {
        String phatLoot = null; //The name of the PhatLoot
        double percent = 100; //The chance of receiving the Loot (defaulted to 100)
        String coll = null; //The Collection to add the Loot to
        int lowerBound = 1; //Stack size of the Loot item (defaulted to 1)
        int upperBound = 1; //Amount to possibly increase the Stack size of the Loot item (defaulted to 1)
        boolean autoEnchant = false; //Whether or not the Loot Item should be automatically enchanted at time of Looting
        boolean tiered = false; //Whether or not the Loot Item should be Tiered
        boolean generateName = false; //Whether or not the Loot Item should have a generated name

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
                percent = getPercent(sender, s);
                if (percent == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a percent");
                    return;
                }
                break;

            case 'c': //Collection Name
                coll = s;
                break;

            case '#': //Amount
                lowerBound = getLowerBound(s);
                upperBound = getUpperBound(s);
                if (lowerBound == -1 || upperBound == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a valid number or range");
                    return;
                }
                item.setAmount(lowerBound);
                break;

            case 'e': //Enchantment
                if (s.equalsIgnoreCase("auto")) {
                    autoEnchant = true;
                } else {
                    Map<Enchantment, Integer> enchantments = getEnchantments(s);
                    if (enchantments == null) {
                        sender.sendMessage("§6" + s + "§4 is not a valid enchantment");
                        return;
                    }
                    item.addUnsafeEnchantments(enchantments);
                }
                break;

            case 'd': //Durability
                short data = getData(s);
                if (data == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a valid data/durability value");
                    return;
                }
                item.setDurability(data);
                break;

            case 't': //Tiered
                tiered = true;
                break;

            case 'l': //Automatic Lore
                generateName = true;
                break;

            default: //Invalid Parameter
                sender.sendMessage("§6" + c + "§4 is not a valid parameter ID");
                return;
            }

            i++;
        }

        //Construct the Loot
        Item loot = new Item(item, upperBound - lowerBound);
        if (autoEnchant) {
            loot.autoEnchant = true;
        }
        if (tiered) {
            loot.tieredName = true;
        }
        if (generateName) {
            loot.generateName = true;
        }
        loot.setProbability(percent);

        setLoot(sender, phatLoot, add, coll, loot);
    }

    private static void setColl(CommandSender sender, boolean add, String name, String[] args) {
        String phatLoot = null; //The name of the PhatLoot
        double percent = 100; //The chance of receiving the Loot (defaulted to 100)
        String coll = null; //The Collection to add the Loot to
        int lowerBound = PhatLootsConfig.defaultLowerNumberOfLoots;
        int upperBound = PhatLootsConfig.defaultUpperNumberOfLoots;

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
                percent = getPercent(sender, s);
                if (percent == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a percent");
                    return;
                }
                break;

            case 'c': //Collection Name
                coll = s;
                break;

            case '#': //Amount
                lowerBound = getLowerBound(s);
                upperBound = getUpperBound(s);
                if (lowerBound == -1 || upperBound == -1) {
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
        Loot loot = new LootCollection(name, lowerBound, upperBound);
        loot.setProbability(percent);

        setLoot(sender, phatLoot, add, coll, loot);
    }

    private static void setCmd(CommandSender sender, boolean add, String[] args) {
        String cmd = null; //The command to be added/removed
        String phatLoot = null; //The name of the PhatLoot
        double percent = 100; //The chance of receiving the Loot (defaulted to 100)
        String coll = null; //The Collection to add the Loot to

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
                percent = getPercent(sender, s);
                if (percent == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a percent");
                    return;
                }
                break;

            case 'c': //Collection Name
                coll = s;
                break;

            case '/': //Command
                cmd = args[i];
                i++;
                while (i < args.length) {
                    cmd += " " + args[i];
                    i++;
                }
                break;

            default: //Invalid Parameter
                sender.sendMessage("§6" + c + "§4 is not a valid parameter ID");
                return;
            }

            i++;
        }

        //Construct the Loot
        Loot loot = new CommandLoot(cmd);
        loot.setProbability(percent);

        setLoot(sender, phatLoot, add, coll, loot);
    }

    private static void setMsg(CommandSender sender, boolean add, String[] args) {
        String msg = null; //The message to be added/removed
        String phatLoot = null; //The name of the PhatLoot
        double percent = 100; //The chance of receiving the Loot (defaulted to 100)
        String coll = null; //The Collection to add the Loot to

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
                percent = getPercent(sender, s);
                if (percent == -1) {
                    sender.sendMessage("§6" + s + "§4 is not a percent");
                    return;
                }
                break;

            case 'c': //Collection Name
                coll = s;
                break;

            case '-': //Message
                msg = args[i];
                i++;
                while (i < args.length) {
                    msg += " " + args[i];
                    i++;
                }
                break;

            default: //Invalid Parameter
                sender.sendMessage("§6" + c + "§4 is not a valid parameter ID");
                return;
            }

            i++;
        }

        //Construct the Loot
        Loot loot = new Message(msg);
        loot.setProbability(percent);

        setLoot(sender, phatLoot, add, coll, loot);
    }

    /**
     * Adds/Removes a Loot to the specified PhatLoot
     *
     * @param sender The CommandSender modifying the PhatLoot
     * @param phatLootName The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param add True the Loot will be added, false if it will be removed
     * @param collName The name of the collection if any
     * @param loot The Loot that will be added/removed
     */
    private static void setLoot(CommandSender sender, String phatLootName, boolean add, String collName, Loot loot) {
        String lootDescription = loot.toString();

        for (PhatLoot phatLoot : getPhatLoots(sender, phatLootName)) {
            //Check if a LootCollection was specified
            LootCollection coll = null;
            if (collName != null) {
                coll = phatLoot.findCollection(collName);
                if (coll == null) {
                    sender.sendMessage("§4Collection §6" + collName + "§4 does not exist");
                    return;
                }
            }

            if (coll == null) {
                if (add) { //Add to PhatLoot
                    if (phatLoot.addLoot(loot)) { //Successful
                        sender.sendMessage("§6" + lootDescription
                                + "§5 added as Loot for PhatLoot §6"
                                + phatLoot.name);
                        phatLoot.save();
                    } else { //Unsuccessful
                        sender.sendMessage("§6" + lootDescription
                                + "§4 is already Loot for PhatLoot §6"
                                + phatLoot.name);
                    }
                } else { //Remove from PhatLoot
                    if (phatLoot.removeLoot(loot)) { //Successful
                        sender.sendMessage("§6" + lootDescription
                                + "§5 removed as Loot for PhatLoot §6"
                                + phatLoot.name);
                        phatLoot.save();
                    } else { //Unsuccessful
                        sender.sendMessage("§6" + lootDescription
                                + "§4 is not Loot for PhatLoot §6"
                                + phatLoot.name);
                    }
                }
            } else {
                if (add) { //Add to LootCollection
                    if (coll.addLoot(loot)) { //Successful
                        sender.sendMessage("§6" + lootDescription
                                + "§5 added as Loot for Collection §6"
                                + coll.name);
                        phatLoot.save();
                    } else { //Unsuccessful
                        sender.sendMessage("§6" + lootDescription
                                + "§4 is already Loot for Collection §6"
                                + coll.name);
                    }
                } else { //Remove from LootCollection
                    if (coll.removeLoot(loot)) { //Successful
                        sender.sendMessage("§6" + lootDescription
                                + "§5 removed as Loot for Collection §6"
                                + coll.name);
                        phatLoot.save();
                    } else { //Unsuccessful
                        sender.sendMessage("§6" + lootDescription
                                + "§4 is not Loot for Collection §6"
                                + coll.name);
                    }
                }
            }
        }
    }

    /**
     * Returns the a LinkedList of PhatLoots.
     * If a name is provided then only the PhatLoot with the given name will be in the List.
     * If no name is provided then each PhatLoot that is linked to the target Block will be in the List
     *
     * @param sender The CommandSender targeting a Block
     * @param name The name of the PhatLoot to be found
     * @return The a LinkedList of PhatLoots
     */
    private static LinkedList<PhatLoot> getPhatLoots(CommandSender sender, String name) {
        LinkedList<PhatLoot> phatLoots = new LinkedList<PhatLoot>();

        if (name != null) {
            //Find the PhatLoot using the given name
            PhatLoot phatLoot = PhatLoots.getPhatLoot(name);

            //Inform the sender if the PhatLoot does not exist
            if (phatLoot != null ) {
                phatLoots.add(phatLoot);
            } else {
                sender.sendMessage("§4PhatLoot §6" + name + "§4 does not exist.");
            }
        } else {
            //Cancel is the sender is console
            if (sender instanceof Player) {
                phatLoots = LootCommand.getPhatLoots((Player) sender);
            } else {
                sender.sendMessage("§4You cannot do this from the console!");
            }
        }

        return phatLoots;
    }

    /**
     * Retrieves an int value from the given string
     *
     * @param sender The CommandSender that will receive error messages
     * @param string The String that contains the amount
     */
    private static int getLowerBound(String string) {
        if (string.contains("-")) {
            string = string.substring(0, string.indexOf('-'));
        }

        try {
            return Integer.parseInt(string);
        } catch (Exception notInt) {
            return -1;
        }
    }

    /**
     * Retrieves an int value from the given string
     *
     * @param sender The CommandSender that will receive error messages
     * @param string The String that contains the amount
     */
    private static int getUpperBound(String string) {
        if (string.contains("-")) {
            string = string.substring(string.indexOf('-') + 1);
        }

        try {
            return Integer.parseInt(string);
        } catch (Exception notInt) {
            return -1;
        }
    }

    /**
     * Retrieves Enchantments from the given string
     *
     * @param string The String that contains the item
     * @return The Enchantments of the item
     */
    private static Map<Enchantment, Integer> getEnchantments(String string) {
        Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
        try {
            for (String split: string.split("&")) {
                Enchantment enchantment = null;
                int level = -1;

                if (split.contains("(")) {
                    int index = split.indexOf('(');
                    level = Integer.parseInt(split.substring(index + 1, split.length() - 1));
                    split = split.substring(0, index);
                }

                for (Enchantment enchant: Enchantment.values()) {
                    if (enchant.getName().equalsIgnoreCase(split)) {
                        enchantment = enchant;
                    }
                }

                if (level < enchantment.getStartLevel()) {
                    level = enchantment.getStartLevel();
                }

                enchantments.put(enchantment, level);
            }
        } catch (Exception notEnchantment) {
            return null;
        }
        return enchantments;
    }

    /**
     * Retrieves a short value from the given string
     *
     * @param sender The CommandSender that will receive error messages
     * @param string The String that contains the item
     */
    private static short getData(String string) {
        short data;
        try {
            data = Short.parseShort(string);
        } catch (Exception notShort) {
            return -1;
        }
        return data;
    }

    /**
     * Retrieves a double value from the given string that ends with %
     *
     * @param sender The CommandSender that will receive error messages
     * @param string The String that contains the percent
     */
    private static double getPercent(CommandSender sender, String string) {
        double percent;
        try {
            percent = Double.parseDouble(string);
            if (percent < 0) {
                if (sender != null) {
                    sender.sendMessage("§4The percent cannot be below 0");
                }
            }
            if (percent > 100) {
                if (sender != null) {
                    sender.sendMessage("§4The percent cannot be above 100");
                }
            } else {
                return percent;
            }
        } catch (Exception notDouble) {
            if (sender != null) {
                sender.sendMessage("§6" + string + "§4 is not a valid number");
            }
        }
        return -1;
    }
}
