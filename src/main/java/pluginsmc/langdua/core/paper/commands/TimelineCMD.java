package pluginsmc.langdua.core.paper.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.MessageManager;
import pluginsmc.langdua.core.paper.objects.TimelineDefinition;
import pluginsmc.langdua.core.paper.objects.TimelineEntry;
import pluginsmc.langdua.core.paper.objects.TransitionEffect;

import java.util.Collection;
import java.util.TreeSet;
import java.util.function.Supplier;

public class TimelineCMD {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Core instance;
    private final MessageManager msg;

    public TimelineCMD(Core instance) {
        this.instance = instance;
        this.msg = instance.getMessageManager();
    }

    public void register() {
        registerRootHelp();
        registerHelp();
        registerList();
        registerCreate();
        registerDelete();
        registerAppend();
        registerRemove();
        registerPlay();
        registerTeleportAllow();
        registerTeleportDeny();
        registerTransitionFade();
        registerTransitionClear();
    }

    private void registerRootHelp() {
        timelineCommand()
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> sendHelp(sender))
                .register();
    }

    private void registerHelp() {
        timelineCommand()
                .withArguments(new LiteralArgument("help"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> sendHelp(sender))
                .register();
    }

    private void registerList() {
        timelineCommand()
                .withArguments(new LiteralArgument("list"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    msg.send(sender, "timeline.list.header");
                    instance.getGame().getTimelines().keySet().stream().sorted().forEach(name -> msg.send(sender, "timeline.list.item", "name", name));
                })
                .register();
    }

    private void registerCreate() {
        timelineCommand()
                .withArguments(new LiteralArgument("create"))
                .withArguments(new StringArgument("name"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    String name = (String) args.get("name");
                    if (instance.getGame().getTimelines().containsKey(name)) {
                        msg.send(sender, "timeline.already-exist", "name", name);
                        return;
                    }
                    instance.getGame().getTimelines().put(name, new TimelineDefinition(name));
                    instance.getTimelineStorageManager().save(instance.getGame().getTimelines());
                    msg.send(sender, "timeline.created", "name", name);
                })
                .register();
    }

    private void registerDelete() {
        timelineCommand()
                .withArguments(new LiteralArgument("delete"))
                .withArguments(namedTimelineArg("name"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    String name = (String) args.get("name");
                    if (instance.getGame().getTimelines().remove(name) == null) {
                        msg.send(sender, "timeline.not-exist", "name", name);
                        return;
                    }
                    instance.getTimelineStorageManager().delete(name);
                    instance.getTimelineStorageManager().save(instance.getGame().getTimelines());
                    msg.send(sender, "timeline.deleted", "name", name);
                })
                .register();
    }

    private void registerAppend() {
        timelineCommand()
                .withArguments(new LiteralArgument("append"))
                .withArguments(namedTimelineArg("timeline"))
                .withArguments(new StringArgument("entry"))
                .withArguments(namedCinematicArg("cinematic"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    TimelineDefinition timeline = requireTimeline(sender, (String) args.get("timeline"));
                    if (timeline == null) {
                        return;
                    }
                    String entryName = (String) args.get("entry");
                    String cinematicName = (String) args.get("cinematic");
                    if (timeline.getEntry(entryName) != null) {
                        msg.send(sender, "timeline.entry-exists", "entry", entryName);
                        return;
                    }
                    timeline.getEntries().add(new TimelineEntry(entryName, cinematicName));
                    instance.getTimelineStorageManager().save(instance.getGame().getTimelines());
                    msg.send(sender, "timeline.entry-added", "entry", entryName, "name", cinematicName);
                })
                .register();
    }

    private void registerRemove() {
        timelineCommand()
                .withArguments(new LiteralArgument("remove"))
                .withArguments(namedTimelineArg("timeline"))
                .withArguments(namedEntryArg("entry"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    TimelineDefinition timeline = requireTimeline(sender, (String) args.get("timeline"));
                    if (timeline == null) {
                        return;
                    }
                    String entryName = (String) args.get("entry");
                    boolean removed = timeline.getEntries().removeIf(entry -> entryName.equalsIgnoreCase(entry.getName()));
                    if (!removed) {
                        msg.send(sender, "timeline.entry-not-exist", "entry", entryName);
                        return;
                    }
                    instance.getTimelineStorageManager().save(instance.getGame().getTimelines());
                    msg.send(sender, "timeline.entry-removed", "entry", entryName);
                })
                .register();
    }

    private void registerPlay() {
        timelineCommand()
                .withArguments(new LiteralArgument("play"))
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withArguments(namedTimelineArg("timeline"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> tryPlay(
                        sender,
                        (Player) args.get("player"),
                        (String) args.get("timeline"),
                        false,
                        false
                ))
                .register();

        timelineCommand()
                .withArguments(new LiteralArgument("play"))
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withArguments(namedTimelineArg("timeline"))
                .withArguments(new LiteralArgument("ignoreworld"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> tryPlay(
                        sender,
                        (Player) args.get("player"),
                        (String) args.get("timeline"),
                        true,
                        false
                ))
                .register();

        timelineCommand()
                .withArguments(new LiteralArgument("play"))
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withArguments(namedTimelineArg("timeline"))
                .withArguments(new LiteralArgument("--force"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> tryPlay(
                        sender,
                        (Player) args.get("player"),
                        (String) args.get("timeline"),
                        false,
                        true
                ))
                .register();

        timelineCommand()
                .withArguments(new LiteralArgument("play"))
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withArguments(namedTimelineArg("timeline"))
                .withArguments(new LiteralArgument("-f"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> tryPlay(
                        sender,
                        (Player) args.get("player"),
                        (String) args.get("timeline"),
                        false,
                        true
                ))
                .register();
    }

    private void registerTeleportAllow() {
        timelineCommand()
                .withArguments(new LiteralArgument("teleport"))
                .withArguments(namedTimelineArg("timeline"))
                .withArguments(namedEntryArg("entry"))
                .withArguments(new LiteralArgument("allow"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    TimelineEntry entry = requireEntry(sender, (String) args.get("timeline"), (String) args.get("entry"));
                    if (entry == null) {
                        return;
                    }
                    entry.setWorldTeleport(true);
                    instance.getTimelineStorageManager().save(instance.getGame().getTimelines());
                    msg.send(sender, "timeline.world-teleport-enabled", "entry", entry.getName());
                })
                .register();
    }

    private void registerTeleportDeny() {
        timelineCommand()
                .withArguments(new LiteralArgument("teleport"))
                .withArguments(namedTimelineArg("timeline"))
                .withArguments(namedEntryArg("entry"))
                .withArguments(new LiteralArgument("deny"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    TimelineEntry entry = requireEntry(sender, (String) args.get("timeline"), (String) args.get("entry"));
                    if (entry == null) {
                        return;
                    }
                    entry.setWorldTeleport(false);
                    instance.getTimelineStorageManager().save(instance.getGame().getTimelines());
                    msg.send(sender, "timeline.world-teleport-disabled", "entry", entry.getName());
                })
                .register();
    }

    private void registerTransitionFade() {
        timelineCommand()
                .withArguments(new LiteralArgument("transition"))
                .withArguments(new LiteralArgument("fade"))
                .withArguments(namedTimelineArg("timeline"))
                .withArguments(namedEntryArg("entry"))
                .withArguments(new IntegerArgument("ticks", 0))
                .withArguments(new IntegerArgument("strength", 1))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    TimelineEntry entry = requireEntry(sender, (String) args.get("timeline"), (String) args.get("entry"));
                    if (entry == null) {
                        return;
                    }
                    entry.getTransition().setEffect(TransitionEffect.DARKEN_FADE);
                    entry.getTransition().setDurationTicks((int) args.get("ticks"));
                    entry.getTransition().setStrength((int) args.get("strength"));
                    instance.getTimelineStorageManager().save(instance.getGame().getTimelines());
                    msg.send(sender, "timeline.transition-updated", "entry", entry.getName());
                })
                .register();
    }

    private void registerTransitionClear() {
        timelineCommand()
                .withArguments(new LiteralArgument("transition"))
                .withArguments(new LiteralArgument("clear"))
                .withArguments(namedTimelineArg("timeline"))
                .withArguments(namedEntryArg("entry"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    TimelineEntry entry = requireEntry(sender, (String) args.get("timeline"), (String) args.get("entry"));
                    if (entry == null) {
                        return;
                    }
                    entry.getTransition().setEffect(TransitionEffect.NONE);
                    entry.getTransition().setDurationTicks(0);
                    entry.getTransition().setStrength(1);
                    instance.getTimelineStorageManager().save(instance.getGame().getTimelines());
                    msg.send(sender, "timeline.transition-cleared", "entry", entry.getName());
                })
                .register();
    }

    private void tryPlay(CommandSender sender, Player player, String timelineName, boolean bypassWorldMetadata, boolean forceWorldTeleport) {
        TimelineDefinition timeline = requireTimeline(sender, timelineName);
        if (timeline == null) {
            return;
        }

        if (!bypassWorldMetadata && !forceWorldTeleport) {
            TimelineEntry blockedEntry = instance.getGame().getTimelinePlayManager().findUnmarkedWorldTeleportEntry(player, timeline);
            if (blockedEntry != null) {
                msg.send(sender, "timeline.play.world-warning", "name", timelineName, "entry", blockedEntry.getName());
                return;
            }
        }

        instance.getGame().getTimelinePlayManager().play(sender, player, timelineName, bypassWorldMetadata);
    }

    private CommandAPICommand timelineCommand() {
        return new CommandAPICommand("timeline")
                .withAliases("sequence", "seq");
    }

    private Argument<String> namedTimelineArg(String nodeName) {
        return namedArg(nodeName, () -> instance.getGame().getTimelines().keySet());
    }

    private Argument<String> namedCinematicArg(String nodeName) {
        return namedArg(nodeName, () -> instance.getGame().getCinematics().keySet());
    }

    private Argument<String> namedEntryArg(String nodeName) {
        return new StringArgument(nodeName).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            String timelineName = info.previousArgs().getOptionalUnchecked("timeline").map(Object::toString).orElse(null);
            if (timelineName == null) {
                return new TreeSet<String>();
            }
            TimelineDefinition timeline = instance.getGame().getTimelines().get(timelineName);
            if (timeline == null) {
                return new TreeSet<String>();
            }
            TreeSet<String> names = new TreeSet<>();
            for (TimelineEntry entry : timeline.getEntries()) {
                if (entry.getName() != null && !entry.getName().isBlank()) {
                    names.add(entry.getName());
                }
            }
            return names;
        }));
    }

    private Argument<String> namedArg(String nodeName, Supplier<Collection<String>> values) {
        return new StringArgument(nodeName).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> new TreeSet<>(values.get())));
    }

    private TimelineDefinition requireTimeline(CommandSender sender, String name) {
        TimelineDefinition timeline = instance.getGame().getTimelines().get(name);
        if (timeline == null) {
            msg.send(sender, "timeline.not-exist", "name", name);
        }
        return timeline;
    }

    private TimelineEntry requireEntry(CommandSender sender, String timelineName, String entryName) {
        TimelineDefinition timeline = requireTimeline(sender, timelineName);
        if (timeline == null) {
            return null;
        }
        TimelineEntry entry = timeline.getEntry(entryName);
        if (entry == null) {
            msg.send(sender, "timeline.entry-not-exist", "entry", entryName);
        }
        return entry;
    }

    private void sendHelp(CommandSender sender) {
        msg.send(sender, "timeline.help.header");
        sendHelpLine(sender, "timeline.help.general");
        sendHelpLine(sender, "timeline.help.list");
        sendHelpLine(sender, "timeline.help.create");
        sendHelpLine(sender, "timeline.help.delete");
        sendHelpLine(sender, "timeline.help.append");
        sendHelpLine(sender, "timeline.help.remove");
        sendHelpLine(sender, "timeline.help.play");
        sendHelpLine(sender, "timeline.help.teleport-allow");
        sendHelpLine(sender, "timeline.help.teleport-deny");
        sendHelpLine(sender, "timeline.help.transition-fade");
        sendHelpLine(sender, "timeline.help.transition-clear");
    }

    private void sendHelpLine(CommandSender sender, String path) {
        sender.sendMessage(MINI_MESSAGE.deserialize(msg.getPrefix() + msg.getRawMessage(path)));
    }
}
