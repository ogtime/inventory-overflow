package inventoryoverflow.inventoryoverflow;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class InventoryOverflow extends JavaPlugin implements Listener {
    private List<Inventory> pages;
    private int currentPage;

    @Override
    public void onEnable() {
        pages = new ArrayList<>();
        pages.add(Bukkit.createInventory(null, 54, "Collected Loot - Page 1"));
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

        // Next button
        ItemStack nextButton = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextButton.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        nextButton.setItemMeta(nextMeta);
        gui.setItem(52, nextButton);

        // Previous button
        ItemStack prevButton = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevButton.getItemMeta();
        prevMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
        prevButton.setItemMeta(prevMeta);
        gui.setItem(53, prevButton);

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
            if (clickedItem == null) {
                return;
            }
            if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
                currentPage = Math.min(pages.size() - 1, currentPage + 1);
                player.openInventory(pages.get(currentPage));
            }else if (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {
                currentPage = Math.max(0, currentPage - 1);
                player.openInventory(pages.get(currentPage));
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        int emptySlots = player.getInventory().firstEmpty();
        int totalSlots = player.getInventory().getSize();
        int remainingSlots = totalSlots - emptySlots;

        Bukkit.getLogger().info(emptySlots + "");
        if (emptySlots != -1) {
            int amountToAdd = Math.min(item.getAmount(), remainingSlots);
            item.setAmount(amountToAdd);
            player.getInventory().addItem(item);
            event.setCancelled(true);
            player.sendMessage("Added " + amountToAdd + " " + item.getType() + " to your inventory.");

            if(amountToAdd != item.getAmount()){
                item.setAmount(item.getAmount() - amountToAdd);
                pages.get(currentPage).addItem(item);
            }
        } else {
            pages.get(currentPage).addItem(item);
            event.setCancelled(true);
            player.sendMessage("Added " + item.getAmount() + " " + item.getType() + " to your collected loot.");
        }
    }
}
