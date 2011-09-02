
package PhatLoots;

import com.griefcraft.model.ProtectionTypes;
import java.util.Calendar;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import java.util.Random;
import org.bukkit.inventory.PlayerInventory;

/**
 * A PhatLoots is a reward made up of money and items
 * 
 * @author Codisimus
 */
public class PhatLoots {
    protected static boolean autoLoot;
    protected static String autoLootMsg;
    protected String name;
    protected String individualLoots = "";
    protected int numberCollectiveLoots;
    private String coll1 = "";
    private String coll2 = "";
    private String coll3 = "";
    private String coll4 = "";
    private String coll5 = "";
    private int total1 = 0;
    private int total2 = 0;
    private int total3 = 0;
    private int total4 = 0;
    private int total5 = 0;
    protected String resetTime;
    protected String resetType;
    protected String restrictedUsers = "";
    private static Random random = new Random();

    /**
     * Constructs a new PhatLoots chest
     * 
     * @param name The name of the PhatLoots which will be created
     */
    public PhatLoots (String name) {
        this.name = name;
        numberCollectiveLoots = PhatLootsMain.defaultNumberCollectiveLoots;
        resetTime = PhatLootsMain.defaultResetTime;
        resetType = PhatLootsMain.defaultResetType;
    }

    /**
     * Activates the PhatLoots by rolling for items to add to the chest
     * 
     * @param player The user who is looting
     * @param block The chest being looted
     */
    protected void getLoot(Player player, Block block) {
        String blockXYZ = (block.getWorld().getName()+","+block.getX()+","+block.getY()+","+block.getZ()+",");
        String lootTime = "NoTiNsTrInG";
        String[] split = restrictedUsers.split("~");
        //Find looted chest
        for (int i=0; i<split.length; i++) {
            String[] users = split[i].split(",");
            String chestXYZ = (users[0]+","+users[1]+","+users[2]+","+users[3]+",");
            if (chestXYZ.equals(blockXYZ))
                //Check if player looted before
                for (int j=4; j<users.length; j++) {
                    if (users[j].contains(player.getName()) || users[j].contains("all")) {
                        //Check if enough time has passed since last loot
                        if (resetType.equals("never"))
                            return;
                        lootTime = users[j+1];
                        String[] time = lootTime.split("'");
                        if(!isTimedOut(time))
                            return;
                    }
                }
        }
        lootIndividual(player, block);
        lootCollective(coll1, player, block);
        lootCollective(coll2, player, block);
        lootCollective(coll3, player, block);
        lootCollective(coll4, player, block);
        lootCollective(coll5, player, block);
        if (restrictedUsers.contains(lootTime))
            restrictedUsers = restrictedUsers.replace(lootTime, getCurrentTime());
        else {
            String user = "all";
            String newTime = getCurrentTime();
            if (resetType.equalsIgnoreCase("interval")) {
                String[] time = resetTime.split("'");
                int resetDay = Integer.parseInt(time[0]);
                int resetHour = Integer.parseInt(time[1]);
                int resetMinute = Integer.parseInt(time[2]);
                int resetSecond = Integer.parseInt(time[3]);
                time = lootTime.split("'");
                int newDay = Integer.parseInt(time[0]) + resetDay;
                int newHour = Integer.parseInt(time[1]) + resetHour;
                int newMinute = Integer.parseInt(time[2]) + resetMinute;
                int newSecond = Integer.parseInt(time[3]) + resetSecond;
                newTime = newDay+"'"+newHour+"'"+newMinute+"'"+newSecond;
            }
            else if(resetType.equalsIgnoreCase("user"))
                user = player.getName();
            restrictedUsers = restrictedUsers.replace(blockXYZ, blockXYZ+user+","+newTime+",");
        }
    }

    /**
     * Determines if given time + resetTime is later than current time
     * 
     * @param time The given time
     * @return false if given time + resetTime is later than current time
     */
    private boolean isTimedOut(String[] time) {
        int lootDay = Integer.parseInt(time[0]);
        int lootHour = Integer.parseInt(time[1]);
        int lootMinute = Integer.parseInt(time[2]);
        int lootSecond = Integer.parseInt(time[3]);
        time = getCurrentTime().split("'");
        int nowDay = Integer.parseInt(time[0]);
        int nowHour = Integer.parseInt(time[1]);
        int nowMinute = Integer.parseInt(time[2]);
        int nowSecond = Integer.parseInt(time[3]);
        time = resetTime.split("'");
        int resetDay = Integer.parseInt(time[0]);
        int resetHour = Integer.parseInt(time[1]);
        int resetMinute = Integer.parseInt(time[2]);
        int resetSecond = Integer.parseInt(time[3]);
        lootSecond = resetSecond + lootSecond;
        if (lootSecond >= 60) {
            lootMinute++;
            lootSecond = lootSecond - 60;
        }
        lootMinute = resetMinute + lootMinute;
        if (lootMinute >= 60) {
            lootHour++;
            lootMinute = lootMinute - 60;
        }
        lootHour = resetHour + lootHour;
        if (lootHour >= 24) {
            lootDay++;
            lootHour = lootHour - 24;
        }
        lootDay = resetDay + lootDay;
        if (lootSecond >= 60) {
            lootMinute++;
            lootSecond = lootSecond - 60;
        }
        if (lootDay < nowDay)
            return true;
        else if(lootDay == nowDay) {
            if (lootHour < nowHour)
                return true;
            else if (lootHour == nowHour) {
                if (lootMinute < nowMinute)
                    return true;
                else if (lootMinute == nowMinute)
                    if (lootSecond <= nowSecond)
                        return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the current time in the format DAY'HOUR'MINUTE'SECOND
     * 
     * @return The current time in the format DAY'HOUR'MINUTE'SECOND
     */
    private static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        String time = (calendar.get(Calendar.DAY_OF_YEAR)+"'");
        time = (time+calendar.get(Calendar.HOUR_OF_DAY)+"'");
        time = (time+calendar.get(Calendar.MINUTE)+"'");
        time = (time+calendar.get(Calendar.SECOND));
        return time;
    }
    
    /**
     * Fills the chest (block) with loot
     * Each item is rolled for to determine if it will by added to the chest
     * Money is rolled for to determine how much will be given within the range
     * 
     * @param player The Player looting
     * @param block The Block being looted
     */
    private void lootIndividual(Player player, Block block) {
        if (!individualLoots.trim().isEmpty()) {
            String[] loot = individualLoots.split("~");
            for (int i = 0 ; i < loot.length; i++) {
                String[] itemInfo = loot[i].split(",");
                if (itemInfo[0].equalsIgnoreCase("money")) {
                    int low = Integer.parseInt(itemInfo[1]);
                    int high = Integer.parseInt(itemInfo[2]);
                    int amount = random.nextInt((high + 1) - low);
                    amount = amount + low;
                    String money = Register.reward(player.getName(), amount);
                    if (money != null)
                        player.sendMessage(money+" added to your account!");
                }
                else {
                    int percent = Integer.parseInt(itemInfo[2]);
                    int roll = random.nextInt(100);
                    if (roll < percent) {
                        ItemStack item = null;
                        if (itemInfo[0].contains(".")) {
                            String[] itemType = itemInfo[0].split(".");
                            item = new ItemStack(Integer.parseInt(itemType[0]), Integer.parseInt(itemInfo[1]));
                            if (itemType.length > 1)
                                item.setDurability((short)Integer.parseInt(itemType[1]));
                        }
                        else
                            item = new ItemStack(Integer.parseInt(itemInfo[0]), Integer.parseInt(itemInfo[1]));
                        if (item == null)
                            return;
                        PlayerInventory sack = player.getInventory();
                        if (autoLoot && sack.firstEmpty() >= 0) {
                            player.sendMessage(autoLootMsg.replaceAll("<item>", item.getType().name()));
                            sack.addItem(item);
                        }
                        else {
                            Chest chest = (Chest)block.getState();
                            chest.getInventory().clear(random.nextInt(15)+10);
                            chest.getInventory().addItem(item);
                        }
                    }
                }
            }
        }
    }

    /**
     * Fills the chest (block) with loot
     * Items are rolled for in order until the maximum number is added to the chest
     * 
     * @param collectiveLoots The String that contains the items and percentages
     * @param block The Block being looted
     */
    private void lootCollective(String collectiveLoots, Player player, Block block) {
        if (!collectiveLoots.trim().isEmpty()) {
            String[] cLoot = collectiveLoots.split("~");
            int numberLooted = 0;
            while (numberLooted < numberCollectiveLoots) {
                for (int i = 0 ; i < cLoot.length; i++) {
                    String[] itemInfo = cLoot[i].split(",");
                    int percent = Integer.parseInt(itemInfo[2]);
                    int roll = random.nextInt(100);
                    if (roll < percent) {
                        ItemStack item = null;
                        if (itemInfo[0].contains(".")) {
                            String[] itemType = itemInfo[0].split(".");
                            item = new ItemStack(Integer.parseInt(itemType[0]), Integer.parseInt(itemInfo[1]));
                            if (itemType.length > 1)
                                item.setDurability((short)Integer.parseInt(itemType[1]));
                        }
                        else 
                            item = new ItemStack(Integer.parseInt(itemInfo[0]), Integer.parseInt(itemInfo[1]));
                        if (item == null)
                            return;
                        PlayerInventory sack = player.getInventory();
                        if (autoLoot && sack.firstEmpty() > 0) {
                            player.sendMessage(autoLootMsg.replaceAll("<item>", item.getType().name()));
                            sack.addItem(item);
                        }
                        else {
                            Chest chest = (Chest)block.getState();
                            chest.getInventory().clear(random.nextInt(15)+10);
                            chest.getInventory().addItem(item);
                        }
                        numberLooted++;
                    }
                    if (numberLooted >= numberCollectiveLoots)
                        return;
                }
            }
        }
    }

    /**
     * Resets the restricted list for all chest of this PhatLoots
     * 
     */
    protected void reset() {
        String[] split = restrictedUsers.split("~");
        restrictedUsers = "";
        for (int i=0; i<split.length; i++) {
            String[] users = split[i].split(",");
            String buttonXYZ = users[0]+","+users[1]+","+users[2]+","+users[3]+",";
            restrictedUsers = restrictedUsers.concat(buttonXYZ);
        }
    }
    
    /**
     * Sets the collective loot for the PhatLoots
     * 
     * @param number The number of the collectiveLoot
     * @param loot The String of loots
     */
    protected void setCollectiveLoots(int number, String loots) {
        if (loots == null || loots.equals(""))
            return;
        String[] loot = loots.split("~");
        int total = 0;
        for (int i = 0; i < loot.length; i++) {
            String[] split = loot[i].split(",");
            total = total + Integer.parseInt(split[2]);
        }
        if (number == 1) {
            coll1 = loots.replaceAll("_", "~");
            total1 = total;
        }
        else if(number == 2) {
            System.out.println("5");
            coll2 = loots.replaceAll("_", "~");
            total2 = total;
        }
        else if(number == 3) {
            coll3 = loots.replaceAll("_", "~");
            total3 = total;
        }
        else if(number == 4) {
            coll4 = loots.replaceAll("_", "~");
            total4 = total;
        }
        else if(number == 5) {
            coll5 = loots.replaceAll("_", "~");
            total5 = total;
        }
    }
    
    /**
     * Adds the collective loot to the PhatLoots
     * 
     * @param number The number of the collectiveLoot
     * @param loot The loot String to be added
     * @return The total percent of the collective loot
     */
    protected int addCollectiveLoot(int number, String loot) {
        String[] split = loot.split(",");
        if (number == 1) {
            coll1 = coll1.concat(loot.concat(",~"));
            total1 = total1 + Integer.parseInt(split[2]);
            return total1;
        }
        else if (number == 2) {
            coll2 = coll2.concat(loot.concat(",~"));
            total2 = total2 + Integer.parseInt(split[2]);
            return total2;
        }
        else if (number == 3) {
            coll3 = coll3.concat(loot.concat(",~"));
            total3 = total3 + Integer.parseInt(split[2]);
            return total3;
        }
        else if (number == 4) {
            coll4 = coll4.concat(loot.concat(",~"));
            total4 = total4 + Integer.parseInt(split[2]);
            return total4;
        }
        else if (number == 5) {
            coll5 = coll5.concat(loot.concat(",~"));
            total5 = total5 + Integer.parseInt(split[2]);
            return total5;
        }
        return 0;
    }

    /**
     * Removes the collective loot from the PhatLoots
     * 
     * @param number The number of the collectiveLoot
     * @param loot The loot String to be removed
     * @return The total percent of the collective loot
     */
    protected int removeCollectiveLoot(int number, String loot) {
        String[] split = loot.split(",");
        if (number == 1) {
            coll1 = coll1.replaceAll(loot.concat(",~"), "");
            total1 = total1 - Integer.parseInt(split[2]);
            return total1;
        }
        else if (number == 2) {
            coll2 = coll2.replaceAll(loot.concat(",~"), "");
            total2 = total2 - Integer.parseInt(split[2]);
            return total2;
        }
        else if (number == 3) {
            coll3 = coll3.replaceAll(loot.concat(",~"), "");
            total3 = total3 - Integer.parseInt(split[2]);
            return total3;
        }
        else if (number == 4) {
            coll4 = coll4.replaceAll(loot.concat(",~"), "");
            total4 = total4 - Integer.parseInt(split[2]);
            return total4;
        }
        else if (number == 5) {
            coll5 = coll5.replaceAll(loot.concat(",~"), "");
            total5 = total5 - Integer.parseInt(split[2]);
            return total5;
        }
        return 0;
    }

    /**
     * Returns the collective loot of the given number
     * 
     * @return The String of collective loots of the given number
     */
    protected String getCollectiveLoots(int number) {
        if (number == 1)
            return coll1;
        else if(number == 2)
            return coll2;
        else if(number == 3)
            return coll3;
        else if(number == 4)
            return coll4;
        else if(number == 5)
            return coll5;
        return null;
    }
    
    /**
     * Adds a chest to the PhatLoots
     * 
     * @param chest The chest which will be added
     */
    protected void addChest(Block chest) {
        String chestXYZ = (chest.getWorld().getName()+","+chest.getX()+","+chest.getY()+","+chest.getZ()+",~");
        restrictedUsers = restrictedUsers+chestXYZ;
        if (PhatLootsMain.lwc != null || !PhatLootsMain.autoLock)
            return;
        int blockId = 0;
        int type = ProtectionTypes.PUBLIC;
        String world = chest.getWorld().getName();
        String owner = "";
        String password = "";
        int x = chest.getX();
        int y = chest.getY();
        int z = chest.getZ();
        PhatLootsMain.lwc.getPhysicalDatabase().registerProtection(blockId, type, world, owner, password, x, y, z);
    }

    /**
     * Removes a chest from the PhatLoots
     * 
     * @param chest The chest that will be removed
     * @return true if the chest was removed
     */
    protected boolean removeChest(Block chest) {
        String chestXYZ = chest.getWorld().getName()+","+chest.getX()+","+chest.getY()+","+chest.getZ()+",";
        String[] split = restrictedUsers.split("~");
        for (int i = 0 ; i < split.length; ++i) {
            if (split[i].startsWith(chestXYZ)) {
                restrictedUsers = restrictedUsers.replaceAll(split[i]+"~", "");
                return true;
            }
        }
        return false;
    }
}
