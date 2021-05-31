package com.whitebird.music.listeners;

/*
 * Listens for playback changes to send the the fragments bound to this activity
 */

public interface MusicStateListener {

    void onServiceConnected();

    void onServiceDisconnected();

    void onQueueChanged();

    void onPlayingMetaChanged();

    void onPlayStateChanged();

    void onRepeatModeChanged();

    void onShuffleModeChanged();

    void onMediaStoreChanged();
}