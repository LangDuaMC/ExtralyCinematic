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
        registerRecordStart();
        registerRecordStop();
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
        new CommandAPICommand("cinematic")
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> sendHelp(sender))
                .register();
    }

    private void registerHelp() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("help"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> sendHelp(sender))
                .register();
    }

    private void registerEdit() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("edit"))
                .withPermission("cinematic.cmd")
                .executesPlayer((dev.jorel.commandapi.executors.PlayerCommandExecutor) (player, args) ->
                        player.openInventory(new CinematicGUI(instance).getCinematicListGUI(player)))
                .register();
    }

    private void registerList() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("list"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    msg.send(sender, "list.header");
                    instance.getGame().getCinematics().keySet().stream().sorted().forEach(name -> msg.send(sender, "list.item", "name", name));
                })
                .register();
    }

    private void registerReload() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("reload"))
                .withPermission("cinematic.admin")
                .executes((sender, args) -> {
                    instance.reloadPlugin();
                    msg.send(sender, "admin.reload");
                })
                .register();
    }

    private void registerPlay() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("play"))
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withArguments(namedCinematicArg("name"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) -> instance.getGame().getPlayManager().play(
                        sender,
                        (Player) args.get("player"),
                        (String) args.get("name")
                ))
                .register();
    }

    private void registerStop() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("stop"))
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withPermission("cinematic.cmd")
                .executes((dev.jorel.commandapi.executors.CommandExecutor) (sender, args) ->
                        instance.getGame().getPlayManager().forceStop(sender, (Player) args.get("player")))
                .register();
    }

    private void registerPath() {
        new CommandAPICommand("cinematic")
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
        new CommandAPICommand("cinematic")
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
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("rec"))
                .withArguments(new StringArgument("name"))
                .withArguments(new IntegerArgument("seconds", 1))
                .withPermission("cinematic.cmd")
                .executesPlayer((player, args) -> {
                    String name = (String) args.get("name");
                    int seconds = (int) args.get("seconds");
                    if (instance.getGame().getCinematics().containsKey(name)) {
                        msg.send(player, "error.already-exist", "name", name);
                        return;
                    }

                    Cinematic cinematic = new Cinematic(name);
                    instance.getGame().getCinematics().put(name, cinematic);
                    instance.getGame().getRecordManager().startCountdownRecord(player, cinematic, seconds);
                })
                .register();
    }

    private void registerRecordStart() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("record"))
                .withArguments(new LiteralArgument("start"))
                .withArguments(new StringArgument("name"))
                .withPermission("cinematic.cmd")
                .executesPlayer((dev.jorel.commandapi.executors.PlayerCommandExecutor) (player, args) ->
                        instance.getGame().getRecordManager().startFreeRecord(player, (String) args.get("name")))
                .register();
    }

    private void registerRecordStop() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("record"))
                .withArguments(new LiteralArgument("stop"))
                .withPermission("cinematic.cmd")
                .executesPlayer((dev.jorel.commandapi.executors.PlayerCommandExecutor) (player, args) ->
                        instance.getGame().getRecordManager().stopFreeRecord(player))
                .register();
    }

    private void registerAddFrame() {
        new CommandAPICommand("cinematic")
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
        new CommandAPICommand("cinematic")
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
        new CommandAPICommand("cinematic")
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
        new CommandAPICommand("cinematic")
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
        new CommandAPICommand("cinematic")
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
        new CommandAPICommand("cinematic")
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
        new CommandAPICommand("cinematic")
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
        new CommandAPICommand("cinematic")
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
        new CommandAPICommand("cinematic")
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
        new CommandAPICommand("cinematic")
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
        sendHelpLine(sender, "help.record-start");
        sendHelpLine(sender, "help.record-stop");
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
