package net.professoradam.lunaticstorage.listener;

import net.professoradam.lunaticstorage.storage.Storage;
import net.professoradam.lunaticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class InventoryMoveItemListener implements Listener {

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Inventory destination = event.getDestination();
        Inventory source = event.getSource();

        Block block = null;

        if (destination.getHolder() instanceof Container) {
            block = ((Container) destination.getHolder()).getBlock();
        }

        if (destination.getHolder() instanceof DoubleChest) {
            block = ((DoubleChest) destination.getHolder()).getLocation().getBlock();
        }

        if (block == null) {
            return;
        }

        if (Utils.isPanel(block)) {
            ItemStack item = event.getItem().clone();

            if (item.getAmount() > 1) {
                return;
            }


            Storage storage = Storage.getStorage(block);
            ItemStack returnItem = storage.insertItemsIntoStorage(item, null);

            if (returnItem.getAmount() > 0) {
                event.setCancelled(true);
            } else {
                event.setItem(new ItemStack(Material.AIR, 0));
            }

            return;
        }

        ItemStack itemStack = event.getItem().clone();
        int amount = itemStack.getAmount();
        List<Map.Entry<ItemStack, Integer>> destinationChanges = List.of(Map.entry(itemStack, amount));
        List<Map.Entry<ItemStack, Integer>> sourceChanges = List.of(Map.entry(itemStack, -amount));

        InventoryChangeEvent destinationEvent = new InventoryChangeEvent(event, destination, destinationChanges);
        InventoryChangeEvent sourceEvent = new InventoryChangeEvent(event, source, sourceChanges);
        Bukkit.getPluginManager().callEvent(destinationEvent);
        Bukkit.getPluginManager().callEvent(sourceEvent);
    }

}
