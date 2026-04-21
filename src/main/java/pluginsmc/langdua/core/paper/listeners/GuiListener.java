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
import pluginsmc.langdua.core.paper.guis.CinematicTimelineGUI;
import pluginsmc.langdua.core.paper.guis.CinematicTrackGUI;
import pluginsmc.langdua.core.paper.guis.CommandEditorGUI;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;
import pluginsmc.langdua.core.paper.objects.TimelineClip;
import pluginsmc.langdua.core.paper.objects.TransitionEffect;
import pluginsmc.langdua.core.paper.objects.TransitionMetadata;

public class GuiListener implements Listener {

    private final Core instance;

    public GuiListener(Core instance) {
        this.instance = instance;
    }

    private String[] splitNamedPage(String rawTitle) {
        return rawTitle.split(" - P");
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
        else if (title.startsWith("Cine: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

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
            } else if (itemName.equals("Tracks & Timeline")) {
                player.openInventory(new CinematicTrackGUI(instance).getTrackGUI(cinematic));
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
        }
        else if (title.startsWith("Frames: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            String rawTitle = title.replace("Frames: ", "");
            String[] parts = splitNamedPage(rawTitle);
            if (parts.length != 2) return;
            int page;
            try { page = Integer.parseInt(parts[1]); } catch (NumberFormatException e) { return; }

            String[] nameTrack = parts[0].split(" - T", 2);
            String cinematicName = nameTrack[0];
            String trackId = nameTrack.length == 2 ? nameTrack[1] : Cinematic.DEFAULT_TRACK_ID;

            Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
            if (cinematic == null) return;
            java.util.List<Frame> frames = cinematic.getOrCreateTrack(trackId).getFrames();

            if (clicked.getType() == Material.ARROW) {
                String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                if (itemName.equals("Next Page")) {
                    player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, trackId, page + 1));
                } else if (itemName.equals("Previous Page")) {
                    player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, trackId, page - 1));
                }
                return;
            }

            if (clicked.getType() == Material.OAK_DOOR) {
                player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
                return;
            }

            if (clicked.getType() == Material.REPEATER) {
                player.openInventory(new CinematicTrackGUI(instance).getTrackGUI(cinematic));
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
                    Frame frame = frames.get(frameIndex);
                    World world = Bukkit.getWorld(frame.getWorld());
                    if (world != null) {
                        Location loc = new Location(world, frame.getX(), frame.getY(), frame.getZ(), frame.getYaw(), frame.getPitch());
                        player.teleport(loc);
                        player.sendMessage(ChatColor.GREEN + "Teleported to frame " + frameIndex + ".");
                    }
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, trackId, page, frameIndex));
                } else if (event.isRightClick()) {
                    if (frameIndex >= 0 && frameIndex < frames.size()) {
                        frames.remove(frameIndex);
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        player.sendMessage(ChatColor.RED + "Deleted frame " + frameIndex);
                        player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, trackId, page));
                    }
                }
            }
        }
        else if (title.startsWith("Tracks: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            String cinematicName = title.replace("Tracks: ", "");
            Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
            if (cinematic == null) return;

            if (clicked.getType() == Material.OAK_DOOR) {
                player.openInventory(new CinematicDashboardGUI(instance).getDashboardGUI(cinematic));
                return;
            }
            if (clicked.getType() == Material.REPEATER) {
                player.openInventory(new CinematicTimelineGUI(instance).getTimelineGUI(cinematic));
                return;
            }
            if (clicked.getType() == Material.CHEST_MINECART) {
                cinematic.getTimeline().add(new TimelineClip(Cinematic.DEFAULT_TRACK_ID));
                instance.getStorageManager().save(instance.getGame().getCinematics());
                player.openInventory(new CinematicTrackGUI(instance).getTrackGUI(cinematic));
                return;
            }
            if (clicked.getType() == Material.WRITABLE_BOOK) {
                player.closeInventory();
                instance.getChatInputManager().requestInput(player, "input.prompt-cmd", input -> {
                    cinematic.getOrCreateTrack(input);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    player.openInventory(new CinematicTrackGUI(instance).getTrackGUI(cinematic));
                });
                return;
            }
            if (clicked.getType() == Material.CLOCK) {
                player.closeInventory();
                instance.getChatInputManager().requestInput(player, "input.prompt-duration", input -> {
                    String[] split = input.split("\\s+");
                    if (split.length != 2) {
                        instance.getMessageManager().send(player, "error.invalid-number");
                        player.openInventory(new CinematicTrackGUI(instance).getTrackGUI(cinematic));
                        return;
                    }
                    try {
                        cinematic.getOrCreateTrack(split[0]).setDurationTicks(Integer.parseInt(split[1]) * 20);
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                    } catch (NumberFormatException ex) {
                        instance.getMessageManager().send(player, "error.invalid-number");
                    }
                    player.openInventory(new CinematicTrackGUI(instance).getTrackGUI(cinematic));
                });
                return;
            }
            if (clicked.getType() == Material.FILLED_MAP) {
                String trackId = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                if (event.isShiftClick() && event.isLeftClick()) {
                    cinematic.getTimeline().clear();
                    cinematic.getTimeline().add(new TimelineClip(trackId));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    player.openInventory(new CinematicTrackGUI(instance).getTrackGUI(cinematic));
                } else if (event.isRightClick()) {
                    cinematic.getTimeline().add(new TimelineClip(trackId));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    player.openInventory(new CinematicTrackGUI(instance).getTrackGUI(cinematic));
                } else {
                    player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, trackId, 1));
                }
                return;
            }
        }
        else if (title.startsWith("Timeline: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            String cinematicName = title.replace("Timeline: ", "");
            Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
            if (cinematic == null) return;

            if (clicked.getType() == Material.OAK_DOOR) {
                player.openInventory(new CinematicTrackGUI(instance).getTrackGUI(cinematic));
                return;
            }
            if (clicked.getType() == Material.BARRIER) {
                cinematic.getTimeline().clear();
                cinematic.getTimeline().add(new TimelineClip(Cinematic.DEFAULT_TRACK_ID));
                instance.getStorageManager().save(instance.getGame().getCinematics());
                player.openInventory(new CinematicTimelineGUI(instance).getTimelineGUI(cinematic));
                return;
            }
            if (clicked.getType() == Material.REPEATER) {
                int clipIndex = event.getSlot();
                if (clipIndex < 0 || clipIndex >= cinematic.getTimeline().size()) return;
                TimelineClip clip = cinematic.getTimeline().get(clipIndex);
                if (event.isShiftClick() && event.isRightClick()) {
                    if (cinematic.getTimeline().size() > 1) {
                        cinematic.getTimeline().remove(clipIndex);
                    }
                } else if (event.isRightClick()) {
                    player.closeInventory();
                    instance.getChatInputManager().requestInput(player, "input.prompt-duration", input -> {
                        String[] split = input.split("\\s+");
                        try {
                            int ticks = Integer.parseInt(split[0]);
                            int strength = split.length > 1 ? Integer.parseInt(split[1]) : 1;
                            TransitionMetadata transition = clip.getTransition();
                            transition.setEffect(TransitionEffect.DARKEN_FADE);
                            transition.setDurationTicks(ticks);
                            transition.setStrength(strength);
                            instance.getStorageManager().save(instance.getGame().getCinematics());
                        } catch (Exception ex) {
                            instance.getMessageManager().send(player, "error.invalid-number");
                        }
                        player.openInventory(new CinematicTimelineGUI(instance).getTimelineGUI(cinematic));
                    });
                    return;
                } else {
                    TransitionMetadata transition = clip.getTransition();
                    if (transition.getEffect() == TransitionEffect.DARKEN_FADE) {
                        clip.setTransition(new TransitionMetadata());
                    } else {
                        transition.setEffect(TransitionEffect.DARKEN_FADE);
                        transition.setDurationTicks(Math.max(20, transition.getDurationTicks()));
                    }
                }
                instance.getStorageManager().save(instance.getGame().getCinematics());
                player.openInventory(new CinematicTimelineGUI(instance).getTimelineGUI(cinematic));
                return;
            }
        }
        else if (title.startsWith("Cmds: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            String rawTitle = title.replace("Cmds: ", "");
            String[] splitPage = rawTitle.split(" - P");
            if (splitPage.length != 2) return;

            int page;
            try { page = Integer.parseInt(splitPage[1]); } catch (Exception e) { return; }

            String[] splitTrackFrame = splitPage[0].split(" - T", 2);
            if (splitTrackFrame.length != 2) return;
            String cinematicName = splitTrackFrame[0];
            String[] splitFrame = splitTrackFrame[1].split(" - F", 2);
            if (splitFrame.length != 2) return;

            String trackId = splitFrame[0];
            int frameIndex;
            try { frameIndex = Integer.parseInt(splitFrame[1]); } catch (Exception e) { return; }

            Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
            if (cinematic == null) return;

            Frame frame = cinematic.getOrCreateTrack(trackId).getFrames().get(frameIndex);

            if (clicked.getType() == Material.ARROW) {
                player.openInventory(new CinematicFrameGUI(instance).getCinematicFrameGUI(player, cinematic, trackId, page));
                return;
            }

            if (clicked.getType() == Material.COMMAND_BLOCK && event.isRightClick()) {
                int cmdIndex = event.getSlot();
                if (cmdIndex >= 0 && cmdIndex < frame.getCommands().size()) {
                    String removed = frame.getCommands().remove(cmdIndex);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    player.sendMessage(ChatColor.RED + "Deleted command: /" + removed);
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, trackId, page, frameIndex));
                }
            }

            if (clicked.getType() == Material.WRITABLE_BOOK) {
                player.closeInventory();
                instance.getChatInputManager().requestInput(player, "input.prompt-cmd", input -> {
                    frame.getCommands().add(input);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    instance.getMessageManager().send(player, "edit.cmd-added", "cmd", input);
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, trackId, page, frameIndex));
                });
            } else if (clicked.getType() == Material.NAME_TAG) {
                player.closeInventory();
                if (event.isLeftClick()) {
                    instance.getChatInputManager().requestInput(player, "input.prompt-title", input -> {
                        frame.setTitle(input);
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        instance.getMessageManager().send(player, "edit.title-updated");
                        player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, trackId, page, frameIndex));
                    });
                } else if (event.isRightClick()) {
                    instance.getChatInputManager().requestInput(player, "input.prompt-subtitle", input -> {
                        frame.setSubtitle(input);
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        instance.getMessageManager().send(player, "edit.subtitle-updated");
                        player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, trackId, page, frameIndex));
                    });
                }
            } else if (clicked.getType() == Material.NOTE_BLOCK) {
                player.closeInventory();
                instance.getChatInputManager().requestInput(player, "input.prompt-sound", input -> {
                    frame.getCommands().add("playsound " + input + " master %player%");
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    instance.getMessageManager().send(player, "edit.sound-added", "sound", input);
                    player.openInventory(new CommandEditorGUI(instance).getCommandEditorGUI(cinematic, trackId, page, frameIndex));
                });
            }
        }
    }
}
