package com.whitebird.music.models;

import java.io.Serializable;

public class Song implements Serializable{



    public  long albumId;
    public  String albumName;
    public  long artistId;
    public  String artistName;
    public  long duration;
    public  long dateAdded;
    public  long dateModified;
    public  long id;
    public  String title;
    public  String name;
    public  String data;
    public  String genre_name;
    public  int trackNumber;
    public  int storage;
    public  int year;
    public  int songsCount;
    public int type;
    public long lastPlayTime;
    public int playCount;
    public int album_count;



    public Song() {
        this.id = -1;
        this.albumId = -1;
        this.artistId = -1;
        this.title = "";
        this.artistName = "";
        this.albumName = "";
        this.duration = -1;
        this.trackNumber = -1;
    }

    public Song(long _id, long _albumId, long _artistId, String _title, String _artistName, String _albumName, int _duration, int _trackNumber,String data) {
        this.id = _id;
        this.albumId = _albumId;
        this.artistId = _artistId;
        this.title = _title;
        this.artistName = _artistName;
        this.albumName = _albumName;
        this.duration = _duration;
        this.trackNumber = _trackNumber;
        this.data = data;
    }

    public long getLastPlayTime() {
        return lastPlayTime;
    }

    public void setLastPlayTime(long lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getAlbum_count() {
        return album_count;
    }

    public void setAlbum_count(int album_count) {
        this.album_count = album_count;
    }
}
