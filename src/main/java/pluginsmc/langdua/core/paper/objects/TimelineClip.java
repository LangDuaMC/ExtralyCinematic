package pluginsmc.langdua.core.paper.objects;

public class TimelineClip {
    private String trackId;
    private TransitionMetadata transition = new TransitionMetadata();

    public TimelineClip() {
    }

    public TimelineClip(String trackId) {
        this.trackId = trackId;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public TransitionMetadata getTransition() {
        return transition == null ? new TransitionMetadata() : transition;
    }

    public void setTransition(TransitionMetadata transition) {
        this.transition = transition == null ? new TransitionMetadata() : transition;
    }
}
