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
import org.bukkit.util.Vector;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.MessageManager;
import pluginsmc.langdua.core.paper.guis.CinematicGUI;
import pluginsmc.langdua.core.paper.hooks.PapiHook;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    // ==========================================
    // 🎥 PHẦN 1: QUAY PHIM (RECORDING)
    // ==========================================

    private void recordLogic(Player player, List<Frame> frames, int seconds) {
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
                    Location loc = player.getLocation().clone();
                    frames.add(new Frame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
                }
                if (elapsedTicks % 20 == 0) {
                    msg.sendActionBar(player, "record.actionbar-timer", "current", String.valueOf(elapsedTicks / 20), "total", String.valueOf(seconds));
                }
                elapsedTicks++;
            }
        }.runTaskTimer(instance, 0L, 1L);
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
                    recordLogic(sender, cine.getFrames(), seconds);
                    this.cancel();
                } else {
                    msg.sendTitle(sender, "record.title-count", null, 0, 20, 20, "count", String.valueOf(count));
                    count--;
                }
            }
        }.runTaskTimer(instance, 0L, 20L);
    }

    @Subcommand("record start")
    @CommandCompletion("<name>")
    public void recordStart(Player player, String cinematicName) {
        UUID uuid = player.getUniqueId();
        if (activeRecordings.containsKey(uuid)) {
            msg.send(player, "error.already-recording");
            return;
        }
        Cinematic cine = new Cinematic(cinematicName);
        instance.getGame().getCinematics().put(cinematicName, cine);
        activeRecordings.put(uuid, cine);
        msg.send(player, "record.start-free", "name", cinematicName);

        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!player.isOnline() || !activeRecordings.containsKey(uuid)) {
                    this.cancel(); activeRecordings.remove(uuid); return;
                }
                if (ticks % instance.getInterpolationSteps() == 0) {
                    Location loc = player.getLocation();
                    cine.getFrames().add(new Frame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
                    msg.sendActionBar(player, "record.actionbar-free", "count", String.valueOf(cine.getFrames().size()));
                }
                ticks++;
            }
        };
        task.runTaskTimer(instance, 0L, 1L);
        activeRecordingTasks.put(uuid, task);
    }

    @Subcommand("record stop")
    public void recordStop(Player player) {
        UUID uuid = player.getUniqueId();
        if (!activeRecordings.containsKey(uuid)) {
            msg.send(player, "error.not-recording");
            return;
        }
        activeRecordingTasks.get(uuid).cancel();
        activeRecordingTasks.remove(uuid);
        Cinematic cine = activeRecordings.remove(uuid);
        instance.getStorageManager().save(instance.getGame().getCinematics());
        msg.send(player, "record.stop-free", "name", cine.getName(), "count", String.valueOf(cine.getFrames().size()));
    }

    // ==========================================
    // 🎬 PHẦN 2: PHÁT PHIM (PLAYING)
    // ==========================================

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

    @Subcommand("play")
    @CommandCompletion("@players @cinematics")
    public void play(CommandSender sender, @Flags("other") Player player, String cinematic) {
        var game = instance.getGame();
        if (!game.getCinematics().containsKey(cinematic)) {
            msg.send(sender, "error.not-exist", "name", cinematic);
            return;
        }
        if (game.getViewers().contains(player.getUniqueId())) return;

        Cinematic cine = game.getCinematics().get(cinematic);
        List<Frame> frames = cine.getFrames();
        if (frames.size() < 2) return;

        Location originalLoc = player.getLocation().clone();
        GameMode originalGM = player.getGameMode();
        Frame first = frames.get(0);
        Location start = new Location(Bukkit.getWorld(first.getWorld()), first.getX(), first.getY(), first.getZ(), first.getYaw(), first.getPitch());

        ArmorStand cam = (ArmorStand) start.getWorld().spawnEntity(start, EntityType.ARMOR_STAND);
        cam.setVisible(false); cam.setGravity(false); cam.setInvulnerable(true);

        player.setGameMode(GameMode.SPECTATOR);
        game.getViewers().add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(instance, () -> player.setSpectatorTarget(cam), 1L);

        final int steps = instance.getInterpolationSteps();
        final int totalTicks = cine.getDuration() > 0 ? (cine.getDuration() * 20) : ((frames.size() - 1) * steps);

        new BukkitRunnable() {
            int currentTick = 0;
            int lastFrameIdx = -1;

            @Override
            public void run() {
                if (!player.isOnline() || !game.getViewers().contains(player.getUniqueId())) {
                    cleanup(); this.cancel(); return;
                }

                if (currentTick == 0 && cine.getBgmSound() != null) {
                    player.playSound(player.getLocation(), cine.getBgmSound(), SoundCategory.MASTER, 1f, 1f);
                }

                double progress = (double) currentTick / totalTicks;
                double eased = easeInOutSine(progress);
                double exactSegment = eased * (frames.size() - 1);
                int segmentIndex = (int) Math.min(exactSegment, frames.size() - 2);
                double localT = exactSegment - segmentIndex;

                // Xử lý Command/Title theo từng frame
                if (segmentIndex > lastFrameIdx) {
                    lastFrameIdx = segmentIndex;
                    Frame f = frames.get(segmentIndex);
                    f.getCommands().forEach(c -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", player.getName())));
                    if (!f.getTitle().isEmpty() || !f.getSubtitle().isEmpty()) {
                        Component t = MiniMessage.miniMessage().deserialize(f.getTitle());
                        Component s = MiniMessage.miniMessage().deserialize(f.getSubtitle());
                        player.showTitle(Title.title(t, s, Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))));
                    }
                }

                Frame f1 = frames.get(segmentIndex), f2 = frames.get(segmentIndex + 1);
                Frame f0 = segmentIndex > 0 ? frames.get(segmentIndex - 1) : f1;
                Frame f3 = segmentIndex < frames.size() - 2 ? frames.get(segmentIndex + 2) : f2;

                double x = catmullRom(f0.getX(), f1.getX(), f2.getX(), f3.getX(), localT);
                double y = catmullRom(f0.getY(), f1.getY(), f2.getY(), f3.getY(), localT);
                double z = catmullRom(f0.getZ(), f1.getZ(), f2.getZ(), f3.getZ(), localT);

                float yaw = (float) catmullRom(smoothAngle(f1.getYaw(), f0.getYaw()), f1.getYaw(), smoothAngle(f1.getYaw(), f2.getYaw()), smoothAngle(smoothAngle(f1.getYaw(), f2.getYaw()), f3.getYaw()), localT);
                float pitch = (float) catmullRom(smoothAngle(f1.getPitch(), f0.getPitch()), f1.getPitch(), smoothAngle(f1.getPitch(), f2.getPitch()), smoothAngle(smoothAngle(f1.getPitch(), f2.getPitch()), f3.getPitch()), localT);

                if (cine.getShakeIntensity() > 0) {
                    yaw += (Math.random() - 0.5) * cine.getShakeIntensity();
                    pitch += (Math.random() - 0.5) * cine.getShakeIntensity();
                }

                cam.teleport(new Location(cam.getWorld(), x, y, z, yaw, pitch));

                if (currentTick >= totalTicks) { cleanup(); this.cancel(); return; }
                currentTick++;
            }

            private void cleanup() {
                game.getViewers().remove(player.getUniqueId());
                if (player.isOnline()) {
                    player.setSpectatorTarget(null);
                    player.setGameMode(originalGM);
                    player.teleport(originalLoc);
                }
                cam.remove();
                msg.send(player, "play.finished");
            }
        }.runTaskTimer(instance, 0L, 1L);
    }

    @Subcommand("stop")
    @CommandCompletion("@players")
    public void stop(CommandSender sender, @Flags("other") Player player) {
        if (instance.getGame().getViewers().remove(player.getUniqueId())) {
            msg.send(sender, "play.force-stop", "player", player.getName());
        }
    }

    // ==========================================
    // ⚙️ PHẦN 3: CHỈNH SỬA (EDITING)
    // ==========================================

    @Subcommand("addframe")
    @CommandCompletion("@cinematics")
    public void addFrame(Player player, String cinematicName) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) cinematics.put(cinematicName, new Cinematic(cinematicName));
        Cinematic cine = cinematics.get(cinematicName);
        Location loc = player.getLocation();
        cine.getFrames().add(new Frame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
        instance.getStorageManager().save(cinematics);
        msg.send(player, "edit.addframe", "name", cinematicName, "index", String.valueOf(cine.getFrames().size() - 1));
    }

    @Subcommand("duration")
    @CommandCompletion("@cinematics <seconds>")
    public void duration(Player player, String cinematicName, int seconds) {
        Cinematic cine = instance.getGame().getCinematics().get(cinematicName);
        if (cine == null) return;
        cine.setDuration(seconds);
        instance.getStorageManager().save(instance.getGame().getCinematics());
        msg.send(player, "edit.duration", "name", cinematicName, "val", String.valueOf(seconds));
    }

    @Subcommand("focus")
    @CommandCompletion("@cinematics set|clear")
    public void focus(Player player, String cinematicName, String action) {
        Cinematic cine = instance.getGame().getCinematics().get(cinematicName);
        if (cine == null) return;
        if (action.equalsIgnoreCase("clear")) cine.clearFocus();
        else cine.setFocus(player.getWorld().getName(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        instance.getStorageManager().save(instance.getGame().getCinematics());
    }

    @Subcommand("shake")
    @CommandCompletion("@cinematics <intensity>")
    public void shake(Player player, String cinematicName, double intensity) {
        Cinematic cine = instance.getGame().getCinematics().get(cinematicName);
        if (cine == null) return;
        cine.setShakeIntensity(intensity);
        instance.getStorageManager().save(instance.getGame().getCinematics());
    }

    @Subcommand("bgm")
    @CommandCompletion("@cinematics <sound|clear>")
    public void bgm(Player player, String cinematicName, String sound) {
        Cinematic cine = instance.getGame().getCinematics().get(cinematicName);
        if (cine == null) return;
        cine.setBgmSound(sound.equalsIgnoreCase("clear") ? null : sound);
        instance.getStorageManager().save(instance.getGame().getCinematics());
    }

    @Subcommand("delete")
    @CommandCompletion("@cinematics")
    public void delete(CommandSender sender, String cinematic) {
        if (instance.getGame().getCinematics().remove(cinematic) != null) {
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.delete", "name", cinematic);
        }
    }

    @Subcommand("edit")
    public void edit(Player player) {
        player.openInventory(new CinematicGUI(instance).getCinematicListGUI(player));
    }
}