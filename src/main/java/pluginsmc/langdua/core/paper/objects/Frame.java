package pluginsmc.langdua.core.paper.objects;

import java.util.ArrayList;
import java.util.List;

public class Frame {
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    private List<String> commands = new ArrayList<>();

    // Subtitle System Variables
    private String title = "";
    private String subtitle = "";

    public Frame() {}

    public Frame(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public List<String> getCommands() { return commands; }
    public void setCommands(List<String> commands) { this.commands = commands; }

    public String getTitle() { return title == null ? "" : title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle == null ? "" : subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
}