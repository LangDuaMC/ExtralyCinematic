package pluginsmc.langdua.core.paper.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
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
import pluginsmc.langdua.core.paper.objects.TimelineClip;
import pluginsmc.langdua.core.paper.objects.TransitionEffect;
import pluginsmc.langdua.core.paper.objects.TransitionMetadata;
import pluginsmc.langdua.core.paper.objects.Frame;

public class CinematicCMD {
    private final Core instance;
    private final MessageManager msg;

    public CinematicCMD(Core instance) {
        this.instance = instance;
        this.msg = instance.getMessageManager();
    }

    public void register() {
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
        registerTrackCreate();
        registerAddFrame();
        registerAddFrameToTrack();
        registerTimelineAppend();
        registerTimelineReset();
        registerTransitionDarken();
        registerTransitionClear();
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

    private void registerEdit() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("edit"))
                .withPermission("cinematic.cmd")
                .executesPlayer((player, args) -> {
                    player.openInventory(new CinematicGUI(instance).getCinematicListGUI(player));
                })
                .register();
    }

    private void registerList() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("list"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    msg.send(sender, "list.header");
                    instance.getGame().getCinematics().keySet().forEach(name ->
                            msg.send(sender, "list.item", "name", name)
                    );
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
                .withArguments(new StringArgument("name"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Player target = (Player) args.get("player");
                    String name = (String) args.get("name");
                    instance.getGame().getPlayManager().play(sender, target, name);
                })
                .register();
    }

    private void registerStop() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("stop"))
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Player target = (Player) args.get("player");
                    instance.getGame().getPlayManager().forceStop(sender, target);
                })
                .register();
    }

    private void registerPath() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("path"))
                .withArguments(new StringArgument("name"))
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
                                if (world == null) {
                                    continue;
                                }
                                world.spawnParticle(Particle.FLAME, frame.getX(), frame.getY(), frame.getZ(), 1, 0, 0, 0, 0);
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
                .withArguments(new StringArgument("name"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    String name = (String) args.get("name");
                    if (instance.getGame().getCinematics().remove(name) != null) {
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
                .executesPlayer((player, args) -> {
                    instance.getGame().getRecordManager().startFreeRecord(player, (String) args.get("name"));
                })
                .register();

        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("record"))
                .withArguments(new LiteralArgument("start"))
                .withArguments(new StringArgument("name"))
                .withArguments(new StringArgument("track"))
                .withPermission("cinematic.cmd")
                .executesPlayer((player, args) -> {
                    instance.getGame().getRecordManager().startFreeRecord(
                            player,
                            (String) args.get("name"),
                            (String) args.get("track")
                    );
                })
                .register();
    }

    private void registerRecordStop() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("record"))
                .withArguments(new LiteralArgument("stop"))
                .withPermission("cinematic.cmd")
                .executesPlayer((player, args) -> {
                    instance.getGame().getRecordManager().stopFreeRecord(player);
                })
                .register();
    }

    private void registerTrackCreate() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("track"))
                .withArguments(new LiteralArgument("create"))
                .withArguments(new StringArgument("name"))
                .withArguments(new StringArgument("track"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    String name = (String) args.get("name");
                    String trackId = (String) args.get("track");
                    Cinematic cinematic = instance.getGame().getCinematics().computeIfAbsent(name, Cinematic::new);
                    cinematic.getOrCreateTrack(trackId);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                })
                .register();
    }

    private void registerAddFrame() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("addframe"))
                .withArguments(new StringArgument("name"))
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

    private void registerAddFrameToTrack() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("addframe"))
                .withArguments(new StringArgument("name"))
                .withArguments(new StringArgument("track"))
                .withPermission("cinematic.cmd")
                .executesPlayer((player, args) -> {
                    String name = (String) args.get("name");
                    String trackId = (String) args.get("track");
                    Cinematic cinematic = instance.getGame().getCinematics().computeIfAbsent(name, Cinematic::new);
                    Location location = player.getLocation();
                    cinematic.getOrCreateTrack(trackId).getFrames().add(new Frame(
                            location.getWorld().getName(),
                            location.getX(),
                            location.getY(),
                            location.getZ(),
                            location.getYaw(),
                            location.getPitch()
                    ));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(player, "edit.generic-updated");
                })
                .register();
    }

    private void registerTimelineAppend() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("timeline"))
                .withArguments(new LiteralArgument("append"))
                .withArguments(new StringArgument("name"))
                .withArguments(new StringArgument("track"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    String trackId = (String) args.get("track");
                    if (!cinematic.getTracks().containsKey(trackId)) {
                        msg.send(sender, "error.not-exist", "name", trackId);
                        return;
                    }
                    cinematic.getTimeline().add(new TimelineClip(trackId));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                })
                .register();
    }

    private void registerTimelineReset() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("timeline"))
                .withArguments(new LiteralArgument("reset"))
                .withArguments(new StringArgument("name"))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    cinematic.getTimeline().clear();
                    cinematic.getTimeline().add(new TimelineClip(Cinematic.DEFAULT_TRACK_ID));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                })
                .register();
    }

    private void registerTransitionDarken() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("transition"))
                .withArguments(new LiteralArgument("darken"))
                .withArguments(new StringArgument("name"))
                .withArguments(new IntegerArgument("clip", 0))
                .withArguments(new IntegerArgument("ticks", 1))
                .withArguments(new IntegerArgument("strength", 1))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    int clipIndex = (int) args.get("clip");
                    if (clipIndex < 0 || clipIndex >= cinematic.getTimeline().size()) {
                        msg.send(sender, "error.invalid-number");
                        return;
                    }
                    TransitionMetadata transition = cinematic.getTimeline().get(clipIndex).getTransition();
                    transition.setEffect(TransitionEffect.DARKEN_FADE);
                    transition.setDurationTicks((int) args.get("ticks"));
                    transition.setStrength((int) args.get("strength"));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                })
                .register();
    }

    private void registerTransitionClear() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("transition"))
                .withArguments(new LiteralArgument("clear"))
                .withArguments(new StringArgument("name"))
                .withArguments(new IntegerArgument("clip", 0))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    int clipIndex = (int) args.get("clip");
                    if (clipIndex < 0 || clipIndex >= cinematic.getTimeline().size()) {
                        msg.send(sender, "error.invalid-number");
                        return;
                    }
                    cinematic.getTimeline().get(clipIndex).setTransition(new TransitionMetadata());
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                })
                .register();
    }

    private void registerAddCommand() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("addcmd"))
                .withArguments(new StringArgument("name"))
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
                .withArguments(new StringArgument("name"))
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
                .withArguments(new StringArgument("name"))
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
                .withArguments(new StringArgument("name"))
                .withArguments(new IntegerArgument("seconds", 0))
                .withPermission("cinematic.cmd")
                .executes((sender, args) -> {
                    Cinematic cinematic = requireCinematic(sender, (String) args.get("name"));
                    if (cinematic == null) {
                        return;
                    }
                    int seconds = (int) args.get("seconds");
                    cinematic.setDuration(seconds);
                    cinematic.getPrimaryTrack().setDurationTicks(seconds * 20);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                })
                .register();
    }

    private void registerFocusSet() {
        new CommandAPICommand("cinematic")
                .withArguments(new LiteralArgument("focus"))
                .withArguments(new StringArgument("name"))
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
                .withArguments(new StringArgument("name"))
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
                .withArguments(new StringArgument("name"))
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
                .withArguments(new StringArgument("name"))
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
                .withArguments(new StringArgument("name"))
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
}
