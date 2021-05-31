package com.whitebird.music.models;

public class Genre {

    String name="",count="",song="";

    public Genre(String name, String count, String song) {
        this.name = name;
        this.count = count;
        this.song = song;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }
}
