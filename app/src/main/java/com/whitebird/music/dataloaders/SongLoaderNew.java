package com.whitebird.music.dataloaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;


import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.models.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class SongLoaderNew extends AsyncTask<Song, int[], Integer> {


    private final Context mContext;

    public SongLoaderNew(Context ctx) {
        this.mContext = ctx;
    }

    @Override
    protected Integer doInBackground(Song... params) {

        try {
            boolean ret = false;

            HashMap<Long, Long> trackGenreMap = new HashMap<>();
            HashMap<Long, String> genreNameMap = new HashMap<>();

            String[] genresProj = {MediaStore.Audio.Genres.Members.AUDIO_ID, MediaStore.Audio.Genres.Members.GENRE_ID};
            Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://media/external/audio/genres/all/members"), genresProj, null, null, null);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                trackGenreMap.put(cursor.getLong(0), cursor.getLong(1));
            }
            cursor.close();

            final String[] genreNameColumns = {MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME};
            cursor = mContext.getContentResolver().query(MediaStore.Audio.Genres.getContentUri("external"), genreNameColumns, null, null, null);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                genreNameMap.put(cursor.getLong(0), cursor.getString(1));
            }
            cursor.close();

            LinkedHashMap<String, Song> trackMap = new LinkedHashMap<>();
            LinkedHashMap<String, Song> ringtoneMap = new LinkedHashMap<>();
            LinkedHashMap<String, Song> genreMap = new LinkedHashMap<>();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor c = mContext.getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " != 0"+"||"+MediaStore.Audio.Media.IS_RINGTONE + " != 0", null, null);


            String genre = "";
           // int folderCounter = 1;
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndex(MediaStore.Audio.Media._ID));
                long albumId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long artistId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));

                String name = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String data = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                String composer = c.getString(c.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
                String dateAdded = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                String dateModified = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
                long duration = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION));
                int track = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.TRACK));
                int year = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.YEAR));

                if (data == null || data.trim().length() == 0)
                    continue;

                Song songData = new Song();

                songData.id = id;


                if (artist != null && !artist.contains("Muziko-Ringtone")) {
                    songData.name = name;
                    songData.title = title;

                    songData.albumId = albumId;
                    songData.artistId = artistId;

                    songData.albumName = album;
                    songData.artistName = artist;
                    songData.duration = duration;
                    songData.dateAdded = Long.valueOf(dateAdded);
                    songData.dateModified = Long.valueOf(dateModified);
//                    songData.composer = composer;
                    songData.data = data;

                    if (!(data.toLowerCase().contains("emulated") || data.toLowerCase().contains("sdcard0"))) {
                        songData.storage = 2;
                    } else {
                        songData.storage = 1;
                    }

                    songData.trackNumber = track;
                    songData.year = year;
                    String gName = "";
                    long gID = 0;

                    if (trackGenreMap.containsKey(id)) {
                        gID = trackGenreMap.get(id);

                        gName = genreNameMap.get(gID);

                    }

                    if (gName != null && !gName.isEmpty()) {

                        songData.genre_name = gName;
                        Song genreData = genreMap.get(gName);
                        if (genreData == null) {
                            genreData = new Song();
                            genreData.id = gID;
                            genreData.albumId = albumId;
                            genreData.title = gName;
                            genreData.dateAdded = Long.valueOf(dateAdded);
                            genreData.songsCount = 1;
                        } else {
                            genreData.songsCount++;
                        }

                        genreMap.put(gName, genreData);
                    } else {
                        genre = mContext.getString(R.string.unknown_genre);
                        songData.genre_name = genre;
                        Song genreData = genreMap.get(genre);
                        if (genreData == null) {
                            genreData = new Song();
                            genreData.id = 0;
                            genreData.albumId = albumId;
                            genreData.title = genre;
                            genreData.dateAdded = Long.valueOf(dateAdded);
                            genreData.songsCount = 1;
                        } else {
                            genreData.songsCount++;
                        }
                        genreMap.put(genre, genreData);
                    }

                    ArrayList<Song> ignore=MuzikoApp.readIgnoreList();
                    boolean isFound=false;
                    for (Song song:ignore){
                        if (song.id==id)
                            isFound=true;

                    }
                    if (!isFound)
                    trackMap.put(""+id, songData);

                }else {
                    songData.name = name;
                    songData.title = title;

                    songData.albumId = albumId;
                    songData.artistId = artistId;

                    songData.albumName = album;
                    songData.artistName = artist;
                    songData.duration = duration;
                    songData.dateAdded = Long.valueOf(dateAdded);
                    songData.dateModified = Long.valueOf(dateModified);
//                    songData.composer = composer;
                    songData.data = data;

                    if (!(data.toLowerCase().contains("emulated") || data.toLowerCase().contains("sdcard0"))) {
                        songData.storage = 2;
                    } else {
                        songData.storage = 1;
                    }

                    songData.trackNumber = track;
                    songData.year = year;
                    ringtoneMap.put(data, songData);


                }

            }

            c.close();
            Log.d("musicFromDevice", "SIZE: " + trackMap.size());

            MuzikoApp.writeAllSong(trackMap);
            MuzikoApp.writeAllRingtong(ringtoneMap);
          //  MuzikoApp.readAllSong();

          /* TrackRealmHelper.insertList(trackMap);

            AppController.Instance().startMd5Updater();*/

            genreMap.clear();
            genreNameMap.clear();
            trackGenreMap.clear();

            return trackMap.size();

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Integer s) {


//        DataHelper.getAllAlbum();

       /* if (s == -1) {
            mContext.startActivity(new Intent(mContext, MainActivity.class));
        } else {
            mContext.startActivity(new Intent(mContext, MainActivity.class));
        }*/
        super.onPostExecute(s);
    }



}
