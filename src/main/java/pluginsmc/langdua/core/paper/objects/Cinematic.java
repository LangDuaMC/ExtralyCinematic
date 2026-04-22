package pluginsmc.langdua.core.paper.objects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Cinematic {
    public static final String DEFAULT_TRACK_ID = "main";

    private String name;
    private Map<String, CinematicTrack> tracks = new LinkedHashMap<>();
    private List<TimelineClip> timeline = new ArrayList<>();

    private String focusWorld = null;
    private Double focusX = null;
    private Double focusY = null;
    private Double focusZ = null;

    private double shakeIntensity = 0.0;

    private int startZoom = 0;
    private int endZoom = 0;

    private String bgmSound = null;

    private int duration = 0;

    public Cinematic(String name) {
        this.name = name;
        ensureStructure();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, CinematicTrack> getTracks() {
        return tracks;
    }

    public void setTracks(Map<String, CinematicTrack> tracks) {
        this.tracks = tracks == null ? new LinkedHashMap<>() : new LinkedHashMap<>(tracks);
    }

    public List<TimelineClip> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineClip> timeline) {
        this.timeline = timeline == null ? new ArrayList<>() : new ArrayList<>(timeline);
    }

    public CinematicTrack getPrimaryTrack() {
        CinematicTrack track = tracks.get(DEFAULT_TRACK_ID);
        if (track == null) {
            track = new CinematicTrack(DEFAULT_TRACK_ID);
            tracks.put(DEFAULT_TRACK_ID, track);
        }
        return track;
    }

    public CinematicTrack getOrCreateTrack(String trackId) {
        return tracks.computeIfAbsent(trackId, CinematicTrack::new);
    }

    public List<Frame> getFrames() {
        return getPrimaryTrack().getFrames();
    }

    public void setFrames(List<Frame> frames) {
        getPrimaryTrack().setFrames(frames);
    }

    public void ensureStructure() {
        if (tracks == null) {
            tracks = new LinkedHashMap<>();
        }
        if (!tracks.containsKey(DEFAULT_TRACK_ID)) {
            tracks.put(DEFAULT_TRACK_ID, new CinematicTrack(DEFAULT_TRACK_ID));
        }
        if (timeline == null) {
            timeline = new ArrayList<>();
        }
        if (timeline.isEmpty()) {
            timeline.add(new TimelineClip(DEFAULT_TRACK_ID));
        }
        for (TimelineClip clip : timeline) {
            if (clip.getTrackId() == null || clip.getTrackId().isBlank()) {
                clip.setTrackId(DEFAULT_TRACK_ID);
            }
        }

        CinematicTrack primaryTrack = tracks.get(DEFAULT_TRACK_ID);
        if (primaryTrack != null && primaryTrack.getDurationTicks() == 0 && duration > 0) {
            primaryTrack.setDurationTicks(duration * 20);
        }
    }

    public boolean hasFocus() { return focusWorld != null && focusX != null && focusY != null && focusZ != null; }
    public void setFocus(String world, double x, double y, double z) {
        this.focusWorld = world;
        this.focusX = x;
        this.focusY = y;
        this.focusZ = z;
    }
    public void clearFocus() {
        this.focusWorld = null;
        this.focusX = null;
        this.focusY = null;
        this.focusZ = null;
    }
    public String getFocusWorld() { return focusWorld; }
    public Double getFocusX() { return focusX; }
    public Double getFocusY() { return focusY; }
    public Double getFocusZ() { return focusZ; }

    public double getShakeIntensity() { return shakeIntensity; }
    public void setShakeIntensity(double shakeIntensity) { this.shakeIntensity = shakeIntensity; }

    public int getStartZoom() { return startZoom; }
    public void setStartZoom(int startZoom) { this.startZoom = startZoom; }
    public int getEndZoom() { return endZoom; }
    public void setEndZoom(int endZoom) { this.endZoom = endZoom; }

    public String getBgmSound() { return bgmSound; }
    public void setBgmSound(String bgmSound) { this.bgmSound = bgmSound; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
}