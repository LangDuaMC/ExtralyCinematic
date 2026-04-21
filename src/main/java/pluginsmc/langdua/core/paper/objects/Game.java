package pluginsmc.langdua.core.paper.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Game {

    // Lưu trữ toàn bộ Cinematic đang có trên Server
    private final Map<String, Cinematic> cinematics = new HashMap<>();

    // Lưu trữ những người chơi đang trong quá trình xem phim (để khóa các thao tác khác)
    private final Set<UUID> viewers = new HashSet<>();

    public Map<String, Cinematic> getCinematics() {
        return cinematics;
    }

    public Set<UUID> getViewers() {
        return viewers;
    }
}