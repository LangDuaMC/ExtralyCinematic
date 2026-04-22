package pluginsmc.langdua.core.paper.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.MessageManager;
import pluginsmc.langdua.core.paper.guis.CinematicDashboardGUI;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;
import pluginsmc.langdua.core.paper.objects.TimelineClip;
import pluginsmc.langdua.core.paper.objects.TransitionEffect;
import pluginsmc.langdua.core.paper.objects.TransitionMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CinematicCMD implements CommandExecutor, TabCompleter {

    private final Core instance;
    private final MessageManager msg;

    public CinematicCMD(Core instance) {
        this.instance = instance;
        this.msg = instance.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("cinematic.cmd")) {
            msg.send(sender, "error.no-permission");
            return true;
        }

        if (args.length == 0) {
            msg.send(sender, "help.main");
            return true;
        }

        String subCmd = args[0].toLowerCase();

        try {
            switch (subCmd) {
                case "edit":
                    if (!(sender instanceof Player player)) return true;
                    player.openInventory(new CinematicDashboardGUI(instance).getInventory());
                    break;

                case "list":
                    msg.send(sender, "list.header");
                    instance.getGame().getCinematics().keySet().forEach(name -> msg.send(sender, "list.item", "name", name));
                    break;

                case "reload":
                    if (!sender.hasPermission("cinematic.admin")) return true;
                    instance.reloadPlugin();
                    msg.send(sender, "admin.reload");
                    break;

                case "play":
                    if (args.length < 3) return true;
                    Player targetPlay = Bukkit.getPlayer(args[1]);
                    if (targetPlay != null) instance.getGame().getPlayManager().play(sender, targetPlay, args[2]);
                    break;

                case "stop":
                    if (args.length < 2) return true;
                    Player targetStop = Bukkit.getPlayer(args[1]);
                    if (targetStop != null) instance.getGame().getPlayManager().forceStop(sender, targetStop);
                    break;

                case "path":
                    if (!(sender instanceof Player playerPath) || args.length < 2) return true;
                    Cinematic cinePath = requireCinematic(sender, args[1]);
                    if (cinePath == null) return true;
                    msg.send(playerPath, "edit.path-visual");
                    new BukkitRunnable() {
                        int ticks = 0;
                        @Override
                        public void run() {
                            if (!playerPath.isOnline() || ticks > 200) { cancel(); return; }
                            for (Frame frame : cinePath.getFrames()) {
                                World world = Bukkit.getWorld(frame.getWorld());
                                if (world != null) world.spawnParticle(Particle.FLAME, frame.getX(), frame.getY(), frame.getZ(), 1, 0, 0, 0, 0);
                            }
                            ticks += 20;
                        }
                    }.runTaskTimer(instance, 0L, 20L);
                    break;

                case "delete":
                    if (args.length < 2) return true;
                    String delName = args[1];
                    if (instance.getGame().getCinematics().remove(delName) != null) {
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        msg.send(sender, "edit.delete", "name", delName);
                    } else {
                        msg.send(sender, "error.not-exist", "name", delName);
                    }
                    break;

                case "rec":
                    if (!(sender instanceof Player playerRec) || args.length < 2) return true;
                    String recName = args[1];
                    int recSeconds = args.length > 2 ? Integer.parseInt(args[2]) : 1;
                    if (instance.getGame().getCinematics().containsKey(recName)) {
                        msg.send(playerRec, "error.already-exist", "name", recName);
                        return true;
                    }
                    Cinematic newCine = new Cinematic(recName);
                    instance.getGame().getCinematics().put(recName, newCine);
                    instance.getGame().getRecordManager().startCountdownRecord(playerRec, newCine, recSeconds);
                    break;

                case "record":
                    if (!(sender instanceof Player playerRecord) || args.length < 2) return true;
                    if (args[1].equalsIgnoreCase("start") && args.length >= 3) {
                        if (args.length >= 4) {
                            instance.getGame().getRecordManager().startFreeRecord(playerRecord, args[2], args[3]);
                        } else {
                            instance.getGame().getRecordManager().startFreeRecord(playerRecord, args[2]);
                        }
                    } else if (args[1].equalsIgnoreCase("stop")) {
                        instance.getGame().getRecordManager().stopFreeRecord(playerRecord);
                    }
                    break;

                case "track":
                    if (args.length < 4 || !args[1].equalsIgnoreCase("create")) return true;
                    Cinematic cineTrack = instance.getGame().getCinematics().computeIfAbsent(args[2], Cinematic::new);
                    cineTrack.getOrCreateTrack(args[3]);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                    break;

                case "addframe":
                    if (!(sender instanceof Player playerAddFrame) || args.length < 2) return true;
                    Cinematic cineAddFrame = instance.getGame().getCinematics().computeIfAbsent(args[1], Cinematic::new);
                    Location locAddFrame = playerAddFrame.getLocation();
                    Frame newFrame = new Frame(locAddFrame.getWorld().getName(), locAddFrame.getX(), locAddFrame.getY(), locAddFrame.getZ(), locAddFrame.getYaw(), locAddFrame.getPitch());
                    if (args.length >= 3) {
                        cineAddFrame.getOrCreateTrack(args[2]).getFrames().add(newFrame);
                    } else {
                        cineAddFrame.getFrames().add(newFrame);
                    }
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(playerAddFrame, "edit.generic-updated");
                    break;

                case "timeline":
                    if (args.length < 3) return true;
                    Cinematic cineTimeline = requireCinematic(sender, args[2]);
                    if (cineTimeline == null) return true;
                    if (args[1].equalsIgnoreCase("append") && args.length >= 4) {
                        String trackId = args[3];
                        if (!cineTimeline.getTracks().containsKey(trackId)) {
                            msg.send(sender, "error.not-exist", "name", trackId);
                            return true;
                        }
                        cineTimeline.getTimeline().add(new TimelineClip(trackId));
                    } else if (args[1].equalsIgnoreCase("reset")) {
                        cineTimeline.getTimeline().clear();
                        cineTimeline.getTimeline().add(new TimelineClip(Cinematic.DEFAULT_TRACK_ID));
                    }
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                    break;

                case "transition":
                    if (args.length < 4) return true;
                    Cinematic cineTrans = requireCinematic(sender, args[2]);
                    if (cineTrans == null) return true;
                    int clipIndex = Integer.parseInt(args[3]);
                    if (clipIndex < 0 || clipIndex >= cineTrans.getTimeline().size()) {
                        msg.send(sender, "error.invalid-number");
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("darken") && args.length >= 6) {
                        TransitionMetadata trans = cineTrans.getTimeline().get(clipIndex).getTransition();
                        trans.setEffect(TransitionEffect.DARKEN_FADE);
                        trans.setDurationTicks(Integer.parseInt(args[4]));
                        trans.setStrength(Integer.parseInt(args[5]));
                    } else if (args[1].equalsIgnoreCase("clear")) {
                        cineTrans.getTimeline().get(clipIndex).setTransition(new TransitionMetadata());
                    }
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                    break;

                case "addcmd":
                    if (args.length < 4) return true;
                    Cinematic cineCmd = requireCinematic(sender, args[1]);
                    if (cineCmd == null) return true;
                    int frameCmd = Integer.parseInt(args[2]);
                    if (!isValidFrame(sender, cineCmd, frameCmd)) return true;
                    String commandStr = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    cineCmd.getFrames().get(frameCmd).getCommands().add(commandStr);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.cmd-added", "cmd", commandStr);
                    break;

                case "title":
                case "subtitle":
                    if (args.length < 4) return true;
                    Cinematic cineTitle = requireCinematic(sender, args[1]);
                    if (cineTitle == null) return true;
                    int frameTitle = Integer.parseInt(args[2]);
                    if (!isValidFrame(sender, cineTitle, frameTitle)) return true;
                    String text = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    if (subCmd.equals("title")) cineTitle.getFrames().get(frameTitle).setTitle(text);
                    else cineTitle.getFrames().get(frameTitle).setSubtitle(text);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                    break;

                case "duration":
                    if (args.length < 3) return true;
                    Cinematic cineDuration = requireCinematic(sender, args[1]);
                    if (cineDuration == null) return true;
                    int secs = Integer.parseInt(args[2]);
                    cineDuration.setDuration(secs);
                    cineDuration.getPrimaryTrack().setDurationTicks(secs * 20);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                    break;

                case "focus":
                    if (args.length < 3) return true;
                    Cinematic cineFocus = requireCinematic(sender, args[1]);
                    if (cineFocus == null) return true;
                    if (args[2].equalsIgnoreCase("set") && sender instanceof Player playerFocus) {
                        Location locFocus = playerFocus.getLocation();
                        cineFocus.setFocus(locFocus.getWorld().getName(), locFocus.getX(), locFocus.getY(), locFocus.getZ());
                    } else if (args[2].equalsIgnoreCase("clear")) {
                        cineFocus.clearFocus();
                    }
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                    break;

                case "shake":
                    if (args.length < 3) return true;
                    Cinematic cineShake = requireCinematic(sender, args[1]);
                    if (cineShake == null) return true;
                    cineShake.setShakeIntensity(Double.parseDouble(args[2]));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                    break;

                case "zoom":
                    if (args.length < 4) return true;
                    Cinematic cineZoom = requireCinematic(sender, args[1]);
                    if (cineZoom == null) return true;
                    cineZoom.setStartZoom(Integer.parseInt(args[2]));
                    cineZoom.setEndZoom(Integer.parseInt(args[3]));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                    break;

                case "bgm":
                    if (args.length < 3) return true;
                    Cinematic cineBgm = requireCinematic(sender, args[1]);
                    if (cineBgm == null) return true;
                    String sound = args[2];
                    cineBgm.setBgmSound("clear".equalsIgnoreCase(sound) ? null : sound);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(sender, "edit.generic-updated");
                    break;

                default:
                    msg.send(sender, "help.main");
                    break;
            }
        } catch (NumberFormatException e) {
            msg.send(sender, "error.invalid-number");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage("§cAn error occurred while executing command.");
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = Arrays.asList("edit", "list", "reload", "play", "stop", "path", "delete", "rec", "record", "track", "addframe", "timeline", "transition", "addcmd", "title", "subtitle", "duration", "focus", "shake", "zoom", "bgm");

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (String cmd : commands) {
                if (cmd.startsWith(partial)) completions.add(cmd);
            }
            return completions;
        }

        String subCmd = args[0].toLowerCase();
        List<String> cinematics = new ArrayList<>(instance.getGame().getCinematics().keySet());

        if (args.length == 2) {
            if (Arrays.asList("play", "stop").contains(subCmd)) {
                return null;
            }
            if (Arrays.asList("path", "delete", "rec", "addframe", "addcmd", "title", "subtitle", "duration", "focus", "shake", "zoom", "bgm").contains(subCmd)) {
                return filter(cinematics, args[1]);
            }
            if (subCmd.equals("record")) return filter(Arrays.asList("start", "stop"), args[1]);
            if (subCmd.equals("track")) return filter(Arrays.asList("create"), args[1]);
            if (subCmd.equals("timeline")) return filter(Arrays.asList("append", "reset"), args[1]);
            if (subCmd.equals("transition")) return filter(Arrays.asList("darken", "clear"), args[1]);
        }

        if (args.length == 3) {
            if (Arrays.asList("play", "record", "track", "timeline", "transition").contains(subCmd)) {
                return filter(cinematics, args[2]);
            }
            if (subCmd.equals("focus")) return filter(Arrays.asList("set", "clear"), args[2]);
        }

        return completions;
    }

    private List<String> filter(List<String> options, String query) {
        String lowerQuery = query.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(lowerQuery)).collect(Collectors.toList());
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