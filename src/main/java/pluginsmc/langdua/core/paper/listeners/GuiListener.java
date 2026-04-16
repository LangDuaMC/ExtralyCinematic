package pluginsmc.langdua.core.paper.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.guis.CinematicDashboardGUI;
import pluginsmc.langdua.core.paper.guis.CinematicFrameGUI;
import pluginsmc.langdua.core.paper.guis.CinematicGUI;
import pluginsmc.langdua.core.paper.guis.CommandEditorGUI;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

public class GuiListener implements Listener {

    private final Core instance;

    public GuiListener(Core instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // 1. Main Menu (Cinematics List)
        if (title.equals("Cinematics List")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            String cinematicName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);

            if (cinematic != null) {
                player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
            }
        }

        // 2. Dashboard
        else if (title.startsWith("Cine: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            String cinematicName = title.replace("Cine: ", "");
            Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
            if (cinematic == null) return;

            String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

            if (itemName.equals("Edit Frames & Commands")) {
                player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, 1));
            } else if (itemName.equals("Visualize Path")) {
                player.closeInventory();
                player.performCommand("cinematic path " + cinematic.getName());
            } else if (itemName.equals("Focus Target")) {
                if (cinematic.hasFocus()) {
                    player.performCommand("cinematic focus " + cinematic.getName() + " clear");
                } else {
                    player.performCommand("cinematic focus " + cinematic.getName() + " set");
                }
                player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
            } else if (itemName.equals("Camera Shake")) {
                double current = cinematic.getShakeIntensity();
                if (event.isLeftClick()) cinematic.setShakeIntensity(current + 1);
                else if (event.isRightClick() && current >= 1) cinematic.setShakeIntensity(current - 1);
                instance.getStorageManager().save(instance.getGame().getCinematics());
                player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
            } else if (itemName.equals("Play Cinematic")) {
                player.closeInventory();
                player.performCommand("cinematic play " + player.getName() + " " + cinematic.getName());
            } else if (itemName.equals("Delete Cinematic")) {
                player.closeInventory();
                player.performCommand("cinematic delete " + cinematic.getName());
            } else if (itemName.equals("Back")) {
                player.openInventory(new CinematicGUI(instance).getCinematicListGUI(player));
            }
        }

        // 3. Frames GUI
        else if (title.startsWith("Frames: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            String rawTitle = title.replace("Frames: ", "");
            String[] parts = rawTitle.split(" - P");
            if (parts.length != 2) return;

            String cinematicName = parts[0];
            int page;
            try { page = Integer.parseInt(parts[1]); } catch (NumberFormatException e) { return; }

            Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
            if (cinematic == null) return;

            if (clicked.getType() == Material.ARROW) {
                String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                if (itemName.equals("Next Page")) {
                    player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, page + 1));
                } else if (itemName.equals("Previous Page")) {
                    player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, page - 1));
                }
                return;
            }

            if (clicked.getType() == Material.OAK_DOOR) {
                player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
                return;
            }

            if (clicked.getType() == Material.ENDER_EYE) {
                player.closeInventory();
                player.performCommand("cinematic path " + cinematic.getName());
                return;
            }

            if (clicked.getType() == Material.PAPER) {
                String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                if (!itemName.startsWith("Frame ")) return;

                int frameIndex;
                try { frameIndex = Integer.parseInt(itemName.replace("Frame ", "")); } catch (NumberFormatException e) { return; }

                if (event.isLeftClick()) {
                    Frame frame = cinematic.getFrames().get(frameIndex);
                    World world = Bukkit.getWorld(frame.getWorld());
                    if (world != null) {
                        Location loc = new Location(world, frame.getX(), frame.getY(), frame.getZ(), frame.getYaw(), frame.getPitch());
                        player.teleport(loc);
                        player.sendMessage(ChatColor.GREEN + "Teleported to frame " + frameIndex + ".");
                    }
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, page, frameIndex));
                } else if (event.isRightClick()) {
                    if (frameIndex >= 0 && frameIndex < cinematic.getFrames().size()) {
                        cinematic.getFrames().remove(frameIndex);
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        player.sendMessage(ChatColor.RED + "Deleted frame " + frameIndex);
                        player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, page));
                    }
                }
            }
        }

        // 4. Command Editor GUI
        else if (title.startsWith("Cmds: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            String rawTitle = title.replace("Cmds: ", "");
            String[] splitPage = rawTitle.split(" - P");
            if (splitPage.length != 2) return;

            int page;
            try { page = Integer.parseInt(splitPage[1]); } catch (Exception e) { return; }

            String[] splitFrame = splitPage[0].split(" - F");
            if (splitFrame.length != 2) return;

            String cinematicName = splitFrame[0];
            int frameIndex;
            try { frameIndex = Integer.parseInt(splitFrame[1]); } catch (Exception e) { return; }

            Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
            if (cinematic == null) return;

            if (clicked.getType() == Material.ARROW) {
                player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, page));
                return;
            }

            if (clicked.getType() == Material.COMMAND_BLOCK && event.isRightClick()) {
                Frame frame = cinematic.getFrames().get(frameIndex);
                int cmdIndex = event.getSlot();
                if (cmdIndex >= 0 && cmdIndex < frame.getCommands().size()) {
                    String removed = frame.getCommands().remove(cmdIndex);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    player.sendMessage(ChatColor.RED + "Deleted command: /" + removed);
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, page, frameIndex));
                }
            }
        }
    }
}