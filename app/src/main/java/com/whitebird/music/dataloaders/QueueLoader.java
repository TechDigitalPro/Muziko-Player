

package com.whitebird.music.dataloaders;

import android.content.Context;
import android.database.Cursor;

import com.whitebird.music.models.Song;

import java.util.ArrayList;
import java.util.List;


public class QueueLoader {


    private static NowPlayingCursor mCursor;

    public static List<Song> getQueueSongs(Context context) {

        final ArrayList<Song> mSongList = new ArrayList<>();
        mCursor = new NowPlayingCursor(context);

        if (mCursor != null && mCursor.moveToFirst()) {
            do {

                /*final long id = mCursor.getLong(0);

                final String songName = mCursor.getString(1);

                final String artist = mCursor.getString(2);

                final long albumId = mCursor.getLong(3);

                final String album = mCursor.getString(4);

                final int duration = mCursor.getInt(5);

                final long artistid = mCursor.getInt(7);

                final int tracknumber = mCursor.getInt(6);
                final String data = mCursor.getString(7);

                final Song song = new Song(id, albumId, artistid, songName, artist, album, duration, tracknumber,data);*/

                mSongList.add(getSongForCursor(mCursor));
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mSongList;
    }

    public static Song getSongForCursor(Cursor cursor) {
        Song song = new Song();
      /*  if ((cursor != null) && (cursor.moveToFirst())) */
        {
            long id = cursor.getLong(0);
            String title = cursor.getString(1);
            String artist = cursor.getString(2);
            String album = cursor.getString(3);
            int duration = cursor.getInt(4);
            int trackNumber = cursor.getInt(5);
            long artistId = cursor.getInt(6);
            long albumId = cursor.getLong(7);
            song = new Song(id, albumId, artistId, title, artist, album, duration, trackNumber,title);
            song.name=cursor.getString(8);
            song.data=cursor.getString(9);
            song.dateAdded=cursor.getLong(11);
            song.dateModified=cursor.getLong(12);
            song.duration=cursor.getLong(13);
            song.trackNumber=cursor.getInt(14);
            song.year=cursor.getInt(15);

        }

      /*  if (cursor != null)
            cursor.close();*/
        return song;
    }

}
