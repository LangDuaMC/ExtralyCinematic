package pluginsmc.langdua.core.paper.managers;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;
import pluginsmc.langdua.core.paper.objects.TimelineDefinition;
import pluginsmc.langdua.core.paper.objects.TimelineEntry;
import pluginsmc.langdua.core.paper.objects.TransitionEffect;

import java.util.ArrayList;
import java.util.List;

public class TimelinePlayManager {
    private final Core instance;

    public TimelinePlayManager(Core instance) {
        this.instance = instance;
    }

    public void play(CommandSender sender, Player player, String timelineName) {
        play(sender, player, timelineName, false);
    }

    public void play(CommandSender sender, Player player, String timelineName, boolean bypassWorldMetadata) {
        TimelineDefinition timeline = instance.getGame().getTimelines().get(timelineName);
        if (timeline == null) {
            instance.getMessageManager().send(sender, "timeline.not-exist", "name", timelineName);
            return;
        }

        Cinematic merged = buildPlayableTimeline(timeline, sender);
        if (merged == null) {
            return;
        }

        instance.getGame().getPlayManager().play(sender, player, merged, timeline.getName(), bypassWorldMetadata);
    }

    public TimelineEntry findUnmarkedWorldTeleportEntry(Player player, TimelineDefinition timeline) {
        timeline.ensureStructure();
        String previousWorld = player.getWorld().getName();

        for (TimelineEntry entry : timeline.getEntries()) {
            Cinematic cinematic = instance.getGame().getCinematics().get(entry.getCinematicName());
            if (cinematic == null) {
                continue;
            }

            String entryStartWorld = instance.getGame().getPlayManager().getStartWorldName(cinematic);
            if (entryStartWorld != null && !entryStartWorld.equals(previousWorld) && !entry.isWorldTeleport()) {
                return entry;
            }

            for (Frame frame : cinematic.getFrames()) {
                if (frame != null && frame.getWorld() != null && !frame.getWorld().isBlank()) {
                    previousWorld = frame.getWorld();
                }
            }
        }

        return null;
    }

    private Cinematic buildPlayableTimeline(TimelineDefinition timeline, CommandSender sender) {
        List<Frame> mergedFrames = new ArrayList<>();
        int totalDuration = 0;

        for (TimelineEntry entry : timeline.getEntries()) {
            Cinematic cinematic = instance.getGame().getCinematics().get(entry.getCinematicName());
            if (cinematic == null) {
                instance.getMessageManager().send(sender, "error.not-exist", "name", entry.getCinematicName());
                return null;
            }

            cinematic.ensureStructure();
            if (cinematic.getFrames().size() < 2) {
                instance.getMessageManager().send(sender, "timeline.entry-invalid", "entry", entry.getName(), "name", cinematic.getName());
                return null;
            }

            // Transition metadata is stored now and intentionally left as playback WIP.
            if (entry.getTransition().getEffect() != TransitionEffect.NONE) {
                instance.getLogger().fine("Timeline transition placeholder for entry '" + entry.getName() + "' is not applied yet.");
            }

            for (Frame frame : cinematic.getFrames()) {
                Frame clone = new Frame(frame.getWorld(), frame.getX(), frame.getY(), frame.getZ(), frame.getYaw(), frame.getPitch());
                clone.setCommands(new ArrayList<>(frame.getCommands()));
                clone.setTitle(frame.getTitle());
                clone.setSubtitle(frame.getSubtitle());
                mergedFrames.add(clone);
            }
            totalDuration += Math.max(0, cinematic.getDuration());
        }

        if (mergedFrames.size() < 2) {
            instance.getMessageManager().send(sender, "timeline.empty");
            return null;
        }

        Cinematic merged = new Cinematic(timeline.getName());
        merged.setFrames(mergedFrames);
        merged.setDuration(totalDuration);
        return merged;
    }
}
