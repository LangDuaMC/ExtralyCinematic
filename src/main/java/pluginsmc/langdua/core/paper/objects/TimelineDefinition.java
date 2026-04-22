package pluginsmc.langdua.core.paper.objects;

import java.util.ArrayList;
import java.util.List;

public class TimelineDefinition {
    private String name;
    private List<TimelineEntry> entries = new ArrayList<>();

    public TimelineDefinition(String name) {
        this.name = name;
        ensureStructure();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TimelineEntry> getEntries() {
        ensureStructure();
        return entries;
    }

    public void setEntries(List<TimelineEntry> entries) {
        this.entries = entries == null ? new ArrayList<>() : new ArrayList<>(entries);
        ensureStructure();
    }

    public void ensureStructure() {
        if (entries == null) {
            entries = new ArrayList<>();
        }
        for (int i = 0; i < entries.size(); i++) {
            TimelineEntry entry = entries.get(i);
            if (entry == null) {
                entries.set(i, new TimelineEntry());
            } else if (entry.getTransition() == null) {
                entry.setTransition(new TransitionMetadata());
            }
        }
    }

    public TimelineEntry getEntry(String entryName) {
        return getEntries().stream()
                .filter(entry -> entryName.equalsIgnoreCase(entry.getName()))
                .findFirst()
                .orElse(null);
    }
}
