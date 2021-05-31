

package com.whitebird.music.dataloaders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.whitebird.music.MuzikoApp;
import com.whitebird.music.models.Playlist;
import com.whitebird.music.models.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistLoader {

    public static ArrayList<Playlist> getPlayList() {

        return MuzikoApp.readAllPlayLists();

    }


    public static ArrayList<Playlist> saveToPlayList(String name, ArrayList<Song> data, Context context) {

        ArrayList<Playlist> playLists = MuzikoApp.readAllPlayLists();
        for (Playlist playlist : playLists) {
            ArrayList<Song> songs = playlist.getSongList();
            if (playlist.getName().equals(name)) {
                songs.addAll(data);
                playLists.set(playLists.indexOf(playlist), playlist);
                MuzikoApp.writeAllPlayLists(playLists);
                return playLists;
            }

        }


        playLists.add(new Playlist(name, data));

        MuzikoApp.writeAllPlayLists(playLists);

        context.sendBroadcast(new Intent(MuzikoApp.INTENT_PLAYLIST_CHANGE));
        return playLists;


    }


/*
public static ArrayList<Playlist> saveToPlayList(String name, ArrayList<Song> data, Context context) {

        ArrayList<Playlist> playLists = MuzikoApp.readAllPlayLists();
        for (Playlist playlist : playLists) {
            ArrayList<Song> songs = playlist.getSongList();
            if (playlist.getName().equals(name)) {

                for (Song s : data) {
                    boolean isExist = false;
                    for (Song song : playlist.getSongList()) {
                        if (s.id == song.id) {
                            isExist = true;
                            break;
                        }
                    }
                    if (!isExist) {
                        songs.add(s);
                    }
                }


                playLists.set(playLists.indexOf(playlist), playlist);
                MuzikoApp.writeAllPlayLists(playLists);
                return playLists;
            }

        }


        playLists.add(new Playlist(name, data));

        MuzikoApp.writeAllPlayLists(playLists);

        context.sendBroadcast(new Intent(MuzikoApp.INTENT_PLAYLIST_CHANGE));
        return playLists;


    }*/


    public static List<Song> getSongsInPlaylist(String name) {

        ArrayList<Playlist> playLists = MuzikoApp.readAllPlayLists();
        for (Playlist playlist : playLists) {
            ArrayList<Song> songs = playlist.getSongList();
            if (playlist.getName().equals(name)) {

                return songs;
            }

        }
        return  new ArrayList<>();

    }

    public static void deletePlaylists(String name,Activity context) {
        ArrayList<Playlist> playLists = MuzikoApp.readAllPlayLists();
        for (Playlist playlist : playLists) {

            if (playlist.getName().equals(name)) {
                playLists.remove(playlist);
                break;
            }

        }
        MuzikoApp.writeAllPlayLists(playLists);

        context.sendBroadcast(new Intent(MuzikoApp.INTENT_PLAYLIST_CHANGE));
    }

    public static void removeSong(String name, Song data, Context context) {

        ArrayList<Playlist> playLists = MuzikoApp.readAllPlayLists();


        for (int i=0;i<playLists.size();i++){
            if (playLists.get(i).getName().equals(name)) {
                ArrayList<Song> songs = playLists.get(i).getSongList();

                for (int j = 0; j < songs.size(); j++) {

                    if (songs.get(j).id==data.id){
                        playLists.get(i).getSongList().remove(j);

                        MuzikoApp.writeAllPlayLists(playLists);
                        context.sendBroadcast(new Intent(MuzikoApp.INTENT_PLAYLIST_CHANGE));
                        return;
                    }

                }
            }

        }







    }



}
