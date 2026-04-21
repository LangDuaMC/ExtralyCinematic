package pluginsmc.langdua.core.paper.objects;

import java.util.ArrayList;
import java.util.List;

public class CinematicTrack {
    private String id;
    private List<Frame> frames = new ArrayList<>();
    private int durationTicks;

    public CinematicTrack() {
    }

    public CinematicTrack(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public void setFrames(List<Frame> frames) {
        this.frames = frames == null ? new ArrayList<>() : new ArrayList<>(frames);
    }

    public boolean isPlayable() {
        return frames.size() >= 2;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(0, durationTicks);
    }
}
