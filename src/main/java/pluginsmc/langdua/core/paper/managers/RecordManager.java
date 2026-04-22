package pluginsmc.langdua.core.paper.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.MessageManager;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecordManager {
    private final Core instance;
    private final MessageManager msg;
    private final Map<UUID, TriggerRecordingSession> sessions = new HashMap<>();
    private final Map<UUID, String> overwriteArming = new HashMap<>();

    public RecordManager(Core instance) {
        this.instance = instance;
        this.msg = instance.getMessageManager();
    }

    public void prepareTriggeredRecord(Player player, String cinematicName, Integer durationSeconds) {
        UUID playerId = player.getUniqueId();
        if (sessions.containsKey(playerId)) {
            msg.send(player, "error.already-recording");
            return;
        }

        String overwriteKey = normalizeOverwriteKey(cinematicName, durationSeconds);
        if (instance.getGame().getCinematics().containsKey(cinematicName) && !overwriteKey.equals(overwriteArming.get(playerId))) {
            overwriteArming.put(playerId, overwriteKey);
            msg.send(player, "record.overwrite-warning", "name", cinematicName);
            return;
        }

        overwriteArming.remove(playerId);
        TriggerRecordingSession session = new TriggerRecordingSession(cinematicName, durationSeconds == null ? 0 : Math.max(0, durationSeconds));
        sessions.put(playerId, session);
        msg.send(player, "record.ready", "name", cinematicName);
        if (session.durationSeconds > 0) {
            msg.send(player, "record.ready-duration", "seconds", String.valueOf(session.durationSeconds));
        }
    }

    public boolean hasTriggeredRecording(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void handleTrigger(Player player) {
        TriggerRecordingSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        if (!session.started) {
            startTriggeredRecording(player, session);
            return;
        }
        finishTriggeredRecording(player, session, true);
    }

    public void handleQuit(Player player) {
        TriggerRecordingSession session = sessions.remove(player.getUniqueId());
        overwriteArming.remove(player.getUniqueId());
        if (session == null) {
            return;
        }
        if (session.task != null) {
            session.task.cancel();
        }
        if (session.started && !session.cinematic.getFrames().isEmpty()) {
            finalizeRecording(session);
        }
    }

    private void startTriggeredRecording(Player player, TriggerRecordingSession session) {
        session.started = true;
        session.cinematic = new Cinematic(session.name);
        captureFrame(player, session.cinematic.getFrames());
        msg.sendTitle(player, "record.title-rec", null, 0, 20, 20);
        msg.send(player, "record.started", "name", session.name);

        session.task = new BukkitRunnable() {
            int elapsedTicks = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    sessions.remove(player.getUniqueId());
                    overwriteArming.remove(player.getUniqueId());
                    if (!session.cinematic.getFrames().isEmpty()) {
                        finalizeRecording(session);
                    }
                    return;
                }

                if (elapsedTicks > 0 && elapsedTicks % instance.getInterpolationSteps() == 0) {
                    captureFrame(player, session.cinematic.getFrames());
                }

                if (session.durationSeconds > 0) {
                    if (elapsedTicks % 20 == 0) {
                        int currentSecond = Math.min(session.durationSeconds, elapsedTicks / 20);
                        msg.sendActionBar(player, "record.actionbar-timer",
                                "current", String.valueOf(currentSecond),
                                "total", String.valueOf(session.durationSeconds));
                    }
                    if (elapsedTicks >= session.durationSeconds * 20) {
                        finishTriggeredRecording(player, session, false);
                        cancel();
                        return;
                    }
                } else if (elapsedTicks % instance.getInterpolationSteps() == 0) {
                    msg.sendActionBar(player, "record.actionbar-free", "count", String.valueOf(session.cinematic.getFrames().size()));
                }

                elapsedTicks++;
            }
        };
        session.task.runTaskTimer(instance, 1L, 1L);
    }

    private void finishTriggeredRecording(Player player, TriggerRecordingSession session, boolean triggeredByPunch) {
        if (session.task != null) {
            session.task.cancel();
            session.task = null;
        }
        if (session.started && player.isOnline()) {
            captureFrame(player, session.cinematic.getFrames());
        }
        sessions.remove(player.getUniqueId());
        overwriteArming.remove(player.getUniqueId());
        finalizeRecording(session);
        if (triggeredByPunch) {
            msg.send(player, "record.stopped-trigger", "name", session.name, "count", String.valueOf(session.cinematic.getFrames().size()));
        } else {
            msg.send(player, "record.finish", "count", String.valueOf(session.cinematic.getFrames().size()));
        }
    }

    private void finalizeRecording(TriggerRecordingSession session) {
        instance.getGame().getCinematics().put(session.name, session.cinematic);
        instance.getStorageManager().save(instance.getGame().getCinematics());
    }

    private void captureFrame(Player player, List<Frame> frames) {
        Location loc = player.getLocation().clone();
        frames.add(new Frame(
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        ));
    }

    private String normalizeOverwriteKey(String cinematicName, Integer durationSeconds) {
        return cinematicName.toLowerCase() + ":" + (durationSeconds == null ? "-" : durationSeconds);
    }

    private static final class TriggerRecordingSession {
        private final String name;
        private final int durationSeconds;
        private Cinematic cinematic;
        private BukkitRunnable task;
        private boolean started;

        private TriggerRecordingSession(String name, int durationSeconds) {
            this.name = name;
            this.durationSeconds = durationSeconds;
        }
    }
}
