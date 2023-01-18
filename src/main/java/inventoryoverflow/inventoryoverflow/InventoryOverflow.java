package inventoryoverflow.inventoryoverflow;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class InventoryOverflow extends JavaPlugin implements Listener {
    private static final double PICKUP_RADIUS = 1;
    private List<Inventory> pages;
    private int currentPage;

    @Override
    public void onEnable() {
        pages = new ArrayList<>();
        pages.add(createNewInventory());
        currentPage = 0;

        // Register events
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        // Register commands
        getCommand("collect").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        Inventory gui = pages.get(currentPage);

        player.openInventory(gui);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory gui = event.getInventory();

        if (gui.getTitle().startsWith("Collected Loot")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();

            // Check if clicking on navigational items
            if (clickedItem == null || clickedItem.getItemMeta() == null || clickedItem.getItemMeta().getDisplayName() == null) {
                return;
            }

            // Next button
            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
                currentPage = Math.min(pages.size() - 1, currentPage + 1);
                player.openInventory(pages.get(currentPage));
            }
            // Previous button
            else if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {
                currentPage = Math.max(0, currentPage - 1);
                player.openInventory(pages.get(currentPage));
            }
            // Collect button
            else if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {

            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        // Get nearby entities
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, PICKUP_RADIUS, +3, PICKUP_RADIUS);
        for (Entity entity : entities) {
            if (entity instanceof Item) {
                ItemStack item = ((Item) entity).getItemStack();
                if (player.getInventory().firstEmpty() == -1) {

                    // Add to custom inv if inv is full
                    addToInventory(item);
                    player.playSound(loc, Sound.ITEM_PICKUP, 1f, 1f);
                    entity.remove();
                }
            }
        }
    }

    private void addToInventory(ItemStack item) {
        Inventory last = pages.get(pages.size() - 1);

        if (last.firstEmpty() == 45) {
            pages.add(createNewInventory());
            currentPage++;
        }

        pages.get(currentPage).addItem(item);
    }

    private Inventory createNewInventory() {
        Inventory gui = Bukkit.createInventory(null, 54, "Collected Loot - Page " + (pages.size() + 1));

        // Previous button
        ItemStack prevButton = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevButton.getItemMeta();
        prevMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
        prevButton.setItemMeta(prevMeta);
        gui.setItem(48, prevButton);

        // Collect button
        ItemStack collectButton = new ItemStack(Material.DOUBLE_PLANT);
        ItemMeta collectMeta = collectButton.getItemMeta();
        collectMeta.setDisplayName(ChatColor.GOLD + "Collect");
        collectButton.setItemMeta(collectMeta);
        gui.setItem(49, collectButton);

        // Next button
        ItemStack nextButton = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextButton.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        nextButton.setItemMeta(nextMeta);
        gui.setItem(50, nextButton);

        return gui;
    }
}
