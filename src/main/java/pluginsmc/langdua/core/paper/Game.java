package pluginsmc.langdua.core.paper;

import pluginsmc.langdua.core.paper.managers.PlayManager;
import pluginsmc.langdua.core.paper.managers.RecordManager;
import pluginsmc.langdua.core.paper.objects.Cinematic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Game {
    private final Core instance;

    private HashMap<String, Cinematic> cinematics;
    private Set<UUID> viewers;
    private final PlayManager playManager;
    private final RecordManager recordManager;

    public Game(Core instance) {
        this.instance = instance;
        this.cinematics = new HashMap<>();
        this.viewers = new HashSet<>();
        this.playManager = new PlayManager(instance);
        this.recordManager = new RecordManager(instance);
    }

    public Core getInstance() {
        return instance;
    }

    public HashMap<String, Cinematic> getCinematics() {
        return cinematics;
    }

    public void setCinematics(HashMap<String, Cinematic> cinematics) {
        this.cinematics = cinematics;
    }

    public Set<UUID> getViewers() {
        return viewers;
    }

    public void setViewers(Set<UUID> viewers) {
        this.viewers = viewers;
    }

    public PlayManager getPlayManager() {
        return playManager;
    }

    public RecordManager getRecordManager() {
        return recordManager;
    }
}