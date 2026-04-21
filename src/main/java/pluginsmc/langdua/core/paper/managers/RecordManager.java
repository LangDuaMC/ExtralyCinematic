package pluginsmc.langdua.core.paper.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.MessageManager;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RecordManager {
    private final Core instance;
    private final MessageManager msg;
    private final Map<UUID, Cinematic> activeRecordings = new HashMap<>();
    private final Map<UUID, String> activeRecordingTrackIds = new HashMap<>();
    private final Map<UUID, BukkitRunnable> activeRecordingTasks = new HashMap<>();

    public RecordManager(Core instance) {
        this.instance = instance;
        this.msg = instance.getMessageManager();
    }

    private void captureFrame(Player player, Cinematic cinematic, String trackId) {
        Location loc = player.getLocation().clone();
        cinematic.getOrCreateTrack(trackId).getFrames().add(new Frame(
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        ));
    }

    public void startCountdownRecord(Player player, Cinematic cine, int seconds) {
        new BukkitRunnable() {
            int count = 3;
            @Override
            public void run() {
                if (!player.isOnline()) { this.cancel(); return; }
                if (count == 0) {
                    msg.sendTitle(player, "record.title-rec", null, 0, 20, 20);
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    recordTicks(player, cine, Cinematic.DEFAULT_TRACK_ID, seconds);
                    this.cancel();
                } else {
                    msg.sendTitle(player, "record.title-count", null, 0, 20, 20, "count", String.valueOf(count));
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1);
                    count--;
                }
            }
        }.runTaskTimer(instance, 0L, 20L);
    }

    private void recordTicks(Player player, Cinematic cinematic, String trackId, int seconds) {
        new BukkitRunnable() {
            int elapsedTicks = 0;
            int totalTicks = seconds * 20;
            @Override
            public void run() {
                if (!player.isOnline()) { this.cancel(); return; }
                if (elapsedTicks >= totalTicks) {
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(player, "record.finish", "count", String.valueOf(cinematic.getOrCreateTrack(trackId).getFrames().size()));
                    this.cancel();
                    return;
                }
                if (elapsedTicks % instance.getInterpolationSteps() == 0) {
                    captureFrame(player, cinematic, trackId);
                }
                if (elapsedTicks % 20 == 0) {
                    msg.sendActionBar(player, "record.actionbar-timer", "current", String.valueOf(elapsedTicks / 20), "total", String.valueOf(seconds));
                }
                elapsedTicks++;
            }
        }.runTaskTimer(instance, 0L, 1L);
    }

    public void startFreeRecord(Player player, String cinematicName) {
        startFreeRecord(player, cinematicName, Cinematic.DEFAULT_TRACK_ID);
    }

    public void startFreeRecord(Player player, String cinematicName, String trackId) {
        UUID playerUUID = player.getUniqueId();
        if (activeRecordings.containsKey(playerUUID)) {
            msg.send(player, "error.already-recording");
            return;
        }
        var cinematics = instance.getGame().getCinematics();
        Cinematic cine = cinematics.computeIfAbsent(cinematicName, Cinematic::new);
        cine.getOrCreateTrack(trackId);
        activeRecordings.put(playerUUID, cine);
        activeRecordingTrackIds.put(playerUUID, trackId);
        msg.send(player, "record.start-free", "name", cinematicName);

        BukkitRunnable task = new BukkitRunnable() {
            int tickCounter = 0;
            @Override
            public void run() {
                if (!player.isOnline() || !activeRecordings.containsKey(playerUUID)) {
                    cancel();
                    activeRecordings.remove(playerUUID);
                    activeRecordingTrackIds.remove(playerUUID);
                    activeRecordingTasks.remove(playerUUID);
                    return;
                }
                if (tickCounter % instance.getInterpolationSteps() == 0) {
                    captureFrame(player, cine, trackId);
                    msg.sendActionBar(player, "record.actionbar-free", "count", String.valueOf(cine.getOrCreateTrack(trackId).getFrames().size()));
                }
                tickCounter++;
            }
        };
        task.runTaskTimer(instance, 0L, 1L);
        activeRecordingTasks.put(playerUUID, task);
    }

    public void stopFreeRecord(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!activeRecordings.containsKey(playerUUID)) {
            msg.send(player, "error.not-recording");
            return;
        }
        String trackId = activeRecordingTrackIds.remove(playerUUID);
        BukkitRunnable task = activeRecordingTasks.remove(playerUUID);
        if (task != null) task.cancel();
        Cinematic cinematic = activeRecordings.remove(playerUUID);
        if (cinematic != null) {
            instance.getStorageManager().save(instance.getGame().getCinematics());
            int count = cinematic.getOrCreateTrack(trackId == null ? Cinematic.DEFAULT_TRACK_ID : trackId).getFrames().size();
            msg.send(player, "record.stop-free", "name", cinematic.getName(), "count", String.valueOf(count));
        }
    }
}
