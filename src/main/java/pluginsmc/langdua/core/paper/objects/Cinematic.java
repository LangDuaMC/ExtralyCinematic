package pluginsmc.langdua.core.paper.objects;

import java.util.ArrayList;
import java.util.List;

public class Cinematic {

    private String name;
    private List<Frame> frames = new ArrayList<>();

    private String focusWorld = null;
    private Double focusX = null;
    private Double focusY = null;
    private Double focusZ = null;

    private double shakeIntensity = 0.0;

    private int startZoom = 0;
    private int endZoom = 0;

    // Background Music Sound ID
    private String bgmSound = null;

    public Cinematic(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Frame> getFrames() { return frames; }
    public void setFrames(List<Frame> frames) { this.frames = frames; }

    public boolean hasFocus() { return focusWorld != null && focusX != null && focusY != null && focusZ != null; }
    public void setFocus(String world, double x, double y, double z) {
        this.focusWorld = world;
        this.focusX = x;
        this.focusY = y;
        this.focusZ = z;
    }
    public void clearFocus() {
        this.focusWorld = null;
        this.focusX = null;
        this.focusY = null;
        this.focusZ = null;
    }
    public String getFocusWorld() { return focusWorld; }
    public Double getFocusX() { return focusX; }
    public Double getFocusY() { return focusY; }
    public Double getFocusZ() { return focusZ; }

    public double getShakeIntensity() { return shakeIntensity; }
    public void setShakeIntensity(double shakeIntensity) { this.shakeIntensity = shakeIntensity; }

    public int getStartZoom() { return startZoom; }
    public void setStartZoom(int startZoom) { this.startZoom = startZoom; }
    public int getEndZoom() { return endZoom; }
    public void setEndZoom(int endZoom) { this.endZoom = endZoom; }

    public String getBgmSound() { return bgmSound; }
    public void setBgmSound(String bgmSound) { this.bgmSound = bgmSound; }
}