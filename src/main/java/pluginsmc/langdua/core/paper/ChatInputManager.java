package pluginsmc.langdua.core.paper;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatInputManager implements Listener {
    private final Core instance;
    private final Map<UUID, Consumer<String>> pendingInputs = new HashMap<>();

    public ChatInputManager(Core instance) {
        this.instance = instance;
    }

    public void requestInput(Player player, String messagePath, Consumer<String> onInput) {
        player.closeInventory();
        player.sendMessage("");
        instance.getMessageManager().send(player, messagePath);
        instance.getMessageManager().send(player, "input.cancel-hint");
        player.sendMessage("");
        pendingInputs.put(player.getUniqueId(), onInput);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (pendingInputs.containsKey(player.getUniqueId())) {
            event.setCancelled(true);

            String input = PlainTextComponentSerializer.plainText().serialize(event.message());
            Consumer<String> action = pendingInputs.remove(player.getUniqueId());

            if (input.equalsIgnoreCase("cancel")) {
                instance.getMessageManager().send(player, "input.cancelled");
                return;
            }

            Bukkit.getScheduler().runTask(instance, () -> action.accept(input));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pendingInputs.remove(event.getPlayer().getUniqueId());
    }
}
