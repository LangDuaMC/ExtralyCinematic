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

import java.util.List;

public class GuiListener implements Listener {
    private final Core instance;

    public GuiListener(Core instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();

        if (title.equals("Cinematics List")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }

            String cinematicName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
            if (cinematic != null) {
                player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
            }
            return;
        }

        if (title.startsWith("Cine: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                return;
            }

            String cinematicName = title.replace("Cine: ", "");
            Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
            if (cinematic == null) {
                return;
            }

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
            } else if (itemName.equals("Duration")) {
                player.closeInventory();
                instance.getChatInputManager().requestInput(player, "input.prompt-duration", input -> {
                    try {
                        cinematic.setDuration(Integer.parseInt(input));
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        instance.getMessageManager().send(player, "edit.generic-updated");
                    } catch (NumberFormatException e) {
                        instance.getMessageManager().send(player, "error.invalid-number");
                    }
                    player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
                });
            } else if (itemName.equals("Background Music (BGM)")) {
                if (event.isLeftClick()) {
                    player.closeInventory();
                    instance.getChatInputManager().requestInput(player, "input.prompt-bgm", input -> {
                        cinematic.setBgmSound(input);
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        instance.getMessageManager().send(player, "edit.generic-updated");
                        player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
                    });
                } else if (event.isRightClick()) {
                    cinematic.setBgmSound(null);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
                }
            } else if (itemName.equals("Dolly Zoom (FOV)")) {
                player.closeInventory();
                if (event.isLeftClick()) {
                    instance.getChatInputManager().requestInput(player, "input.prompt-zoom-start", input -> {
                        try {
                            cinematic.setStartZoom(Integer.parseInt(input));
                            instance.getStorageManager().save(instance.getGame().getCinematics());
                            instance.getMessageManager().send(player, "edit.generic-updated");
                        } catch (Exception e) {
                            instance.getMessageManager().send(player, "error.invalid-number");
                        }
                        player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
                    });
                } else if (event.isRightClick()) {
                    instance.getChatInputManager().requestInput(player, "input.prompt-zoom-end", input -> {
                        try {
                            cinematic.setEndZoom(Integer.parseInt(input));
                            instance.getStorageManager().save(instance.getGame().getCinematics());
                            instance.getMessageManager().send(player, "edit.generic-updated");
                        } catch (Exception e) {
                            instance.getMessageManager().send(player, "error.invalid-number");
                        }
                        player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
                    });
                }
            } else if (itemName.equals("Camera Shake")) {
                player.closeInventory();
                instance.getChatInputManager().requestInput(player, "input.prompt-shake", input -> {
                    try {
                        cinematic.setShakeIntensity(Double.parseDouble(input));
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        instance.getMessageManager().send(player, "edit.generic-updated");
                    } catch (NumberFormatException e) {
                        instance.getMessageManager().send(player, "error.invalid-number");
                    }
                    player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
                });
            } else if (itemName.equals("Play Cinematic")) {
                player.closeInventory();
                player.performCommand("cinematic play " + player.getName() + " " + cinematic.getName());
            } else if (itemName.equals("Delete Cinematic")) {
                player.closeInventory();
                player.performCommand("cinematic delete " + cinematic.getName());
            } else if (itemName.equals("Back")) {
                player.openInventory(new CinematicGUI(instance).getCinematicListGUI(player));
            }
            return;
        }

        if (title.startsWith("Frames: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }

            String rawTitle = title.replace("Frames: ", "");
            String[] parts = rawTitle.split(" - P");
            if (parts.length != 2) {
                return;
            }

            int page;
            try {
                page = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return;
            }

            Cinematic cinematic = instance.getGame().getCinematics().get(parts[0]);
            if (cinematic == null) {
                return;
            }
            List<Frame> frames = cinematic.getFrames();

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
                if (!itemName.startsWith("Frame ")) {
                    return;
                }

                int frameIndex;
                try {
                    frameIndex = Integer.parseInt(itemName.replace("Frame ", ""));
                } catch (NumberFormatException e) {
                    return;
                }

                if (event.isLeftClick()) {
                    Frame frame = frames.get(frameIndex);
                    World world = Bukkit.getWorld(frame.getWorld());
                    if (world != null) {
                        Location loc = new Location(world, frame.getX(), frame.getY(), frame.getZ(), frame.getYaw(), frame.getPitch());
                        player.teleport(loc);
                        player.sendMessage(ChatColor.GREEN + "Teleported to frame " + frameIndex + ".");
                    }
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, page, frameIndex));
                } else if (event.isRightClick() && frameIndex >= 0 && frameIndex < frames.size()) {
                    frames.remove(frameIndex);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    player.sendMessage(ChatColor.RED + "Deleted frame " + frameIndex);
                    player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, page));
                }
            }
            return;
        }

        if (title.startsWith("Cmds: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }

            String rawTitle = title.replace("Cmds: ", "");
            String[] splitPage = rawTitle.split(" - P");
            if (splitPage.length != 2) {
                return;
            }

            int page;
            try {
                page = Integer.parseInt(splitPage[1]);
            } catch (Exception e) {
                return;
            }

            String[] splitFrame = splitPage[0].split(" - F");
            if (splitFrame.length != 2) {
                return;
            }

            Cinematic cinematic = instance.getGame().getCinematics().get(splitFrame[0]);
            if (cinematic == null) {
                return;
            }

            int frameIndex;
            try {
                frameIndex = Integer.parseInt(splitFrame[1]);
            } catch (Exception e) {
                return;
            }

            Frame frame = cinematic.getFrames().get(frameIndex);

            if (clicked.getType() == Material.ARROW) {
                player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, page));
                return;
            }

            if (clicked.getType() == Material.COMMAND_BLOCK && event.isRightClick()) {
                int cmdIndex = event.getSlot();
                if (cmdIndex >= 0 && cmdIndex < frame.getCommands().size()) {
                    String removed = frame.getCommands().remove(cmdIndex);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    player.sendMessage(ChatColor.RED + "Deleted command: /" + removed);
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, page, frameIndex));
                }
                return;
            }

            if (clicked.getType() == Material.WRITABLE_BOOK) {
                player.closeInventory();
                instance.getChatInputManager().requestInput(player, "input.prompt-cmd", input -> {
                    frame.getCommands().add(input);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    instance.getMessageManager().send(player, "edit.cmd-added", "cmd", input);
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, page, frameIndex));
                });
            } else if (clicked.getType() == Material.NAME_TAG) {
                player.closeInventory();
                if (event.isLeftClick()) {
                    instance.getChatInputManager().requestInput(player, "input.prompt-title", input -> {
                        frame.setTitle(input);
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        instance.getMessageManager().send(player, "edit.title-updated");
                        player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, page, frameIndex));
                    });
                } else if (event.isRightClick()) {
                    instance.getChatInputManager().requestInput(player, "input.prompt-subtitle", input -> {
                        frame.setSubtitle(input);
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        instance.getMessageManager().send(player, "edit.subtitle-updated");
                        player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, page, frameIndex));
                    });
                }
            } else if (clicked.getType() == Material.NOTE_BLOCK) {
                player.closeInventory();
                instance.getChatInputManager().requestInput(player, "input.prompt-sound", input -> {
                    frame.getCommands().add("playsound " + input + " master %player%");
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    instance.getMessageManager().send(player, "edit.sound-added", "sound", input);
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, page, frameIndex));
                });
            }
        }
    }
}
