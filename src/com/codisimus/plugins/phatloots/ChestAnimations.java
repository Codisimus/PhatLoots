package com.codisimus.plugins.phatloots;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

public class ChestAnimations {
    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final String NMS_PACKAGE = "net.minecraft.server." + VERSION + ".";
    private static final String CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit." + VERSION + ".";

    private static final boolean USE_API;

    static {
        boolean useApi = true;
        try {
            Class.forName("org.bukkit.block.Lidded");
        } catch (ClassNotFoundException ex) {
            useApi = false;
        }
        USE_API = useApi;
    }

    public static void openChest(Block block) {
        playChestAction(null, block, true);
    }

    public static void openChest(Player player, Block block) {
        playChestAction(player, block, true);
    }

    public static void closeChest(Block block) {
        playChestAction(null, block, false);
    }

    public static void closeChest(Player player, Block block) {
        playChestAction(player, block, false);
    }
    
    private static void playChestActionNoReflection(Block block, boolean open) {
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) {
            return;
        }
        if (open) {
            chest.open();
        } else {
            chest.close();
        }
    }

    public static void playChestAction(Player player, Block block, boolean open) {
        if (USE_API) {
            playChestActionNoReflection(block, open);
            return;
        }
        Location location = block.getLocation();
        Object blockPosition = construct(NMS_PACKAGE + "BlockPosition", location.getX(), location.getY(), location.getZ());
        int openCode = open ? 1 : 0;

        if (player != null) {
            Object nmsBlock = invokeStaticMethod(CRAFTBUKKIT_PACKAGE + "util.CraftMagicNumbers", "getBlock", block);
            Object packet = construct(NMS_PACKAGE + "PacketPlayOutBlockAction", blockPosition, nmsBlock, 1, openCode);
            Object entityPlayer = invokeMethod(player, "getHandle");
            invokeMethod(entityPlayer, "getHandle");
            Object playerConnection = getVariable(entityPlayer, "playerConnection");
            invokeMethod(playerConnection, "sendPacket", packet);
        } else {
            Object world = invokeMethod(location.getWorld(), "getHandle");
            Object tileEntity = invokeMethod(world, "getTileEntity", blockPosition);
            Object nmsBlock = invokeMethod(tileEntity, "getBlock");
            invokeMethod(world, "playBlockAction", blockPosition, nmsBlock, 1, openCode);
        }
    }

    private static Object getVariable(Object obj, String variableName) {
        try {
            return obj.getClass().getField(variableName).get(obj);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            PhatLoots.logger.log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }

    private static Object getStaticVariable(Class c, String variableName) {
        try {
            return c.getField(variableName).get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            PhatLoots.logger.log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }

    private static Object invokeMethod(Object obj, String methodName, Object... params) {
        Object ret = null;
        Class c = obj.getClass();
        for (Method method : c.getMethods()) {
            if (method.getName().equals(methodName)) {
                try {
                    ret = method.invoke(obj, params);
                    break;
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    //Fail silently
                }
            }
        }
        return ret;
    }

    private static Object invokeStaticMethod(String path, String methodName, Object... params) {
        Object ret = null;
        try {
            Class c = Class.forName(path);
            for (Method method : c.getMethods()) {
                if (method.getName().equals(methodName)) {
                    try {
                        ret = method.invoke(null, params);
                        break;
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        //Fail silently
                    }
                }
            }
        } catch (ClassNotFoundException ex) {
            PhatLoots.logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return ret;
    }

    private static Object construct(String path, Object... params) {
        Object ret = null;
        try {
            Class c = Class.forName(path);
            for (Constructor ctor : c.getConstructors()) {
                try {
                    ret = ctor.newInstance(params);
                    break;
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    //Fail silently
                }
            }
        } catch (ClassNotFoundException ex) {
            PhatLoots.logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return ret;
    }
}
