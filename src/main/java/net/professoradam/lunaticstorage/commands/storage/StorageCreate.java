package net.professoradam.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.*;
import de.janschuri.lunaticlib.common.command.HasParams;
import de.janschuri.lunaticlib.common.command.HasParentCommand;
import de.janschuri.lunaticlib.common.config.LunaticCommandMessageKey;
import net.professoradam.lunaticstorage.LunaticStorage;
import net.professoradam.lunaticstorage.commands.StorageCommand;
import net.professoradam.lunaticstorage.storage.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public class StorageCreate extends StorageCommand implements HasParentCommand, HasParams {

    private static final StorageCreate INSTANCE = new StorageCreate();

    private static final CommandMessageKey NO_ITEM_IN_HAND_MK = new LunaticCommandMessageKey(INSTANCE, "no_item_in_hand")
            .defaultMessage("en", "You need to hold an item in your main hand.")
            .defaultMessage("de", "Du musst einen Gegenstand in deiner Haupt-Hand halten.");
    private static final CommandMessageKey NO_BLOCK_IN_HAND_MK = new LunaticCommandMessageKey(INSTANCE, "no_block_in_hand")
            .defaultMessage("en", "You need to hold a block in your main hand.")
            .defaultMessage("de", "Du musst einen Block in deiner Haupt-Hand halten.");
    private static final CommandMessageKey HELP_MK = new LunaticCommandMessageKey(INSTANCE, "help")
            .defaultMessage("en", INSTANCE.getDefaultHelpMessage("Create a storage item, range item or a panel."))
            .defaultMessage("de", INSTANCE.getDefaultHelpMessage("Erstelle ein Storageitem, Rangeitem oder ein Panel."));

    @Override
    public Command getParentCommand() {
        return new Storage();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.admin.create";
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!(sender instanceof PlayerSender)) {
            sender.sendMessage(getMessage(NO_CONSOLE_COMMAND_MK));
            return true;
        }

        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessage(NO_PERMISSION_MK));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(getMessage(WRONG_USAGE_MK));
            return true;
        }

        String type = args[0];
        long range = -1;

        if (args.length > 1) {
            try {
                range = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(getMessage(NO_NUMBER_MK));
                return true;
            }

            if (range < 0) {
                sender.sendMessage(getMessage(WRONG_USAGE_MK));
                return true;
            }
        }

        PlayerSender player = (PlayerSender) sender;
        Player p = Bukkit.getPlayer(player.getUniqueId());

        if (!player.hasItemInMainHand()) {
            sender.sendMessage(getMessage(NO_ITEM_IN_HAND_MK));
            return true;
        }

        assert p != null;
        ItemStack item = p.getInventory().getItemInMainHand();

        if (type.equalsIgnoreCase("storageitem")) {
            createStorageItem(item);
        } else if (type.equalsIgnoreCase("rangeitem")) {
            if (range == -1) {
                range = LunaticStorage.getPluginConfig().getDefaultRangeItem();
            }
            createRangeItem(item, range);
        } else if (type.equalsIgnoreCase("panel")) {
            if (range == -1) {
                range = LunaticStorage.getPluginConfig().getDefaultRangePanel();
            }

            if (!item.getType().isBlock()) {
                sender.sendMessage(getMessage(NO_BLOCK_IN_HAND_MK));
                return true;
            }

            createPanelItem(item, range);
        } else {
            sender.sendMessage(getMessage(WRONG_USAGE_MK));
            return true;
        }


            return true;
    }

    @Override
    public Map<CommandMessageKey, String> getHelpMessages() {
        return Map.of(
                HELP_MK, getPermission()
        );
    }

    private ItemStack createStorageItem(ItemStack item) {

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.STORAGE, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createRangeItem(ItemStack item, long range) {

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.RANGE, PersistentDataType.LONG, range);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createPanelItem(ItemStack item, long range) {

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.PANEL_BLOCK, PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(Key.PANEL_RANGE, PersistentDataType.LONG, range);
        item.setItemMeta(meta);

        return item;
    }


    @Override
    public List<Map<String, String>> getParams() {
        Map<String, String> panelMap = Map.of(
                "panel", getPermission(),
                "rangeitem", getPermission(),
                "storageitem", getPermission()
        );

        return List.of(panelMap);
    }

    @Override
    public List<MessageKey> getParamsNames() {
        return List.of(
                TYPE_MK
        );
    }
}
