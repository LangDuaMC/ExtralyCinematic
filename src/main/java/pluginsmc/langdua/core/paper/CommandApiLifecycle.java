package pluginsmc.langdua.core.paper;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPILogger;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;

final class CommandApiLifecycle {
    private static final CommandAPILogger FALLBACK_LOGGER = CommandAPILogger.bindToMethods(
            message -> System.out.println("[ExtralyCinematic/CommandAPI] " + message),
            message -> System.out.println("[ExtralyCinematic/CommandAPI] WARN " + message),
            message -> System.err.println("[ExtralyCinematic/CommandAPI] ERROR " + message),
            (message, throwable) -> {
                System.err.println("[ExtralyCinematic/CommandAPI] ERROR " + message);
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }
    );

    private CommandApiLifecycle() {
    }

    static void load(LifecycleEventOwner owner) {
        if (CommandAPI.isLoaded()) {
            return;
        }

        CommandAPI.setLogger(FALLBACK_LOGGER);
        CommandAPI.onLoad(new CommandAPIPaperConfig(owner).silentLogs(true));
    }
}
