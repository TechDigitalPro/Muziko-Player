package com.whitebird.music.service.playback;


import androidx.annotation.Nullable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface Playback {

    boolean setDataSource(String path);

    void setNextDataSource(@Nullable String path);

    void setCallbacks(PlaybackCallbacks callbacks);

    boolean isInitialized();

    boolean start();

    void stop();

    void release();

    boolean pause();

    boolean isPlaying();

    int duration();

    int position();

    long seek(long whereto);

    boolean setVolume(float vol);

    boolean setAudioSessionId(int sessionId);

    int getAudioSessionId();
 //   MediaPlayer getPlayer();

    interface PlaybackCallbacks {
        void onTrackWentToNext();
        void onTrackEnded();
    }
}
