package com.whitebird.music.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.control.metrialRenge.IRangeBarFormatter;
import com.whitebird.music.control.metrialRenge.RangeBar;
import com.whitebird.music.cutter.ringtone_lib.WaveformView;
import com.whitebird.music.cutter.ringtone_lib.soundfile.CheapSoundFile;
import com.whitebird.music.helpers.FileHelper;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.widgets.PlayPauseButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;



/*
 Created by dev on 13/09/2016.*/


public class ShareRingtone {
    private final Handler handler = new Handler();
    private final ArrayList<Song> selectedItems = new ArrayList<>();
    private Activity mcontext;
    private WaveformView mWaveformView;
    private CheapSoundFile mSoundFile;
    private Timer timer = null;
    private MediaPlayer mediaPlayer;

    private RangeBar rangebar;
    private EditText ringtoneName;
    private CheckBox notSaveCheckbox;
    private boolean songPlaying = false;
    private int start = 0;
    private int end = 0;
    private boolean mediaPlayerReleased = false;
    private MaterialDialog shareDialog;
    private MaterialDialog progressDialog;
    private Song selectedItem;
    private PlayPauseButton playpause;

    public void open(final Activity context, final Song queue) {
        mcontext = context;


        View view =
                LayoutInflater.from(mcontext).inflate(R.layout.dialog_share_ringtone, null, false);
        mWaveformView = view.findViewById(R.id.waveform);
        RelativeLayout buttonlayout = view.findViewById(R.id.buttonlayout);


        rangebar = view.findViewById(R.id.rangebar);
        rangebar.setBarColor(ContextCompat.getColor(context, R.color.gray));
        rangebar.setPinColor(Config.accentColor(context, Helpers.getATEKey(context)));
        rangebar.setTickColor(ContextCompat.getColor(context, R.color.gray));
        rangebar.setDrawTicks(false);
        rangebar.setConnectingLineColor(Config.accentColor(context, Helpers.getATEKey(context)));
        rangebar.setSelectorColor(Config.accentColor(context, Helpers.getATEKey(context)));

        final View shareRingtoneLayout =
                LayoutInflater.from(mcontext)
                        .inflate(R.layout.dialog_save_share_ringtone, null, false);
        notSaveCheckbox = shareRingtoneLayout.findViewById(R.id.notSaveCheckbox);
        ringtoneName = shareRingtoneLayout.findViewById(R.id.ringtoneName);

        playpause = view.findViewById(R.id.playpause);
        playpause.setColor(Config.accentColor(context, Helpers.getATEKey(context)));

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


        progressDialog =
                new MaterialDialog.Builder(mcontext)
                        .title("Sharing AU-Clip")
                        .content("Please wait for AU-Clip to be generated")
                        .progress(true, 0)
                        .build();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(queue.data);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        timerStart();

        rangebar.setTickEnd((queue.duration) / 1000);

        start = rangebar.getLeftIndex() * 1000;
        end = rangebar.getRightIndex() * 1000;
        rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
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
                    // btnPlay.setVisibility(View.VISIBLE);
                    //btnPause.setVisibility(View.GONE);
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
               mediaPlayer.seekTo(rangebar.getLeftIndex() * 1000);
                    playpause.setPlayed(false);
                    playpause.startAnimation();
                }


            }
        });


        shareDialog =
                new MaterialDialog.Builder(context)
                        .theme(Theme.LIGHT)



                        .negativeColor(Config.accentColor(context, Helpers.getATEKey(context)))
                        .titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                        .neutralColorRes(R.color.dialog_negetive_button)
                        .positiveColor(Config.accentColor(context, Helpers.getATEKey(context)))
                        .customView(view, false)
                        .autoDismiss(false)
                        .positiveText("Share")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (Double.parseDouble(rangebar.getRightPinValue())
                                        - Double.parseDouble(rangebar.getLeftPinValue())
                                        < 10) {
//                                        AppController.toast(context, "AU-Clip must be at least 10 seconds");
                                } else {
                                    dialog.dismiss();
                                    timerStop();
                                    if (mediaPlayer != null) {
                                        if (!mediaPlayerReleased) {
                                            if (mediaPlayer.isPlaying()) {
                                                mediaPlayer.stop();
                                            }
                                            mediaPlayer.release();
                                            mediaPlayerReleased = true;
                                        }
                                    }

                                    progressDialog.show();


                                    mcontext.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            shareRingtone(
                                                    context,
                                                    queue,
                                                    "",
                                                    rangebar.getLeftIndex(),
                                                    rangebar.getRightIndex());
                                        }
                                    });
                                      /*  AsyncJob.doInBackground(
                                                () -> {
                                                    shareRingtone(
                                                            context,
                                                            queue,
                                                            "",
                                                            rangebar.getLeftIndex(),
                                                            rangebar.getRightIndex());
                                                });*/
                                }
                            }
                        }).negativeText("Send")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (Double.parseDouble(rangebar.getRightPinValue())
                                        - Double.parseDouble(rangebar.getLeftPinValue())
                                        < 10) {
                                    MuzikoApp.toast(context, "AU-Clip must be at least 10 seconds");
                                } else {
                                    dialog.dismiss();
                                    timerStop();
                                    if (mediaPlayer != null) {
                                        if (!mediaPlayerReleased) {
                                            if (mediaPlayer.isPlaying()) {
                                                mediaPlayer.stop();
                                            }
                                            mediaPlayer.release();
                                            mediaPlayerReleased = true;
                                        }
                                    }

//                                    MuzikoApp.toast(mcontext, "Under development !");
                                    return;




                                    /*                                          Uncomment when development                                               */




                                        /*   new MaterialDialog.Builder(context)
                                            .theme(Theme.LIGHT)
                                            .titleColorRes(R.color.normal_blue)
                                            .neutralColorRes(R.color.normal_blue)
                                            .positiveColorRes(R.color.normal_blue)
                                            .negativeColorRes(R.color.dialog_negetive_button)
                                            .title("Set AU-Clip name")
                                            .customView(shareRingtoneLayout, false)
                                            .autoDismiss(false)
                                            .negativeText("Cancel")
                                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog namedialog, @NonNull DialogAction namewhich) {
                                                    namedialog.dismiss();
                                                }
                                            })
                                            .positiveText("Set")
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog namedialog, @NonNull DialogAction which) {
                                                    namedialog.dismiss();
                                                    progressDialog.show();

                                                    if (notSaveCheckbox.isChecked()) {

                                                        mcontext.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                sendRingtone(
                                                                        context,
                                                                        queue,
                                                                        "",
                                                                        rangebar
                                                                                .getLeftIndex(),
                                                                        rangebar
                                                                                .getRightIndex());
                                                            }
                                                        });

                                                    } else {
                                                        mcontext.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                sendRingtone(
                                                                        context,
                                                                        queue,
                                                                        ringtoneName
                                                                                .getText()
                                                                                .toString(),
                                                                        rangebar
                                                                                .getLeftIndex(),
                                                                        rangebar
                                                                                .getRightIndex());
                                                            }
                                                        });

                                                    }
                                                }
                                            })
                                            .show();*/
                                }
                            }
                        })
                        .neutralText("Cancel")
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                                MyApplication.pauseDeletingTempRingtone = false;
                                timerStop();
                                if (mediaPlayer != null) {
                                    if (!mediaPlayerReleased) {
                                        if (mediaPlayer.isPlaying()) {
                                            mediaPlayer.stop();
                                        }
                                        mediaPlayer.release();
                                        mediaPlayerReleased = true;
                                    }
                                }
                                dialog.dismiss();

//                                if (PlayerConstants.QUEUE_STATE
//                                        != PlayerConstants.QUEUE_STATE_STOPPED
//                                        && songPlaying) {
//                                    AppController.Instance().serviceResume(false);
//                                }
                            }
                        })
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                timerStop();
                                if (mediaPlayer != null) {
                                    if (!mediaPlayerReleased) {
                                        if (mediaPlayer.isPlaying()) {
                                            mediaPlayer.stop();
                                        }
                                        mediaPlayer.release();
                                        mediaPlayerReleased = true;
                                    }
                                }
                                dialogInterface.dismiss();

                               /* if (PlayerConstants.QUEUE_STATE
                                        != PlayerConstants.QUEUE_STATE_STOPPED
                                        && songPlaying) {
                                    AppController.Instance().serviceResume(false);
                                }*/
                            }
                        })
                        .build();

        shareDialog.show();
    }

    private void timerStart() {
        if (timer == null) timer = new Timer();

        timer.scheduleAtFixedRate(new PreviewTimerTask(), 0, 1000);
    }

    private void timerStop() {
        try {
            if (timer != null) timer.cancel();

            timer = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shareRingtone(
            final Context context,
            final Song queueItem,
            final CharSequence title,
            final int start,
            final int end) {
        File mFile = new File(queueItem.data);
        String mExtension = FileHelper.getExtensionFromFilename(queueItem.data);

        final String outPath =
                FileHelper.makeRingtoneFilename(
                        mcontext,
                        title.length() == 0 ? mcontext.getString(R.string.au_clip) : title);

        final File deleteOldFile = new File(outPath);
        deleteOldFile.delete();

        mSoundFile = null;
        try {
            mSoundFile = CheapSoundFile.create(mFile.getAbsolutePath(), null);
        } catch (IOException e) {
            e.printStackTrace();
            e.printStackTrace();
//            AppController.toast(mcontext, "Unable to create ringtone from file");
            return;
        }

        mWaveformView.setSoundFile(mSoundFile);
        final int startFrame = mWaveformView.secondsToFrames((double) start);
        final int endFrame = mWaveformView.secondsToFrames((double) end);
        final int duration = (int) ((double) end - (double) start + 0.5);

        final File outFile = new File(outPath);
        try {

            // Write the new file
            mSoundFile.WriteFile(outFile, startFrame, endFrame - startFrame);

            // Try to load the new file to make sure it worked
            final CheapSoundFile.ProgressListener listener =
                    new CheapSoundFile.ProgressListener() {
                        @Override
                        public boolean reportProgress(double fractionComplete) {
                            return true;
                        }
                    };
            CheapSoundFile.create(outPath, listener);
        } catch (Exception ex) {
            ex.printStackTrace();
            //					mProgressDialogMaterial.dismiss();
            CharSequence errorMessage;
            if (ex.getMessage().equals("No space left on device")) {
                errorMessage = context.getResources().getText(R.string.no_space_error);
                ex = null;
            } else {
                errorMessage = context.getResources().getText(R.string.write_error);
            }
//            AppController.toast(context, errorMessage.toString());
            return;
        }

        long length = outFile.length();
        if (length <= 512) {
            outFile.delete();
            progressDialog.dismiss();

            new AlertDialog.Builder(context)
                    .setTitle(R.string.alert_title_failure)
                    .setMessage(R.string.too_small_error)
                    .setPositiveButton(R.string.alert_ok_button, null)
                    .setCancelable(false)
                    .show();
            return;
        }

        progressDialog.dismiss();

        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + outPath));
            share.putExtra(Intent.EXTRA_SUBJECT, "Shared Ringtone");
            share.putExtra(Intent.EXTRA_TEXT, queueItem.title);
            mcontext.startActivity(Intent.createChooser(share, "Share ringtone via..."));
        } catch (Exception ex) {
//            AppController.toast(mcontext, "Error sharing ringtone! Contact Developer");
            ex.printStackTrace();
        }

        shareDialog.dismiss();

//        if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_STOPPED && songPlaying) {
//            AppController.Instance().serviceResume(false);
//        }
    }

    private void sendRingtone(
            final Context context,
            final Song queueItem,
            final CharSequence title,
            final int start,
            final int end) {
        File mFile = new File(queueItem.data);
//        String mExtension = FileHelper.getExtensionFromFilename(queueItem.data);

        final String outPath =
                FileHelper.makeRingtoneFilename(
                        mcontext,
                        title.length() == 0 ? mcontext.getString(R.string.au_clip) : title);

        final File deleteOldFile = new File(outPath);
        deleteOldFile.delete();

        mSoundFile = null;
        try {
            mSoundFile = CheapSoundFile.create(mFile.getAbsolutePath(), null);
        } catch (IOException e) {
            e.printStackTrace();
            e.printStackTrace();
//            AppController.toast(mcontext, "Unable to create ringtone from file");
            return;
        }

        mWaveformView.setSoundFile(mSoundFile);
        final int startFrame = mWaveformView.secondsToFrames((double) start);
        final int endFrame = mWaveformView.secondsToFrames((double) end);
        final int duration = (int) ((double) end - (double) start + 0.5);

        final File outFile = new File(outPath);
        try {

            // Write the new file
            mSoundFile.WriteFile(outFile, startFrame, endFrame - startFrame);

            // Try to load the new file to make sure it worked
            final CheapSoundFile.ProgressListener listener = new CheapSoundFile.ProgressListener() {
                @Override
                public boolean reportProgress(double fractionComplete) {
                    return true;
                }
            };
            CheapSoundFile.create(outPath, listener);
        } catch (Exception ex) {
            ex.printStackTrace();
            //					mProgressDialogMaterial.dismiss();
            CharSequence errorMessage;
            if (ex.getMessage().equals("No space left on device")) {
                errorMessage = context.getResources().getText(R.string.no_space_error);
                ex = null;
            } else {
                errorMessage = context.getResources().getText(R.string.write_error);
            }
//            AppController.toast(context, errorMessage.toString());
            return;
        }

        progressDialog.dismiss();

        Song tempQueueItem = new Song();
        tempQueueItem.data = outPath;
        tempQueueItem.title = queueItem.title + " - ringtone";
        tempQueueItem.artistName = queueItem.artistName;
        tempQueueItem.artistName = queueItem.artistName;

        selectedItem = tempQueueItem;

    }

    private class PreviewTimerTask extends TimerTask {
        public void run() {

            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    int progress = 0;
                    int position = mediaPlayer.getCurrentPosition();
                    if (position > end) {
                        mediaPlayer.pause();
                        mediaPlayer.seekTo(start);
                        mcontext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
