package fonda.scheduler.rest;

import lombok.Getter;

@Getter
public class PathAttributes {

    private final String path;
    private final long size;
    private final long timestamp;

    private PathAttributes() {
        this.path = null;
        this.size = -1;
        this.timestamp = -1;
    }
}