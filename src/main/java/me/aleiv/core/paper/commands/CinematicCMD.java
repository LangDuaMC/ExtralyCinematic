package me.aleiv.core.paper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.taskchain.TaskChain;
import lombok.NonNull;
import me.aleiv.core.paper.Core;
import me.aleiv.core.paper.objects.Cinematic;
import me.aleiv.core.paper.objects.Frame;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("cinematic")
@CommandPermission("cinematic.cmd")
public class CinematicCMD extends BaseCommand {

    private @NonNull Core instance;

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

    @Subcommand("play")
    @CommandCompletion("@players")
    public void play(CommandSender sender, @Flags("other") Player player, String cinematic) {
        var game = instance.getGame();
        var cinematics = game.getCinematics();

        if (!cinematics.containsKey(cinematic)) {
            sender.sendMessage(ChatColor.RED + "Cinematic doesn't exist.");
            return;
        }

        var cine = cinematics.get(cinematic);
        var frames = cine.getFrames();

        if (frames.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Cinematic is empty.");
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

        // Determine how many interpolation steps should occur between two keyframes.  A value
        // of 1 disables interpolation and reproduces the original behaviour.  See
        // config.yml for details.
        final int steps = instance.getInterpolationSteps();

        // Iterate through the list of frames and interpolate linearly between successive
        // positions.  Commands attached to a frame are executed on the first
        // interpolation tick of that frame.
        for (int i = 0; i < frames.size(); i++) {
            final Frame currentFrame = frames.get(i);
            // The first frame simply teleports the camera to the start position.  We
            // skip interpolation here because the player was already teleported when
            // the ArmorStand was spawned (startLoc above).
            if (i == 0) {
                // execute commands attached to the first frame immediately
                if (!currentFrame.getCommands().isEmpty()) {
                    for (String cmd : currentFrame.getCommands()) {
                        String finalCmd = cmd.replace("%player%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                    }
                }
                continue;
            }
            final Frame prevFrame = frames.get(i - 1);
            // Precompute world for interpolation; if world changes unexpectedly we use the
            // current frame's world.  World teleportation is generally not supported as
            // cinematics are meant to be recorded in a single world.
            final org.bukkit.World worldForSegment = Bukkit.getWorld(prevFrame.getWorld() != null ? prevFrame.getWorld() : currentFrame.getWorld());
            for (int s = 0; s < steps; s++) {
                // Capture loop variables for lambda
                final int stepIndex = s;
                final boolean isLastSegment = (i == frames.size() - 1) && (s == steps - 1);
                // Determine if we should run commands attached to this keyframe.  We only run
                // them once on the first interpolation step of the segment.
                final boolean runCommands = (s == 0) && !currentFrame.getCommands().isEmpty();

                chain.delay(1).sync(() -> {
                    // If the cinematic was forcibly stopped, clean up and abort further
                    // processing for this viewer.  This check avoids constantly polling
                    // player state elsewhere.
                    if (!game.getViewers().contains(player.getUniqueId())) {
                        if (cam.isValid()) {
                            player.setSpectatorTarget(null);
                            cam.remove();
                            player.setGameMode(originalGameMode);
                            player.teleport(originalLoc);
                        }
                        return;
                    }
                    // Compute interpolation factor.  When steps=1 this yields 0.0 so we
                    // teleport directly to the currentFrame each tick.  Otherwise the
                    // location is smoothly interpolated between prevFrame and currentFrame.
                    double t = (steps <= 1) ? 0.0D : (double) stepIndex / (double) steps;
                    // Linear interpolation for position coordinates
                    double x = prevFrame.getX() + (currentFrame.getX() - prevFrame.getX()) * t;
                    double y = prevFrame.getY() + (currentFrame.getY() - prevFrame.getY()) * t;
                    double z = prevFrame.getZ() + (currentFrame.getZ() - prevFrame.getZ()) * t;
                    // Interpolate yaw and pitch taking into account angle wrapping.  We
                    // normalise the angles to avoid large jumps across the 0/360 border.
                    float yawA = prevFrame.getYaw();
                    float yawB = currentFrame.getYaw();
                    float diffYaw = yawB - yawA;
                    if (Math.abs(diffYaw) > 180f) {
                        // take the shorter path around the circle
                        if (diffYaw > 0) {
                            yawA += 360f;
                        } else {
                            yawB += 360f;
                        }
                        diffYaw = yawB - yawA;
                    }
                    float interpYaw = (float) (yawA + diffYaw * t);
                    // Wrap the resulting yaw back into [-180,180]
                    if (interpYaw > 180f) interpYaw -= 360f;
                    float pitchA = prevFrame.getPitch();
                    float pitchB = currentFrame.getPitch();
                    float diffPitch = pitchB - pitchA;
                    float interpPitch = (float) (pitchA + diffPitch * t);
                    var loc = new Location(worldForSegment, x, y, z, interpYaw, interpPitch);
                    cam.teleport(loc);
                    // Run commands associated with this frame (only once per segment)
                    if (runCommands) {
                        for (String cmd : currentFrame.getCommands()) {
                            String finalCmd = cmd.replace("%player%", player.getName());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                        }
                    }
                    // If this is the very last interpolation step of the entire cinematic,
                    // restore the player's state and notify the sender.
                    if (isLastSegment) {
                        game.getViewers().remove(player.getUniqueId());
                        player.setSpectatorTarget(null);
                        cam.remove();
                        player.setGameMode(originalGameMode);
                        player.teleport(originalLoc);
                        sender.sendMessage(ChatColor.GREEN + "Cinematic finished.");
                    }
                });
            }
        }
        chain.sync(TaskChain::abort).execute();
    }

    @Subcommand("stop")
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
}