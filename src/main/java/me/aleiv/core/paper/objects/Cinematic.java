package me.aleiv.core.paper.objects;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Cinematic {

    List<Frame> frames;
    List<String> viewers;
    String name;

    public Cinematic(String name) {
        this.frames = new ArrayList<>();
        this.viewers = new ArrayList<>();
        this.name = name;

    }
}
