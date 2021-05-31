

package com.whitebird.music.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.whitebird.music.MusicPlayer;
import com.whitebird.music.service.MusicService;


public final class PreferencesUtility {

    public static final String ARTIST_SORT_ORDER = "artist_sort_order";
    public static final String ARTIST_SONG_SORT_ORDER = "artist_song_sort_order";
    public static final String ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order";
    public static final String ALBUM_SORT_ORDER = "album_sort_order";
    public static final String ALBUM_SONG_SORT_ORDER = "album_song_sort_order";
    public static final String SONG_SORT_ORDER = "song_sort_order";
    public static final String HOME_SORT_ORDER = "home_sort_order";
    public static final String GENRE_SORT_ORDER = "genre_sort_order";
    private static final String NOW_PLAYING_SELECTOR = "now_paying_selector";
    private static final String TOGGLE_ANIMATIONS = "toggle_animations";
    private static final String TOGGLE_SYSTEM_ANIMATIONS = "toggle_system_animations";
    private static final String TOGGLE_ARTIST_GRID = "toggle_artist_grid";
    private static final String TOGGLE_ALBUM_GRID = "toggle_album_grid";
    private static final String TOGGLE_GENRE_GRID = "toggle_genre_grid";
    private static final String TOGGLE_PLAYLIST_VIEW = "toggle_playlist_view";
    private static final String TOGGLE_SHOW_AUTO_PLAYLIST = "toggle_show_auto_playlist";
    private static final String LAST_FOLDER = "last_folder";

    private static final String TOGGLE_HEADPHONE_PAUSE = "toggle_headphone_pause";
    private static final String THEME_PREFERNCE = "theme_preference";
    private static final String START_PAGE_INDEX = "start_page_index";
    private static final String START_PAGE_PREFERENCE_LASTOPENED = "start_page_preference_latopened";
    private static final String NOW_PLAYNG_THEME_VALUE = "now_playing_theme_value";
    private static final String TOGGLE_XPOSED_TRACKSELECTOR = "toggle_xposed_trackselector";
    public static final String LAST_ADDED_CUTOFF = "last_added_cutoff";
    public static final String GESTURES = "gestures";

    public static final String FULL_UNLOCKED = "full_version_unlocked";

    private static final String SHOW_LOCKSCREEN_ALBUMART = "show_albumart_lockscreen";
    private static final String ARTIST_ALBUM_IMAGE = "artist_album_image";
    private static final String ARTIST_ALBUM_IMAGE_MOBILE = "artist_album_image_mobile";
    private static final String ALWAYS_LOAD_ALBUM_IMAGES_LASTFM = "always_load_album_images_lastfm";

    private static PreferencesUtility sInstance;

    private static SharedPreferences mPreferences;
    private static Context context;
    private ConnectivityManager connManager = null;

    public PreferencesUtility(final Context context) {
        this.context = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static final PreferencesUtility getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesUtility(context.getApplicationContext());
        }
        return sInstance;
    }


    public void setOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public boolean isArtistsInGrid() {
        return mPreferences.getBoolean(TOGGLE_ARTIST_GRID, true);
    }

    public void setArtistsInGrid(final boolean b) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(TOGGLE_ARTIST_GRID, b);
        editor.apply();
    }

    public boolean isAlbumsInGrid() {
        return mPreferences.getBoolean(TOGGLE_ALBUM_GRID, true);
    }
    public boolean isGenreInGrid() {
        return mPreferences.getBoolean(TOGGLE_GENRE_GRID, true);
    }

    public void setAlbumsInGrid(final boolean b) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(TOGGLE_ALBUM_GRID, b);
        editor.apply();
    }
    public void setGenreInGrid(final boolean b) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(TOGGLE_GENRE_GRID, b);
        editor.apply();
    }
    public void unregisterOnSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }
    public boolean pauseEnabledOnDetach() {
        return mPreferences.getBoolean(TOGGLE_HEADPHONE_PAUSE, true);
    }


    public boolean getStatus(String name) {
        boolean status;
        status = mPreferences.getBoolean(name, false);
        return status;
    }

    public static void setStatus(String name, boolean istrue) {

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(name, istrue);
        editor.commit();
    }


    public String getTheme() {
        return mPreferences.getString(THEME_PREFERNCE, "light");
    }

    public int getStartPageIndex() {
        return mPreferences.getInt(START_PAGE_INDEX, 0);
    }

    public void setStartPageIndex(final int index) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(START_PAGE_INDEX, index);
        editor.apply();
    }

    public void setLastOpenedAsStartPagePreference(boolean preference) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(START_PAGE_PREFERENCE_LASTOPENED, preference);
        editor.apply();
    }

    public boolean lastOpenedIsStartPagePreference() {
        return mPreferences.getBoolean(START_PAGE_PREFERENCE_LASTOPENED, true);
    }

    private void setSortOrder(final String key, final String value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public final String getArtistSortOrder() {
        return mPreferences.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }

    public void setArtistSortOrder(final String value) {
        setSortOrder(ARTIST_SORT_ORDER, value);
    }

    public final String getArtistSongSortOrder() {
        return mPreferences.getString(ARTIST_SONG_SORT_ORDER,
                SortOrder.ArtistSongSortOrder.SONG_A_Z);
    }

    public void setArtistSongSortOrder(final String value) {
        setSortOrder(ARTIST_SONG_SORT_ORDER, value);
    }

    public final String getArtistAlbumSortOrder() {
        return mPreferences.getString(ARTIST_ALBUM_SORT_ORDER,
                SortOrder.ArtistAlbumSortOrder.ALBUM_A_Z);
    }

    public void setArtistAlbumSortOrder(final String value) {
        setSortOrder(ARTIST_ALBUM_SORT_ORDER, value);
    }

    public final String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public void setAlbumSortOrder(final String value) {
        setSortOrder(ALBUM_SORT_ORDER, value);
    }

    public final String getGenreSortOrder() {
        return mPreferences.getString(GENRE_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public void setGenreSortOrder(final String value) {
        setSortOrder(GENRE_SORT_ORDER, value);
    }

    public final String getAlbumSongSortOrder() {
        return mPreferences.getString(ALBUM_SONG_SORT_ORDER,
                SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
    }

    public void setAlbumSongSortOrder(final String value) {
        setSortOrder(ALBUM_SONG_SORT_ORDER, value);
    }

    public final String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }

    public void setSongSortOrder(final String value) {
        setSortOrder(SONG_SORT_ORDER, value);
    }

    public void setHomeSortOrder(final String value) {
        setSortOrder(HOME_SORT_ORDER, value);
    }
    public final String getHomeSortOrder() {
        return mPreferences.getString(HOME_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }
    public final boolean didNowplayingThemeChanged() {
        return mPreferences.getBoolean(NOW_PLAYNG_THEME_VALUE, false);
    }

    public void setNowPlayingThemeChanged(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(NOW_PLAYNG_THEME_VALUE, value);
        editor.apply();
    }

    public boolean getXPosedTrackselectorEnabled() {
        return mPreferences.getBoolean(TOGGLE_XPOSED_TRACKSELECTOR, false);
    }

    public int getPlaylistView() {
        return mPreferences.getInt(TOGGLE_PLAYLIST_VIEW ,1);
    }

    public void setPlaylistView(final int i) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(TOGGLE_PLAYLIST_VIEW, i);
        editor.apply();
    }

    public boolean showAutoPlaylist() {
        return mPreferences.getBoolean(TOGGLE_SHOW_AUTO_PLAYLIST, true);
    }

    public void setToggleShowAutoPlaylist(final boolean b) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(TOGGLE_SHOW_AUTO_PLAYLIST, b);
        editor.apply();
    }

    /** @parm lastAddedMillis timestamp in millis used as a cutoff for last added playlist */
    public void setLastAddedCutoff(long lastAddedMillis) {
        mPreferences.edit().putLong(LAST_ADDED_CUTOFF, lastAddedMillis).apply();
    }

    public long getLastAddedCutoff() {
        return mPreferences.getLong(LAST_ADDED_CUTOFF, 0L);
    }

    public boolean isGesturesEnabled() {
        return mPreferences.getBoolean(GESTURES, true);
    }

    public void storeLastFolder(String path) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(LAST_FOLDER, path);
        editor.apply();
    }

    public String getLastFolder() {
        return mPreferences.getString(LAST_FOLDER, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath());
    }

    public boolean fullUnlocked() {
        return mPreferences.getBoolean(FULL_UNLOCKED, false);
    }

    public void setFullUnlocked(final boolean b) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(FULL_UNLOCKED, b);
        editor.apply();
    }



    public boolean isTimer() {
        return mPreferences.getBoolean("timer", false);
    }

    public void setTimer(final boolean b) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("timer", b);
        editor.apply();
    }



    public boolean getSetAlbumartLockscreen() {
        return mPreferences.getBoolean(SHOW_LOCKSCREEN_ALBUMART, true);
    }

    public void updateService(Bundle extras) {
        if(!MusicPlayer.isPlaybackServiceConnected())return;
        final Intent intent = new Intent(context, MusicService.class);
        intent.setAction(MusicService.UPDATE_PREFERENCES);
        intent.putExtras(extras);
        context.startService(intent);
    }

    public boolean loadArtistAndAlbumImages() {
        if (mPreferences.getBoolean(ARTIST_ALBUM_IMAGE, true)) {
            if (!mPreferences.getBoolean(ARTIST_ALBUM_IMAGE_MOBILE, true)) {
                if (connManager == null) connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = connManager.getActiveNetworkInfo();
                return ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI;
            }
            return true;
        }
        return false;
    }

    public boolean alwaysLoadAlbumImagesFromLastfm() {
        return mPreferences.getBoolean(ALWAYS_LOAD_ALBUM_IMAGES_LASTFM, false);
    }

    public void setSleepTimeLastSong(boolean b) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("SleepTimeLastSong", b);
        editor.apply();
    }

    public boolean getSleepTimeLastSong() {
        return mPreferences.getBoolean("SleepTimeLastSong", false);
    }
}

