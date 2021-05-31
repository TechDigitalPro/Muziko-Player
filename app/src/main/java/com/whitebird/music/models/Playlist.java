

package com.whitebird.music.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Playlist implements Serializable{

    public String name="";
    public ArrayList<Song> songList=new ArrayList<>();
    public long playistId;

    public Playlist() {
    }

    public Playlist(String name, ArrayList<Song> songList) {
        this.name = name;
        this.songList = songList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public void setSongList(ArrayList<Song> songList) {
        this.songList = songList;
    }

    public long getPlayistId() {
        return playistId;
    }

    public void setPlayistId(long playistId) {
        this.playistId = playistId;
    }
}
