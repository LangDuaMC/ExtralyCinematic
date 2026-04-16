package pluginsmc.langdua.core.paper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.MessageManager;
import pluginsmc.langdua.core.paper.guis.CinematicGUI;
import pluginsmc.langdua.core.paper.hooks.PapiHook;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@CommandAlias("cinematic")
@CommandPermission("cinematic.cmd")
public class CinematicCMD extends BaseCommand {

    private @NonNull Core instance;
    private MessageManager msg;
    private Map<UUID, Cinematic> activeRecordings = new HashMap<>();
    private Map<UUID, BukkitRunnable> activeRecordingTasks = new HashMap<>();

    public CinematicCMD(Core instance) {
        this.instance = instance;
        this.msg = instance.getMessageManager();
    }

    public void record(Player player, List<Frame> frames, int seconds) {
        new BukkitRunnable() {
            int elapsedTicks = 0;
            int totalTicks = seconds * 20;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                if (elapsedTicks >= totalTicks) {
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(player, "record.finish", "count", String.valueOf(frames.size()));
                    this.cancel();
                    return;
                }

                if (elapsedTicks % instance.getInterpolationSteps() == 0) {
                    Location loc = player.getLocation().clone();
                    frames.add(new Frame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
                }

                if (elapsedTicks % 20 == 0) {
                    msg.sendActionBar(player, "record.actionbar-timer", "current", String.valueOf(elapsedTicks / 20), "total", String.valueOf(seconds));
                }
                elapsedTicks++;
            }
        }.runTaskTimer(instance, 0L, 1L);
    }

    @Subcommand("rec")
    @CommandCompletion("<name> <seconds>")
    public void rec(Player sender, String cinematic, int seconds) {
        var cinematics = instance.getGame().getCinematics();
        if (cinematics.containsKey(cinematic)) {
            msg.send(sender, "error.already-exist", "name", cinematic);
            return;
        }

        var cine = new Cinematic(cinematic);
        cinematics.put(cinematic, cine);
        var frames = cine.getFrames();

        new BukkitRunnable() {
            int count = 3;

            @Override
            public void run() {
                if (!sender.isOnline()) {
                    this.cancel();
                    return;
                }
                if (count == 0) {
                    msg.sendTitle(sender, "record.title-rec", null, 0, 20, 20);
                    sender.playSound(sender.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    record(sender, frames, seconds);
                    this.cancel();
                } else {
                    msg.sendTitle(sender, "record.title-count", null, 0, 20, 20, "count", String.valueOf(count));
                    sender.playSound(sender.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1);
                    count--;
                }
            }
        }.runTaskTimer(instance, 0L, 20L);
    }

    @Subcommand("record start")
    @CommandCompletion("<name>")
    public void recordStart(Player player, String cinematicName) {
        UUID playerUUID = player.getUniqueId();
        var cinematics = instance.getGame().getCinematics();

        if (activeRecordings.containsKey(playerUUID)) {
            msg.send(player, "error.already-recording");
            return;
        }

        if (cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.already-exist", "name", cinematicName);
            return;
        }

        Cinematic newCinematic = new Cinematic(cinematicName);
        cinematics.put(cinematicName, newCinematic);
        activeRecordings.put(playerUUID, newCinematic);

        msg.send(player, "record.start-free", "name", cinematicName);

        BukkitRunnable recordingTask = new BukkitRunnable() {
            int tickCounter = 0;

            @Override
            public void run() {
                if (!player.isOnline() || !activeRecordings.containsKey(playerUUID)) {
                    cancel();
                    activeRecordings.remove(playerUUID);
                    activeRecordingTasks.remove(playerUUID);
                    return;
                }

                if (tickCounter % instance.getInterpolationSteps() == 0) {
                    Location loc = player.getLocation().clone();
                    newCinematic.getFrames().add(new Frame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
                    msg.sendActionBar(player, "record.actionbar-free", "count", String.valueOf(newCinematic.getFrames().size()));
                }
                tickCounter++;
            }
        };
        recordingTask.runTaskTimer(instance, 0L, 1L);
        activeRecordingTasks.put(playerUUID, recordingTask);
    }

    @Subcommand("record stop")
    public void recordStop(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!activeRecordings.containsKey(playerUUID)) {
            msg.send(player, "error.not-recording");
            return;
        }

        BukkitRunnable task = activeRecordingTasks.remove(playerUUID);
        if (task != null) task.cancel();

        Cinematic cinematic = activeRecordings.remove(playerUUID);
        if (cinematic != null) {
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(player, "record.stop-free", "name", cinematic.getName(), "count", String.valueOf(cinematic.getFrames().size()));
        }
    }

    @Subcommand("addframe")
    @Description("Manually adds a waypoint (keyframe) at your current location.")
    @CommandCompletion("<name>")
    public void addFrame(Player player, String cinematicName) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            cinematics.put(cinematicName, new Cinematic(cinematicName));
        }
        Cinematic cine = cinematics.get(cinematicName);
        Location loc = player.getLocation();
        cine.getFrames().add(new Frame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
        instance.getStorageManager().save(cinematics);
        msg.send(player, "edit.addframe", "name", cinematicName, "index", String.valueOf(cine.getFrames().size() - 1));
    }

    @Subcommand("duration")
    @Description("Sets the total playback duration (in seconds). Perfect for waypoint mode.")
    @CommandCompletion("<name> <seconds>")
    public void duration(Player player, String cinematicName, int seconds) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }
        Cinematic cine = cinematics.get(cinematicName);
        cine.setDuration(seconds);
        instance.getStorageManager().save(cinematics);
        msg.send(player, "edit.duration", "name", cinematicName, "val", String.valueOf(seconds));
    }

    @Subcommand("path")
    @CommandCompletion("<name>")
    public void path(Player player, String cinematicName) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }

        var frames = cinematics.get(cinematicName).getFrames();
        if (frames.isEmpty()) return;

        msg.send(player, "edit.path-visual");

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
    @CommandCompletion("<name> set|clear")
    public void focus(Player player, String cinematicName, String action) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }

        Cinematic cine = cinematics.get(cinematicName);
        if (action.equalsIgnoreCase("clear")) {
            cine.clearFocus();
            msg.send(player, "edit.focus-clear", "name", cinematicName);
        } else if (action.equalsIgnoreCase("set")) {
            Location loc = player.getLocation();
            cine.setFocus(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
            msg.send(player, "edit.focus-set", "name", cinematicName);
        }
        instance.getStorageManager().save(cinematics);
    }

    @Subcommand("shake")
    @CommandCompletion("<name> <intensity>")
    public void shake(Player player, String cinematicName, double intensity) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }
        cinematics.get(cinematicName).setShakeIntensity(intensity);
        instance.getStorageManager().save(cinematics);
        msg.send(player, "edit.shake-set", "name", cinematicName, "val", String.valueOf(intensity));
    }

    @Subcommand("zoom")
    @CommandCompletion("<name> <start> <end>")
    public void zoom(Player player, String cinematicName, int startZoom, int endZoom) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }
        Cinematic cine = cinematics.get(cinematicName);
        cine.setStartZoom(startZoom);
        cine.setEndZoom(endZoom);
        instance.getStorageManager().save(cinematics);
        msg.send(player, "edit.zoom-set", "name", cinematicName, "start", String.valueOf(startZoom), "end", String.valueOf(endZoom));
    }

    @Subcommand("bgm")
    @CommandCompletion("<name> <sound_string|clear>")
    public void bgm(Player player, String cinematicName, String sound) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }
        Cinematic cine = cinematics.get(cinematicName);
        if (sound.equalsIgnoreCase("clear")) {
            cine.setBgmSound(null);
            msg.send(player, "edit.bgm-clear", "name", cinematicName);
        } else {
            cine.setBgmSound(sound);
            msg.send(player, "edit.bgm-set", "name", cinematicName, "val", sound);
        }
        instance.getStorageManager().save(cinematics);
    }

    @Subcommand("title")
    @CommandCompletion("<name> <frameIndex> <text...>")
    public void setTitle(CommandSender sender, String cinematic, int frameIndex, String text) {
        var cine = instance.getGame().getCinematics().get(cinematic);
        if (cine != null && frameIndex >= 0 && frameIndex < cine.getFrames().size()) {
            cine.getFrames().get(frameIndex).setTitle(text);
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.title-set", "frame", String.valueOf(frameIndex));
        }
    }

    @Subcommand("subtitle")
    @CommandCompletion("<name> <frameIndex> <text...>")
    public void setSubtitle(CommandSender sender, String cinematic, int frameIndex, String text) {
        var cine = instance.getGame().getCinematics().get(cinematic);
        if (cine != null && frameIndex >= 0 && frameIndex < cine.getFrames().size()) {
            cine.getFrames().get(frameIndex).setSubtitle(text);
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.subtitle-set", "frame", String.valueOf(frameIndex));
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
    @CommandCompletion("@players <name>")
    public void play(CommandSender sender, @Flags("other") Player player, String cinematic) {
        var game = instance.getGame();
        var cinematics = game.getCinematics();
        if (!cinematics.containsKey(cinematic)) {
            msg.send(sender, "error.not-exist", "name", cinematic);
            return;
        }

        var cine = cinematics.get(cinematic);
        var frames = cine.getFrames();
        if (frames.size() < 2) return;

        var originalLoc = player.getLocation().clone();
        var originalGameMode = player.getGameMode();

        var firstFrame = frames.get(0);
        var world = Bukkit.getWorld(firstFrame.getWorld());
        if (world == null) return;

        var startLoc = new Location(world, firstFrame.getX(), firstFrame.getY(), firstFrame.getZ(), firstFrame.getYaw(), firstFrame.getPitch());

        ArmorStand cam = (ArmorStand) world.spawnEntity(startLoc, EntityType.ARMOR_STAND);
        cam.setVisible(false);
        cam.setGravity(false);
        cam.setInvulnerable(true);
        cam.setBasePlate(false);

        player.setGameMode(GameMode.SPECTATOR);
        game.getViewers().add(player.getUniqueId());

        Bukkit.getScheduler().runTaskLater(instance, () -> {
            if (player.isOnline()) player.setSpectatorTarget(cam);
        }, 1L);

        final int steps = instance.getInterpolationSteps();
        final int totalFrames = frames.size();
        final int totalTicks = cine.getDuration() > 0 ? (cine.getDuration() * 20) : ((totalFrames - 1) * steps);
        int[] lastPassedFrame = {-1};

        new BukkitRunnable() {
            int currentTick = 0;

            @Override
            public void run() {
                if (!game.getViewers().contains(player.getUniqueId()) || !player.isOnline()) {
                    cleanup();
                    this.cancel();
                    return;
                }

                if (currentTick == 0 && cine.getBgmSound() != null && !cine.getBgmSound().isEmpty()) {
                    player.playSound(player.getLocation(), cine.getBgmSound(), SoundCategory.MASTER, 1.0f, 1.0f);
                }

                boolean isLastTick = (currentTick >= totalTicks);
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
                    if (lastPassedFrame[0] < frames.size()) {
                        Frame fCmd = frames.get(lastPassedFrame[0]);

                        // Xử lý Command & PlaceholderAPI
                        if (!fCmd.getCommands().isEmpty()) {
                            for (String cmd : fCmd.getCommands()) {
                                String finalCmd = cmd.replace("%player%", player.getName());
                                if (instance.isPapiEnabled()) {
                                    finalCmd = PapiHook.parse(player, finalCmd);
                                }
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                            }
                        }

                        // Xử lý Title/Subtitle & PlaceholderAPI
                        boolean hasTitle = fCmd.getTitle() != null && !fCmd.getTitle().isEmpty();
                        boolean hasSub = fCmd.getSubtitle() != null && !fCmd.getSubtitle().isEmpty();
                        if (hasTitle || hasSub) {
                            String rawTitle = hasTitle ? fCmd.getTitle() : "";
                            String rawSub = hasSub ? fCmd.getSubtitle() : "";

                            if (instance.isPapiEnabled()) {
                                rawTitle = PapiHook.parse(player, rawTitle);
                                rawSub = PapiHook.parse(player, rawSub);
                            }

                            Component t = hasTitle ? MiniMessage.miniMessage().deserialize(rawTitle) : Component.empty();
                            Component s = hasSub ? MiniMessage.miniMessage().deserialize(rawSub) : Component.empty();
                            player.showTitle(Title.title(t, s, Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(1000))));
                        }
                    }
                }

                Frame f1 = frames.get(segmentIndex);
                Frame f2 = frames.get(segmentIndex + 1);
                Frame f0 = segmentIndex > 0 ? frames.get(segmentIndex - 1) : f1;
                Frame f3 = segmentIndex < totalFrames - 2 ? frames.get(segmentIndex + 2) : f2;

                org.bukkit.World worldForSegment = Bukkit.getWorld(f1.getWorld() != null ? f1.getWorld() : f2.getWorld());
                if (worldForSegment == null) worldForSegment = world;

                double x = catmullRom(f0.getX(), f1.getX(), f2.getX(), f3.getX(), localT);
                double y = catmullRom(f0.getY(), f1.getY(), f2.getY(), f3.getY(), localT);
                double z = catmullRom(f0.getZ(), f1.getZ(), f2.getZ(), f3.getZ(), localT);

                float interpYaw;
                float interpPitch;
                if (cine.hasFocus() && cine.getFocusWorld() != null && cine.getFocusWorld().equals(worldForSegment.getName())) {
                    Vector direction = new Vector(cine.getFocusX() - x, cine.getFocusY() - y, cine.getFocusZ() - z);
                    Location lookLoc = new Location(worldForSegment, x, y, z);
                    if (direction.lengthSquared() > 0.0001) lookLoc.setDirection(direction);
                    interpYaw = lookLoc.getYaw();
                    interpPitch = lookLoc.getPitch();
                } else {
                    interpYaw = (float) catmullRom(smoothAngle(f1.getYaw(), f0.getYaw()), f1.getYaw(), smoothAngle(f1.getYaw(), f2.getYaw()), smoothAngle(smoothAngle(f1.getYaw(), f2.getYaw()), f3.getYaw()), localT);
                    interpPitch = (float) catmullRom(smoothAngle(f1.getPitch(), f0.getPitch()), f1.getPitch(), smoothAngle(f1.getPitch(), f2.getPitch()), smoothAngle(smoothAngle(f1.getPitch(), f2.getPitch()), f3.getPitch()), localT);
                }

                if (cine.getShakeIntensity() > 0) {
                    interpYaw += (float) ((Math.random() - 0.5) * cine.getShakeIntensity());
                    interpPitch += (float) ((Math.random() - 0.5) * cine.getShakeIntensity());
                }

                cam.teleport(new Location(worldForSegment, x, y, z, interpYaw, interpPitch));

                if (cine.getStartZoom() != 0 || cine.getEndZoom() != 0) {
                    double zoomProgress = cine.getStartZoom() + (cine.getEndZoom() - cine.getStartZoom()) * easedProgress;
                    int zoomLevel = (int) Math.round(zoomProgress);
                    if (zoomLevel > 0)
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, zoomLevel - 1, false, false, false));
                    else if (zoomLevel < 0)
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, (-zoomLevel) - 1, false, false, false));
                    else {
                        player.removePotionEffect(PotionEffectType.SLOWNESS);
                        player.removePotionEffect(PotionEffectType.SPEED);
                    }
                }

                if (isLastTick) {
                    cleanup();
                    if (player.isOnline()) msg.send(sender, "play.finished");
                    this.cancel();
                }
                currentTick++;
            }

            private void cleanup() {
                game.getViewers().remove(player.getUniqueId());
                if (player.isOnline()) {
                    player.setSpectatorTarget(null);
                    player.setGameMode(originalGameMode);
                    player.teleport(originalLoc);
                    player.removePotionEffect(PotionEffectType.SLOWNESS);
                    player.removePotionEffect(PotionEffectType.SPEED);
                    if (cine.getBgmSound() != null && !cine.getBgmSound().isEmpty()) {
                        player.stopSound(cine.getBgmSound(), SoundCategory.MASTER);
                    }
                }
                if (cam.isValid()) cam.remove();
            }
        }.runTaskTimer(instance, 0L, 1L);
    }

    @Subcommand("stop")
    @CommandCompletion("@players")
    public void stop(CommandSender sender, @Flags("other") Player player) {
        if (instance.getGame().getViewers().remove(player.getUniqueId())) {
            msg.send(sender, "play.force-stop", "player", player.getName());
        }
    }

    @Subcommand("delete")
    @CommandCompletion("<name>")
    public void delete(CommandSender sender, String cinematic) {
        if (instance.getGame().getCinematics().remove(cinematic) != null) {
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.delete", "name", cinematic);
        }
    }

    @Subcommand("addcmd")
    @CommandCompletion("<name> <frameIndex> <command>")
    public void addCmd(CommandSender sender, String cinematic, int frameIndex, String command) {
        var cine = instance.getGame().getCinematics().get(cinematic);
        if (cine != null && frameIndex >= 0 && frameIndex < cine.getFrames().size()) {
            cine.getFrames().get(frameIndex).getCommands().add(command);
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.cmd-add", "frame", String.valueOf(frameIndex));
        }
    }

    @Subcommand("list")
    public void listCinematics(CommandSender sender) {
        msg.send(sender, "list.header");
        instance.getGame().getCinematics().keySet().forEach(name -> msg.send(sender, "list.item", "name", name));
    }

    @Subcommand("edit")
    public void edit(Player player) {
        player.openInventory(new CinematicGUI(instance).getCinematicListGUI(player));
    }
}