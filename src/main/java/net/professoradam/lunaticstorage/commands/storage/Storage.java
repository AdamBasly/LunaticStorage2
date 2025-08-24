package net.professoradam.lunaticstorage.commands.storage;

import de.janschuri.lunaticlib.Command;
import de.janschuri.lunaticlib.CommandMessageKey;
import de.janschuri.lunaticlib.MessageKey;
import de.janschuri.lunaticlib.common.command.HasHelpCommand;
import de.janschuri.lunaticlib.common.command.HasSubcommands;
import de.janschuri.lunaticlib.common.command.LunaticHelpCommand;
import de.janschuri.lunaticlib.common.config.LunaticCommandMessageKey;
import de.janschuri.lunaticlib.common.config.LunaticMessageKey;
import net.professoradam.lunaticstorage.commands.StorageCommand;
import de.janschuri.lunaticlib.Sender;

import java.util.List;
import java.util.Map;

public class Storage extends StorageCommand implements HasSubcommands, HasHelpCommand {

    private static final Storage INSTANCE = new Storage();

    private static final MessageKey PAGE_MK = new LunaticMessageKey("page")
            .defaultMessage("en", "Page %page%/%pages%")
            .defaultMessage("de", "Seite %page%/%pages%");
    private static final CommandMessageKey HELP_MK = new LunaticCommandMessageKey(INSTANCE, "help")
            .defaultMessage("en", INSTANCE.getDefaultHelpMessage("Show the storage help page."))
            .defaultMessage("de", INSTANCE.getDefaultHelpMessage("Zeige die Storage Hilfe Seite."));
    private static final CommandMessageKey HELP_HEADER_MK = new LunaticCommandMessageKey(INSTANCE, "help_header")
            .defaultMessage("en", "Storage-Help")
            .defaultMessage("de", "Storage-Hilfe");

    @Override
    public List<Command> getSubcommands() {
        return List.of(
            new StorageRandom(),
            new StorageReload(),
            new StorageGet(),
            new StorageCreate(),
            new StorageContainer(),
            new StorageCheck(),
            getHelpCommand()
        );
    }

    @Override
    public LunaticHelpCommand getHelpCommand() {
        return new LunaticHelpCommand(this);
    }

    @Override
    public MessageKey pageParamName() {
        return PAGE_MK;
    }

    @Override
    public MessageKey getHelpHeader() {
        return HELP_HEADER_MK;
    }

    @Override
    public String getPermission() {
        return "lunaticstorage.storage";
    }

    @Override
    public String getName() {
        return "storage";
    }

    @Override
    public boolean execute(Sender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessage(NO_PERMISSION_MK));
            return true;
        }
        if (args.length == 0) {
            getHelpCommand().execute(sender, args);
            return true;
        }

        final String subcommand = args[0];

        for (Command sc : getSubcommands()) {
            if (checkIsSubcommand(sc, subcommand)) {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                return sc.execute(sender, newArgs);
            }
        }
        getHelpCommand().execute(sender, new String[0]);


        return true;
    }

    @Override
    public Map<CommandMessageKey, String> getHelpMessages() {
        return Map.of(
                HELP_MK, getPermission()
        );
    }

    @Override
    public boolean isPrimaryCommand() {
        return true;
    }
}
