package pluginsmc.langdua.core.paper.objects;

import java.util.ArrayList;
import java.util.List;

public class Cinematic {

    private String name;
    private List<Frame> frames = new ArrayList<>();

    // Tọa độ Focus (Khóa mục tiêu)
    private String focusWorld = null;
    private Double focusX = null;
    private Double focusY = null;
    private Double focusZ = null;

    public Cinematic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public void setFrames(List<Frame> frames) {
        this.frames = frames;
    }

    public boolean hasFocus() {
        return focusWorld != null && focusX != null && focusY != null && focusZ != null;
    }

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
}