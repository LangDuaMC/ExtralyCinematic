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
    private File file;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageManager(Core instance) {
        this.instance = instance;
        createConfig();
    }

    private void createConfig() {
        file = new File(instance.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            config = YamlConfiguration.loadConfiguration(file);

            config.set("prefix", "<gold>[ExtralyCinematic]</gold> ");
            config.set("error.no-permission", "<red>You don't have permission!</red>");
            config.set("error.not-exist", "<red>Cinematic '<name>' doesn't exist.</red>");
            config.set("error.already-exist", "<red>Cinematic '<name>' already exists.</red>");
            config.set("error.not-recording", "<red>You are not recording.</red>");
            config.set("error.already-recording", "<red>You are already recording.</red>");

            config.set("record.start-free", "<green>Started recording '<name>'. Move to capture frames.</green>");
            config.set("record.stop-free", "<green>Stopped recording '<name>'. Keyframes: <yellow><count></yellow></green>");
            config.set("record.finish", "<green>Recording finished! Saved <yellow><count></yellow> keyframes.</green>");
            config.set("record.actionbar-timer", "<yellow>Recording: <current>/<total>s</yellow>");
            config.set("record.actionbar-free", "<yellow>Recording... Keyframes: <count></yellow>");
            config.set("record.title-rec", "<dark_red>REC.</dark_red>");
            config.set("record.title-count", "<dark_red><count></dark_red>");

            config.set("edit.path-visual", "<aqua>Visualizing path for 10 seconds...</aqua>");
            config.set("edit.focus-set", "<green>Set focus target for '<name>'.</green>");
            config.set("edit.focus-clear", "<green>Cleared focus target for '<name>'.</green>");
            config.set("edit.shake-set", "<green>Set shake intensity for '<name>' to <val>.</green>");
            config.set("edit.zoom-set", "<green>Set Dolly Zoom for '<name>': Start=<start> End=<end>.</green>");
            config.set("edit.bgm-set", "<green>Set BGM for '<name>' to: <val></green>");
            config.set("edit.bgm-clear", "<green>Cleared BGM for '<name>'.</green>");
            config.set("edit.title-set", "<green>Set title for frame <frame>.</green>");
            config.set("edit.subtitle-set", "<green>Set subtitle for frame <frame>.</green>");
            config.set("edit.cmd-add", "<green>Command added to frame <frame>.</green>");
            config.set("edit.delete", "<green>Deleted cinematic: '<name>'.</green>");

            // New Waypoint Messages
            config.set("edit.addframe", "<green>Added Keyframe #<index> to '<name>'.</green>");
            config.set("edit.duration", "<green>Set total playback duration for '<name>' to <val> seconds.</green>");

            config.set("play.finished", "<green>Cinematic finished.</green>");
            config.set("play.force-stop", "<green>Force stopped cinematic for <player>.</green>");
            config.set("list.header", "<gold>--- ExtralyCinematic List ---</gold>");
            config.set("list.item", "<yellow>- <name></yellow>");

            try { config.save(file); } catch (IOException ignored) {}
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private String getRawString(String path, String... placeholders) {
        String msg = config.getString(path, "<red>Missing: " + path + "</red>");
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