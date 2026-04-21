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

        // 1. Khởi tạo Data & Storage
        game = new Game(this);
        storageManager = new StorageManager(this);
        // Nạp data an toàn
        game.getCinematics().putAll(storageManager.load());

        // 2. Khởi tạo Message Manager
        messageManager = new MessageManager(this);

        // 3. Khởi tạo Commands & Auto-complete (Phím Tab)
        commandManager = new PaperCommandManager(this);
        commandManager.getCommandCompletions().registerCompletion("cinematics", c -> game.getCinematics().keySet());
        commandManager.registerCommand(new CinematicCMD(this));

        // 4. Đăng ký Listeners
        Bukkit.getPluginManager().registerEvents(new GlobalListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // 5. Đăng ký Hooks
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiEnabled = true;
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            wgEnabled = true;
            Bukkit.getPluginManager().registerEvents(new WorldGuardHook(this), this);
        }
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            mmEnabled = true;
            Bukkit.getPluginManager().registerEvents(new MythicMobsHook(this), this);
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
    public MessageManager getMessageManager() { return messageManager; }
    public int getInterpolationSteps() { return getConfig().getInt("interpolation-steps", 10); }
    public boolean isPapiEnabled() { return papiEnabled; }
}