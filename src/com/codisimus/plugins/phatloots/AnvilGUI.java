package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.phatloots.util.NMSUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Adapted to a reflection version for better
 * multi-version support.
 *
 * @author chasechocolate
 */
public class AnvilGUI {

    private Player player;
    private AnvilClickEventHandler handler;
    private static Class<?> blockPosition;
    private static Class<?> packetPlayOutOpenWindow;
    private static Class<?> containerAnvil;
    private static Class<?> chatMessage;
    private static Class<?> entityHuman;
    private HashMap<AnvilSlot, ItemStack> items = new HashMap<AnvilSlot, ItemStack>();
    private Inventory inv;
    private Listener listener;

    private void loadClasses() {
        blockPosition = NMSUtil.getNMSClass("BlockPosition");
        packetPlayOutOpenWindow = NMSUtil.getNMSClass("PacketPlayOutOpenWindow");
        containerAnvil = NMSUtil.getNMSClass("ContainerAnvil");
        entityHuman = NMSUtil.getNMSClass("EntityHuman");
        chatMessage = NMSUtil.getNMSClass("ChatMessage");
    }

    public AnvilGUI(final Player player, final AnvilClickEventHandler handler) {
        loadClasses();
        this.player = player;
        this.handler = handler;

        this.listener = new Listener() {

            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (!(event.getWhoClicked() instanceof Player))
                    return;

                if (!event.getInventory().equals(inv))
                    return;

                event.setCancelled(true);

                int slot = event.getRawSlot();
                ItemStack item = event.getInventory().getItem(2);
                String name = "";

                if (item != null) {
                    if (item.getItemMeta() != null) {
                        ItemMeta meta = item.getItemMeta();

                        if (meta.hasDisplayName()) {
                            name = meta.getDisplayName();
                        }
                    }
                }

                AnvilClickEvent clickEvent = new AnvilClickEvent(AnvilSlot.bySlot(slot), name);

                handler.onAnvilClick(clickEvent);

                if (clickEvent.getWillClose()) {
                    event.getWhoClicked().closeInventory();
                }

                if (clickEvent.getWillDestroy()) {
                    destroy();
                }
            }


            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                if (event.getPlayer() instanceof Player) {
                    Inventory inv = event.getInventory();
                    player.setLevel(player.getLevel() - 1);
                    if (inv.equals(AnvilGUI.this.inv)) {
                        inv.clear();
                        destroy();
                    }
                }
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                if (event.getPlayer().equals(getPlayer())) {
                    player.setLevel(player.getLevel() - 1);
                    destroy();
                }
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, PhatLoots.plugin); //Replace with instance of main class
    }

    public Player getPlayer() {
        return player;
    }

    public AnvilGUI setSlot(AnvilSlot slot, ItemStack item) {
        items.put(slot, item);
        return this;
    }

    public void open()  {
        player.setLevel(player.getLevel() + 1);

        try {
            Object p = NMSUtil.getHandle(player);


            Object container = containerAnvil.getConstructor(NMSUtil.getNMSClass("PlayerInventory"), NMSUtil.getNMSClass("World"), blockPosition, entityHuman).newInstance(NMSUtil.getPlayerField(player, "inventory"), NMSUtil.getPlayerField(player, "world"), blockPosition.getConstructor(int.class, int.class, int.class).newInstance(0, 0, 0), p);
            NMSUtil.getField(NMSUtil.getNMSClass("Container"), "checkReachable").set(container, false);

            //Set the items to the items from the inventory given
            Object bukkitView = NMSUtil.invokeMethod("getBukkitView", container);
            inv = (Inventory) NMSUtil.invokeMethod("getTopInventory", bukkitView);

            for (AnvilSlot slot : items.keySet()) {
                inv.setItem(slot.getSlot(), items.get(slot));
            }

            //Counter stuff that the game uses to keep track of inventories
            int c = (int) NMSUtil.invokeMethod("nextContainerCounter", p);

            //Send the packet
            Constructor<?> chatMessageConstructor = chatMessage.getConstructor(String.class, Object[].class);
            Object playerConnection = NMSUtil.getPlayerField(player, "playerConnection");
            Object packet = packetPlayOutOpenWindow.getConstructor(int.class, String.class, NMSUtil.getNMSClass("IChatBaseComponent"), int.class).newInstance(c, "minecraft:anvil", chatMessageConstructor.newInstance("Repairing", new Object[]{}), 0);

            Method sendPacket = NMSUtil.getMethod("sendPacket", playerConnection.getClass(), packetPlayOutOpenWindow);
            sendPacket.invoke(playerConnection, packet);

            //Set their active container to the container
            Field activeContainerField = NMSUtil.getField(entityHuman, "activeContainer");
            if (activeContainerField != null) {
                activeContainerField.set(p, container);

                //Set their active container window id to that counter stuff
                NMSUtil.getField(NMSUtil.getNMSClass("Container"), "windowId").set(activeContainerField.get(p), c);

                //Add the slot listener
                NMSUtil.getMethod("addSlotListener", activeContainerField.get(p).getClass(), p.getClass()).invoke(activeContainerField.get(p), p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        player = null;
        handler = null;
        items = null;

        HandlerList.unregisterAll(listener);

        listener = null;
    }

    public enum AnvilSlot {
        INPUT_LEFT(0),
        INPUT_RIGHT(1),
        OUTPUT(2);

        private int slot;

        AnvilSlot(int slot) {
            this.slot = slot;
        }

        public static AnvilSlot bySlot(int slot) {
            for (AnvilSlot anvilSlot : values()) {
                if (anvilSlot.getSlot() == slot) {
                    return anvilSlot;
                }
            }

            return null;
        }

        public int getSlot() {
            return slot;
        }
    }

    public interface AnvilClickEventHandler {
        void onAnvilClick(AnvilClickEvent event);
    }

    public class AnvilClickEvent {
        private AnvilSlot slot;

        private String name;

        private boolean close = true;
        private boolean destroy = true;

        public AnvilClickEvent(AnvilSlot slot, String name) {
            this.slot = slot;
            this.name = name;
        }

        public AnvilSlot getSlot() {
            return slot;
        }

        public String getName() {
            return name;
        }

        public boolean getWillClose() {
            return close;
        }

        public void setWillClose(boolean close) {
            this.close = close;
        }

        public boolean getWillDestroy() {
            return destroy;
        }

        public void setWillDestroy(boolean destroy) {
            this.destroy = destroy;
        }
    }
}