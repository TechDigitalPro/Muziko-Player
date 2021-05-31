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
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.Utils;
import com.whitebird.music.widgets.PlayPauseButton;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayRingTone {
    private static final int aimStart = 0;
    Song queue;
    private Activity mcontext;
    private Timer timer = null;
    private int start = 0;
    private MediaPlayer mediaPlayer;
    private PlayPauseButton playpause;
    private TextView startText, endText, title;
    private SeekBar playerSeekBar;
    private boolean userTouch = false;
    private boolean songPlaying = false;

    public void open(final Activity context, final Song queue) {
        mcontext = context;
        this.queue = queue;
        View view =
                LayoutInflater.from(mcontext).inflate(R.layout.dialog_play_song, null, false);

        RelativeLayout buttonlayout = view.findViewById(R.id.buttonlayout);

        playerSeekBar = view.findViewById(R.id.playerSeekBar);
        startText = view.findViewById(R.id.startText);
        endText = view.findViewById(R.id.endText);
        title = view.findViewById(R.id.title);
        playpause = view.findViewById(R.id.playpause);
        playpause.setColor(Config.accentColor(context, Helpers.getATEKey(context)));
        endText.setText(MuzikoUtils.makeShortTimeString(mcontext, queue.duration / 1000));
        title.setText(queue.title);
        if (MusicPlayer.isPlaying())
            MusicPlayer.playOrPause();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(queue.data);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (playerSeekBar != null) {

            playerSeekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Config.accentColor(context, Helpers.getATEKey(context)), PorterDuff.Mode.SRC_IN));
            playerSeekBar.getThumb().setColorFilter(new PorterDuffColorFilter(Config.accentColor(context, Helpers.getATEKey(context)), PorterDuff.Mode.SRC_IN));

            LayerDrawable progressBarDrawable = (LayerDrawable) playerSeekBar.getProgressDrawable();
            Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
            Drawable progressDrawable = progressBarDrawable.getDrawable(1);

            backgroundDrawable.setColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_IN);
            progressDrawable.setColorFilter(Config.accentColor(context, Helpers.getATEKey(context)), PorterDuff.Mode.SRC_IN);
        }

        playerSeekBar.setMax((int) queue.duration);
        playerSeekBar.setProgress(0);
        playerSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


                        startText.setText(Utils.getDuration(progress + 1000));


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
                } else {
                    mediaPlayer.pause();
                    playpause.setPlayed(false);
                    playpause.startAnimation();
                }


            }
        });

        mediaPlayer.seekTo(start);
        mediaPlayer.start();
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
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        timerStop();
                        mediaPlayer.stop();
                        mediaPlayer.release();
                           /* if (PlayerConstants.QUEUE_STATE
                                    != PlayerConstants.QUEUE_STATE_STOPPED
                                    && songPlaying) {
                                AppController.Instance().serviceResume(false);
                            }*/
                    }
                })
                .show();

    }

    private void timerStart() {
        if (timer == null) timer = new Timer();

        timer.scheduleAtFixedRate(new PreviewTimerTask(), 0, 150);
    }

    private void timerStop() {
        try {
            if (timer != null) timer.cancel();

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

                    if (progress > queue.duration) {
                        mediaPlayer.pause();
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
