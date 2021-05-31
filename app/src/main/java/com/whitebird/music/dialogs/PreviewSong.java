package com.whitebird.music.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.R;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.Utils;
import com.whitebird.music.widgets.PlayPauseButton;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class PreviewSong {
    private static final int aimStart = 10000;
    private Activity mcontext;
    private Timer timer = null;
    private int start = 0;
    private MediaPlayer mediaPlayer;
    /* private ImageButton btnPause;
     private ImageButton btnPlay;*/
    private TextView startText;
    private PlayPauseButton playpause;
    private SeekBar playerSeekBar;
    private boolean userTouch = false;
    private boolean songPlaying = false;

    public void open(final Activity context, final Song queue) {
        mcontext = context;
        songPlaying = false;
        View view = LayoutInflater.from(mcontext).inflate(R.layout.dialog_preview_track, null, false);

        RelativeLayout buttonlayout = view.findViewById(R.id.buttonlayout);

        playerSeekBar = view.findViewById(R.id.playerSeekBar);
        startText = view.findViewById(R.id.startText);
        playpause = view.findViewById(R.id.playpause);
        playpause.setColor(Config.accentColor(context, Helpers.getATEKey(context)));

        songPlaying = MusicPlayer.isPlaying();
        if (songPlaying)
            MusicPlayer.playOrPause();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(queue.data);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int aimEnd = 20000;
        int end = 0;
        if (queue.duration > aimEnd) {
            start = aimStart;
            end = aimEnd;
        } else {
            start = (int) (queue.duration - aimStart);
            end = (int) queue.duration;
        }

        playerSeekBar.setMax(aimStart);

        if (playerSeekBar != null) {

            playerSeekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Config.accentColor(context, Helpers.getATEKey(context)), PorterDuff.Mode.SRC_IN));
            playerSeekBar.getThumb().setColorFilter(new PorterDuffColorFilter(Config.accentColor(context, Helpers.getATEKey(context)), PorterDuff.Mode.SRC_IN));

            LayerDrawable progressBarDrawable = (LayerDrawable) playerSeekBar.getProgressDrawable();
            Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
            Drawable progressDrawable = progressBarDrawable.getDrawable(1);

            backgroundDrawable.setColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_IN);
            progressDrawable.setColorFilter(Config.accentColor(context, Helpers.getATEKey(context)), PorterDuff.Mode.SRC_IN);
        }

        playerSeekBar.setProgress(0);
        playerSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // TODO Auto-generated method stub

                        startText.setText(Utils.getDuration(progress));

                        if (fromUser) {
                            mediaPlayer.seekTo(progress + aimStart);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        userTouch = true;
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        userTouch = false;
                    }
                });
        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!playpause.isPlayed()) {
                    playpause.setPlayed(true);
                    playpause.startAnimation();
                    mediaPlayer.start();
                    if (MusicPlayer.isPlaying()) {
                        MusicPlayer.playOrPause();
                    }
                } else {
                    if (songPlaying) {
                        MusicPlayer.playOrPause();
                    }
                    mediaPlayer.pause();
                    playpause.setPlayed(false);
                    playpause.startAnimation();
                }


            }
        });

        mediaPlayer.seekTo(start);
        mediaPlayer.start();
        playpause.setPlayed(true);
        playpause.startAnimation();
        timerStart();
        new MaterialDialog.Builder(mcontext)
                .theme(Theme.LIGHT)
                .negativeColor(Config.accentColor(context, Helpers.getATEKey(context)))
                .titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                .neutralColorRes(R.color.dialog_negetive_button)
                .positiveColor(Config.accentColor(context, Helpers.getATEKey(context)))
                .customView(view, false)
                .autoDismiss(false)
                .neutralText("Cancel")
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        if (songPlaying && !MusicPlayer.isPlaying()) {
                            MusicPlayer.playOrPause();
                        }
                    }
                }).dismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                timerStop();
                mediaPlayer.stop();
                mediaPlayer.release();
                if (songPlaying && !MusicPlayer.isPlaying()) {
                    MusicPlayer.playOrPause();
                }
            }
        })
                .show();

    }

    private void timerStart() {
        if (timer == null)
            timer = new Timer();
        timer.scheduleAtFixedRate(new PreviewTimerTask(), 0, 150);
    }

    private void timerStop() {
        try {
            if (timer != null)
                timer.cancel();
            timer = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PreviewTimerTask extends TimerTask {

        public void run() {
            if (mediaPlayer != null && !userTouch) {
                if (mediaPlayer.isPlaying()) {
                    int progress = 0;
                    int position = mediaPlayer.getCurrentPosition();
                    progress = position - aimStart;
                    playerSeekBar.setProgress(progress);
                    if (progress > aimStart) {
                        mediaPlayer.pause();
                        if (songPlaying) {
                            MusicPlayer.playOrPause();
                        }
                        mediaPlayer.seekTo(start);
                        mcontext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playerSeekBar.setProgress(0);
                                playpause.setPlayed(false);
                                playpause.startAnimation();
                            }
                        });

                    }
                }
            }
        }
    }
}
