package com.whitebird.music.dataloaders;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.whitebird.music.MuzikoApp;
import com.whitebird.music.models.Playlist;
import com.whitebird.music.models.Song;
import com.whitebird.music.models.StartTimeWrapper;
import com.whitebird.music.utils.StaticData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class DataHelper {


    public static ArrayList<Song> getAllTrack() {
        ArrayList track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        return track;

    }



    public static ArrayList<Song> getAllRingtone() {
        ArrayList track = new ArrayList();
        track.addAll(MuzikoApp.readAllRingtong().values());
        return track;
    }

    public static void updateSong(Context ctx,Song song) {

        HashMap<String, Song> data = MuzikoApp.readAllSong();
        Song song1 = data.get(song.data);
        if (song1 != null) {
            song1.title = song.title;
            song1.name = song.name;
            song1.artistName = song.artistName;
            song1.albumName = song.albumName;
            song1.genre_name = song.genre_name;
            song1.year = song.year;
            song1.trackNumber = song.trackNumber;
            data.put(song.data, song1);
            MuzikoApp.writeAllSong(data);
        }


        ArrayList<Song> activitySong = MuzikoApp.readAllActivity();
        for (Song s:activitySong){
            if (s.id==song.id){
                s.title = song.title;
                s.name = song.name;
                s.artistName = song.artistName;
                s.albumName = song.albumName;
                s.genre_name = song.genre_name;
                s.year = song.year;
                s.trackNumber = song.trackNumber;
                MuzikoApp.writeActivity(activitySong);
                break;

            }

        }

        ArrayList<Playlist> playLists= MuzikoApp.readAllPlayLists();

        for (Playlist playlist:playLists){
            ArrayList<Song> songs=playlist.getSongList();
            for (Song s:songs){

                if (s.id==song.id){
                    s.title = song.title;
                    s.artistName = song.artistName;
                    s.albumName = song.albumName;
                    s.genre_name = song.genre_name;
                    s.year = song.year;
                    s.trackNumber = song.trackNumber;
                }
            }
        }
        MuzikoApp.writeAllPlayLists(playLists);

        ctx.sendBroadcast(new Intent(MuzikoApp.INTENT_TRACK_CHANGED));


    }

    public static void setStartTime(StartTimeWrapper data) {
        HashMap<String, StartTimeWrapper> list = MuzikoApp.readAllTime();
        list.put("" + data.songId, data);
        MuzikoApp.writeAllTime(list);
    }

    public static ArrayList<StartTimeWrapper> getAllTime() {
        ArrayList time = new ArrayList();
        time.addAll(MuzikoApp.readAllTime().values());
        return time;
    }

    public static ArrayList<Song> getAllAlbum() {
        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        HashMap<String, Song> data = new HashMap<>();

        for (Song song : track) {
            Song model = data.get(song.albumName);
            if (model != null) {
                model.songsCount++;
                data.put(song.albumName, model);
            } else {
                song.songsCount++;
                data.put(song.albumName, song);
            }
        }
        track = new ArrayList();
        track.addAll(data.values());
        return track;

    }

    public static ArrayList<Song> getAllArtist() {

        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        HashMap<String, Song> data = new HashMap<>();

        for (Song song : track) {
            Song model = data.get(song.artistName);
            if (model != null) {
                model.songsCount++;
                data.put(song.artistName, model);
            } else {
                song.songsCount++;
                song.setAlbum_count(DataHelper.getAlbumsForArtist(song.artistId).size());
                data.put(song.artistName, song);

            }
        }
        track = new ArrayList();
        track.addAll(data.values());
        return track;

    }


    public static ArrayList<Song> searchArtists(String paramString, int limit) {

        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        HashMap<String, Song> data = new HashMap<>();

        for (Song song : track) {
            Song model = data.get(song.artistName);
            if (model != null) {
                model.songsCount++;
                model.type = StaticData.TYPE_ARTIST;
                data.put(song.artistName, model);
            } else {
                song.songsCount++;
                if (song.artistName.toLowerCase().contains(paramString.toLowerCase())) {
                    song.type = StaticData.TYPE_ARTIST;
                    data.put(song.artistName, song);
                }

            }
        }
        track = new ArrayList();
        track.addAll(data.values());
        return track;

    }


    public static ArrayList<Song> searchAlbums(String paramString, int limit) {


        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        HashMap<String, Song> data = new HashMap<>();

        for (Song song : track) {
            Song model = data.get(song.albumName);

            if (model != null) {
                model.songsCount++;
                model.type = StaticData.TYPE_ALBUM;
                data.put(song.albumName, model);
            } else {
                song.songsCount++;
                if (song.albumName.toLowerCase().contains(paramString.toLowerCase())) {
                    song.type = StaticData.TYPE_ALBUM;
                    data.put(song.albumName, song);

                }
            }
        }
        track = new ArrayList();
        track.addAll(data.values());
        return track;

    }

    public static ArrayList<Song> searchGenre(String paramString, int limit) {


        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        HashMap<String, Song> data = new HashMap<>();

        for (Song song : track) {
            Song model = data.get(song.genre_name);

            if (model != null) {
                model.songsCount++;
                model.type = StaticData.TYPE_ALBUM;
                data.put(song.genre_name, model);
            } else {
                song.songsCount++;
                if (song.genre_name.toLowerCase().contains(paramString.toLowerCase())) {
                    song.type = StaticData.TYPE_ALBUM;
                    data.put(song.genre_name, song);

                }
            }
        }
        track = new ArrayList();
        track.addAll(data.values());
        return track;

    }

    public static ArrayList<Song> searchSongs(String paramString, int limit) {


        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());

        ArrayList<Song> songs = new ArrayList<>();

        for (Song song : track) {
            if (songs.size() > limit) {
                return songs;
            }

            if (song.name.toLowerCase().contains(paramString.toLowerCase())) {
                song.type = StaticData.TYPE_SONG;
                songs.add(song);
            }

        }

        return songs;

    }

    public static ArrayList<Song> searchRingTone(String paramString, int limit) {


        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllRingtong().values());

        ArrayList<Song> songs = new ArrayList<>();

        for (Song song : track) {
            if (songs.size() > limit) {
                return songs;
            }

            if (song.name.toLowerCase().contains(paramString.toLowerCase())) {
                song.type = StaticData.TYPE_SONG;
                songs.add(song);
            }

        }

        return songs;

    }


    public static ArrayList<Song> getAllGenre() {

        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        HashMap<String, Song> data = new HashMap<>();

        for (Song song : track) {
            Song model = data.get(song.genre_name);
            if (model != null) {
                model.songsCount++;
                data.put(song.genre_name, model);
            } else {
                song.songsCount++;
                data.put(song.genre_name, song);
            }
        }
        track = new ArrayList();
        track.addAll(data.values());
        return track;

    }


    public static ArrayList<Song> getSongsForArtist(long artistId) {

        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());

        ArrayList<Song> songs = new ArrayList<>();
        for (Song song : track) {
            if (song.artistId == artistId) {
                songs.add(song);
            }

        }

        return songs;

    }

    public static ArrayList<Song> getSongsForAlbum(String albumName) {

        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());

        ArrayList<Song> songs = new ArrayList<>();
        for (Song song : track) {
            if (song.albumName.equals(albumName)) {
                songs.add(song);
            }

        }

        return songs;

    }

    public static ArrayList<Song> getSongsForGenre(String genreName) {

        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());

        ArrayList<Song> songs = new ArrayList<>();
        for (Song song : track) {
            if (song.genre_name.equals(genreName)) {
                songs.add(song);
            }

        }

        return songs;

    }


    public static Song getAlbum(long albumID) {
        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        HashMap<String, Song> data = new HashMap<>();

        for (Song song : track) {
            if (song.albumId == albumID)
                return song;

        }
        return new Song();


    }

    public static Song getGenre(String genreName) {
        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        HashMap<String, Song> data = new HashMap<>();

        for (Song song : track) {
            if (song.genre_name.equals(genreName))
                return song;

        }
        return new Song();


    }

    public static ArrayList<Song> getLastAddedSongs() {


        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        Collections.sort(track, new Comparator<Song>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(Song o1, Song o2) {
                return Long.compare(o2.dateAdded, o1.dateAdded);
            }
        });

        ArrayList<Song> songs = new ArrayList<>();

        for (Song song : track) {
            if (songs.size() >= 20) {
                return songs;
            }
            song.type = StaticData.TYPE_SONG;
            songs.add(song);
        }

        return songs;

    }


    public static ArrayList<Song> getAlbumsForArtist(long artistID) {

        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        HashMap<String, Song> data = new HashMap<>();

        for (Song song : track) {
            Song model = data.get(song.albumName);
            if (model != null) {
                model.songsCount++;
                data.put(song.albumName, model);
            } else {
                if (song.artistId == artistID) {
                    song.songsCount++;

                    data.put(song.albumName, song);
                }
            }
        }
        track = new ArrayList();
        track.addAll(data.values());
        return track;

    }

    public static ArrayList<Song> getAlbumsForGener(String genreName) {

        ArrayList<Song> track = new ArrayList();
        track.addAll(MuzikoApp.readAllSong().values());
        HashMap<String, Song> data = new HashMap<>();

        for (Song song : track) {
            Song model = data.get(song.albumName);
            if (model != null) {
                model.songsCount++;
                data.put(song.albumName, model);
            } else {
                if (song.genre_name.equals(genreName)) {
                    song.songsCount++;

                    data.put(song.albumName, song);
                }
            }
        }
        track = new ArrayList();
        track.addAll(data.values());
        return track;

    }


    public static Song getSong(long id) {
        return MuzikoApp.readAllSong().get("" + id);
    }
}
