package me.aleiv.core.paper.objects;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Frame {
    String world;
    double x;
    double y;
    double z;
    float yaw;
    float pitch;
    List<String> commands;

    public Frame(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.commands = new ArrayList<>();
    }

    public List<String> getCommands() {
        if (commands == null) commands = new ArrayList<>();
        return commands;
    }
}