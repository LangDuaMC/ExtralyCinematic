package pluginsmc.langdua.core.paper;

import co.aikar.commands.PaperCommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import lombok.Getter;
import lombok.Setter;
import pluginsmc.langdua.core.paper.commands.CinematicCMD;
import pluginsmc.langdua.core.paper.guis.CinematicGUI;
import pluginsmc.langdua.core.paper.listeners.GlobalListener;
import pluginsmc.langdua.core.paper.listeners.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {

    private static @Getter Core instance;
    private static @Getter
    @Setter TaskChainFactory taskChainFactory;
    private @Getter Game game;
    private @Getter PaperCommandManager commandManager;
    private @Getter StorageManager storageManager;
    private @Getter int interpolationSteps;
    private @Getter CinematicGUI cinematicGUI;

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        interpolationSteps = Math.max(1, getConfig().getInt("playback.interpolation-steps", 1));

        storageManager = new StorageManager(this);

        game = new Game(this);
        game.setCinematics(storageManager.load());
        game.runTaskTimerAsynchronously(this, 0L, 20L);

        taskChainFactory = BukkitTaskChainFactory.create(this);
        Bukkit.getPluginManager().registerEvents(new GlobalListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this); // Register GuiListener
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new CinematicCMD(this));

        cinematicGUI = new CinematicGUI(this); // Initialize CinematicGUI
    }

    @Override
    public void onDisable() {
        if (storageManager != null && game != null) {
            storageManager.save(game.getCinematics());
        }
    }
}
