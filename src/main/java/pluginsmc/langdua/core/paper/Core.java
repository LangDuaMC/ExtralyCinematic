package pluginsmc.langdua.core.paper;

import co.aikar.commands.PaperCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pluginsmc.langdua.core.paper.commands.CinematicCMD;
import pluginsmc.langdua.core.paper.hooks.MythicMobsHook;
import pluginsmc.langdua.core.paper.hooks.WorldGuardHook;
import pluginsmc.langdua.core.paper.listeners.GlobalListener;
import pluginsmc.langdua.core.paper.listeners.GuiListener;
import pluginsmc.langdua.core.paper.managers.PlayManager;
import pluginsmc.langdua.core.paper.managers.RecordManager;
import pluginsmc.langdua.core.paper.objects.Game;

public class Core extends JavaPlugin {

    private Game game;
    private StorageManager storageManager;
    private MessageManager messageManager;
    private ChatInputManager chatInputManager;
    private RecordManager recordManager;
    private PlayManager playManager;

    // Trạng thái các Hook
    private boolean papiEnabled = false;
    private boolean mmEnabled = false;
    private boolean wgEnabled = false;

    // Config
    private int interpolationSteps = 10;

    @Override
    public void onEnable() {
        // 1. Khởi tạo Config & Data
        saveDefaultConfig();
        saveResource("messages.yml", false); // Tự động đẩy messages.yml ra ngoài nếu chưa có
        interpolationSteps = getConfig().getInt("interpolation-steps", 10);

        // 2. Khởi tạo đối tượng & Manager
        this.game = new Game();
        this.storageManager = new StorageManager(this);
        this.messageManager = new MessageManager(this);
        this.chatInputManager = new ChatInputManager(this);
        this.recordManager = new RecordManager(this);
        this.playManager = new PlayManager(this);

        // Load dữ liệu Cinematic từ thư mục
        this.storageManager.load();

        // 3. Đăng ký Commands & Auto-Complete (ACF)
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.getCommandCompletions().registerCompletion("cinematics", c -> game.getCinematics().keySet());
        commandManager.registerCommand(new CinematicCMD(this));

        // 4. Đăng ký Listeners
        Bukkit.getPluginManager().registerEvents(new GlobalListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(this.chatInputManager, this);

        // 5. Khởi tạo Hooks
        setupHooks();

        getLogger().info("ExtralyCinematic has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Lưu lại toàn bộ dữ liệu trước khi tắt Server
        if (storageManager != null && game != null) {
            storageManager.save(game.getCinematics());
        }

        // Thoát hết Camera cho an toàn
        if (game != null) {
            for (java.util.UUID viewer : game.getViewers()) {
                org.bukkit.entity.Player player = Bukkit.getPlayer(viewer);
                if (player != null && player.isOnline()) {
                    playManager.forceStop(Bukkit.getConsoleSender(), player);
                }
            }
        }

        getLogger().info("ExtralyCinematic disabled safely.");
    }

    private void setupHooks() {
        // PlaceholderAPI Hook
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiEnabled = true;
            getLogger().info("Successfully hooked into PlaceholderAPI!");
        }

        // MythicMobs Hook
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            mmEnabled = true;
            Bukkit.getPluginManager().registerEvents(new MythicMobsHook(this), this);
            getLogger().info("Successfully hooked into MythicMobs!");
        }

        // WorldGuard Hook
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            wgEnabled = true;
            Bukkit.getPluginManager().registerEvents(new WorldGuardHook(this), this);
            getLogger().info("Successfully hooked into WorldGuard!");
        }
    }

    // ==========================================
    // GETTERS
    // ==========================================

    public Game getGame() { return game; }
    public StorageManager getStorageManager() { return storageManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public ChatInputManager getChatInputManager() { return chatInputManager; }
    public RecordManager getRecordManager() { return recordManager; }
    public PlayManager getPlayManager() { return playManager; }

    public boolean isPapiEnabled() { return papiEnabled; }
    public boolean isMmEnabled() { return mmEnabled; }
    public boolean isWgEnabled() { return wgEnabled; }

    public int getInterpolationSteps() { return interpolationSteps; }
}