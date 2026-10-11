package com.riverstone.menu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RiverstoneMenu extends JavaPlugin implements Listener {
    private static final String MENU_TITLE = ChatColor.BLACK + "✦ " + ChatColor.GREEN + ChatColor.BOLD + "RIVERSTONE MENU";
    private static final String CHANNEL = "BungeeCord";
    private NamespacedKey menuItemKey;
    private final Set<UUID> openingMenu = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        menuItemKey = new NamespacedKey(this, "riverstone_menu_compass");
        getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        getServer().getPluginManager().registerEvents(this, this);
        // Keep the menu item in hotbar slot 9 (inventory index 8).
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                enforceMenuCompass(player);
            }
        }, 1L, 20L);
        getLogger().info("Riverstone Menu enabled. Install this plugin on every Paper backend players should be able to access it from.");
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(this, () -> enforceMenuCompass(event.getPlayer()));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> enforceMenuCompass(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(this::isMenuCompass);
    }

    private void enforceMenuCompass(Player player) {
        PlayerInventory inv = player.getInventory();
        // Remove any duplicate copies of our special compass from the rest of the inventory.
        for (int slot = 0; slot < inv.getSize(); slot++) {
            if (slot != 8 && isMenuCompass(inv.getItem(slot))) {
                inv.setItem(slot, null);
            }
        }
        ItemStack current = inv.getItem(8);
        if (!isMenuCompass(current)) {
            inv.setItem(8, createCompass());
        }
    }

    private ItemStack createCompass() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "§lRiverstone Menu");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "Riverstone Network");
        lore.add(ChatColor.GRAY + "Right-click to open the server menu");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(menuItemKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    private boolean isMenuCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(menuItemKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (!isMenuCompass(item)) return;
        event.setCancelled(true);
        openMenu(event.getPlayer());
    }

    private void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);
        ItemStack filler = namedItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);
        inv.setItem(11, namedItem(Material.DIAMOND_SWORD, ChatColor.GREEN + "§lPVP SERVER", List.of(
                ChatColor.GRAY + "Join Riverstone PvP", ChatColor.DARK_GREEN + "Click to connect")));
        inv.setItem(13, namedItem(Material.GRASS_BLOCK, ChatColor.GREEN + "§lLOBBY", List.of(
                ChatColor.GRAY + "Return to the network lobby", ChatColor.DARK_GREEN + "Click to connect")));
        inv.setItem(15, namedItem(Material.OAK_DOOR, ChatColor.GREEN + "§lBACK TO GAME", List.of(
                ChatColor.GRAY + "Close this menu and keep playing")));
        inv.setItem(22, namedItem(Material.CRAFTING_TABLE, ChatColor.WHITE + "§lNORMAL MINECRAFT MENU", List.of(
                ChatColor.GRAY + "The pause screen is controlled by Minecraft", ChatColor.GRAY + "Press Esc (Java) or open Pause (Bedrock)")));
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
    }

    private ItemStack namedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (isMenuInventory(event.getView().getTitle())) {
            event.setCancelled(true);
            if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
            switch (event.getRawSlot()) {
                case 11 -> connect(player, getConfig().getString("servers.pvp", "Riverstone-pvp"));
                case 13 -> connect(player, getConfig().getString("servers.lobby", "lobby"));
                case 15 -> {
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
                }
                case 22 -> player.sendMessage(ChatColor.GREEN + "Riverstone: " + ChatColor.GRAY + "The normal pause menu belongs to your Minecraft client. Press Esc on Java, or open Pause on Bedrock.");
                default -> { }
            }
            return;
        }

        // Protect slot 9 (index 8) and the special compass from clicks, hotbar swaps, and shift-clicking.
        if (event.getClickedInventory() instanceof PlayerInventory) {
            if (event.getSlot() == 8 || isMenuCompass(event.getCurrentItem())) event.setCancelled(true);
        }
        if (event.getHotbarButton() == 8) event.setCancelled(true);
    }

    private boolean isMenuInventory(String title) {
        return MENU_TITLE.equals(title);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (isMenuInventory(event.getView().getTitle())) {
            event.setCancelled(true);
            return;
        }
        int topSize = event.getView().getTopInventory().getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot == topSize + 8) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (isMenuCompass(event.getItemDrop().getItemStack())) event.setCancelled(true);
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (isMenuCompass(event.getMainHandItem()) || isMenuCompass(event.getOffHandItem())) event.setCancelled(true);
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event) {
        if (isMenuInventory(event.getView().getTitle()) && event.getPlayer() instanceof Player player) {
            openingMenu.remove(player.getUniqueId());
        }
    }

    private void connect(Player player, String serverName) {
        if (serverName == null || serverName.isBlank()) {
            player.sendMessage(ChatColor.RED + "That server is not configured yet.");
            return;
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(this, CHANNEL, bytes.toByteArray());
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Connecting to " + ChatColor.WHITE + serverName + ChatColor.GREEN + "...");
        } catch (IOException exception) {
            player.sendMessage(ChatColor.RED + "Could not send the server-switch request. Please try again.");
            getLogger().warning("Could not send Connect plugin message for " + player.getName() + ": " + exception.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("riverstonemenu")) return false;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }
        openMenu(player);
        return true;
    }
}
