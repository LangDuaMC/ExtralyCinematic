package pluginsmc.langdua.core.paper;

import co.aikar.commands.PaperCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pluginsmc.langdua.core.paper.commands.CinematicCMD;
import pluginsmc.langdua.core.paper.hooks.MythicMobsHook;
import pluginsmc.langdua.core.paper.hooks.WorldGuardHook;
import pluginsmc.langdua.core.paper.listeners.GlobalListener;
import pluginsmc.langdua.core.paper.listeners.GuiListener;
import pluginsmc.langdua.core.paper.listeners.PlayerJoinListener;

import java.util.HashMap;

public class Core extends JavaPlugin {

    private static Core instance;
    private PaperCommandManager commandManager;
    private Game game;
    private StorageManager storageManager;
    private MessageManager messageManager;
    private boolean papiEnabled = false;
    private boolean wgEnabled = false;
    private boolean mmEnabled = false;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // 1. Initialize Message Manager (MiniMessage)
        messageManager = new MessageManager(this);

        // 2. Initialize Commands
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new CinematicCMD(this));

        // 3. Initialize Data & Storage
        game = new Game(this);
        storageManager = new StorageManager(this);

        // Fix: Cast Map to HashMap safely
        game.setCinematics((HashMap<String, pluginsmc.langdua.core.paper.objects.Cinematic>) storageManager.load());

        // 4. Register Listeners
        Bukkit.getPluginManager().registerEvents(new GlobalListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getLogger().info("ExtralyCinematic has been enabled successfully!");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiEnabled = true;
            getLogger().info("Successfully hooked into PlaceholderAPI!");
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            wgEnabled = true;
            getLogger().info("Successfully hooked into WorldGuard!");
        }
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            mmEnabled = true;
            getLogger().info("Successfully hooked into MythicMobs!");
        }
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            mmEnabled = true;
            Bukkit.getPluginManager().registerEvents(new MythicMobsHook(this), this);
            getLogger().info("Successfully hooked into MythicMobs!");
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            wgEnabled = true;
            Bukkit.getPluginManager().registerEvents(new WorldGuardHook(this), this);
            getLogger().info("Successfully hooked into WorldGuard!");
        }
    }

    @Override
    public void onDisable() {
        if (storageManager != null && game != null) {
            storageManager.save(game.getCinematics());
        }
    }

    public static Core getInstance() { return instance; }
    public Game getGame() { return game; }
    public StorageManager getStorageManager() { return storageManager; }
    public PaperCommandManager getCommandManager() { return commandManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public int getInterpolationSteps() { return getConfig().getInt("interpolation-steps", 10); }
    public boolean isPapiEnabled() { return papiEnabled; }
    public boolean isWgEnabled() { return wgEnabled; }
    public boolean isMmEnabled() { return mmEnabled; }
}