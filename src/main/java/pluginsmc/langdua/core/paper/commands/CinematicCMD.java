package pluginsmc.langdua.core.paper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.NonNull;
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
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.MessageManager;
import pluginsmc.langdua.core.paper.guis.CinematicGUI;
import pluginsmc.langdua.core.paper.hooks.PapiHook;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.time.Duration;
import java.util.*;

@CommandAlias("cinematic")
@CommandPermission("cinematic.cmd")
public class CinematicCMD extends BaseCommand {

    private @NonNull Core instance;
    private MessageManager msg;
    private Map<UUID, Cinematic> activeRecordings = new HashMap<>();
    private Map<UUID, BukkitRunnable> activeRecordingTasks = new HashMap<>();

    public CinematicCMD(Core instance) {
        this.instance = instance;
        this.msg = instance.getMessageManager();
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

    @Subcommand("rec")
    @CommandCompletion("<name> <seconds>")
    public void rec(Player sender, String cinematic, int seconds) {
        var cinematics = instance.getGame().getCinematics();
        if (cinematics.containsKey(cinematic)) {
            msg.send(sender, "error.already-exist", "name", cinematic);
            return;
        }
        Cinematic cine = new Cinematic(cinematic);
        cinematics.put(cinematic, cine);
        new BukkitRunnable() {
            int count = 3;
            @Override
            public void run() {
                if (!sender.isOnline()) { this.cancel(); return; }
                if (count == 0) {
                    msg.sendTitle(sender, "record.title-rec", null, 0, 20, 20);
                    startTickRecord(sender, cine.getFrames(), seconds);
                    this.cancel();
                } else {
                    msg.sendTitle(sender, "record.title-count", null, 0, 20, 20, "count", String.valueOf(count));
                    count--;
                }
            }
        }.runTaskTimer(instance, 0L, 20L);
    }

    private void startTickRecord(Player player, List<Frame> frames, int seconds) {
        new BukkitRunnable() {
            int elapsedTicks = 0;
            int totalTicks = seconds * 20;
            @Override
            public void run() {
                if (!player.isOnline()) { this.cancel(); return; }
                if (elapsedTicks >= totalTicks) {
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    msg.send(player, "record.finish", "count", String.valueOf(frames.size()));
                    this.cancel();
                    return;
                }
                if (elapsedTicks % instance.getInterpolationSteps() == 0) {
                    Location loc = player.getLocation();
                    frames.add(new Frame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
                }
                if (elapsedTicks % 20 == 0) {
                    msg.sendActionBar(player, "record.actionbar-timer", "current", String.valueOf(elapsedTicks / 20), "total", String.valueOf(seconds));
                }
                elapsedTicks++;
            }
        }.runTaskTimer(instance, 0L, 1L);
    }

    @Subcommand("record start")
    @CommandCompletion("<name>")
    public void recordStart(Player player, String cinematicName) {
        if (activeRecordings.containsKey(player.getUniqueId())) {
            msg.send(player, "error.already-recording");
            return;
        }
        Cinematic cine = new Cinematic(cinematicName);
        instance.getGame().getCinematics().put(cinematicName, cine);
        activeRecordings.put(player.getUniqueId(), cine);
        msg.send(player, "record.start-free", "name", cinematicName);

        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks % instance.getInterpolationSteps() == 0) {
                    Location loc = player.getLocation();
                    cine.getFrames().add(new Frame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
                    msg.sendActionBar(player, "record.actionbar-free", "count", String.valueOf(cine.getFrames().size()));
                }
                ticks++;
            }
        };
        task.runTaskTimer(instance, 0L, 1L);
        activeRecordingTasks.put(player.getUniqueId(), task);
    }

    @Subcommand("record stop")
    public void recordStop(Player player) {
        BukkitRunnable task = activeRecordingTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
        Cinematic cine = activeRecordings.remove(player.getUniqueId());
        if (cine != null) {
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(player, "record.stop-free", "name", cine.getName(), "count", String.valueOf(cine.getFrames().size()));
        }
    }

    @Subcommand("play")
    @CommandCompletion("@players @cinematics")
    public void play(CommandSender sender, @Flags("other") Player player, String cinematicName) {
        Cinematic cine = instance.getGame().getCinematics().get(cinematicName);
        if (cine == null) { msg.send(sender, "error.not-exist", "name", cinematicName); return; }
        if (instance.getGame().getViewers().contains(player.getUniqueId())) return;

        List<Frame> frames = cine.getFrames();
        if (frames.size() < 2) return;

        Location originalLoc = player.getLocation().clone();
        GameMode originalGM = player.getGameMode();
        Frame f1 = frames.get(0);
        Location start = new Location(Bukkit.getWorld(f1.getWorld()), f1.getX(), f1.getY(), f1.getZ(), f1.getYaw(), f1.getPitch());

        if (!start.getChunk().isLoaded()) {
            start.getChunk().load(true);
        }

        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(start);

        ArmorStand cam = (ArmorStand) start.getWorld().spawnEntity(start, EntityType.ARMOR_STAND);
        cam.setVisible(false);
        cam.setGravity(false);
        cam.setInvulnerable(true);
        cam.addScoreboardTag("extraly_cam");

        instance.getGame().getViewers().add(player.getUniqueId());

        Bukkit.getScheduler().runTaskLater(instance, () -> {
            if (!player.isOnline()) {
                cam.remove();
                instance.getGame().getViewers().remove(player.getUniqueId());
                return;
            }

            player.setSpectatorTarget(cam);

            final int totalTicks = cine.getDuration() > 0 ? cine.getDuration() * 20 : (frames.size() - 1) * instance.getInterpolationSteps();

            new BukkitRunnable() {
                int currentTick = 0;
                int lastFrameIdx = -1;

                @Override
                public void run() {
                    if (!player.isOnline() || !instance.getGame().getViewers().contains(player.getUniqueId()) || currentTick > totalTicks) {
                        cleanup();
                        this.cancel();
                        return;
                    }

                    if (player.getSpectatorTarget() == null || !player.getSpectatorTarget().equals(cam)) {
                        player.setSpectatorTarget(cam);
                    }

                    if (currentTick == 0 && cine.getBgmSound() != null && !cine.getBgmSound().isEmpty()) {
                        player.playSound(player.getLocation(), cine.getBgmSound(), SoundCategory.MASTER, 1f, 1f);
                    }

                    boolean isLastTick = (currentTick >= totalTicks);
                    double progress = totalTicks == 0 ? 1.0 : (double) currentTick / totalTicks;
                    double eased = easeInOutSine(progress);
                    double exactSegment = eased * (frames.size() - 1);
                    int segmentIndex = (int) Math.min(exactSegment, frames.size() - 2);
                    double localT = exactSegment - segmentIndex;

                    if (isLastTick) {
                        segmentIndex = frames.size() - 2;
                        localT = 1.0;
                    }

                    // QUAN TRỌNG: Quét qua TẤT CẢ các frame đã bay qua, không bỏ sót frame nào kể cả frame cuối.
                    int currentI = isLastTick ? frames.size() - 1 : segmentIndex;

                    while (lastFrameIdx < currentI) {
                        lastFrameIdx++;
                        if (lastFrameIdx < frames.size()) {
                            Frame f = frames.get(lastFrameIdx);
                            try {
                                // 1. Xử lý Commands
                                if (f.getCommands() != null && !f.getCommands().isEmpty()) {
                                    f.getCommands().forEach(c -> {
                                        if (c == null || c.trim().isEmpty()) return;
                                        String cmd = c.replace("%player%", player.getName());
                                        if (instance.isPapiEnabled()) cmd = PapiHook.parse(player, cmd);
                                        if (cmd.startsWith("/")) cmd = cmd.substring(1);
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                                    });
                                }

                                // 2. Xử lý Titles
                                String tStr = f.getTitle();
                                String sStr = f.getSubtitle();
                                boolean hasTitle = tStr != null && !tStr.trim().isEmpty();
                                boolean hasSubtitle = sStr != null && !sStr.trim().isEmpty();

                                if (hasTitle || hasSubtitle) {
                                    if (instance.isPapiEnabled()) {
                                        tStr = hasTitle ? PapiHook.parse(player, tStr) : "";
                                        sStr = hasSubtitle ? PapiHook.parse(player, sStr) : "";
                                    }
                                    Component t = hasTitle ? MiniMessage.miniMessage().deserialize(tStr) : Component.empty();
                                    Component s = hasSubtitle ? MiniMessage.miniMessage().deserialize(sStr) : Component.empty();
                                    player.showTitle(Title.title(t, s, Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))));
                                }
                            } catch (Throwable e) {
                                instance.getLogger().warning("Loi thuc thi tinh nang o frame " + lastFrameIdx + ": " + e.getMessage());
                            }
                        }
                    }

                    if (isLastTick) {
                        cleanup();
                        this.cancel();
                        return;
                    }

                    Frame fr0 = segmentIndex > 0 ? frames.get(segmentIndex - 1) : frames.get(segmentIndex);
                    Frame fr1 = frames.get(segmentIndex);
                    Frame fr2 = frames.get(segmentIndex + 1);
                    Frame fr3 = segmentIndex < frames.size() - 2 ? frames.get(segmentIndex + 2) : fr2;

                    double x = catmullRom(fr0.getX(), fr1.getX(), fr2.getX(), fr3.getX(), localT);
                    double y = catmullRom(fr0.getY(), fr1.getY(), fr2.getY(), fr3.getY(), localT);
                    double z = catmullRom(fr0.getZ(), fr1.getZ(), fr2.getZ(), fr3.getZ(), localT);

                    float yaw = (float) catmullRom(smoothAngle(fr1.getYaw(), fr0.getYaw()), fr1.getYaw(), smoothAngle(fr1.getYaw(), fr2.getYaw()), smoothAngle(smoothAngle(fr1.getYaw(), fr2.getYaw()), fr3.getYaw()), localT);
                    float pitch = (float) catmullRom(smoothAngle(fr1.getPitch(), fr0.getPitch()), fr1.getPitch(), smoothAngle(fr1.getPitch(), fr2.getPitch()), smoothAngle(smoothAngle(fr1.getPitch(), fr2.getPitch()), fr3.getPitch()), localT);

                    if (cine.getShakeIntensity() > 0) {
                        yaw += (Math.random() - 0.5) * cine.getShakeIntensity();
                        pitch += (Math.random() - 0.5) * cine.getShakeIntensity();
                    }

                    Location targetLoc = new Location(cam.getWorld(), x, y, z, yaw, pitch);
                    if (!targetLoc.getChunk().isLoaded()) {
                        targetLoc.getChunk().load(true);
                    }
                    cam.teleport(targetLoc);

                    if (cine.getStartZoom() != 0 || cine.getEndZoom() != 0) {
                        int zoom = (int) (cine.getStartZoom() + (cine.getEndZoom() - cine.getStartZoom()) * eased);
                        if (zoom > 0) player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2, zoom - 1, false, false));
                    }

                    currentTick++;
                }

                private void cleanup() {
                    instance.getGame().getViewers().remove(player.getUniqueId());
                    if (player.isOnline()) {
                        player.setSpectatorTarget(null);
                        player.setGameMode(originalGM);
                        player.teleport(originalLoc);
                        player.removePotionEffect(PotionEffectType.SLOWNESS);
                    }
                    cam.remove();
                    msg.send(player, "play.finished");
                }
            }.runTaskTimer(instance, 0L, 1L);
        }, 10L);
    }

    @Subcommand("stop")
    @CommandCompletion("@players")
    public void stop(CommandSender sender, @Flags("other") Player player) {
        if (instance.getGame().getViewers().remove(player.getUniqueId())) {
            msg.send(sender, "play.force-stop", "player", player.getName());
        }
    }

    @Subcommand("path")
    @CommandCompletion("@cinematics")
    public void path(Player player, String cinematicName) {
        Cinematic cine = instance.getGame().getCinematics().get(cinematicName);
        if (cine == null) return;
        msg.send(player, "edit.path-visual");
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 200 || !player.isOnline()) { this.cancel(); return; }
                cine.getFrames().forEach(f -> {
                    Location l = new Location(Bukkit.getWorld(f.getWorld()), f.getX(), f.getY(), f.getZ());
                    l.getWorld().spawnParticle(org.bukkit.Particle.FLAME, l, 1, 0, 0, 0, 0);
                });
                ticks += 20;
            }
        }.runTaskTimer(instance, 0L, 20L);
    }

    @Subcommand("addframe")
    @CommandCompletion("@cinematics")
    public void addFrame(Player player, String cinematicName) {
        var cinematics = instance.getGame().getCinematics();
        Cinematic cine = cinematics.computeIfAbsent(cinematicName, Cinematic::new);
        Location l = player.getLocation();
        cine.getFrames().add(new Frame(l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch()));
        instance.getStorageManager().save(cinematics);
        msg.send(player, "edit.addframe", "name", cinematicName, "index", String.valueOf(cine.getFrames().size() - 1));
    }

    @Subcommand("addcmd")
    @CommandCompletion("@cinematics <frameIndex> <command>")
    public void addCmd(CommandSender sender, String cinematic, int index, String command) {
        Cinematic cine = instance.getGame().getCinematics().get(cinematic);
        if (cine != null && index < cine.getFrames().size()) {
            cine.getFrames().get(index).getCommands().add(command);
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.cmd-added", "cmd", command);
        }
    }

    @Subcommand("title")
    @CommandCompletion("@cinematics <frameIndex> <text...>")
    public void setTitle(CommandSender sender, String cinematic, int index, String text) {
        Cinematic cine = instance.getGame().getCinematics().get(cinematic);
        if (cine != null && index < cine.getFrames().size()) {
            cine.getFrames().get(index).setTitle(text);
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.title-updated");
        }
    }

    @Subcommand("delete")
    @CommandCompletion("@cinematics")
    public void delete(CommandSender sender, String cinematic) {
        if (instance.getGame().getCinematics().remove(cinematic) != null) {
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.delete", "name", cinematic);
        }
    }

    @Subcommand("list")
    public void list(CommandSender sender) {
        msg.send(sender, "list.header");
        instance.getGame().getCinematics().keySet().forEach(n -> msg.send(sender, "list.item", "name", n));
    }

    @Subcommand("edit")
    public void edit(Player player) {
        player.openInventory(new CinematicGUI(instance).getCinematicListGUI(player));
    }

    @Subcommand("reload")
    @CommandPermission("cinematic.admin")
    public void reload(CommandSender sender) {
        instance.reloadPlugin();
        msg.send(sender, "admin.reload");
    }
}