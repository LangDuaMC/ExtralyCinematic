package pluginsmc.langdua.core.paper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pluginsmc.langdua.core.paper.objects.Cinematic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class Game {
    private final Core instance;

    private HashMap<String, Cinematic> cinematics;
    private Set<UUID> viewers;

    public Game(Core instance) {
        this.instance = instance;
        cinematics = new HashMap<>();
        viewers = new HashSet<>();
    }
}