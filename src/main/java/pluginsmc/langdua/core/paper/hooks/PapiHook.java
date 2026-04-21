package pluginsmc.langdua.core.paper.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PapiHook {
    public static String parse(Player player, String text) {
        if (text == null || text.isEmpty()) return text;
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}