package pluginsmc.langdua.core.paper.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.hooks.PapiHook;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.time.Duration;

public class PlayManager {
    private final Core instance;

    public PlayManager(Core instance) {
        this.instance = instance;
    }

    private double catmullRom(double p0, double p1, double p2, double p3, double t) {
        return 0.5 * ((2 * p1) + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t + (-p0 + 3 * p1 - 3 * p2 + p3) * t * t * t);
    }

    private float smoothAngle(float prevAngle, float currentAngle) {
        float diff = currentAngle - prevAngle;
        while (diff < -180.0f) diff += 360.0f;
        while (diff > 180.0f) diff -= 360.0f;
        return prevAngle + diff;
    }

    private double easeInOutSine(double x) {
        return -(Math.cos(Math.PI * x) - 1) / 2;
    }

    public void play(CommandSender sender, Player player, String cinematic) {
        var game = instance.getGame();
        var msg = instance.getMessageManager();

        if (game.getViewers().contains(player.getUniqueId())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player is already watching a cinematic!</red>"));
            return;
        }

        var cinematics = game.getCinematics();
        if (!cinematics.containsKey(cinematic)) {
            msg.send(sender, "error.not-exist", "name", cinematic);
            return;
        }

        var cine = cinematics.get(cinematic);
        var frames = cine.getFrames();
        if (frames.size() < 2) return;

        var originalLoc = player.getLocation().clone();
        var originalGameMode = player.getGameMode();
        var firstFrame = frames.get(0);
        var world = Bukkit.getWorld(firstFrame.getWorld());
        if (world == null) return;

        var startLoc = new Location(world, firstFrame.getX(), firstFrame.getY(), firstFrame.getZ(), firstFrame.getYaw(), firstFrame.getPitch());

        player.setGameMode(GameMode.SPECTATOR);
        game.getViewers().add(player.getUniqueId());
        startLoc.getChunk().load();

        player.teleportAsync(startLoc).thenAccept(success -> {
            Bukkit.getScheduler().runTaskLater(instance, () -> {
                if (!player.isOnline() || !game.getViewers().contains(player.getUniqueId())) return;

                ArmorStand cam = (ArmorStand) world.spawnEntity(startLoc, EntityType.ARMOR_STAND);
                cam.setVisible(false); cam.setGravity(false); cam.setInvulnerable(true); cam.setBasePlate(false);
                player.setSpectatorTarget(cam);

                final int steps = instance.getInterpolationSteps();
                final int totalFrames = frames.size();
                final int totalTicks = cine.getDuration() > 0 ? (cine.getDuration() * 20) : ((totalFrames - 1) * steps);
                int[] lastPassedFrame = {-1};

                new BukkitRunnable() {
                    int currentTick = 0;
                    @Override
                    public void run() {
                        if (!game.getViewers().contains(player.getUniqueId()) || !player.isOnline()) {
                            cleanup(); this.cancel(); return;
                        }

                        if (currentTick == 0 && cine.getBgmSound() != null && !cine.getBgmSound().isEmpty()) {
                            player.playSound(player.getLocation(), cine.getBgmSound(), SoundCategory.MASTER, 1.0f, 1.0f);
                        }

                        boolean isLastTick = (currentTick >= totalTicks);
                        double progress = totalTicks == 0 ? 1.0 : (double) currentTick / totalTicks;
                        double easedProgress = easeInOutSine(progress);
                        double exactSegment = easedProgress * (totalFrames - 1);
                        int segmentIndex = (int) Math.min(exactSegment, totalFrames - 2);
                        double localT = exactSegment - segmentIndex;

                        if (isLastTick) { segmentIndex = totalFrames - 2; localT = 1.0; }

                        int currentI = segmentIndex + 1;
                        while (lastPassedFrame[0] < currentI) {
                            lastPassedFrame[0]++;
                            if (lastPassedFrame[0] < frames.size()) {
                                Frame fCmd = frames.get(lastPassedFrame[0]);
                                if (!fCmd.getCommands().isEmpty()) {
                                    for (String cmd : fCmd.getCommands()) {
                                        String finalCmd = cmd.replace("%player%", player.getName());
                                        if (instance.isPapiEnabled()) finalCmd = PapiHook.parse(player, finalCmd);
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                                    }
                                }
                                boolean hasTitle = fCmd.getTitle() != null && !fCmd.getTitle().isEmpty();
                                boolean hasSub = fCmd.getSubtitle() != null && !fCmd.getSubtitle().isEmpty();
                                if (hasTitle || hasSub) {
                                    String rawTitle = hasTitle ? fCmd.getTitle() : "";
                                    String rawSub = hasSub ? fCmd.getSubtitle() : "";
                                    if (instance.isPapiEnabled()) {
                                        rawTitle = PapiHook.parse(player, rawTitle);
                                        rawSub = PapiHook.parse(player, rawSub);
                                    }
                                    Component t = hasTitle ? MiniMessage.miniMessage().deserialize(rawTitle) : Component.empty();
                                    Component s = hasSub ? MiniMessage.miniMessage().deserialize(rawSub) : Component.empty();
                                    player.showTitle(Title.title(t, s, Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(1000))));
                                }
                            }
                        }

                        Frame f1 = frames.get(segmentIndex), f2 = frames.get(segmentIndex + 1);
                        Frame f0 = segmentIndex > 0 ? frames.get(segmentIndex - 1) : f1;
                        Frame f3 = segmentIndex < totalFrames - 2 ? frames.get(segmentIndex + 2) : f2;

                        org.bukkit.World worldForSegment = Bukkit.getWorld(f1.getWorld() != null ? f1.getWorld() : f2.getWorld());
                        if (worldForSegment == null) worldForSegment = world;

                        double x = catmullRom(f0.getX(), f1.getX(), f2.getX(), f3.getX(), localT);
                        double y = catmullRom(f0.getY(), f1.getY(), f2.getY(), f3.getY(), localT);
                        double z = catmullRom(f0.getZ(), f1.getZ(), f2.getZ(), f3.getZ(), localT);

                        float interpYaw, interpPitch;
                        if (cine.hasFocus() && cine.getFocusWorld() != null && cine.getFocusWorld().equals(worldForSegment.getName())) {
                            Vector direction = new Vector(cine.getFocusX() - x, cine.getFocusY() - y, cine.getFocusZ() - z);
                            Location lookLoc = new Location(worldForSegment, x, y, z);
                            if (direction.lengthSquared() > 0.0001) lookLoc.setDirection(direction);
                            interpYaw = lookLoc.getYaw(); interpPitch = lookLoc.getPitch();
                        } else {
                            interpYaw = (float) catmullRom(smoothAngle(f1.getYaw(), f0.getYaw()), f1.getYaw(), smoothAngle(f1.getYaw(), f2.getYaw()), smoothAngle(smoothAngle(f1.getYaw(), f2.getYaw()), f3.getYaw()), localT);
                            interpPitch = (float) catmullRom(smoothAngle(f1.getPitch(), f0.getPitch()), f1.getPitch(), smoothAngle(f1.getPitch(), f2.getPitch()), smoothAngle(smoothAngle(f1.getPitch(), f2.getPitch()), f3.getPitch()), localT);
                        }

                        if (cine.getShakeIntensity() > 0) {
                            interpYaw += (float) ((Math.random() - 0.5) * cine.getShakeIntensity());
                            interpPitch += (float) ((Math.random() - 0.5) * cine.getShakeIntensity());
                        }

                        cam.teleport(new Location(worldForSegment, x, y, z, interpYaw, interpPitch));

                        if (cine.getStartZoom() != 0 || cine.getEndZoom() != 0) {
                            double zoomProgress = cine.getStartZoom() + (cine.getEndZoom() - cine.getStartZoom()) * easedProgress;
                            int zoomLevel = (int) Math.round(zoomProgress);
                            if (zoomLevel > 0) player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, zoomLevel - 1, false, false, false));
                            else if (zoomLevel < 0) player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, (-zoomLevel) - 1, false, false, false));
                            else { player.removePotionEffect(PotionEffectType.SLOWNESS); player.removePotionEffect(PotionEffectType.SPEED); }
                        }

                        if (isLastTick) {
                            cleanup();
                            if (player.isOnline()) msg.send(sender, "play.finished");
                            this.cancel();
                        }
                        currentTick++;
                    }

                    private void cleanup() {
                        game.getViewers().remove(player.getUniqueId());
                        if (player.isOnline()) {
                            if (player.getGameMode() != GameMode.SPECTATOR) player.setGameMode(GameMode.SPECTATOR);
                            player.setSpectatorTarget(null);
                            player.teleport(originalLoc);
                            Bukkit.getScheduler().runTaskLater(instance, () -> {
                                if (player.isOnline()) {
                                    player.setGameMode(originalGameMode);
                                    player.removePotionEffect(PotionEffectType.SLOWNESS);
                                    player.removePotionEffect(PotionEffectType.SPEED);
                                }
                            }, 1L);
                            if (cine.getBgmSound() != null && !cine.getBgmSound().isEmpty()) {
                                try { player.stopSound(cine.getBgmSound(), SoundCategory.MASTER); } catch (Exception ignored) {}
                            }
                        }
                        if (cam.isValid()) Bukkit.getScheduler().runTaskLater(instance, () -> { if (cam.isValid()) cam.remove(); }, 2L);
                    }
                }.runTaskTimer(instance, 0L, 1L);
            }, 10L);
        });
    }

    public void forceStop(CommandSender sender, Player player) {
        if (instance.getGame().getViewers().remove(player.getUniqueId())) {
            instance.getMessageManager().send(sender, "play.force-stop", "player", player.getName());
        }
    }
}