package net.professoradam.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.*;
import de.janschuri.lunaticlib.common.command.HasParams;
import de.janschuri.lunaticlib.common.command.HasParentCommand;
import de.janschuri.lunaticlib.common.config.LunaticCommandMessageKey;
import net.professoradam.lunaticstorage.LunaticStorage;
import net.professoradam.lunaticstorage.commands.StorageCommand;
import net.professoradam.lunaticstorage.storage.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public class StorageGet extends StorageCommand implements HasParentCommand, HasParams {

    private static final StorageGet INSTANCE = new StorageGet();
    private static final CommandMessageKey HELP_MK = new LunaticCommandMessageKey(INSTANCE, "help")
            .defaultMessage("en", INSTANCE.getDefaultHelpMessage("Get a storage item, range item or a panel."))
            .defaultMessage("de", INSTANCE.getDefaultHelpMessage("Erhalte ein Storageitem, Rangeitem oder ein Panel."));

    @Override
    public Command getParentCommand() {
        return new Storage();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.admin.get";
    }

    @Override
    public String getName() {
        return "get";
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

        ItemStack item;

        if (type.equalsIgnoreCase("storageitem")) {
            item = createStorageItem();
        } else if (type.equalsIgnoreCase("rangeitem")) {
            if (range == -1) {
                range = LunaticStorage.getPluginConfig().getDefaultRangeItem();
            }
            item = createRangeItem(range);
        } else if (type.equalsIgnoreCase("panel")) {
            if (range == -1) {
                range = LunaticStorage.getPluginConfig().getDefaultRangePanel();
            }
            // Panel price logic
            PlayerSender player = (PlayerSender) sender;
            Player p = Bukkit.getPlayer(player.getUniqueId());
            double panelPrice = LunaticStorage.getPluginConfig().getPanelPrice();
            if (!net.professoradam.lunaticstorage.utils.Utils.hasEnoughMoney(p, panelPrice)) {
                sender.sendMessage("You do not have enough money to buy a panel.");
                return true;
            }
            boolean paid = net.professoradam.lunaticstorage.utils.Utils.withdrawMoney(p, panelPrice);
            if (!paid) {
                sender.sendMessage("Failed to withdraw money for panel.");
                return true;
            }
            item = createPanelItem(range);
        } else {
            sender.sendMessage(getMessage(WRONG_USAGE_MK));
            return true;
        }

        PlayerSender player = (PlayerSender) sender;
        Player p = Bukkit.getPlayer(player.getUniqueId());
        p.getInventory().addItem(item);
        return true;
    }

    @Override
    public Map<CommandMessageKey, String> getHelpMessages() {
        return Map.of(
                HELP_MK, getPermission()
        );
    }

    private ItemStack createStorageItem() {
        ItemStack item = new ItemStack(LunaticStorage.getPluginConfig().getStorageItem());

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.STORAGE, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createRangeItem(long range) {
        ItemStack item = new ItemStack(LunaticStorage.getPluginConfig().getRangeItem());

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Key.RANGE, PersistentDataType.LONG, range);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createPanelItem(long range) {
        ItemStack item = new ItemStack(LunaticStorage.getPluginConfig().getStoragePanelBlock());

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
