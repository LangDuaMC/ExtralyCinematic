package pluginsmc.langdua.core.paper.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.guis.CinematicFrameGUI;
import pluginsmc.langdua.core.paper.guis.CommandEditorGUI;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

public class GuiListener implements Listener {
    private final Core instance;

    public GuiListener(Core instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();

        if (title.startsWith("Cmds: ")) {
            event.setCancelled(true);

            String[] parts = title.replace("Cmds: ", "").split(" - ");
            String cineName = parts[0];
            int frameIndex = Integer.parseInt(parts[1].replace("F", ""));
            int page = Integer.parseInt(parts[2].replace("P", ""));

            Cinematic cine = instance.getGame().getCinematics().get(cineName);
            if (cine == null) return;
            Frame frame = cine.getFrames().get(frameIndex);

            int slot = event.getSlot();

            if (slot == 45) {
                player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cine, page));
                return;
            }

            if (slot < 45 && event.getClick().isRightClick()) {
                if (slot < frame.getCommands().size()) {
                    frame.getCommands().remove(slot);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cine, page, frameIndex));
                }
                return;
            }

            if (slot == 48) {
                instance.getChatInputManager().requestInput(player, "input.prompt-cmd", input -> {
                    frame.getCommands().add(input);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    instance.getMessageManager().send(player, "edit.cmd-added", "cmd", input);
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cine, page, frameIndex));
                });
                return;
            }

            if (slot == 49) {
                if (event.getClick().isLeftClick()) {
                    instance.getChatInputManager().requestInput(player, "input.prompt-title", input -> {
                        frame.setTitle(input);
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        instance.getMessageManager().send(player, "edit.title-updated");
                        player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cine, page, frameIndex));
                    });
                } else if (event.getClick().isRightClick()) {
                    instance.getChatInputManager().requestInput(player, "input.prompt-subtitle", input -> {
                        frame.setSubtitle(input);
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        instance.getMessageManager().send(player, "edit.subtitle-updated");
                        player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cine, page, frameIndex));
                    });
                }
                return;
            }

            if (slot == 50) {
                instance.getChatInputManager().requestInput(player, "input.prompt-sound", input -> {
                    frame.getCommands().add("playsound " + input + " master %player%");
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    instance.getMessageManager().send(player, "edit.sound-added", "sound", input);
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cine, page, frameIndex));
                });
                return;
            }
        }
    }
}