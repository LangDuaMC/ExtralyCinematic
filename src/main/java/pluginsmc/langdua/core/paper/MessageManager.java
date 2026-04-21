package pluginsmc.langdua.core.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
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
        File file = new File(instance.getDataFolder(), "message.yml");
        if (!file.exists()) {
            instance.saveResource("message.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        loadConfig();
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
        if (msg.isEmpty() || msg.contains("Missing string")) return;
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