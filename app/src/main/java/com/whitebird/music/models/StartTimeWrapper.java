package com.whitebird.music.models;

import java.io.Serializable;

public class StartTimeWrapper implements Serializable{

    public long songId,duration;

    public StartTimeWrapper(long songId, long duration) {
        this.songId = songId;
        this.duration = duration;
    }


}
