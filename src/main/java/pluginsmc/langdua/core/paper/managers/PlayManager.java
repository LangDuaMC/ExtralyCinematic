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
import pluginsmc.langdua.core.paper.objects.Frame;

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

    public static final class PlaybackOptions {
        private final boolean bypassWorldMetadata;
        private final int positionInterpolationSteps;
        private final int rotationInterpolationSteps;
        private final double smoothingPower;
        private final boolean normalizePosition;
        private final boolean normalizeRotation;
        private final boolean releaseMode;
        private final int fps;
        private final boolean dynamicFps;

        public PlaybackOptions(
                boolean bypassWorldMetadata,
                int positionInterpolationSteps,
                int rotationInterpolationSteps,
                double smoothingPower,
                boolean normalizePosition,
                boolean normalizeRotation,
                boolean releaseMode,
                int fps,
                boolean dynamicFps
        ) {
            this.bypassWorldMetadata = bypassWorldMetadata;
            this.positionInterpolationSteps = Math.max(1, positionInterpolationSteps);
            this.rotationInterpolationSteps = Math.max(1, rotationInterpolationSteps);
            this.smoothingPower = smoothingPower <= 0.0D ? 1.0D : smoothingPower;
            this.normalizePosition = normalizePosition;
            this.normalizeRotation = normalizeRotation;
            this.releaseMode = releaseMode;
            this.fps = Math.max(1, Math.min(20, fps));
            this.dynamicFps = dynamicFps;
        }

        public static PlaybackOptions defaults(int interpolationSteps) {
            return new PlaybackOptions(false, interpolationSteps, interpolationSteps, 1.0D, false, false, false, 20, false);
        }

        public boolean bypassWorldMetadata() {
            return bypassWorldMetadata;
        }

        public int positionInterpolationSteps() {
            return positionInterpolationSteps;
        }

        public int rotationInterpolationSteps() {
            return rotationInterpolationSteps;
        }

        public double smoothingPower() {
            return smoothingPower;
        }

        public boolean normalizePosition() {
            return normalizePosition;
        }

        public boolean normalizeRotation() {
            return normalizeRotation;
        }

        public boolean releaseMode() {
            return releaseMode;
        }

        public int fps() {
            return fps;
        }

        public boolean dynamicFps() {
            return dynamicFps;
        }
    }

    public void play(CommandSender sender, Player player, String cinematicName) {
        Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
        if (cinematic == null) {
            instance.getMessageManager().send(sender, "error.not-exist", "name", cinematicName);
            return;
        }
        play(sender, player, cinematic, cinematicName, PlaybackOptions.defaults(instance.getInterpolationSteps()));
    }

    public void play(CommandSender sender, Player player, Cinematic cinematic, String playbackName) {
        play(sender, player, cinematic, playbackName, PlaybackOptions.defaults(instance.getInterpolationSteps()));
    }

    public void play(CommandSender sender, Player player, String cinematicName, boolean bypassWorldMetadata) {
        Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
        if (cinematic == null) {
            instance.getMessageManager().send(sender, "error.not-exist", "name", cinematicName);
            return;
        }
        play(sender, player, cinematic, cinematicName,
                new PlaybackOptions(bypassWorldMetadata, instance.getInterpolationSteps(), instance.getInterpolationSteps(), 1.0D, false, false, false, 20, false));
    }

    public void play(CommandSender sender, Player player, Cinematic cinematic, String playbackName, boolean bypassWorldMetadata) {
        play(sender, player, cinematic, playbackName,
                new PlaybackOptions(bypassWorldMetadata, instance.getInterpolationSteps(), instance.getInterpolationSteps(), 1.0D, false, false, false, 20, false));
    }

    public void play(CommandSender sender, Player player, Cinematic cinematic, String playbackName, PlaybackOptions options) {
        if (sessions.containsKey(player.getUniqueId())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player is already watching a cinematic!</red>"));
            return;
        }

        cinematic.ensureStructure();
        List<Frame> frames = cinematic.getFrames();
        if (frames.size() < 2) {
            instance.getMessageManager().send(sender, "error.not-exist", "name", playbackName);
            return;
        }

        Frame firstFrame = frames.getFirst();
        World world = options.bypassWorldMetadata() ? player.getWorld() : Bukkit.getWorld(firstFrame.getWorld());
        if (world == null) {
            instance.getLogger().warning("Cannot play '" + playbackName + "' because the first frame world is unavailable.");
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

        instance.getGame().getViewers().add(player.getUniqueId());
        player.setGameMode(GameMode.SPECTATOR);
        startLocation.getChunk().load();

        player.teleportAsync(startLocation).thenAccept(success -> {
            if (!success || !player.isOnline()) {
                instance.getGame().getViewers().remove(player.getUniqueId());
                return;
            }

            Bukkit.getScheduler().runTaskLater(instance, () -> {
                if (!player.isOnline() || !instance.getGame().getViewers().contains(player.getUniqueId())) {
                    instance.getGame().getViewers().remove(player.getUniqueId());
                    return;
                }

                ArmorStand camera = spawnCamera(startLocation);
                player.setSpectatorTarget(camera);

                PlaybackSession session = new PlaybackSession(
                        sender,
                        player,
                        cinematic,
                        playbackName,
                        frames,
                        options,
                        camera,
                        originalLocation,
                        originalGameMode
                );
                sessions.put(player.getUniqueId(), session);
                session.start();
            }, 2L);
        });
    }

    public boolean requiresWorldTeleport(Player player, Cinematic cinematic, boolean bypassWorldMetadata) {
        if (bypassWorldMetadata) {
            return false;
        }

        cinematic.ensureStructure();
        List<Frame> frames = cinematic.getFrames();
        if (frames.isEmpty()) {
            return false;
        }

        String currentWorld = player.getWorld().getName();
        String previousWorld = currentWorld;
        for (Frame frame : frames) {
            if (frame == null || frame.getWorld() == null || frame.getWorld().isBlank()) {
                continue;
            }
            if (!frame.getWorld().equals(previousWorld)) {
                return true;
            }
            previousWorld = frame.getWorld();
        }
        return false;
    }

    public String getStartWorldName(Cinematic cinematic) {
        cinematic.ensureStructure();
        for (Frame frame : cinematic.getFrames()) {
            if (frame != null && frame.getWorld() != null && !frame.getWorld().isBlank()) {
                return frame.getWorld();
            }
        }
        return null;
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

    private double bias(double t, double power) {
        if (power == 1.0D || t <= 0.0D || t >= 1.0D) {
            return t;
        }
        double left = Math.pow(t, power);
        double right = Math.pow(1.0D - t, power);
        if (left + right == 0.0D) {
            return t;
        }
        return left / (left + right);
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
        private final String playbackName;
        private final List<Frame> frames;
        private final PlaybackOptions options;
        private final ArmorStand camera;
        private final Location originalLocation;
        private final GameMode originalGameMode;
        private final Random shakeRandom;
        private final int pollIntervalTicks;
        private final double[] positionCumulativeWeights;
        private final double[] rotationCumulativeWeights;

        private BukkitRunnable task;
        private int tick = 0;
        private int lastDispatchedFrame = -1;
        private boolean stopped;

        private PlaybackSession(
                CommandSender source,
                Player player,
                Cinematic cinematic,
                String playbackName,
                List<Frame> frames,
                PlaybackOptions options,
                ArmorStand camera,
                Location originalLocation,
                GameMode originalGameMode
        ) {
            this.source = source;
            this.player = player;
            this.cinematic = cinematic;
            this.playbackName = playbackName;
            this.frames = frames;
            this.options = options;
            this.camera = camera;
            this.originalLocation = originalLocation;
            this.originalGameMode = originalGameMode;
            this.shakeRandom = new Random(player.getUniqueId().getMostSignificantBits() ^ playbackName.hashCode());
            this.pollIntervalTicks = options.dynamicFps() ? 1 : Math.max(1, Math.round(20.0F / options.fps()));
            this.positionCumulativeWeights = buildCumulativeWeights(true);
            this.rotationCumulativeWeights = buildCumulativeWeights(false);
        }

        private void start() {
            if (cinematic.getBgmSound() != null && !cinematic.getBgmSound().isBlank()) {
                player.playSound(player.getLocation(), cinematic.getBgmSound(), SoundCategory.MASTER, 1.0F, 1.0F);
            }

            if (options.dynamicFps()) {
                instance.getMessageManager().send(source, "play.dfps-wip");
            }

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (stopped || !player.isOnline()) {
                        stop(false, null);
                        cancel();
                        return;
                    }

                    if (player.getGameMode() != GameMode.SPECTATOR) {
                        player.setGameMode(GameMode.SPECTATOR);
                    }
                    if (player.getSpectatorTarget() == null || !player.getSpectatorTarget().equals(camera)) {
                        player.setSpectatorTarget(camera);
                    }

                    tickPlayback();
                }
            };
            task.runTaskTimer(instance, 0L, pollIntervalTicks);
        }

        private void tickPlayback() {
            int segmentCount = frames.size() - 1;
            int positionDurationTicks = getPositionDurationTicks(segmentCount);
            int rotationDurationTicks = getRotationDurationTicks(segmentCount);
            int durationTicks = Math.max(positionDurationTicks, rotationDurationTicks);
            boolean finalTick = tick >= durationTicks;
            InterpolationState positionState = resolveState(segmentCount, positionDurationTicks, finalTick, positionCumulativeWeights);
            InterpolationState rotationState = resolveState(segmentCount, rotationDurationTicks, finalTick, rotationCumulativeWeights);
            if (finalTick) {
                positionState = new InterpolationState(segmentCount - 1, 1.0D);
                rotationState = new InterpolationState(segmentCount - 1, 1.0D);
            }

            dispatchPassedFrames(finalTick ? frames.size() - 1 : Math.max(positionState.segmentIndex() + 1, rotationState.segmentIndex() + 1));
            moveCamera(positionState, rotationState);
            applyZoom(ease(durationTicks <= 0 ? 1.0D : Math.max(0.0D, Math.min(1.0D, (double) tick / durationTicks))));

            if (finalTick) {
                stop(false, null);
                if (task != null) {
                    task.cancel();
                }
                return;
            }

            tick += pollIntervalTicks;
        }

        private int getPositionDurationTicks(int segmentCount) {
            if (cinematic.getDuration() > 0) {
                return cinematic.getDuration() * 20;
            }
            return Math.max(1, segmentCount * options.positionInterpolationSteps());
        }

        private int getRotationDurationTicks(int segmentCount) {
            if (cinematic.getDuration() > 0) {
                return cinematic.getDuration() * 20;
            }
            return Math.max(1, segmentCount * options.rotationInterpolationSteps());
        }

        private InterpolationState resolveState(int segmentCount, int durationTicks, boolean finalTick, double[] cumulativeWeights) {
            double progress = durationTicks <= 0 ? 1.0D : (double) tick / durationTicks;
            progress = Math.max(0.0D, Math.min(1.0D, progress));
            double eased = ease(progress);
            if (finalTick) {
                return new InterpolationState(segmentCount - 1, 1.0D);
            }
            if (cumulativeWeights == null) {
                double exactSegment = eased * segmentCount;
                int segmentIndex = Math.min((int) exactSegment, segmentCount - 1);
                double localT = exactSegment - segmentIndex;
                return new InterpolationState(segmentIndex, localT);
            }

            double targetWeight = eased * cumulativeWeights[cumulativeWeights.length - 1];
            int segmentIndex = 0;
            while (segmentIndex < cumulativeWeights.length - 1 && targetWeight > cumulativeWeights[segmentIndex + 1]) {
                segmentIndex++;
            }
            double segmentStart = cumulativeWeights[segmentIndex];
            double segmentEnd = cumulativeWeights[segmentIndex + 1];
            double localT = segmentEnd <= segmentStart ? 0.0D : (targetWeight - segmentStart) / (segmentEnd - segmentStart);
            localT = Math.max(0.0D, Math.min(1.0D, localT));
            return new InterpolationState(Math.min(segmentIndex, segmentCount - 1), localT);
        }

        private double[] buildCumulativeWeights(boolean positionMode) {
            boolean normalized = positionMode ? options.normalizePosition() : options.normalizeRotation();
            int segmentCount = frames.size() - 1;
            if (!normalized || segmentCount <= 1) {
                return null;
            }

            double[] rawWeights = new double[segmentCount];
            for (int i = 0; i < segmentCount; i++) {
                Frame a = frames.get(i);
                Frame b = frames.get(i + 1);
                rawWeights[i] = positionMode ? distanceWeight(a, b) : rotationWeight(a, b);
            }

            double[] smoothedWeights = new double[segmentCount];
            for (int i = 0; i < segmentCount; i++) {
                double total = rawWeights[i];
                int samples = 1;
                if (i > 0) {
                    total += rawWeights[i - 1];
                    samples++;
                }
                if (i + 1 < segmentCount) {
                    total += rawWeights[i + 1];
                    samples++;
                }
                smoothedWeights[i] = Math.max(0.0001D, total / samples);
            }

            double[] cumulative = new double[segmentCount + 1];
            cumulative[0] = 0.0D;
            for (int i = 0; i < segmentCount; i++) {
                cumulative[i + 1] = cumulative[i] + smoothedWeights[i];
            }
            return cumulative;
        }

        private double distanceWeight(Frame a, Frame b) {
            double dx = b.getX() - a.getX();
            double dy = b.getY() - a.getY();
            double dz = b.getZ() - a.getZ();
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        private double rotationWeight(Frame a, Frame b) {
            float yawDelta = Math.abs(unwrapAngle(a.getYaw(), b.getYaw()) - a.getYaw());
            float pitchDelta = Math.abs(b.getPitch() - a.getPitch());
            return Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
        }

        private void dispatchPassedFrames(int currentFrame) {
            while (lastDispatchedFrame < currentFrame) {
                lastDispatchedFrame++;
                if (lastDispatchedFrame >= 0 && lastDispatchedFrame < frames.size()) {
                    dispatchFrame(player, frames.get(lastDispatchedFrame));
                }
            }
        }

        private void moveCamera(InterpolationState positionState, InterpolationState rotationState) {
            Frame f1 = frames.get(positionState.segmentIndex());
            Frame f2 = frames.get(positionState.segmentIndex() + 1);
            Frame f0 = positionState.segmentIndex() > 0 ? frames.get(positionState.segmentIndex() - 1) : f1;
            Frame f3 = positionState.segmentIndex() < frames.size() - 2 ? frames.get(positionState.segmentIndex() + 2) : f2;

            World world = options.bypassWorldMetadata()
                    ? camera.getWorld()
                    : Bukkit.getWorld(f1.getWorld() != null ? f1.getWorld() : f2.getWorld());
            if (world == null) {
                return;
            }

            double positionT = bias(positionState.localT(), options.smoothingPower());
            double x = catmullRom(f0.getX(), f1.getX(), f2.getX(), f3.getX(), positionT);
            double y = catmullRom(f0.getY(), f1.getY(), f2.getY(), f3.getY(), positionT);
            double z = catmullRom(f0.getZ(), f1.getZ(), f2.getZ(), f3.getZ(), positionT);

            float yaw;
            float pitch;
            if (cinematic.hasFocus()) {
                Vector direction = new Vector(cinematic.getFocusX() - x, cinematic.getFocusY() - y, cinematic.getFocusZ() - z);
                Location look = new Location(world, x, y, z);
                if (direction.lengthSquared() > 0.0001D) {
                    look.setDirection(direction);
                }
                yaw = look.getYaw();
                pitch = look.getPitch();
            } else {
                Frame rf1 = frames.get(rotationState.segmentIndex());
                Frame rf2 = frames.get(rotationState.segmentIndex() + 1);
                Frame rf0 = rotationState.segmentIndex() > 0 ? frames.get(rotationState.segmentIndex() - 1) : rf1;
                Frame rf3 = rotationState.segmentIndex() < frames.size() - 2 ? frames.get(rotationState.segmentIndex() + 2) : rf2;
                double rotationT = bias(rotationState.localT(), options.smoothingPower());
                float yaw0 = unwrapAngle(rf1.getYaw(), rf0.getYaw());
                float yaw1 = rf1.getYaw();
                float yaw2 = unwrapAngle(rf1.getYaw(), rf2.getYaw());
                float yaw3 = unwrapAngle(yaw2, rf3.getYaw());
                yaw = (float) catmullRom(yaw0, yaw1, yaw2, yaw3, rotationT);
                pitch = (float) catmullRom(rf0.getPitch(), rf1.getPitch(), rf2.getPitch(), rf3.getPitch(), rotationT);
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
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    player.setSpectatorTarget(null);
                }
                Location exitLocation = options.releaseMode() ? camera.getLocation().clone() : originalLocation;
                player.teleport(exitLocation);
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

    private record InterpolationState(int segmentIndex, double localT) {
    }
}
