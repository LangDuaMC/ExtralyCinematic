package pluginsmc.langdua.core.paper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.taskchain.TaskChain;
import lombok.NonNull;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.guis.CinematicGUI;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@CommandAlias("cinematic")
@CommandPermission("cinematic.cmd")
public class CinematicCMD extends BaseCommand {

    private @NonNull Core instance;
    private Map<UUID, Cinematic> activeRecordings = new HashMap<>();
    private Map<UUID, BukkitRunnable> activeRecordingTasks = new HashMap<>();

    public CinematicCMD(Core instance) {
        this.instance = instance;
    }

    public void record(Player player, List<Frame> frames, int seconds) {
        var chain = Core.newChain();
        var count = 0;
        for (int i = 0; i < seconds; i++) {
            for (int j = 0; j < 20; j++) {
                var c = (int) count / 20;
                chain.delay(1).sync(() -> {
                    player.sendActionBar(ChatColor.YELLOW + "" + c + "/" + seconds);
                    var loc = player.getLocation().clone();
                    var frame = new Frame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    frames.add(frame);
                });
                count++;
            }
        }
        chain.sync(() -> instance.getStorageManager().save(instance.getGame().getCinematics()))
                .sync(TaskChain::abort).execute();
    }

    @Subcommand("rec")
    @Description("Records a new cinematic.")
    @CommandCompletion("<name> <seconds>")
    public void rec(Player sender, String cinematic, int seconds) {
        var game = instance.getGame();
        var cinematics = game.getCinematics();

        if (cinematics.containsKey(cinematic)) {
            sender.sendMessage(ChatColor.RED + "Cinematic already exist.");
        } else {
            var cine = new Cinematic(cinematic);
            cinematics.put(cinematic, cine);

            var frames = cine.getFrames();
            var chain = Core.newChain();
            var count = 3;

            while (count >= 0) {
                final var c = count;
                chain.delay(20).sync(() -> {
                    if (c == 0) {
                        sender.sendTitle(ChatColor.DARK_RED + "REC.", "", 0, 20, 20);
                        sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        record(sender, frames, seconds);
                    } else {
                        sender.sendTitle(ChatColor.DARK_RED + "" + c, "", 0, 20, 20);
                        sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1);
                    }
                });
                count--;
            }
            chain.sync(TaskChain::abort).execute();
        }
    }

    @Subcommand("record start")
    @Description("Starts recording a new cinematic on-the-fly.")
    @CommandCompletion("<name>")
    public void recordStart(Player player, String cinematicName) {
        UUID playerUUID = player.getUniqueId();
        var game = instance.getGame();
        var cinematics = game.getCinematics();

        if (activeRecordings.containsKey(playerUUID)) {
            player.sendMessage(ChatColor.RED + "You are already recording a cinematic. Stop it before starting a new one.");
            return;
        }

        if (cinematics.containsKey(cinematicName)) {
            player.sendMessage(ChatColor.RED + "A cinematic with this name already exists.");
            return;
        }

        Cinematic newCinematic = new Cinematic(cinematicName);
        cinematics.put(cinematicName, newCinematic);
        activeRecordings.put(playerUUID, newCinematic);

        player.sendMessage(ChatColor.GREEN + "Started recording cinematic '" + cinematicName + "'. Move around to capture frames.");

        BukkitRunnable recordingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !activeRecordings.containsKey(playerUUID)) {
                    cancel();
                    activeRecordings.remove(playerUUID);
                    activeRecordingTasks.remove(playerUUID);
                    player.sendMessage(ChatColor.RED + "Recording stopped due to disconnect or error.");
                    return;
                }

                Location loc = player.getLocation().clone();
                Frame frame = new Frame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                newCinematic.getFrames().add(frame);
                player.sendActionBar(ChatColor.YELLOW + "Recording... Frame count: " + newCinematic.getFrames().size());
            }
        };
        recordingTask.runTaskTimer(instance, 0L, 1L);
        activeRecordingTasks.put(playerUUID, recordingTask);
    }

    @Subcommand("record stop")
    @Description("Stops the current on-the-fly cinematic recording.")
    public void recordStop(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!activeRecordings.containsKey(playerUUID)) {
            player.sendMessage(ChatColor.RED + "You are not recording any cinematic.");
            return;
        }

        BukkitRunnable task = activeRecordingTasks.remove(playerUUID);
        if (task != null) {
            task.cancel();
        }

        Cinematic cinematic = activeRecordings.remove(playerUUID);
        if (cinematic != null) {
            instance.getStorageManager().save(instance.getGame().getCinematics());
            player.sendMessage(ChatColor.GREEN + "Stopped recording cinematic '" + cinematic.getName() + "'. Total frames: " + cinematic.getFrames().size());
        } else {
            player.sendMessage(ChatColor.RED + "An error occurred while stopping the recording.");
        }
    }

    @Subcommand("path")
    @Description("Visualizes the cinematic path with particles.")
    @CommandCompletion("<name>")
    public void path(Player player, String cinematicName) {
        var game = instance.getGame();
        var cinematics = game.getCinematics();

        if (!cinematics.containsKey(cinematicName)) {
            player.sendMessage(ChatColor.RED + "Cinematic doesn't exist.");
            return;
        }

        var cine = cinematics.get(cinematicName);
        var frames = cine.getFrames();

        if (frames.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Cinematic is empty.");
            return;
        }

        player.sendMessage(ChatColor.AQUA + "[ExtralyCinematic] " + ChatColor.GREEN + "Visualizing path for 10 seconds...");

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 200 || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                for (int i = 0; i < frames.size(); i++) {
                    Frame f = frames.get(i);
                    org.bukkit.World world = Bukkit.getWorld(f.getWorld());
                    if (world == null) continue;

                    Location loc = new Location(world, f.getX(), f.getY(), f.getZ());
                    world.spawnParticle(org.bukkit.Particle.FLAME, loc, 1, 0, 0, 0, 0);

                    if (i % 10 == 0 || i == frames.size() - 1) {
                        Location dirLoc = loc.clone();
                        dirLoc.setYaw(f.getYaw());
                        dirLoc.setPitch(f.getPitch());
                        Vector dir = dirLoc.getDirection().multiply(0.5);
                        world.spawnParticle(org.bukkit.Particle.END_ROD, loc.add(dir), 1, 0, 0, 0, 0);
                    }
                }
                ticks += 5;
            }
        }.runTaskTimer(instance, 0L, 5L);
    }

    @Subcommand("focus")
    @Description("Sets a focus target for the cinematic camera.")
    @CommandCompletion("<name> set|clear")
    public void focus(Player player, String cinematicName, String action) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            player.sendMessage(ChatColor.RED + "Cinematic doesn't exist.");
            return;
        }

        Cinematic cine = cinematics.get(cinematicName);

        if (action.equalsIgnoreCase("clear")) {
            cine.clearFocus();
            instance.getStorageManager().save(cinematics);
            player.sendMessage(ChatColor.GREEN + "Cleared focus target for " + cinematicName);
        } else if (action.equalsIgnoreCase("set")) {
            Location loc = player.getLocation();
            cine.setFocus(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
            instance.getStorageManager().save(cinematics);
            player.sendMessage(ChatColor.GREEN + "Set focus target for " + cinematicName + " at your current location.");
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /cinematic focus <name> set|clear");
        }
    }

    private double catmullRom(double p0, double p1, double p2, double p3, double t) {
        return 0.5 * ((2 * p1) + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t + (-p0 + 3 * p1 - 3 * p2 + p3) * t * t * t);
    }

    private float smoothAngle(float prevAngle, float currentAngle) {
        float diff = currentAngle - prevAngle;
        while (diff < -180.0f) diff += 360.0f;
        while (diff > 180.0f) diff -= 360.0f;
        return prevAngle + diff;
    }

    private double easeInOutSine(double x) {
        return -(Math.cos(Math.PI * x) - 1) / 2;
    }

    @Subcommand("play")
    @Description("Plays an existing cinematic for a player.")
    @CommandCompletion("@players <name>")
    public void play(CommandSender sender, @Flags("other") Player player, String cinematic) {
        var game = instance.getGame();
        var cinematics = game.getCinematics();

        if (!cinematics.containsKey(cinematic)) {
            sender.sendMessage(ChatColor.RED + "Cinematic doesn't exist.");
            return;
        }

        var cine = cinematics.get(cinematic);
        var frames = cine.getFrames();

        if (frames.size() < 2) {
            sender.sendMessage(ChatColor.RED + "Cinematic must have at least 2 frames.");
            return;
        }

        var originalLoc = player.getLocation().clone();
        var originalGameMode = player.getGameMode();

        var firstFrame = frames.get(0);
        var world = Bukkit.getWorld(firstFrame.getWorld());
        var startLoc = new Location(world, firstFrame.getX(), firstFrame.getY(), firstFrame.getZ(), firstFrame.getYaw(), firstFrame.getPitch());

        ArmorStand cam = (ArmorStand) world.spawnEntity(startLoc, EntityType.ARMOR_STAND);
        cam.setVisible(false);
        cam.setGravity(false);
        cam.setInvulnerable(true);
        cam.setMarker(true);

        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(cam);

        game.getViewers().add(player.getUniqueId());

        var chain = Core.newChain();
        final int steps = instance.getInterpolationSteps();
        final int totalFrames = frames.size();
        final int totalTicks = (totalFrames - 1) * steps;

        int[] lastPassedFrame = {-1};

        for (int tick = 0; tick <= totalTicks; tick++) {
            final int currentTick = tick;
            final boolean isLastTick = (tick == totalTicks);

            chain.delay(1).sync(() -> {
                if (!game.getViewers().contains(player.getUniqueId())) {
                    if (cam.isValid()) {
                        player.setSpectatorTarget(null);
                        cam.remove();
                        player.setGameMode(originalGameMode);
                        player.teleport(originalLoc);
                    }
                    return;
                }

                double progress = totalTicks == 0 ? 1.0 : (double) currentTick / totalTicks;
                double easedProgress = easeInOutSine(progress);

                double exactSegment = easedProgress * (totalFrames - 1);
                int segmentIndex = (int) Math.min(exactSegment, totalFrames - 2);
                double localT = exactSegment - segmentIndex;

                if (isLastTick) {
                    segmentIndex = totalFrames - 2;
                    localT = 1.0;
                }

                int currentI = segmentIndex + 1;

                while (lastPassedFrame[0] < currentI) {
                    lastPassedFrame[0]++;
                    Frame fCmd = frames.get(lastPassedFrame[0]);
                    if (!fCmd.getCommands().isEmpty()) {
                        for (String cmd : fCmd.getCommands()) {
                            String finalCmd = cmd.replace("%player%", player.getName());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                        }
                    }
                }

                if (isLastTick && lastPassedFrame[0] < totalFrames - 1) {
                    while (lastPassedFrame[0] < totalFrames - 1) {
                        lastPassedFrame[0]++;
                        Frame fCmd = frames.get(lastPassedFrame[0]);
                        if (!fCmd.getCommands().isEmpty()) {
                            for (String cmd : fCmd.getCommands()) {
                                String finalCmd = cmd.replace("%player%", player.getName());
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                            }
                        }
                    }
                }

                Frame f1 = frames.get(segmentIndex);
                Frame f2 = frames.get(segmentIndex + 1);
                Frame f0 = segmentIndex > 0 ? frames.get(segmentIndex - 1) : f1;
                Frame f3 = segmentIndex < totalFrames - 2 ? frames.get(segmentIndex + 2) : f2;

                org.bukkit.World worldForSegment = Bukkit.getWorld(f1.getWorld() != null ? f1.getWorld() : f2.getWorld());

                double x = catmullRom(f0.getX(), f1.getX(), f2.getX(), f3.getX(), localT);
                double y = catmullRom(f0.getY(), f1.getY(), f2.getY(), f3.getY(), localT);
                double z = catmullRom(f0.getZ(), f1.getZ(), f2.getZ(), f3.getZ(), localT);

                float interpYaw;
                float interpPitch;

                // Xử lý Focus Target
                if (cine.hasFocus() && cine.getFocusWorld().equals(worldForSegment.getName())) {
                    Vector direction = new Vector(cine.getFocusX() - x, cine.getFocusY() - y, cine.getFocusZ() - z);
                    Location lookLoc = new Location(worldForSegment, x, y, z);
                    lookLoc.setDirection(direction);
                    interpYaw = lookLoc.getYaw();
                    interpPitch = lookLoc.getPitch();
                } else {
                    float y1 = f1.getYaw();
                    float y0 = smoothAngle(y1, f0.getYaw());
                    float y2 = smoothAngle(y1, f2.getYaw());
                    float y3 = smoothAngle(y2, f3.getYaw());

                    float p1 = f1.getPitch();
                    float p0 = smoothAngle(p1, f0.getPitch());
                    float p2 = smoothAngle(p1, f2.getPitch());
                    float p3 = smoothAngle(p2, f3.getPitch());

                    interpYaw = (float) catmullRom(y0, y1, y2, y3, localT);
                    interpPitch = (float) catmullRom(p0, p1, p2, p3, localT);
                }

                Location loc = new Location(worldForSegment, x, y, z, interpYaw, interpPitch);
                cam.teleport(loc);

                if (isLastTick) {
                    game.getViewers().remove(player.getUniqueId());
                    player.setSpectatorTarget(null);
                    cam.remove();
                    player.setGameMode(originalGameMode);
                    player.teleport(originalLoc);
                    sender.sendMessage(ChatColor.AQUA + "[ExtralyCinematic] " + ChatColor.GREEN + "Cinematic finished.");
                }
            });
        }
        chain.sync(TaskChain::abort).execute();
    }

    @Subcommand("stop")
    @Description("Stops a cinematic for a specific player.")
    @CommandCompletion("@players")
    public void stop(CommandSender sender, @Flags("other") Player player) {
        var game = instance.getGame();
        if (game.getViewers().contains(player.getUniqueId())) {
            game.getViewers().remove(player.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "Force cinematic to " + player.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Can't play cinematic.");
        }
    }

    @Subcommand("delete")
    @Description("Deletes an existing cinematic.")
    @CommandCompletion("<name>")
    public void delete(CommandSender sender, String cinematic) {
        var game = instance.getGame();
        var cinematics = game.getCinematics();

        if (!cinematics.containsKey(cinematic)) {
            sender.sendMessage(ChatColor.RED + "Cinematic doesn't exist.");
        } else {
            cinematics.remove(cinematic);
            instance.getStorageManager().save(cinematics);
            sender.sendMessage(ChatColor.GREEN + "Deleted cinematic: " + cinematic);
        }
    }

    @Subcommand("addcmd")
    @Description("Adds a command to a specific frame of a cinematic.")
    @CommandCompletion("<name> <frameIndex> <command>")
    public void addCmd(CommandSender sender, String cinematic, int frameIndex, String command) {
        var game = instance.getGame();
        var cinematics = game.getCinematics();

        if (!cinematics.containsKey(cinematic)) {
            sender.sendMessage(ChatColor.RED + "Cinematic doesn't exist.");
            return;
        }

        var cine = cinematics.get(cinematic);
        if (frameIndex < 0 || frameIndex >= cine.getFrames().size()) {
            sender.sendMessage(ChatColor.RED + "Lỗi: Index out range. Range: 0 -> " + (cine.getFrames().size() - 1));
            return;
        }

        cine.getFrames().get(frameIndex).getCommands().add(command);
        instance.getStorageManager().save(cinematics);
        sender.sendMessage(ChatColor.GREEN + "Add command to frame" + frameIndex + cinematic + ": /" + command);
    }

    @Subcommand("list")
    @Description("Lists all available cinematics.")
    public void listCinematics(CommandSender sender) {
        var cinematics = instance.getGame().getCinematics();
        if (cinematics.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No cinematics found.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "--- ExtralyCinematic List ---");
        cinematics.keySet().forEach(name -> sender.sendMessage(ChatColor.YELLOW + "- " + name));
        sender.sendMessage(ChatColor.GOLD + "--------------------------");
    }

    @Subcommand("edit")
    @Description("Opens the cinematic editor GUI.")
    public void edit(Player player) {
        CinematicGUI cinematicGUI = new CinematicGUI(instance);
        player.openInventory(cinematicGUI.getCinematicListGUI(player));
    }

    @HelpCommand
    @Subcommand("help")
    @Description("Displays help for ExtralyCinematic commands.")
    public void help(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- ExtralyCinematic Commands ---");
        sender.sendMessage(ChatColor.YELLOW + "/cinematic record start <name>" + ChatColor.GRAY + " - Start recording.");
        sender.sendMessage(ChatColor.YELLOW + "/cinematic record stop" + ChatColor.GRAY + " - Stop and save recording.");
        sender.sendMessage(ChatColor.YELLOW + "/cinematic play <player> <name>" + ChatColor.GRAY + " - Play cinematic.");
        sender.sendMessage(ChatColor.YELLOW + "/cinematic path <name>" + ChatColor.GRAY + " - Visualize path.");
        sender.sendMessage(ChatColor.YELLOW + "/cinematic focus <name> set|clear" + ChatColor.GRAY + " - Set camera target.");
        sender.sendMessage(ChatColor.YELLOW + "/cinematic edit" + ChatColor.GRAY + " - Open GUI Editor.");
        sender.sendMessage(ChatColor.YELLOW + "/cinematic help" + ChatColor.GRAY + " - Show this message.");
        sender.sendMessage(ChatColor.GOLD + "--------------------------");
    }
}