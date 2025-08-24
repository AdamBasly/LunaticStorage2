package net.professoradam.lunaticstorage.listener;

import com.jeff_media.customblockdata.CustomBlockData;
import de.janschuri.lunaticlib.common.config.LunaticMessageKey;
import de.janschuri.lunaticlib.platform.bukkit.util.EventUtils;
import net.professoradam.lunaticstorage.LunaticStorage;
import net.professoradam.lunaticstorage.storage.Key;
import net.professoradam.lunaticstorage.storage.StorageContainer;
import net.professoradam.lunaticstorage.utils.Logger;
import net.professoradam.lunaticstorage.utils.Utils;
import de.janschuri.lunaticlib.MessageKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChestClickListener implements Listener {

    private static final MessageKey containerAlreadyMarked = new LunaticMessageKey("container_already_marked");
    private static final MessageKey containerMarked = new LunaticMessageKey("container_marked");

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (EventUtils.isFakeEvent(event)) {
            return;
        }


        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) {
            return;
        }

        if (clickedBlock.getState() instanceof Container && Utils.isStorageContainer(clickedBlock)) {
            try {
                LunaticStorage.getGlowingBlocks().unsetGlowing(clickedBlock, player);
            } catch (Exception e) {
                Logger.errorLog("Error while unsetting glowing block");
            }
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR || !itemInHand.getItemMeta().getPersistentDataContainer().has(Key.STORAGE, PersistentDataType.INTEGER)) {
            return;
        }


        if (clickedBlock.getState() instanceof Container && event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);

            Container container = (Container) clickedBlock.getState();

            UUID worldUUID = container.getWorld().getUID();

            ItemMeta storageMeta = itemInHand.getItemMeta();

            PersistentDataContainer dataContainer = storageMeta.getPersistentDataContainer();


            // Check if the storage item already has the world in its list and add it if not
            if (!dataContainer.has(Key.STORAGE_ITEM_WORLDS, PersistentDataType.STRING)) {
                dataContainer.set(Key.STORAGE_ITEM_WORLDS, PersistentDataType.STRING, worldUUID.toString());
                itemInHand.setItemMeta(storageMeta);
            } else {
                String worldUUIDString = dataContainer.get(Key.STORAGE_ITEM_WORLDS, PersistentDataType.STRING);
                List<UUID> worlds = new ArrayList<>();
                if (worldUUIDString != null) {
                    worlds = Utils.getUUIDListFromString(worldUUIDString);
                }

                if (!worlds.contains(worldUUID)) {
                    worlds.add(worldUUID);
                    dataContainer.set(Key.STORAGE_ITEM_WORLDS, PersistentDataType.STRING, Utils.getUUIDListAsString(worlds));
                    itemInHand.setItemMeta(storageMeta);
                }
            }

            NamespacedKey worldKey = new NamespacedKey(LunaticStorage.getInstance(), worldUUID.toString());

            List<String> chests = new ArrayList<>();
            List<Block> containerBlocks = new ArrayList<>();

            if (container instanceof Chest) {
                Chest chest = (Chest) container;
                if (Utils.isDoubleChest(chest)) {
                    DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
                    Chest leftChest = (Chest) doubleChest.getLeftSide();
                    Chest rightChest = (Chest) doubleChest.getRightSide();
                    String leftChestID = Utils.serializeCoords(leftChest.getLocation());
                    String rightChestID = Utils.serializeCoords(rightChest.getLocation());
                    chests.add(leftChestID);
                    chests.add(rightChestID);

                    containerBlocks.add(leftChest.getBlock());
                    containerBlocks.add(rightChest.getBlock());
                } else {
                    String chestID = Utils.serializeCoords(container.getLocation());
                    chests.add(chestID);
                    containerBlocks.add(container.getBlock());
                }
            } else {
                String chestID = Utils.serializeCoords(container.getLocation());
                chests.add(chestID);
                containerBlocks.add(container.getBlock());
            }

            if (addChestsToPersistentDataContainer(dataContainer, worldKey, chests)) {

                for (Block block : containerBlocks) {
                    if (!Utils.isStorageContainer(block)) {

                        StorageContainer storageContainer = StorageContainer.getStorageContainer(block);

                        if (!storageContainer.getInventory().isEmpty()) {
                            storageContainer.addInvToWhitelist();
                            storageContainer.setWhitelistEnabled(true);
                        }

                    }

                    PersistentDataContainer blockDataContainer = new CustomBlockData(block, LunaticStorage.getInstance());
                    blockDataContainer.set(Key.STORAGE_CONTAINER, PersistentDataType.INTEGER, 1);
                }


                itemInHand.setItemMeta(storageMeta);
                player.sendMessage(LunaticStorage.getLanguageConfig().getMessage(containerMarked));
            } else {
                player.sendMessage(LunaticStorage.getLanguageConfig().getMessage(containerAlreadyMarked));
            }
        }
    }

    private boolean addChestsToPersistentDataContainer(PersistentDataContainer dataContainer, NamespacedKey worldKey, List<String> chests) {
        if (!dataContainer.has(worldKey, PersistentDataType.BYTE_ARRAY)) {
            byte[] worldChests = Utils.getArrayFromList(chests);
            dataContainer.set(worldKey, PersistentDataType.BYTE_ARRAY, worldChests);
            return true;
        } else {
            List<String> oldChests = Utils.getListFromArray(dataContainer.get(worldKey, PersistentDataType.BYTE_ARRAY));

            boolean allAlreadyMarked = true;

            for (String chest : chests) {
                if (!oldChests.contains(chest)) {
                    oldChests.add(chest);
                    allAlreadyMarked = false;
                }
            }

            dataContainer.set(worldKey, PersistentDataType.BYTE_ARRAY, Utils.getArrayFromList(oldChests));

            return !allAlreadyMarked;
        }
    }
}