package pluginsmc.langdua.core.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;

public class MessageManager {
    private static final String PREFIX = "<gold>[ExtralyCinematic]</gold> ";

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageManager(Core instance) {
    }

    public void reload() {
    }

    private String applyPlaceholders(String message, String... placeholders) {
        String result = message;
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                result = result.replace("<" + placeholders[i] + ">", placeholders[i + 1]);
            }
        }
        return result;
    }

    private String messageFor(String path) {
        return switch (path) {
            case "input.prompt-cmd" -> "<yellow>Enter a command without the leading slash:</yellow>";
            case "input.prompt-title" -> "<yellow>Enter the title text. MiniMessage is supported:</yellow>";
            case "input.prompt-subtitle" -> "<yellow>Enter the subtitle text. MiniMessage is supported:</yellow>";
            case "input.prompt-sound" -> "<yellow>Enter a sound name, for example entity.zombie.ambient:</yellow>";
            case "input.prompt-duration" -> "<yellow>Enter a duration in seconds:</yellow>";
            case "input.prompt-shake" -> "<yellow>Enter a camera shake intensity, for example 0.5:</yellow>";
            case "input.prompt-zoom-start" -> "<yellow>Enter the starting zoom level:</yellow>";
            case "input.prompt-zoom-end" -> "<yellow>Enter the ending zoom level:</yellow>";
            case "input.prompt-bgm" -> "<yellow>Enter a background music sound name:</yellow>";
            case "input.cancel-hint" -> "<gray>(Type <red>cancel</red> to abort)</gray>";
            case "input.cancelled" -> "<red>Action cancelled.</red>";

            case "edit.cmd-added" -> "<green>Added command: <white>/<cmd></white></green>";
            case "edit.title-updated" -> "<green>Title updated.</green>";
            case "edit.subtitle-updated" -> "<green>Subtitle updated.</green>";
            case "edit.sound-added" -> "<green>Added sound: <white><sound></white></green>";
            case "edit.generic-updated" -> "<green>Updated cinematic settings.</green>";
            case "edit.addframe" -> "<green>Added frame <index> to <name>.</green>";
            case "edit.path-visual" -> "<green>Showing the cinematic path with particles.</green>";
            case "edit.delete" -> "<red>Deleted cinematic: <name></red>";

            case "error.not-exist" -> "<red>Cinematic '<name>' does not exist.</red>";
            case "error.already-exist" -> "<red>Cinematic '<name>' already exists.</red>";
            case "error.invalid-number" -> "<red>Invalid number.</red>";
            case "error.already-recording" -> "<red>You are already recording a cinematic.</red>";
            case "error.not-recording" -> "<red>You are not recording a cinematic.</red>";

            case "play.finished" -> "<green>Cinematic playback finished.</green>";
            case "play.force-stop" -> "<red>Force-stopped cinematic for <player>.</red>";

            case "record.title-rec" -> "<red>RECORDING</red>";
            case "record.title-count" -> "<yellow><count></yellow>";
            case "record.actionbar-timer" -> "<yellow>Recording: <current>/<total>s</yellow>";
            case "record.actionbar-free" -> "<yellow>Recorded frames: <count></yellow>";
            case "record.finish" -> "<green>Recording finished. Saved <count> frames.</green>";
            case "record.start-free" -> "<green>Started free recording for '<name>'. Use /cinematic record stop to finish.</green>";
            case "record.stop-free" -> "<green>Stopped recording '<name>'. Saved <count> frames.</green>";

            case "list.header" -> "<yellow>Available cinematics:</yellow>";
            case "list.item" -> "<gray>- <white><name></white></gray>";

            case "help.header" -> "<yellow>Cinematic commands:</yellow>";
            case "help.general" -> "<gray>- <white>/cinematic</white> <dark_gray>|</dark_gray> <white>/cinematic help</white> <gray>Show this help</gray>";
            case "help.edit" -> "<gray>- <white>/cinematic edit</white> <gray>Open the editor GUI</gray>";
            case "help.list" -> "<gray>- <white>/cinematic list</white> <gray>List all cinematics</gray>";
            case "help.reload" -> "<gray>- <white>/cinematic reload</white> <gray>Reload config and data</gray>";
            case "help.play" -> "<gray>- <white>/cinematic play <player> <name></white> <gray>Play a cinematic for a player</gray>";
            case "help.stop" -> "<gray>- <white>/cinematic stop <player></white> <gray>Stop a player's cinematic</gray>";
            case "help.path" -> "<gray>- <white>/cinematic path <name></white> <gray>Show the path with particles</gray>";
            case "help.delete" -> "<gray>- <white>/cinematic delete <name></white> <gray>Delete a cinematic</gray>";
            case "help.rec" -> "<gray>- <white>/cinematic rec <name> <seconds></white> <gray>Countdown-record into a new cinematic</gray>";
            case "help.record-start" -> "<gray>- <white>/cinematic record start <name></white> <gray>Start free recording</gray>";
            case "help.record-stop" -> "<gray>- <white>/cinematic record stop</white> <gray>Stop free recording</gray>";
            case "help.addframe" -> "<gray>- <white>/cinematic addframe <name></white> <gray>Add a frame to the cinematic</gray>";
            case "help.addcmd" -> "<gray>- <white>/cinematic addcmd <name> <frame> <command...></white> <gray>Add a frame command</gray>";
            case "help.title" -> "<gray>- <white>/cinematic title <name> <frame> <text...></white> <gray>Set the frame title</gray>";
            case "help.subtitle" -> "<gray>- <white>/cinematic subtitle <name> <frame> <text...></white> <gray>Set the frame subtitle</gray>";
            case "help.duration" -> "<gray>- <white>/cinematic duration <name> <seconds></white> <gray>Set cinematic duration</gray>";
            case "help.focus-set" -> "<gray>- <white>/cinematic focus <name> set</white> <gray>Set focus to your current location</gray>";
            case "help.focus-clear" -> "<gray>- <white>/cinematic focus <name> clear</white> <gray>Clear focus</gray>";
            case "help.shake" -> "<gray>- <white>/cinematic shake <name> <intensity></white> <gray>Set shake intensity</gray>";
            case "help.zoom" -> "<gray>- <white>/cinematic zoom <name> <start> <end></white> <gray>Set start and end zoom</gray>";
            case "help.bgm" -> "<gray>- <white>/cinematic bgm <name> <sound></white> <gray>Set BGM or use 'clear'</gray>";

            case "timeline.list.header" -> "<yellow>Available timelines:</yellow>";
            case "timeline.list.item" -> "<gray>- <white><name></white></gray>";
            case "timeline.not-exist" -> "<red>Timeline '<name>' does not exist.</red>";
            case "timeline.already-exist" -> "<red>Timeline '<name>' already exists.</red>";
            case "timeline.created" -> "<green>Created timeline '<name>'.</green>";
            case "timeline.deleted" -> "<red>Deleted timeline '<name>'.</red>";
            case "timeline.entry-added" -> "<green>Added entry '<entry>' using cinematic '<name>'.</green>";
            case "timeline.entry-removed" -> "<green>Removed entry '<entry>'.</green>";
            case "timeline.entry-exists" -> "<red>Timeline entry '<entry>' already exists.</red>";
            case "timeline.entry-not-exist" -> "<red>Timeline entry '<entry>' does not exist.</red>";
            case "timeline.entry-invalid" -> "<red>Timeline entry '<entry>' points to cinematic '<name>', which is not playable.</red>";
            case "timeline.empty" -> "<red>Timeline has no playable cinematic sequence.</red>";
            case "timeline.transition-updated" -> "<green>Updated transition metadata for entry '<entry>'.</green>";
            case "timeline.transition-cleared" -> "<green>Cleared transition metadata for entry '<entry>'.</green>";
            case "timeline.help.header" -> "<yellow>Timeline commands:</yellow>";
            case "timeline.help.general" -> "<gray>- <white>/timeline</white> <dark_gray>|</dark_gray> <white>/timeline help</white> <gray>Show this help</gray>";
            case "timeline.help.list" -> "<gray>- <white>/timeline list</white> <gray>List all timelines</gray>";
            case "timeline.help.create" -> "<gray>- <white>/timeline create <name></white> <gray>Create a timeline</gray>";
            case "timeline.help.delete" -> "<gray>- <white>/timeline delete <name></white> <gray>Delete a timeline</gray>";
            case "timeline.help.append" -> "<gray>- <white>/timeline append <timeline> <entry> <cinematic></white> <gray>Add a named cinematic entry</gray>";
            case "timeline.help.remove" -> "<gray>- <white>/timeline remove <timeline> <entry></white> <gray>Remove a named entry</gray>";
            case "timeline.help.play" -> "<gray>- <white>/timeline play <player> <timeline></white> <gray>Play a timeline for a player</gray>";
            case "timeline.help.transition-fade" -> "<gray>- <white>/timeline transition fade <timeline> <entry> <ticks> <strength></white> <gray>Store fade metadata for an entry</gray>";
            case "timeline.help.transition-clear" -> "<gray>- <white>/timeline transition clear <timeline> <entry></white> <gray>Clear entry transition metadata</gray>";

            case "admin.reload" -> "<green>Reloaded plugin configuration and cinematic data.</green>";

            default -> "<red>Unknown message key: " + path + "</red>";
        };
    }

    public String getRawMessage(String path, String... placeholders) {
        return applyPlaceholders(messageFor(path), placeholders);
    }

    public String getPrefix() {
        return PREFIX;
    }

    public void send(CommandSender sender, String path, String... placeholders) {
        sender.sendMessage(miniMessage.deserialize(PREFIX + getRawMessage(path, placeholders)));
    }

    public void sendActionBar(Player player, String path, String... placeholders) {
        player.sendActionBar(miniMessage.deserialize(getRawMessage(path, placeholders)));
    }

    public void sendTitle(Player player, String titlePath, String subPath, int in, int stay, int out, String... placeholders) {
        Component mainTitle = titlePath != null ? miniMessage.deserialize(getRawMessage(titlePath, placeholders)) : Component.empty();
        Component subTitle = subPath != null ? miniMessage.deserialize(getRawMessage(subPath, placeholders)) : Component.empty();
        Title.Times times = Title.Times.times(Duration.ofMillis(in * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(out * 50L));
        player.showTitle(Title.title(mainTitle, subTitle, times));
    }
}
