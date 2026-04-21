package pluginsmc.langdua.core.paper.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.hooks.PapiHook;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.CinematicTrack;
import pluginsmc.langdua.core.paper.objects.Frame;
import pluginsmc.langdua.core.paper.objects.TimelineClip;
import pluginsmc.langdua.core.paper.objects.TransitionEffect;
import pluginsmc.langdua.core.paper.objects.TransitionMetadata;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PlayManager {
    private static final String CAMERA_TAG = "extraly_cam";

    private final Core instance;
    private final Map<UUID, PlaybackSession> sessions = new HashMap<>();

    public PlayManager(Core instance) {
        this.instance = instance;
    }

    public void play(CommandSender sender, Player player, String cinematicName) {
        var game = instance.getGame();
        var msg = instance.getMessageManager();

        if (sessions.containsKey(player.getUniqueId())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player is already watching a cinematic!</red>"));
            return;
        }

        Cinematic cinematic = game.getCinematics().get(cinematicName);
        if (cinematic == null) {
            msg.send(sender, "error.not-exist", "name", cinematicName);
            return;
        }
        cinematic.ensureStructure();

        List<ResolvedClip> clips = resolveTimeline(cinematic);
        if (clips.isEmpty()) {
            msg.send(sender, "error.not-exist", "name", cinematicName);
            return;
        }

        ResolvedClip firstClip = clips.get(0);
        Frame firstFrame = firstClip.frames().getFirst();
        World world = Bukkit.getWorld(firstFrame.getWorld());
        if (world == null) {
            return;
        }

        Location originalLocation = player.getLocation().clone();
        GameMode originalGameMode = player.getGameMode();
        Location startLocation = new Location(
                world,
                firstFrame.getX(),
                firstFrame.getY(),
                firstFrame.getZ(),
                firstFrame.getYaw(),
                firstFrame.getPitch()
        );

        game.getViewers().add(player.getUniqueId());
        player.setGameMode(GameMode.SPECTATOR);
        startLocation.getChunk().load();

        player.teleportAsync(startLocation).thenAccept(success -> {
            if (!success || !player.isOnline()) {
                game.getViewers().remove(player.getUniqueId());
                return;
            }

            Bukkit.getScheduler().runTaskLater(instance, () -> {
                if (!player.isOnline() || !game.getViewers().contains(player.getUniqueId())) {
                    game.getViewers().remove(player.getUniqueId());
                    return;
                }

                ArmorStand camera = spawnCamera(startLocation);
                player.setSpectatorTarget(camera);

                PlaybackSession session = new PlaybackSession(
                        sender,
                        player,
                        cinematic,
                        clips,
                        camera,
                        originalLocation,
                        originalGameMode
                );
                sessions.put(player.getUniqueId(), session);
                session.start();
            }, 2L);
        });
    }

    public void forceStop(CommandSender sender, Player player) {
        PlaybackSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.stop(true, sender);
        }
    }

    public void shutdown() {
        for (PlaybackSession session : new ArrayList<>(sessions.values())) {
            session.stop(false, null);
        }
        sessions.clear();
    }

    private ArmorStand spawnCamera(Location location) {
        ArmorStand camera = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        camera.setVisible(false);
        camera.setGravity(false);
        camera.setInvulnerable(true);
        camera.setBasePlate(false);
        camera.setMarker(true);
        camera.setPersistent(false);
        camera.setSilent(true);
        camera.addScoreboardTag(CAMERA_TAG);
        return camera;
    }

    private List<ResolvedClip> resolveTimeline(Cinematic cinematic) {
        List<ResolvedClip> resolved = new ArrayList<>();
        for (TimelineClip clip : cinematic.getTimeline()) {
            CinematicTrack track = cinematic.getTracks().get(clip.getTrackId());
            if (track == null || !track.isPlayable()) {
                continue;
            }
            int durationTicks = track.getDurationTicks();
            if (durationTicks <= 0) {
                if (resolved.isEmpty() && cinematic.getDuration() > 0) {
                    durationTicks = cinematic.getDuration() * 20;
                } else {
                    durationTicks = Math.max(1, (track.getFrames().size() - 1) * instance.getInterpolationSteps());
                }
            }
            resolved.add(new ResolvedClip(track, clip.getTransition(), durationTicks, track.getFrames()));
        }
        if (resolved.isEmpty() && cinematic.getPrimaryTrack().isPlayable()) {
            CinematicTrack track = cinematic.getPrimaryTrack();
            int durationTicks = track.getDurationTicks() > 0
                    ? track.getDurationTicks()
                    : (cinematic.getDuration() > 0
                    ? cinematic.getDuration() * 20
                    : Math.max(1, (track.getFrames().size() - 1) * instance.getInterpolationSteps()));
            resolved.add(new ResolvedClip(track, new TransitionMetadata(), durationTicks, track.getFrames()));
        }
        return resolved;
    }

    private double catmullRom(double p0, double p1, double p2, double p3, double t) {
        return 0.5D * ((2 * p1)
                + (-p0 + p2) * t
                + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t
                + (-p0 + 3 * p1 - 3 * p2 + p3) * t * t * t);
    }

    private float unwrapAngle(float reference, float angle) {
        float diff = angle - reference;
        while (diff < -180.0F) {
            diff += 360.0F;
        }
        while (diff > 180.0F) {
            diff -= 360.0F;
        }
        return reference + diff;
    }

    private double ease(double progress) {
        return -(Math.cos(Math.PI * progress) - 1.0D) / 2.0D;
    }

    private void dispatchFrame(Player player, Frame frame) {
        if (!frame.getCommands().isEmpty()) {
            for (String rawCommand : frame.getCommands()) {
                if (rawCommand == null || rawCommand.isBlank()) {
                    continue;
                }
                String command = rawCommand.replace("%player%", player.getName());
                if (instance.isPapiEnabled()) {
                    command = PapiHook.parse(player, command);
                }
                if (command.startsWith("/")) {
                    command = command.substring(1);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }

        boolean hasTitle = frame.getTitle() != null && !frame.getTitle().isBlank();
        boolean hasSubtitle = frame.getSubtitle() != null && !frame.getSubtitle().isBlank();
        if (!hasTitle && !hasSubtitle) {
            return;
        }

        String title = hasTitle ? frame.getTitle() : "";
        String subtitle = hasSubtitle ? frame.getSubtitle() : "";
        if (instance.isPapiEnabled()) {
            title = PapiHook.parse(player, title);
            subtitle = PapiHook.parse(player, subtitle);
        }

        Component titleComponent = hasTitle ? MiniMessage.miniMessage().deserialize(title) : Component.empty();
        Component subtitleComponent = hasSubtitle ? MiniMessage.miniMessage().deserialize(subtitle) : Component.empty();
        player.showTitle(Title.title(
                titleComponent,
                subtitleComponent,
                Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(3000), Duration.ofMillis(500))
        ));
    }

    private final class PlaybackSession {
        private final CommandSender source;
        private final Player player;
        private final Cinematic cinematic;
        private final List<ResolvedClip> clips;
        private final ArmorStand camera;
        private final Location originalLocation;
        private final GameMode originalGameMode;
        private final Random shakeRandom;
        private final int[] lastDispatchedFrame;

        private BukkitRunnable task;
        private int clipIndex = 0;
        private int clipTick = 0;
        private boolean stopped;

        private PlaybackSession(
                CommandSender source,
                Player player,
                Cinematic cinematic,
                List<ResolvedClip> clips,
                ArmorStand camera,
                Location originalLocation,
                GameMode originalGameMode
        ) {
            this.source = source;
            this.player = player;
            this.cinematic = cinematic;
            this.clips = clips;
            this.camera = camera;
            this.originalLocation = originalLocation;
            this.originalGameMode = originalGameMode;
            this.shakeRandom = new Random(player.getUniqueId().getMostSignificantBits() ^ cinematic.getName().hashCode());
            this.lastDispatchedFrame = new int[clips.size()];
            for (int i = 0; i < lastDispatchedFrame.length; i++) {
                lastDispatchedFrame[i] = -1;
            }
        }

        private void start() {
            if (cinematic.getBgmSound() != null && !cinematic.getBgmSound().isBlank()) {
                player.playSound(player.getLocation(), cinematic.getBgmSound(), SoundCategory.MASTER, 1.0F, 1.0F);
            }

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (stopped || !player.isOnline()) {
                        stop(false, null);
                        cancel();
                        return;
                    }

                    if (clipIndex >= clips.size()) {
                        stop(false, null);
                        cancel();
                        return;
                    }

                    if (player.getSpectatorTarget() == null || !player.getSpectatorTarget().equals(camera)) {
                        player.setSpectatorTarget(camera);
                    }

                    ResolvedClip clip = clips.get(clipIndex);
                    tickClip(clip);
                }
            };
            task.runTaskTimer(instance, 0L, 1L);
        }

        private void tickClip(ResolvedClip clip) {
            boolean lastTickOfClip = clipTick >= clip.durationTicks();
            double progress = clip.durationTicks() <= 0 ? 1.0D : (double) clipTick / clip.durationTicks();
            progress = Math.max(0.0D, Math.min(1.0D, progress));
            double eased = ease(progress);

            int segmentCount = clip.frames().size() - 1;
            double exactSegment = eased * segmentCount;
            int segmentIndex = Math.min((int) exactSegment, segmentCount - 1);
            double localT = exactSegment - segmentIndex;
            if (lastTickOfClip) {
                segmentIndex = segmentCount - 1;
                localT = 1.0D;
            }

            dispatchPassedFrames(clip, lastTickOfClip ? clip.frames().size() - 1 : segmentIndex + 1);
            moveCamera(clip, segmentIndex, localT, eased);
            applyZoom(eased);

            if (lastTickOfClip) {
                clipIndex++;
                clipTick = 0;
                if (clipIndex >= clips.size()) {
                    stop(false, null);
                    if (task != null) {
                        task.cancel();
                    }
                    return;
                }
                applyTransition(clips.get(clipIndex).transition());
                return;
            }

            clipTick++;
        }

        private void dispatchPassedFrames(ResolvedClip clip, int currentFrame) {
            while (lastDispatchedFrame[clipIndex] < currentFrame) {
                lastDispatchedFrame[clipIndex]++;
                if (lastDispatchedFrame[clipIndex] >= 0 && lastDispatchedFrame[clipIndex] < clip.frames().size()) {
                    dispatchFrame(player, clip.frames().get(lastDispatchedFrame[clipIndex]));
                }
            }
        }

        private void moveCamera(ResolvedClip clip, int segmentIndex, double localT, double eased) {
            List<Frame> frames = clip.frames();
            Frame f1 = frames.get(segmentIndex);
            Frame f2 = frames.get(segmentIndex + 1);
            Frame f0 = segmentIndex > 0 ? frames.get(segmentIndex - 1) : f1;
            Frame f3 = segmentIndex < frames.size() - 2 ? frames.get(segmentIndex + 2) : f2;

            World world = Bukkit.getWorld(f1.getWorld() != null ? f1.getWorld() : f2.getWorld());
            if (world == null) {
                return;
            }

            double x = catmullRom(f0.getX(), f1.getX(), f2.getX(), f3.getX(), localT);
            double y = catmullRom(f0.getY(), f1.getY(), f2.getY(), f3.getY(), localT);
            double z = catmullRom(f0.getZ(), f1.getZ(), f2.getZ(), f3.getZ(), localT);

            float yaw;
            float pitch;
            if (cinematic.hasFocus() && cinematic.getFocusWorld() != null && cinematic.getFocusWorld().equals(world.getName())) {
                Vector direction = new Vector(cinematic.getFocusX() - x, cinematic.getFocusY() - y, cinematic.getFocusZ() - z);
                Location look = new Location(world, x, y, z);
                if (direction.lengthSquared() > 0.0001D) {
                    look.setDirection(direction);
                }
                yaw = look.getYaw();
                pitch = look.getPitch();
            } else {
                float yaw0 = unwrapAngle(f1.getYaw(), f0.getYaw());
                float yaw1 = f1.getYaw();
                float yaw2 = unwrapAngle(f1.getYaw(), f2.getYaw());
                float yaw3 = unwrapAngle(yaw2, f3.getYaw());
                yaw = (float) catmullRom(yaw0, yaw1, yaw2, yaw3, localT);

                pitch = (float) catmullRom(f0.getPitch(), f1.getPitch(), f2.getPitch(), f3.getPitch(), localT);
            }

            if (cinematic.getShakeIntensity() > 0.0D) {
                yaw += (shakeRandom.nextFloat() - 0.5F) * (float) cinematic.getShakeIntensity();
                pitch += (shakeRandom.nextFloat() - 0.5F) * (float) cinematic.getShakeIntensity();
            }

            Location target = new Location(world, x, y, z, yaw, pitch);
            if (!target.getChunk().isLoaded()) {
                target.getChunk().load();
            }
            camera.teleport(target);
            camera.setRotation(yaw, pitch);
        }

        private void applyZoom(double eased) {
            int startZoom = cinematic.getStartZoom();
            int endZoom = cinematic.getEndZoom();
            if (startZoom == 0 && endZoom == 0) {
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                player.removePotionEffect(PotionEffectType.SPEED);
                return;
            }

            int zoom = (int) Math.round(startZoom + (endZoom - startZoom) * eased);
            if (zoom > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, zoom - 1, false, false, false));
                player.removePotionEffect(PotionEffectType.SPEED);
            } else if (zoom < 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, (-zoom) - 1, false, false, false));
                player.removePotionEffect(PotionEffectType.SLOWNESS);
            } else {
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                player.removePotionEffect(PotionEffectType.SPEED);
            }
        }

        private void applyTransition(TransitionMetadata transition) {
            if (transition.getEffect() != TransitionEffect.DARKEN_FADE || transition.getDurationTicks() <= 0) {
                return;
            }
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.DARKNESS,
                    transition.getDurationTicks(),
                    Math.max(0, transition.getStrength() - 1),
                    false,
                    false,
                    false
            ));
        }

        private void stop(boolean forced, CommandSender stopSource) {
            if (stopped) {
                return;
            }
            stopped = true;

            sessions.remove(player.getUniqueId());
            instance.getGame().getViewers().remove(player.getUniqueId());

            if (task != null) {
                task.cancel();
            }

            if (player.isOnline()) {
                player.setSpectatorTarget(null);
                player.teleport(originalLocation);
                Bukkit.getScheduler().runTaskLater(instance, () -> {
                    if (!player.isOnline()) {
                        return;
                    }
                    player.setGameMode(originalGameMode);
                    player.removePotionEffect(PotionEffectType.SLOWNESS);
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.removePotionEffect(PotionEffectType.DARKNESS);
                }, 1L);

                if (cinematic.getBgmSound() != null && !cinematic.getBgmSound().isBlank()) {
                    try {
                        player.stopSound(cinematic.getBgmSound(), SoundCategory.MASTER);
                    } catch (Exception ignored) {
                    }
                }
            }

            if (camera.isValid()) {
                camera.remove();
                Bukkit.getScheduler().runTaskLater(instance, () -> {
                    if (camera.isValid()) {
                        camera.remove();
                    }
                }, 1L);
            }

            if (forced) {
                instance.getMessageManager().send(stopSource == null ? source : stopSource, "play.force-stop", "player", player.getName());
            } else if (player.isOnline()) {
                instance.getMessageManager().send(source, "play.finished");
            }
        }
    }

    private record ResolvedClip(
            CinematicTrack track,
            TransitionMetadata transition,
            int durationTicks,
            List<Frame> frames
    ) {
    }
}
