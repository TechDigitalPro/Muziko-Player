package com.whitebird.music.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import com.whitebird.music.MusicPlayer;
import com.whitebird.music.R;

import com.whitebird.music.control.metrialRenge.IRangeBarFormatter;
import com.whitebird.music.control.metrialRenge.RangeBar;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.models.Song;
import com.whitebird.music.models.StartTimeWrapper;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.widgets.PlayPauseButton;

import java.io.IOException;

public class PlayFrom {
    private Context mcontext;
    private MediaPlayer mediaPlayer;

    private RangeBar rangebar;
    private boolean songPlaying = false;
    private int start = 0;
    private int end = 0;
    private PlayPauseButton playpause;
    private boolean mediaPlyerReleased = false;

    public void open(final Context context, final Song queue) {
        mcontext = context;

        View view = LayoutInflater.from(mcontext).inflate(R.layout.dialog_play_from, null, false);
        RelativeLayout buttonlayout = view.findViewById(R.id.buttonlayout);

        rangebar = view.findViewById(R.id.rangebar);

        rangebar = view.findViewById(R.id.rangebar);
        rangebar.setBarColor(ContextCompat.getColor(context, R.color.gray));
        rangebar.setPinColor(Config.accentColor(context, Helpers.getATEKey(context)));
        rangebar.setTickColor(ContextCompat.getColor(context, R.color.gray));
        rangebar.setDrawTicks(false);
        rangebar.setConnectingLineColor(Config.accentColor(context, Helpers.getATEKey(context)));
        rangebar.setSelectorColor(Config.accentColor(context, Helpers.getATEKey(context)));

        playpause = view.findViewById(R.id.playpause);
        playpause.setColor(Config.accentColor(context, Helpers.getATEKey(context)));

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(queue.data);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        rangebar.setDisableRightThumb(true);

        rangebar.setTickEnd((queue.duration) / 1000);

      //  Song queueItem = TrackRealmHelper.getTrack(queue.data);

      /*  if (queueItem.startFrom > 0) {
            start = queueItem.startFrom / 1000;
            rangebar.setRangePinsByValue(start, rangebar.getRightIndex());
        } else {
            start = rangebar.getLeftIndex() * 1000;
        }*/
        end = rangebar.getRightIndex() * 1000;
        rangebar.setOnRangeBarChangeListener(
                new RangeBar.OnRangeBarChangeListener() {
                    @Override
                    public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                        start = leftPinIndex * 1000;
                        end = rightPinIndex * 1000;
                        int seekto = 0;
                        int currentposition = mediaPlayer.getCurrentPosition();
                        if (currentposition / 1000 != leftPinIndex) {
                            seekto = leftPinIndex * 1000;
                            mediaPlayer.seekTo(seekto);
                        }
                        if (mediaPlayer.getCurrentPosition() / 1000 > rightPinIndex) {
                            mediaPlayer.pause();

                            mediaPlayer.seekTo(leftPinIndex * 1000);
                        }
                    }
                });

        rangebar.setFormatter(new IRangeBarFormatter() {
            @Override
            public String format(String value) {
                double d = Double.parseDouble(value);
                int totalSecs = (int) d;
                int hours = totalSecs / 3600;
                int minutes = (totalSecs % 3600) / 60;
                int seconds = totalSecs % 60;

                return String.format("%02d:%02d", minutes, seconds);
            }
        } );
        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!playpause.isPlayed()) {
                    playpause.setPlayed(true);
                    playpause.startAnimation();
                    mediaPlayer.start();
                } else {
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(rangebar.getLeftIndex() * 1000);
                    playpause.setPlayed(false);
                    playpause.startAnimation();
                }


            }
        });


        if (MusicPlayer.isPlaying())
            MusicPlayer.playOrPause();

        new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                .neutralColorRes(R.color.dialog_negetive_button)
                .positiveColor(Config.accentColor(context, Helpers.getATEKey(context)))
                .customView(view, false)
                .autoDismiss(true)
                .positiveText("Set")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DataHelper.setStartTime(new StartTimeWrapper(queue.id,Long.parseLong(rangebar.getLeftPinValue())*1000));
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlyerReleased = true;
                        dialog.dismiss();
                    }
                } )
                .neutralText("Cancel")
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (mediaPlayer != null) {
                            if (!mediaPlyerReleased) {
                                if (mediaPlayer.isPlaying()) {
                                    mediaPlayer.stop();
                                }
                                mediaPlayer.release();
                            }
                        }

                          /*  if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_STOPPED
                                    && songPlaying) {
                                AppController.Instance().serviceResume(false);
                            }*/
                    }
                })
                .show();
    }
}
