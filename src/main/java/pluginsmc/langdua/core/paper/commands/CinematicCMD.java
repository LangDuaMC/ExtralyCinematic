package pluginsmc.langdua.core.paper.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.MessageManager;
import pluginsmc.langdua.core.paper.guis.CinematicGUI;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

@CommandAlias("cinematic")
@CommandPermission("cinematic.cmd")
public class CinematicCMD extends BaseCommand {

    private @NonNull Core instance;
    private MessageManager msg;

    public CinematicCMD(Core instance) {
        this.instance = instance;
        this.msg = instance.getMessageManager();
    }

    // ==========================================
    // 🎥 PHẦN 1: GHI HÌNH (RECORDING)
    // ==========================================

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
        instance.getRecordManager().startCountdownRecord(sender, cine, seconds);
    }

    @Subcommand("record start")
    @CommandCompletion("<name>")
    public void recordStart(Player player, String cinematicName) {
        instance.getRecordManager().startFreeRecord(player, cinematicName);
    }

    @Subcommand("record stop")
    public void recordStop(Player player) {
        instance.getRecordManager().stopFreeRecord(player);
    }


    // ==========================================
    // 🎬 PHẦN 2: TRÌNH CHIẾU (PLAYING)
    // ==========================================

    @Subcommand("play")
    @CommandCompletion("@players @cinematics") // Đã thêm Auto-complete
    public void play(CommandSender sender, @Flags("other") Player player, String cinematic) {
        instance.getPlayManager().play(sender, player, cinematic);
    }

    @Subcommand("stop")
    @CommandCompletion("@players")
    public void stop(CommandSender sender, @Flags("other") Player player) {
        instance.getPlayManager().forceStop(sender, player);
    }


    // ==========================================
    // ⚙️ PHẦN 3: CHỈNH SỬA & TIỆN ÍCH (EDITING)
    // ==========================================

    @Subcommand("addframe")
    @Description("Manually adds a waypoint (keyframe) at your current location.")
    @CommandCompletion("@cinematics") // Đã thêm Auto-complete
    public void addFrame(Player player, String cinematicName) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            cinematics.put(cinematicName, new Cinematic(cinematicName));
        }
        Cinematic cine = cinematics.get(cinematicName);
        Location loc = player.getLocation();
        cine.getFrames().add(new Frame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
        instance.getStorageManager().save(cinematics);
        msg.send(player, "edit.addframe", "name", cinematicName, "index", String.valueOf(cine.getFrames().size() - 1));
    }

    @Subcommand("duration")
    @Description("Sets the total playback duration (in seconds).")
    @CommandCompletion("@cinematics <seconds>") // Đã thêm Auto-complete
    public void duration(Player player, String cinematicName, int seconds) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }
        Cinematic cine = cinematics.get(cinematicName);
        cine.setDuration(seconds);
        instance.getStorageManager().save(cinematics);
        msg.send(player, "edit.duration", "name", cinematicName, "val", String.valueOf(seconds));
    }

    @Subcommand("path")
    @CommandCompletion("@cinematics") // Đã thêm Auto-complete
    public void path(Player player, String cinematicName) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }

        var frames = cinematics.get(cinematicName).getFrames();
        if (frames.isEmpty()) return;

        msg.send(player, "edit.path-visual");

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 200 || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                for (int i = 0; i < frames.size(); i++) {
                    Frame f = frames.get(i);
                    org.bukkit.World world = Bukkit.getWorld(f.getWorld());
                    if (world == null) continue;
                    Location loc = new Location(world, f.getX(), f.getY(), f.getZ());
                    world.spawnParticle(org.bukkit.Particle.FLAME, loc, 1, 0, 0, 0, 0);
                    if (i % 10 == 0 || i == frames.size() - 1) {
                        Location dirLoc = loc.clone();
                        dirLoc.setYaw(f.getYaw());
                        dirLoc.setPitch(f.getPitch());
                        Vector dir = dirLoc.getDirection().multiply(0.5);
                        world.spawnParticle(org.bukkit.Particle.END_ROD, loc.add(dir), 1, 0, 0, 0, 0);
                    }
                }
                ticks += 5;
            }
        }.runTaskTimer(instance, 0L, 5L);
    }

    @Subcommand("focus")
    @CommandCompletion("@cinematics set|clear") // Đã thêm Auto-complete
    public void focus(Player player, String cinematicName, String action) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }
        Cinematic cine = cinematics.get(cinematicName);
        if (action.equalsIgnoreCase("clear")) {
            cine.clearFocus();
            msg.send(player, "edit.focus-clear", "name", cinematicName);
        } else if (action.equalsIgnoreCase("set")) {
            Location loc = player.getLocation();
            cine.setFocus(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
            msg.send(player, "edit.focus-set", "name", cinematicName);
        }
        instance.getStorageManager().save(cinematics);
    }

    @Subcommand("shake")
    @CommandCompletion("@cinematics <intensity>") // Đã thêm Auto-complete
    public void shake(Player player, String cinematicName, double intensity) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }
        cinematics.get(cinematicName).setShakeIntensity(intensity);
        instance.getStorageManager().save(cinematics);
        msg.send(player, "edit.shake-set", "name", cinematicName, "val", String.valueOf(intensity));
    }

    @Subcommand("zoom")
    @CommandCompletion("@cinematics <start> <end>") // Đã thêm Auto-complete
    public void zoom(Player player, String cinematicName, int startZoom, int endZoom) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }
        Cinematic cine = cinematics.get(cinematicName);
        cine.setStartZoom(startZoom);
        cine.setEndZoom(endZoom);
        instance.getStorageManager().save(cinematics);
        msg.send(player, "edit.zoom-set", "name", cinematicName, "start", String.valueOf(startZoom), "end", String.valueOf(endZoom));
    }

    @Subcommand("bgm")
    @CommandCompletion("@cinematics <sound_string|clear>") // Đã thêm Auto-complete
    public void bgm(Player player, String cinematicName, String sound) {
        var cinematics = instance.getGame().getCinematics();
        if (!cinematics.containsKey(cinematicName)) {
            msg.send(player, "error.not-exist", "name", cinematicName);
            return;
        }
        Cinematic cine = cinematics.get(cinematicName);
        if (sound.equalsIgnoreCase("clear")) {
            cine.setBgmSound(null);
            msg.send(player, "edit.bgm-clear", "name", cinematicName);
        } else {
            cine.setBgmSound(sound);
            msg.send(player, "edit.bgm-set", "name", cinematicName, "val", sound);
        }
        instance.getStorageManager().save(cinematics);
    }

    @Subcommand("title")
    @CommandCompletion("@cinematics <frameIndex> <text...>") // Đã thêm Auto-complete
    public void setTitle(CommandSender sender, String cinematic, int frameIndex, String text) {
        var cine = instance.getGame().getCinematics().get(cinematic);
        if (cine != null && frameIndex >= 0 && frameIndex < cine.getFrames().size()) {
            cine.getFrames().get(frameIndex).setTitle(text);
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.title-set", "frame", String.valueOf(frameIndex));
        }
    }

    @Subcommand("subtitle")
    @CommandCompletion("@cinematics <frameIndex> <text...>") // Đã thêm Auto-complete
    public void setSubtitle(CommandSender sender, String cinematic, int frameIndex, String text) {
        var cine = instance.getGame().getCinematics().get(cinematic);
        if (cine != null && frameIndex >= 0 && frameIndex < cine.getFrames().size()) {
            cine.getFrames().get(frameIndex).setSubtitle(text);
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.subtitle-set", "frame", String.valueOf(frameIndex));
        }
    }

    @Subcommand("delete")
    @CommandCompletion("@cinematics") // Đã thêm Auto-complete
    public void delete(CommandSender sender, String cinematic) {
        if (instance.getGame().getCinematics().remove(cinematic) != null) {
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.delete", "name", cinematic);
        }
    }

    @Subcommand("addcmd")
    @CommandCompletion("@cinematics <frameIndex> <command>") // Đã thêm Auto-complete
    public void addCmd(CommandSender sender, String cinematic, int frameIndex, String command) {
        var cine = instance.getGame().getCinematics().get(cinematic);
        if (cine != null && frameIndex >= 0 && frameIndex < cine.getFrames().size()) {
            cine.getFrames().get(frameIndex).getCommands().add(command);
            instance.getStorageManager().save(instance.getGame().getCinematics());
            msg.send(sender, "edit.cmd-add", "frame", String.valueOf(frameIndex));
        }
    }

    @Subcommand("list")
    public void listCinematics(CommandSender sender) {
        msg.send(sender, "list.header");
        instance.getGame().getCinematics().keySet().forEach(name -> msg.send(sender, "list.item", "name", name));
    }

    @Subcommand("edit")
    public void edit(Player player) {
        player.openInventory(new CinematicGUI(instance).getCinematicListGUI(player));
    }
}