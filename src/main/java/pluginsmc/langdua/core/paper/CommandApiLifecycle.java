package pluginsmc.langdua.core.paper;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;

final class CommandApiLifecycle {

    private CommandApiLifecycle() {
    }

    static void load(LifecycleEventOwner owner) {
        if (CommandAPI.isLoaded()) {
            return;
        }

        CommandAPI.onLoad(new CommandAPIPaperConfig(owner).silentLogs(true));
    }
}
