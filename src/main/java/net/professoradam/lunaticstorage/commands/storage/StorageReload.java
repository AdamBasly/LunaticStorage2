package net.professoradam.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.Command;
import de.janschuri.lunaticlib.common.command.HasParentCommand;
import de.janschuri.lunaticlib.common.config.LunaticCommandMessageKey;
import net.professoradam.lunaticstorage.LunaticStorage;
import net.professoradam.lunaticstorage.commands.StorageCommand;
import de.janschuri.lunaticlib.CommandMessageKey;
import de.janschuri.lunaticlib.Sender;

import java.util.Map;

public class StorageReload extends StorageCommand implements HasParentCommand {

    private static final StorageReload INSTANCE = new StorageReload();
    private static final CommandMessageKey HELP_MK = new LunaticCommandMessageKey(INSTANCE, "help")
            .defaultMessage("en", INSTANCE.getDefaultHelpMessage("Reload the config."))
            .defaultMessage("de", INSTANCE.getDefaultHelpMessage("Lade die Config neu."));

    private final CommandMessageKey reloadedMK = new LunaticCommandMessageKey(this, "reloaded");

    @Override
    public Command getParentCommand() {
        return new Storage();
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.admin.reload";
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessage(NO_PERMISSION_MK));
            return true;
        }


            LunaticStorage.loadConfig();
            sender.sendMessage(getMessage(reloadedMK));

        return true;
    }

    @Override
    public Map<CommandMessageKey, String> getHelpMessages() {
        return Map.of(
                HELP_MK, getPermission()
        );
    }
}
