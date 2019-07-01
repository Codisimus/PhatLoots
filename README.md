## This repo is no longer being updated. For future updates, please view: https://github.com/RednedEpic/PhatLoots.

PhatLoots is a Bukkit plugin that allows a server admin to setup 'Loot Tables' that give Players Items, money, exp, etc. in the following scenarios:

1.) Player opens a Chest
    Chests may be placed around the World by an admin and linked to specific Loot Tables. When a Player finds one of these Chests and opens it, it will randomly choose 'loot' that appears in the Chest.

2.) Player right clicks a Block
    Any Block may function exactly like a chest as described above.

3.) A Dispenser is triggered by redstone
    If a the dispenser is linked to a PhatLoot, the loot will be dispensed

4.) When a Mob (Friendly or Hostile) dies
    The items/money/exp/etc. that the mob drops may be controlled by a PhatLoots loot table
    Mob loot tables may be unique based on the world or region that they are in
    Mob loot tables may be specific to the type of mob (ex. a Villiger's profession)
    Named Mobs may have their own loot table assigned to them

5.) When a Mob (specifically Zombie or Skeleton) spawns
    The armor and weapon that a mob spawns with may be controlled by a loot table

6.) When a Player fishes
    The item that the player fishes out of the water may be controlled by a loot table

* Each chest that is looted may be given a cooldown time until it can be looted again.
* Chests may be global or individual, global is essentially first come first server and allows for ninjaing of items.
* PhatLoots may be set to 'autoloot' so that items are sent directly to the Player's inventory.
* Players may be given permissions to modify what loot they receive.
* Loot Collections allow for complicated loot table structures.
* Chests may be automatically linked so that you do not have to walk around and link them all.
* Weapons and Armor may be automatically enchanted, named, and tiered to make them more exciting.
* In game GUI (/loot info <PhatLoot>) using a chest inventory allows for easily viewing the information of a PhatLoot
* Nearly all messages/features are customizable and may be disabled.
