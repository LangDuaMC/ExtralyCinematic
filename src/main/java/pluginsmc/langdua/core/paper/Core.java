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
import pluginsmc.langdua.core.paper.objects.Cinematic;

import java.util.Map;

public class Core extends JavaPlugin {

    private static Core instance;
    private PaperCommandManager commandManager;
    private Game game;
    private StorageManager storageManager;
    private MessageManager messageManager;
    private ChatInputManager chatInputManager;

    private boolean papiEnabled = false;
    private boolean wgEnabled = false;
    private boolean mmEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // 1. Khởi tạo
        this.messageManager = new MessageManager(this);
        this.game = new Game(this);
        this.storageManager = new StorageManager(this);
        this.chatInputManager = new ChatInputManager(this);

        // 2. Nạp Data bọc lỗi
        Map<String, Cinematic> loaded = storageManager.load();
        if (loaded != null) {
            game.getCinematics().putAll(loaded);
        }

        // 3. Đăng ký Lệnh & Tab-complete
        commandManager = new PaperCommandManager(this);
        commandManager.getCommandCompletions().registerCompletion("cinematics", c -> game.getCinematics().keySet());
        commandManager.registerCommand(new CinematicCMD(this));

        // 4. Đăng ký Sự kiện
        Bukkit.getPluginManager().registerEvents(new GlobalListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(this.chatInputManager, this);

        // 5. Setup Hooks
        setupHooks();

        getLogger().info("ExtralyCinematic enabled successfully!");
    }

    private void setupHooks() {
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

    public void reloadPlugin() {
        // 1. Reload config.yml
        reloadConfig();

        // 2. Reload message.yml
        if (messageManager != null) {
            messageManager.reload();
        }

        // 3. Reload cinematics (từ thư mục /cinematics)
        if (game != null && storageManager != null) {
            game.getCinematics().clear();
            Map<String, Cinematic> loaded = storageManager.load();
            if (loaded != null) {
                game.getCinematics().putAll(loaded);
            }
        }
    }

    @Override
    public void onDisable() {
        // 1. Lưu dữ liệu
        if (storageManager != null && game != null) {
            storageManager.save(game.getCinematics());
        }

        // 2. Dọn rác Ghost Entity (Camera)
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntitiesByClass(org.bukkit.entity.ArmorStand.class)) {
                if (entity.getScoreboardTags().contains("extraly_cam")) {
                    entity.remove();
                }
            }
        }

        // 3. Nhả người chơi đang kẹt xem phim
        if (game != null) {
            for (java.util.UUID uuid : game.getViewers()) {
                org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.setSpectatorTarget(null);
                    p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                }
            }
        }
    }

    public static Core getInstance() { return instance; }
    public Game getGame() { return game; }
    public StorageManager getStorageManager() { return storageManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public ChatInputManager getChatInputManager() { return chatInputManager; }
    public int getInterpolationSteps() { return getConfig().getInt("interpolation-steps", 10); }
    public boolean isPapiEnabled() { return papiEnabled; }
}