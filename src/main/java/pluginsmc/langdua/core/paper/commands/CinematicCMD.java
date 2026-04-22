package pluginsmc.langdua.core.paper.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.MessageManager;
import pluginsmc.langdua.core.paper.guis.CinematicGUI;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.util.Collection;
import java.util.TreeSet;
import java.util.function.Supplier;

public class CinematicCMD {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Core instance;
    private final MessageManager msg;

    public CinematicCMD(Core instance) {
        this.instance = instance;
        this.msg = instance.getMessageManager();
    }

    public void register() {
        registerRootHelp();
        registerHelp();
        registerEdit();
        registerList();
        registerReload();
        registerPlay();
        registerStop();
        registerPath();
        registerDelete();
        registerRec();
        registerAddFrame();
        registerAddCommand();
        registerTitle();
        registerSubtitle();
        registerDuration();
        registerFocusSet();
        registerFocusClear();
        registerShake();
        registerZoom();
        registerBgm();
    }

    private void registerRootHelp() {
        cinematicCommand()
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> sendHelp(sender))
                .register();
    }

    private void registerHelp() {
        cinematicCommand()
                .withArguments(new LiteralArgument("help"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> sendHelp(sender))
                .register();
    }

    private void registerEdit() {
        cinematicCommand()
                .withArguments(new LiteralArgument("edit"))
                .withPermission("cinematic.cmd")
                .executesPlayer((dev.jorel.commandapi.executors.PlayerCommandExecutor) (player, args) ->
                        player.openInventory(new CinematicGUI(instance).getInventory()))
                .register();
    }

    private void registerList() {
        cinematicCommand()
                .withArguments(new LiteralArgument("list"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    msg.send(sender, "list.header");
                    instance.getGame().getCinematics().keySet().stream().sorted().forEach(name -> msg.send(sender, "list.item", "name", name));
                })
                .register();
    }

    private void registerReload() {
        cinematicCommand()
                .withArguments(new LiteralArgument("reload"))
                .withPermission("cinematic.admin")
                .executes((sender, args) -> {
                    instance.reloadPlugin();
                    msg.send(sender, "admin.reload");
                })
                .register();
    }

    private void registerPlay() {
        cinematicCommand()
                .withArguments(new LiteralArgument("play"))
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withArguments(namedCinematicArg("name"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> tryPlay(
                        sender,
                        (Player) args.get("player"),
                        (String) args.get("name"),
                        null
                ))
                .register();

        cinematicCommand()
                .withArguments(new LiteralArgument("play"))
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withArguments(namedCinematicArg("name"))
                .withArguments(new GreedyStringArgument("options"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> tryPlay(
                        sender,
                        (Player) args.get("player"),
                        (String) args.get("name"),
                        (String) args.get("options")
                ))
                .register();
    }

    private void registerStop() {
        cinematicCommand()
                .withArguments(new LiteralArgument("stop"))
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) ->
                        instance.getGame().getPlayManager().forceStop(sender, (Player) args.get("player")))
                .register();
    }

    private void registerPath() {
        cinematicCommand()
                .withArguments(new LiteralArgument("path"))
                .withArguments(namedCinematicArg("name"))
                .withPermission("cinematic.cmd")
                .executesPlayer((player, args) -> {
                    Cinematic cinematic = requireCinematic(player, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }

                    msg.send(player, "edit.path-visual");
                    new BukkitRunnable() {
                        int ticks = 0;

                        @Override
                        public void run() {
                            if (!player.isOnline() || ticks > 200) {
                                cancel();
                                return;
                            }
                            for (Frame frame : cinematic.getFrames()) {
                                World world = Bukkit.getWorld(frame.getWorld());
                                if (world != null) {
                                    world.spawnParticle(Particle.FLAME, frame.getX(), frame.getY(), frame.getZ(), 1, 0, 0, 0, 0);
                                }
                            }
                            ticks += 20;
                        }
                    }.runTaskTimer(instance, 0L, 20L);
                })
                .register();
    }

    private void registerDelete() {
        cinematicCommand()
                .withArguments(new LiteralArgument("delete"))
                .withArguments(namedCinematicArg("name"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    String name = (String) args.get("name");
                    if (instance.getGame().getCinematics().remove(name) != null) {
                        instance.getStorageManager().delete(name);
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        msg.send(sender, "edit.delete", "name", name);
                    } else {
                        msg.send(sender, "error.not-exist", "name", name);
                    }
                })
                .register();
    }

    private void registerRec() {
        registerRecLiteral("rec");
        registerRecLiteral("record");
    }

    private void registerAddFrame() {
        cinematicCommand()
                .withArguments(new LiteralArgument("addframe"))
                .withArguments(namedCinematicArg("name"))
                .withPermission("cinematic.cmd")
                .executesPlayer((player, args) -> {
                    String name = (String) args.get("name");
                    Cinematic cinematic = instance.getGame().getCinematics().computeIfAbsent(name, Cinematic::new);
                    Location location = player.getLocation();
                    cinematic.getFrames().add(new Frame(
                            location.getWorld().getName(),
                            location.getX(),
                            location.getY(),
                            location.getZ(),
                            location.getYaw(),
                            location.getPitch()
                    ));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(player, "edit.addframe", "name", name, "index", String.valueOf(cinematic.getFrames().size() - 1));
                })
                .register();
    }

    private void registerAddCommand() {
        cinematicCommand()
                .withArguments(new LiteralArgument("addcmd"))
                .withArguments(namedCinematicArg("name"))
                .withArguments(new IntegerArgument("frame", 0))
                .withArguments(new GreedyStringArgument("command"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    int frameIndex = (int) args.get("frame");
                    if (!isValidFrame(sender, cinematic, frameIndex)) {
                        return;
                    }
                    String command = (String) args.get("command");
                    cinematic.getFrames().get(frameIndex).getCommands().add(command);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.cmd-added", "cmd", command);
                })
                .register();
    }

    private void registerTitle() {
        cinematicCommand()
                .withArguments(new LiteralArgument("title"))
                .withArguments(namedCinematicArg("name"))
                .withArguments(new IntegerArgument("frame", 0))
                .withArguments(new GreedyStringArgument("text"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    int frameIndex = (int) args.get("frame");
                    if (!isValidFrame(sender, cinematic, frameIndex)) {
                        return;
                    }
                    cinematic.getFrames().get(frameIndex).setTitle((String) args.get("text"));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.title-updated");
                })
                .register();
    }

    private void registerSubtitle() {
        cinematicCommand()
                .withArguments(new LiteralArgument("subtitle"))
                .withArguments(namedCinematicArg("name"))
                .withArguments(new IntegerArgument("frame", 0))
                .withArguments(new GreedyStringArgument("text"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    int frameIndex = (int) args.get("frame");
                    if (!isValidFrame(sender, cinematic, frameIndex)) {
                        return;
                    }
                    cinematic.getFrames().get(frameIndex).setSubtitle((String) args.get("text"));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.subtitle-updated");
                })
                .register();
    }

    private void registerDuration() {
        cinematicCommand()
                .withArguments(new LiteralArgument("duration"))
                .withArguments(namedCinematicArg("name"))
                .withArguments(new IntegerArgument("seconds", 0))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    cinematic.setDuration((int) args.get("seconds"));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                })
                .register();
    }

    private void registerFocusSet() {
        cinematicCommand()
                .withArguments(new LiteralArgument("focus"))
                .withArguments(namedCinematicArg("name"))
                .withArguments(new LiteralArgument("set"))
                .withPermission("cinematic.cmd")
                .executesPlayer((player, args) -> {
                    Cinematic cinematic = requireCinematic(player, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    Location location = player.getLocation();
                    cinematic.setFocus(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(player, "edit.generic-updated");
                })
                .register();
    }

    private void registerFocusClear() {
        cinematicCommand()
                .withArguments(new LiteralArgument("focus"))
                .withArguments(namedCinematicArg("name"))
                .withArguments(new LiteralArgument("clear"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    cinematic.clearFocus();
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                })
                .register();
    }

    private void registerShake() {
        cinematicCommand()
                .withArguments(new LiteralArgument("shake"))
                .withArguments(namedCinematicArg("name"))
                .withArguments(new DoubleArgument("intensity", 0.0D))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    cinematic.setShakeIntensity((double) args.get("intensity"));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                })
                .register();
    }

    private void registerZoom() {
        cinematicCommand()
                .withArguments(new LiteralArgument("zoom"))
                .withArguments(namedCinematicArg("name"))
                .withArguments(new IntegerArgument("start"))
                .withArguments(new IntegerArgument("end"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    cinematic.setStartZoom((int) args.get("start"));
                    cinematic.setEndZoom((int) args.get("end"));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                })
                .register();
    }

    private void registerBgm() {
        cinematicCommand()
                .withArguments(new LiteralArgument("bgm"))
                .withArguments(namedCinematicArg("name"))
                .withArguments(new StringArgument("sound"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    String sound = (String) args.get("sound");
                    cinematic.setBgmSound("clear".equalsIgnoreCase(sound) ? null : sound);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                })
                .register();
    }

    private void registerRecLiteral(String literal) {
        cinematicCommand()
                .withArguments(new LiteralArgument(literal))
                .withArguments(new StringArgument("name"))
                .withPermission("cinematic.cmd")
                .executesPlayer((dev.jorel.commandapi.executors.PlayerCommandExecutor) (player, args) ->
                        instance.getGame().getRecordManager().prepareTriggeredRecord(player, (String) args.get("name"), null))
                .register();

        cinematicCommand()
                .withArguments(new LiteralArgument(literal))
                .withArguments(new StringArgument("name"))
                .withArguments(new IntegerArgument("duration", 1))
                .withPermission("cinematic.cmd")
                .executesPlayer((dev.jorel.commandapi.executors.PlayerCommandExecutor) (player, args) ->
                        instance.getGame().getRecordManager().prepareTriggeredRecord(player, (String) args.get("name"), (Integer) args.get("duration")))
                .register();
    }

<<<<<<< HEAD
    private void tryPlay(CommandSender sender, Player player, String cinematicName, String rawOptions) {
=======
    private void tryPlay(CommandSender sender, Player player, String cinematicName, boolean bypassWorldMetadata, boolean forceWorldTeleport) {
>>>>>>> 1f33dcc (Refine recording controls and world-safe playback)
        Cinematic cinematic = requireCinematic(sender, cinematicName);
        if (cinematic == null) {
            return;
        }

<<<<<<< HEAD
        PlaybackOptionParser.ParsedPlaybackOptions parsedOptions;
        try {
            parsedOptions = PlaybackOptionParser.parse(rawOptions, instance.getInterpolationSteps());
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(MINI_MESSAGE.deserialize(msg.getPrefix() + "<red>" + ex.getMessage() + "</red>"));
            return;
        }

        if (!parsedOptions.forceWorldTeleport()
                && instance.getGame().getPlayManager().requiresWorldTeleport(player, cinematic, parsedOptions.playbackOptions().bypassWorldMetadata())) {
=======
        if (!forceWorldTeleport && instance.getGame().getPlayManager().requiresWorldTeleport(player, cinematic, bypassWorldMetadata)) {
>>>>>>> 1f33dcc (Refine recording controls and world-safe playback)
            msg.send(sender, "play.world-warning", "name", cinematicName);
            return;
        }

<<<<<<< HEAD
        instance.getGame().getPlayManager().play(sender, player, cinematic, cinematicName, parsedOptions.playbackOptions());
=======
        instance.getGame().getPlayManager().play(sender, player, cinematic, cinematicName, bypassWorldMetadata);
>>>>>>> 1f33dcc (Refine recording controls and world-safe playback)
    }

    private CommandAPICommand cinematicCommand() {
        return new CommandAPICommand("cinematic")
                .withAliases("cine", "cutscene", "cin");
    }

    private Argument<String> namedCinematicArg(String nodeName) {
        return namedArg(nodeName, () -> instance.getGame().getCinematics().keySet());
    }

    private Argument<String> namedArg(String nodeName, Supplier<Collection<String>> values) {
        return new StringArgument(nodeName).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> new TreeSet<>(values.get())));
    }

    private Cinematic requireCinematic(CommandSender sender, String name) {
        Cinematic cinematic = instance.getGame().getCinematics().get(name);
        if (cinematic == null) {
            msg.send(sender, "error.not-exist", "name", name);
        }
        return cinematic;
    }

    private boolean isValidFrame(CommandSender sender, Cinematic cinematic, int frameIndex) {
        if (frameIndex < 0 || frameIndex >= cinematic.getFrames().size()) {
            msg.send(sender, "error.invalid-number");
            return false;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        msg.send(sender, "help.header");
        sendHelpLine(sender, "help.general");
        sendHelpLine(sender, "help.edit");
        sendHelpLine(sender, "help.list");
        sendHelpLine(sender, "help.play");
        sendHelpLine(sender, "help.stop");
        sendHelpLine(sender, "help.path");
        sendHelpLine(sender, "help.delete");
        sendHelpLine(sender, "help.rec");
        sendHelpLine(sender, "help.addframe");
        sendHelpLine(sender, "help.addcmd");
        sendHelpLine(sender, "help.title");
        sendHelpLine(sender, "help.subtitle");
        sendHelpLine(sender, "help.duration");
        sendHelpLine(sender, "help.focus-set");
        sendHelpLine(sender, "help.focus-clear");
        sendHelpLine(sender, "help.shake");
        sendHelpLine(sender, "help.zoom");
        sendHelpLine(sender, "help.bgm");

        if (sender.hasPermission("cinematic.admin")) {
            sendHelpLine(sender, "help.reload");
        }
    }

    private void sendHelpLine(CommandSender sender, String path) {
        sender.sendMessage(MINI_MESSAGE.deserialize(msg.getPrefix() + msg.getRawMessage(path)));
    }
}
