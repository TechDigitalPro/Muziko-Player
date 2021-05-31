

package com.whitebird.music;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;

import com.whitebird.music.dataloaders.SongLoader;
import com.whitebird.music.models.Song;
import com.whitebird.music.service.MusicService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

public class MusicPlayer {

    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap = new WeakHashMap<>();
    ///  private static final long[] sEmptyList;
    public static MusicService mService = null;
    private static ContentValues[] mContentValuesCache = null;

    static {
        // mConnectionMap = new WeakHashMap<Context, ServiceBinder>();
        //sEmptyList = new long[0];
    }

    public static final ServiceToken bindToService(final Context context,
                                                   final ServiceConnection callback) {

        Activity realActivity = ((Activity) context).getParent();
        if (realActivity == null) {
            realActivity = (Activity) context;
        }
        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MusicService.class));
        final ServiceBinder binder = new ServiceBinder(callback,
                contextWrapper.getApplicationContext());
        if (contextWrapper.bindService(
                new Intent().setClass(contextWrapper, MusicService.class), binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }
        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }
        mContextWrapper.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            mService = null;
        }
    }

    public static final boolean isPlaybackServiceConnected() {
        return mService != null;
    }  
    
   /* public static final MediaPlayer getPlayer() {
        return mService.getPlayer();
    }*/

    public static void next() {
        try {
            if (mService != null) {
                mService.playNextSong(true);
            }
        } catch (final Exception ignored) {
        }
    }

    public static void initPlaybackServiceWithSettings(final Context context) {

    }

/*    public static void asyncNext(final Context context) {
        final Intent previous = new Intent(context, MusicService.class);
        previous.setAction(MusicService.NEXT_ACTION);
        context.startService(previous);
    }*/

    public static void previous(final Context context, final boolean force) {
        //  final Intent previous = new Intent(context, MusicService.class);

        if (mService != null) {
            mService.playPreviousSong(false);
        }


    }

    public static void playOrPause() {
        try {
            if (mService != null) {
                if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.play();
                }
            }
        } catch (final Exception ignored) {
        }
    }

    public static void cycleRepeat() {
        try {
            if (mService != null) {
                mService.cycleRepeatMode();
            }

        } catch (final Exception ignored) {
        }
    }

    public static void cycleShuffle() {
        if (mService != null) {
            mService.toggleShuffle();

        }
    }

    public static final boolean isPlaying() {
        if (mService != null) {
            try {
                return mService.isPlaying();
            } catch (final Exception ignored) {
            }
        }
        return false;
    }

    public static final int getShuffleMode() {
        if (mService != null) {
            try {
                return mService.getShuffleMode();
            } catch (final Exception ignored) {
            }
        }
        return 0;
    }

    public static void setShuffleMode(int mode) {
        try {
            if (mService != null) {
                mService.setShuffleMode(mode);
            }
        } catch (Exception ignored) {

        }
    }

    public static final int getRepeatMode() {
        if (mService != null) {
            try {
                return mService.getRepeatMode();
            } catch (final Exception ignored) {
            }
        }
        return 0;
    }

    public static final String getTrackName() {
        if (mService != null) {
            try {
                return mService.getCurrentSong().title;
            } catch (final Exception ignored) {
            }
        }
        return null;
    }

    public static final String getArtistName() {
        if (mService != null) {
            try {
                return mService.getCurrentSong().artistName;
            } catch (final Exception ignored) {
            }
        }
        return null;
    }

    public static final String getAlbumName() {
        if (mService != null) {
            try {
                return mService.getCurrentSong().albumName;
            } catch (final Exception ignored) {
            }
        }
        return null;
    }

    public static final long getCurrentAlbumId() {
        if (mService != null) {
            try {
                return mService.getCurrentSong().albumId;
            } catch (final Exception ignored) {
            }
        }
        return -1;
    }

    public static final long getCurrentAudioId() {
        if (mService != null) {
            try {
                return mService.getCurrentSong().id;
            } catch (final Exception ignored) {
            }
        }
        return -1;
    }

    public static final Song getCurrentTrack() {
        if (mService != null) {
            try {
                return mService.getCurrentSong();
            } catch (final Exception ignored) {
            }
        }
        return null;
    }

    public static final Song getTrack(int index) {
        if (mService != null) {
            try {
                return mService.getPlayingQueue().get(index);
            } catch (final Exception ignored) {
            }
        }
        return null;
    }

  /*  public static final long getNextAudioId() {
        if (mService != null) {
            try {
                return mService.getNextPosition(true);
            } catch (final Exception ignored) {
            }
        }
        return -1;
    }*/

   /* public static final long getPreviousAudioId() {
        if (mService != null) {
            try {
                return mService.getPreviousAudioId();
            } catch (final Exception ignored) {
            }
        }
        return -1;
    }*/

    public static final long getCurrentArtistId() {
        if (mService != null) {
            try {
                return mService.getCurrentSong().artistId;
            } catch (final Exception ignored) {
            }
        }
        return -1;
    }

    public static final int getAudioSessionId() {
        if (mService != null) {
            try {
                return mService.getAudioSessionId();
            } catch (final Exception ignored) {
            }
        }
        return -1;
    }

    public static final ArrayList<Song> getQueue() {
        try {
            if (mService != null) {
                return mService.getPlayingQueue();
            } else {
            }
        } catch (final Exception ignored) {
        }
        return new ArrayList<>();
    }

/*    public static final long getQueueItemAtPosition(int position) {
        try {
            if (mService != null) {
                return mService.getQueueItemAtPosition(position);
            } else {
            }
        } catch (final Exception ignored) {
        }
        return -1;
    }

    public static final int getQueueSize() {
        try {
            if (mService != null) {
                return mService.getQueueSize();
            } else {
            }
        } catch (final Exception ignored) {
        }
        return 0;
    }*/

    public static final int getQueuePosition() {
        try {
            if (mService != null) {
                return mService.getPosition();
            }
        } catch (final Exception ignored) {
        }
        return 0;
    }

    public static void setQueuePosition(final int position) {
        if (mService != null) {
            try {
                mService.setPosition(position);
            } catch (final Exception ignored) {
            }
        }
    }

    public static void refresh() {
        try {
            if (mService != null) {
                // mService.refresh();
            }
        } catch (final Exception ignored) {
        }
    }

/*    public static final int getQueueHistorySize() {
        if (mService != null) {
            try {
                return mService.getQueueHistorySize();
            } catch (final Exception ignored) {
            }
        }
        return 0;
    }

    public static final int getQueueHistoryPosition(int position) {
        if (mService != null) {
            try {
                return mService.getQueueHistoryPosition(position);
            } catch (final Exception ignored) {
            }
        }
        return -1;
    }

    public static final int[] getQueueHistoryList() {
        if (mService != null) {
            try {
                return mService.getQueueHistoryList();
            } catch (final Exception ignored) {
            }
        }
        return null;
    }*/

    public static final int removeTrack(Song song) {
        try {
            if (mService != null) {
                mService.removeSong(song);
                return 1;
            }
        } catch (final Exception ingored) {
        }
        return 0;
    }

    public static final void removeTrackAtPosition(final int position) {
        try {
            if (mService != null) {
                mService.removeSong(position);
            }
        } catch (final Exception ingored) {
        }

    }

    public static void moveQueueItem(final int from, final int to) {
        if (mService != null && from >= 0 && to >= 0 && from < getPlayingQueue().size() && to < getPlayingQueue().size()) {
            mService.moveSong(from, to);

        }

    }

   /* public static void playArtist(final Context context, final long artistId, int position, boolean shuffle) {
        final long[] artistList = getSongListForArtist(context, artistId);
        if (artistList != null) {
            playAll(context, artistList, position, artistId, IdType.Artist, shuffle);
        }
    }*/

/*    public static void playAlbum(final Context context, final long albumId, int position, boolean shuffle) {
        final long[] albumList = getSongListForAlbum(context, albumId);
        if (albumList != null) {
            playAll(context, albumList, position, albumId, IdType.Album, shuffle);
        }
    }*/

/*    public static void playAll(final Context context, final long[] list, int position,
                               final long sourceId, final IdType sourceType,
                               final boolean forceShuffle) {
        if (list == null || list.length == 0 || mService == null) {
            return;
        }
        try {
            if (forceShuffle) {
                mService.setShuffleMode(MusicService.SHUFFLE_NORMAL);
            }
            final long currentId = mService.getAudioId();
            final int currentQueuePosition = getQueuePosition();
            if (position != -1 && currentQueuePosition == position && currentId == list[position]) {
                final long[] playlist = getQueue();
                if (Arrays.equals(list, playlist)) {
                    mService.play();
                    return;
                }
            }
            if (position < 0) {
                position = 0;
            }
            mService.open(list, forceShuffle ? -1 : position, sourceId, sourceType.mId);
            mService.play();
        } catch (final Exception ignored) {
        }
    }*/


    public static void playAll(final List<Song> queue, final int startPosition, final boolean startPlaying) {
        if (!tryToHandleOpenPlayingQueue((ArrayList<Song>) queue, startPosition, startPlaying) && mService != null) {
            mService.openQueue((ArrayList<Song>) queue, startPosition, true);
        }
    }

    public static ArrayList<Song> getPlayingQueue() {
        if (mService != null) {
            return mService.getPlayingQueue();
        }
        return new ArrayList<>();
    }

    public static void playSongAt(final int position) {
        if (mService != null) {
            mService.playSongAt(position);
        }
    }

    private static boolean tryToHandleOpenPlayingQueue(final ArrayList<Song> queue, final int startPosition, final boolean startPlaying) {
        if (getPlayingQueue() == queue) {
            if (startPlaying) {
                playSongAt(startPosition);
            } else {
                setQueuePosition(startPosition);
            }
            return true;
        }
        return false;
    }

    public static void playNext(Song song) {
        if (mService != null) {
            if (getPlayingQueue().size() > 0) {
                mService.addSong(position() + 1, song);
            } else {
                ArrayList<Song> queue = new ArrayList<>();
                queue.add(song);
                playAll(queue, 0, false);
            }

            Toast.makeText(mService, mService.getResources().getString(R.string.added_title_to_playing_queue), Toast.LENGTH_SHORT).show();
        }

    }

    public static void playNext(ArrayList<Song> song) {
        if (mService != null) {
            if (getPlayingQueue().size() > 0) {
                mService.addSong(position() + 1, song);
            } else {
                ArrayList<Song> queue = new ArrayList<>();
                queue.addAll(song);
                playAll(queue, 0, false);
            }

            Toast.makeText(mService, mService.getResources().getString(R.string.added_title_to_playing_queue), Toast.LENGTH_SHORT).show();
        }

    }

    public static void shuffleAll(final Context context) {
        Cursor cursor = SongLoader.makeSongCursor(context, null, null);
        ArrayList<Song> queue = SongLoader.getSongsForCursor(cursor);
        int startPosition = 0;
        if (!queue.isEmpty()) {
            startPosition = new Random().nextInt(queue.size());
        }

        if (!tryToHandleOpenPlayingQueue(queue, startPosition, true) && mService != null) {
            playAll(queue, startPosition, true);
            setShuffleMode(MusicService.SHUFFLE_MODE_SHUFFLE);
        }
    }
/*
    public static final long[] getSongListForArtist(final Context context, final long id) {
        final String[] projection = new String[]{
                BaseColumns._ID
        };
        final String selection = MediaStore.Audio.AudioColumns.ARTIST_ID + "=" + id + " AND "
                + MediaStore.Audio.AudioColumns.IS_MUSIC + "=1";
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
                MediaStore.Audio.AudioColumns.ALBUM_KEY + "," + MediaStore.Audio.AudioColumns.TRACK);
        if (cursor != null) {
            final long[] mList = SongLoader.getSongListForCursor(cursor);
            cursor.close();
            cursor = null;
            return mList;
        }
        return sEmptyList;
    }*/
/*
    public static final long[] getSongListForAlbum(final Context context, final long id) {
        final String[] projection = new String[]{
                BaseColumns._ID
        };
        final String selection = MediaStore.Audio.AudioColumns.ALBUM_ID + "=" + id + " AND " + MediaStore.Audio.AudioColumns.IS_MUSIC
                + "=1";
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
                MediaStore.Audio.AudioColumns.TRACK + ", " + MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            final long[] mList = SongLoader.getSongListForCursor(cursor);
            cursor.close();
            cursor = null;
            return mList;
        }
        return sEmptyList;
    }*/

    public static final int getSongCountForAlbumInt(final Context context, final long id) {
        int songCount = 0;
        if (id == -1) {
            return songCount;
        }

        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                if (!cursor.isNull(0)) {
                    songCount = cursor.getInt(0);
                }
            }
            cursor.close();
            cursor = null;
        }

        return songCount;
    }

   /* public static final String getReleaseDateForAlbum(final Context context, final long id) {
        if (id == -1) {
            return null;
        }
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(uri, new String[]{
                MediaStore.Audio.AlbumColumns.FIRST_YEAR
        }, null, null, null);
        String releaseDate = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                releaseDate = cursor.getString(0);
            }
            cursor.close();
            cursor = null;
        }
        return releaseDate;
    }*/

    public static void seek(final long position) {
        if (mService != null) {
            try {
                mService.seek(position);
            } catch (final Exception ignored) {
            }
        }
    }

 /*   public static void seekRelative(final long deltaInMs) {
        if (mService != null) {
            try {
                mService.seekRelative(deltaInMs);
            } catch (final Exception ignored) {
            }
        }
    }*/

    public static final int position() {
        if (mService != null) {
            try {
                return mService.getPosition();
            } catch (final Exception ignored) {
            }
        }
        return 0;
    }

    public static int getSongProgressMillis() {
        if (mService != null) {
            return mService.getSongProgressMillis();
        }
        return -1;
    }

    public static final long duration() {
        if (mService != null) {
            try {
                return mService.getSongDurationMillis();
            } catch (final Exception ignored) {
            }
        }
        return 0;
    }

  /*  public static void clearQueue() {
        if (mService != null) {
            try {
                mService.removeTracks(0, Integer.MAX_VALUE);
            } catch (final Exception ignored) {
            }
        }
    }*/

    public static void addToQueue(ArrayList<Song> songs) {
        if (mService != null) {
            if (getPlayingQueue().size() > 0) {
                mService.addSongs(songs);
            } else {
                playAll(songs, 0, false);
            }
            final String toast = songs.size() == 1 ? mService.getResources().getString(R.string.added_title_to_playing_queue) : mService.getResources().getString(R.string.added_x_titles_to_playing_queue, songs.size());
            Toast.makeText(mService, toast, Toast.LENGTH_SHORT).show();

        }

    }

    public static void addToQueue(Song song) {
        if (mService != null) {
            if (getPlayingQueue().size() > 0) {
                mService.addSong(song);
            } else {
                ArrayList<Song> queue = new ArrayList<>();
                queue.add(song);
                playAll(queue, 0, false);
            }
            Toast.makeText(mService, mService.getResources().getString(R.string.added_title_to_playing_queue), Toast.LENGTH_SHORT).show();

        }

    }

    public static final String makeLabel(final Context context, final int pluralInt,
                                         final int number) {
        return context.getResources().getQuantityString(pluralInt, number, number);
    }

    /*public static void addToPlaylist(final Context context, final long[] ids, final long playlistid) {
        final int size = ids.length;
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + "play_order" + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
        Cursor cursor = null;
        int base = 0;

        try {
            cursor = resolver.query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                base = cursor.getInt(0) + 1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        int numinserted = 0;
        for (int offSet = 0; offSet < size; offSet += 1000) {
            makeInsertItems(ids, offSet, 1000, base);
            numinserted += resolver.bulkInsert(uri, mContentValuesCache);
        }
        final String message = context.getResources().getQuantityString(
                R.plurals.NNNtrackstoplaylist, numinserted, numinserted);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void makeInsertItems(final long[] ids, final int offset, int len, final int base) {
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }

        if (mContentValuesCache == null || mContentValuesCache.length != len) {
            mContentValuesCache = new ContentValues[len];
        }
        for (int i = 0; i < len; i++) {
            if (mContentValuesCache[i] == null) {
                mContentValuesCache[i] = new ContentValues();
            }
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }
*/
    /*public static final long createPlaylist(final Context context, final String name) {
        if (name != null && name.length() > 0) {
            final ContentResolver resolver = context.getContentResolver();
            final String[] projection = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME
            };
            final String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);
            if (cursor.getCount() <= 0) {
                final ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                final Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values);
                return Long.parseLong(uri.getLastPathSegment());
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            return -1;
        }
        return -1;
    }*/

    public static void clearQueue() {
    }

    public static void openFile(String path) {
    }

/*    public static final void openFile(final String path) {
        if (mService != null) {
            try {
                mService.openFile(path);
            } catch (final Exception ignored) {
            }
        }
    }*/

    public static final class ServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;
        private final Context mContext;


        public ServiceBinder(final ServiceConnection callback, final Context context) {
            mCallback = callback;
            mContext = context;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            mService = binder.getService();
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
            initPlaybackServiceWithSettings(mContext);
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            mService = null;
        }
    }

    public static final class ServiceToken {
        public ContextWrapper mWrappedContext;

        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }
}