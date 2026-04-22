package pluginsmc.langdua.core.paper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pluginsmc.langdua.core.paper.managers.PlayManager;
import pluginsmc.langdua.core.paper.managers.RecordManager;
import pluginsmc.langdua.core.paper.managers.TimelinePlayManager;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.TimelineDefinition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class Game {
    private final Core instance;

    private HashMap<String, Cinematic> cinematics;
    private HashMap<String, TimelineDefinition> timelines;
    private Set<UUID> viewers;
    private final PlayManager playManager;
    private final TimelinePlayManager timelinePlayManager;
    private final RecordManager recordManager;

    public Game(Core instance) {
        this.instance = instance;
        cinematics = new HashMap<>();
        timelines = new HashMap<>();
        viewers = new HashSet<>();
        playManager = new PlayManager(instance);
        timelinePlayManager = new TimelinePlayManager(instance);
        recordManager = new RecordManager(instance);
    }
}
