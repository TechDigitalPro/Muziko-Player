

package com.whitebird.music;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.MediaDescription;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.media.MediaBrowserService;

import androidx.annotation.Nullable;

import com.whitebird.music.dataloaders.DataHelper;

import com.whitebird.music.models.Song;
import com.whitebird.music.utils.MuzikoUtils;

import java.util.ArrayList;
import java.util.List;

@TargetApi(21)
public class WearBrowserService extends MediaBrowserService {

    public static final String MEDIA_ID_ROOT = "__ROOT__";
    public static final int TYPE_ARTIST = 0;
    public static final int TYPE_ALBUM = 1;
    public static final int TYPE_SONG = 2;
    public static final int TYPE_PLAYLIST = 3;
    public static final int TYPE_ARTIST_SONG_ALBUMS = 4;
    public static final int TYPE_ALBUM_SONGS = 5;
    public static final int TYPE_ARTIST_ALL_SONGS = 6;
    public static final int TYPE_PLAYLIST_ALL_SONGS = 7;
    public static WearBrowserService sInstance;
    MediaSession mSession;
    private Context mContext;
    private boolean mServiceStarted;

    public static WearBrowserService getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mContext = this;
        mSession = new MediaSession(this, "WearBrowserService");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceStarted = false;
        mSession.release();
    }

    @Override
    public void onLoadChildren(String parentId, Result<List<MediaBrowser.MediaItem>> result) {

        result.detach();
        loadChildren(parentId, result);

    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    private void setSessionActive() {
        if (!mServiceStarted) {
            startService(new Intent(getApplicationContext(), WearBrowserService.class));
            mServiceStarted = true;
        }

        if (!mSession.isActive()) {
            mSession.setActive(true);
        }
    }

    private void setSessionInactive() {
        if (mServiceStarted) {
            stopSelf();
            mServiceStarted = false;
        }

        if (mSession.isActive()) {
            mSession.setActive(false);
        }
    }

    private void addMediaRoots(List<MediaBrowser.MediaItem> mMediaRoot) {
        mMediaRoot.add(new MediaBrowser.MediaItem(
                new MediaDescription.Builder()
                        .setMediaId(Integer.toString(TYPE_ARTIST))
                        .setTitle(getString(R.string.artists))
                        .setIconUri(Uri.parse("android.resource://" +
                                "com.whitebird.music/minmap/app_icon"))
                        .setSubtitle(getString(R.string.artists))
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
        ));

        mMediaRoot.add(new MediaBrowser.MediaItem(
                new MediaDescription.Builder()
                        .setMediaId(Integer.toString(TYPE_ALBUM))
                        .setTitle(getString(R.string.albums))
                        .setIconUri(Uri.parse("android.resource://" +
                                "com.whitebird.music/minmap/app_icon"))
                        .setSubtitle(getString(R.string.albums))
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
        ));

        mMediaRoot.add(new MediaBrowser.MediaItem(
                new MediaDescription.Builder()
                        .setMediaId(Integer.toString(TYPE_SONG))
                        .setTitle(getString(R.string.songs))
                        .setIconUri(Uri.parse("android.resource://" +
                                "com.whitebird.music/minmap/app_icon"))
                        .setSubtitle(getString(R.string.songs))
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
        ));


        mMediaRoot.add(new MediaBrowser.MediaItem(
                new MediaDescription.Builder()
                        .setMediaId(Integer.toString(TYPE_PLAYLIST))
                        .setTitle(getString(R.string.playlists))
                        .setIconUri(Uri.parse("android.resource://" +
                                "com.whitebird.music/minmap/app_icon"))
                        .setSubtitle(getString(R.string.playlists))
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
        ));

    }

    private void loadChildren(final String parentId, final Result<List<MediaBrowser.MediaItem>> result) {

        final List<MediaBrowser.MediaItem> mediaItems = new ArrayList<>();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {

                if (parentId.equals(MEDIA_ID_ROOT)) {
                    addMediaRoots(mediaItems);
                } else {
                    switch (Integer.parseInt(Character.toString(parentId.charAt(0)))) {
                        case TYPE_ARTIST:
                            List<Song> artistList = DataHelper.getAllArtist();
                            for (Song artist : artistList) {
                                String albumNmber = MuzikoUtils.makeLabel(mContext, R.plurals.Nalbums, 0/*artist.albumCount*/);
                                String songCount = MuzikoUtils.makeLabel(mContext, R.plurals.Nsongs, artist.songsCount);
                                fillMediaItems(mediaItems, Integer.toString(TYPE_ARTIST_SONG_ALBUMS) + Long.toString(artist.id), artist.name, Uri.parse("android.resource://" +
                                        "com.whitebird.music/minmap/app_icon"), MuzikoUtils.makeCombinedString(mContext, albumNmber, songCount), MediaBrowser.MediaItem.FLAG_BROWSABLE);
                            }
                            break;
                        case TYPE_ALBUM:
                            List<Song> albumList = DataHelper.getAllAlbum();
                            for (Song album : albumList) {
                                fillMediaItems(mediaItems, Integer.toString(TYPE_ALBUM_SONGS) + Long.toString(album.albumId), album.title, MuzikoUtils.getAlbumArtUri(album.albumId), album.artistName, MediaBrowser.MediaItem.FLAG_BROWSABLE);
                            }
                            break;
                        case TYPE_SONG:
                            List<Song> songList = DataHelper.getAllTrack();
                            for (Song song : songList) {
                                fillMediaItems(mediaItems, String.valueOf(song.id), song.title, MuzikoUtils.getAlbumArtUri(song.albumId), song.artistName, MediaBrowser.MediaItem.FLAG_PLAYABLE);
                            }
                            break;
                        case TYPE_ALBUM_SONGS:
                          /*  List<Song> albumSongList = DataHelper.getSongsForAlbum(Long.parseLong(parentId.substring(1)));
                            for (Song song : albumSongList) {
                                fillMediaItems(mediaItems, String.valueOf(song.id), song.title, MuzikoUtils.getAlbumArtUri(song.albumId), song.artistName, MediaBrowser.MediaItem.FLAG_PLAYABLE);
                            }*/
                            break;
                        case TYPE_ARTIST_SONG_ALBUMS:
                            fillMediaItems(mediaItems, Integer.toString(TYPE_ARTIST_ALL_SONGS) + Long.parseLong(parentId.substring(1)), "All songs", Uri.parse("android.resource://" +
                                    "com.whitebird.music/minmap/app_icon"), "All songs by artist", MediaBrowser.MediaItem.FLAG_BROWSABLE);
                            List<Song> artistAlbums = DataHelper.getAlbumsForArtist(Long.parseLong(parentId.substring(1)));
                            for (Song album : artistAlbums) {
                                String songCount = MuzikoUtils.makeLabel(mContext, R.plurals.Nsongs, album.songsCount);
                                fillMediaItems(mediaItems, Integer.toString(TYPE_ALBUM_SONGS) + Long.toString(album.albumId), album.albumName, MuzikoUtils.getAlbumArtUri(album.albumId), songCount, MediaBrowser.MediaItem.FLAG_BROWSABLE);

                            }
                            break;
                        case TYPE_ARTIST_ALL_SONGS:
                            List<Song> artistSongs = DataHelper.getSongsForArtist(Long.parseLong(parentId.substring(1)));
                            for (Song song : artistSongs) {
                                fillMediaItems(mediaItems, String.valueOf(song.id), song.title, MuzikoUtils.getAlbumArtUri(song.albumId), song.albumName, MediaBrowser.MediaItem.FLAG_PLAYABLE);
                            }
                            break;
                        case TYPE_PLAYLIST:
                           /* List<Playlist> playlistList = PlaylistLoader.getPlayList();
                            for (Playlist playlist : playlistList) {
                                String songCount = MuzikoUtils.makeLabel(mContext, R.plurals.Nsongs, playlist.songCount);
                                fillMediaItems(mediaItems, Integer.toString(TYPE_PLAYLIST_ALL_SONGS) + Long.toString(playlist.id), playlist.name,
                                        Uri.parse("android.resource://" +
                                                "naman14.timber/minmap/app_icon"), songCount, MediaBrowser.MediaItem.FLAG_BROWSABLE);
                            }
                            break;
                        case TYPE_PLAYLIST_ALL_SONGS:
                            List<Song> playlistSongs = PlaylistSongLoader.getSongsInPlaylist(mContext, Long.parseLong(parentId.substring(1)));
                            for (Song song : playlistSongs) {
                                fillMediaItems(mediaItems, String.valueOf(song.id), song.title, MuzikoUtils.getAlbumArtUri(song.albumId), song.albumName, MediaBrowser.MediaItem.FLAG_PLAYABLE);
                            }
                            break;*/

                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                result.sendResult(mediaItems);
            }
        }.execute();

    }

    private void fillMediaItems(List<MediaBrowser.MediaItem> mediaItems, String mediaId, String title, Uri icon, String subTitle, int playableOrBrowsable) {
        mediaItems.add(new MediaBrowser.MediaItem(
                new MediaDescription.Builder()
                        .setMediaId(mediaId)
                        .setTitle(title)
                        .setIconUri(icon)
                        .setSubtitle(subTitle)
                        .build(), playableOrBrowsable
        ));
    }

    private final class MediaSessionCallback extends MediaSession.Callback {

        @Override
        public void onPlay() {
            setSessionActive();
        }

        @Override
        public void onSeekTo(long position) {

        }

        @Override
        public void onPlayFromMediaId(final String mediaId, Bundle extras) {
            ArrayList<Song> songs=new ArrayList<>();
            songs.add(DataHelper.getSong( Long.parseLong(mediaId)));
            setSessionActive();
            MusicPlayer.playAll(songs, 0, false);
        }

        @Override
        public void onPause() {

        }

        @Override
        public void onStop() {
            setSessionInactive();
        }

        @Override
        public void onSkipToNext() {

        }

        @Override
        public void onSkipToPrevious() {

        }

        @Override
        public void onFastForward() {

        }

        @Override
        public void onRewind() {

        }

        @Override
        public void onCustomAction(String action, Bundle extras) {

        }
    }

}
