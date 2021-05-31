/*

package com.whitebird.music.service;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.whitebird.music.service.playback.Playback;

public class ExoMultiPlayer implements Playback, Player.EventListener {

    private static final String TAG = ExoMultiPlayer.class.getSimpleName();


    private ExoPlayer mCurrentPlayer = null;
    private Context context;
    private static ExoMultiPlayer instance;
    private PlaybackCallbacks callbacks;
    DataSource.Factory dataSourceFactory;
    ExtractorsFactory extractorsFactory;

    public ExoMultiPlayer(Context context) {
        try {
            this.context = context;
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Muziko"), bandwidthMeter);
            extractorsFactory = new DefaultExtractorsFactory();

            mCurrentPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
            mCurrentPlayer.addListener(this);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_ENDED) {
            if (callbacks != null)
                callbacks.onTrackEnded();
        }

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        mCurrentPlayer.release();
        new ExoMultiPlayer(context);
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public boolean setDataSource(String path) {
        //isNewSong = true;
        //setBuffer(true);

        String url = "null";
        try {
            url = path.replace(" ", "%20");
            MediaSource videoSource = new ExtractorMediaSource(Uri.parse(url),
                    dataSourceFactory, extractorsFactory, null, null);
            mCurrentPlayer.prepare(videoSource);
            //  mCurrentPlayer.setPlayWhenReady(true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    @Override
    public void setNextDataSource(@Nullable String path) {


    }

    @Override
    public void setCallbacks(PlaybackCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public boolean isInitialized() {
        return mCurrentPlayer != null;
    }

    @Override
    public boolean start() {
        try {
            mCurrentPlayer.setPlayWhenReady(true);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Override
    public void stop() {
        mCurrentPlayer.setPlayWhenReady(false);
        mCurrentPlayer.stop();
        mCurrentPlayer.release();
        mCurrentPlayer = null;


    }

    @Override
    public void release() {
        stop();

    }

    @Override
    public boolean pause() {
        try {
            mCurrentPlayer.setPlayWhenReady(false);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Override
    public boolean isPlaying() {
        return mCurrentPlayer != null && mCurrentPlayer.getPlayWhenReady();
    }

    @Override
    public int duration() {
        return (int) mCurrentPlayer.getDuration();
    }

    @Override
    public int position() {
        return (int) mCurrentPlayer.getCurrentPosition();
    }

    @Override
    public long seek(long whereto) {
        mCurrentPlayer.seekTo((int) whereto);
        return whereto;
    }

    @Override
    public boolean setVolume(float vol) {
        //  mCurrentPlayer.setVolume
        return false;
    }

    @Override
    public boolean setAudioSessionId(int sessionId) {
        // mCurrentPlayer.set
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
*/
