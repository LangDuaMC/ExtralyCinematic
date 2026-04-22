package pluginsmc.langdua.core.paper.objects;

public class TimelineEntry {
    private String name;
    private String cinematicName;
    private TransitionMetadata transition = new TransitionMetadata();

    public TimelineEntry() {
    }

    public TimelineEntry(String name, String cinematicName) {
        this.name = name;
        this.cinematicName = cinematicName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCinematicName() {
        return cinematicName;
    }

    public void setCinematicName(String cinematicName) {
        this.cinematicName = cinematicName;
    }

    public TransitionMetadata getTransition() {
        return transition == null ? new TransitionMetadata() : transition;
    }

    public void setTransition(TransitionMetadata transition) {
        this.transition = transition == null ? new TransitionMetadata() : transition;
    }
}
