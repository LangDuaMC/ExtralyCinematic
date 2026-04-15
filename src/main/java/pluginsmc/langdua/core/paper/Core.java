package pluginsmc.langdua.core.paper;

import co.aikar.commands.PaperCommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pluginsmc.langdua.core.paper.commands.CinematicCMD;
import pluginsmc.langdua.core.paper.listeners.GlobalListener;
import pluginsmc.langdua.core.paper.listeners.GuiListener;
import pluginsmc.langdua.core.paper.listeners.PlayerJoinListener;

public class Core extends JavaPlugin {

    private static Core instance;
    private static TaskChainFactory taskChainFactory;

    private PaperCommandManager commandManager;
    private Game game;
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        taskChainFactory = BukkitTaskChainFactory.create(this);
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new CinematicCMD(this));
        game = new Game(this);
        storageManager = new StorageManager(this);
        game.setCinematics(storageManager.load());
        Bukkit.getPluginManager().registerEvents(new GlobalListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getLogger().info("ExtralyCinematic has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (storageManager != null && game != null) {
            storageManager.save(game.getCinematics());
        }
        getLogger().info("ExtralyCinematic has been disabled!");
    }

    public static Core getInstance() {
        return instance;
    }

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    public Game getGame() {
        return game;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public int getInterpolationSteps() {
        return getConfig().getInt("interpolation-steps", 10);
    }
}