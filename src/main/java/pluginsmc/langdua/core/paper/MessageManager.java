package pluginsmc.langdua.core.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class MessageManager {
    private final Core instance;
    private FileConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageManager(Core instance) {
        this.instance = instance;
        loadConfig();
    }

    private void loadConfig() {
        File file = new File(instance.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            try {
                // Thử lấy file từ trong lõi jar ra
                instance.saveResource("messages.yml", false);
            } catch (IllegalArgumentException e) {
                // NẾU QUÊN TẠO FILE TRONG SOURCE -> KHÔNG CRASH, TỰ TẠO FILE MỚI!
                instance.getLogger().warning("Không tìm thấy messages.yml trong file jar! Đang tự động tạo file mặc định...");
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (IOException ignored) {}
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

        // Tự động chèn các câu mặc định nếu file bị trống
        boolean save = false;
        if (!config.contains("prefix")) { config.set("prefix", "<gold>[Cinematic]</gold> "); save = true; }
        if (!config.contains("error.not-exist")) { config.set("error.not-exist", "<red>Cinematic '<name>' does not exist!</red>"); save = true; }
        if (!config.contains("error.already-exist")) { config.set("error.already-exist", "<red>Cinematic '<name>' already exists!</red>"); save = true; }
        if (!config.contains("play.finished")) { config.set("play.finished", "<green>Cinematic playback finished.</green>"); save = true; }
        if (!config.contains("list.header")) { config.set("list.header", "<yellow>Available Cinematics:</yellow>"); save = true; }
        if (!config.contains("list.item")) { config.set("list.item", "<gray>- <white><name></white></gray>"); save = true; }
        if (!config.contains("record.title-rec")) { config.set("record.title-rec", "<red>RECORDING</red>"); save = true; }
        if (!config.contains("record.title-count")) { config.set("record.title-count", "<yellow><count></yellow>"); save = true; }

        if (save) {
            try { config.save(file); } catch (IOException ignored) {}
        }
    }

    private String getRawString(String path, String... placeholders) {
        String msg = config.getString(path, "<red>Missing string: " + path + "</red>");
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                msg = msg.replace("<" + placeholders[i] + ">", placeholders[i + 1]);
            }
        }
        return msg;
    }

    public void send(CommandSender sender, String path, String... placeholders) {
        String prefix = config.getString("prefix", "");
        String msg = getRawString(path, placeholders);
        if (msg.isEmpty()) return;
        sender.sendMessage(miniMessage.deserialize(prefix + msg));
    }

    public void sendActionBar(Player player, String path, String... placeholders) {
        player.sendActionBar(miniMessage.deserialize(getRawString(path, placeholders)));
    }

    public void sendTitle(Player player, String titlePath, String subPath, int in, int stay, int out, String... placeholders) {
        Component mainTitle = titlePath != null ? miniMessage.deserialize(getRawString(titlePath, placeholders)) : Component.empty();
        Component subTitle = subPath != null ? miniMessage.deserialize(getRawString(subPath, placeholders)) : Component.empty();
        Title.Times times = Title.Times.times(Duration.ofMillis(in * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(out * 50L));
        player.showTitle(Title.title(mainTitle, subTitle, times));
    }
}