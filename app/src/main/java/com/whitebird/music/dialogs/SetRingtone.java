package com.whitebird.music.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
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
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.control.metrialRenge.IRangeBarFormatter;
import com.whitebird.music.control.metrialRenge.RangeBar;
import com.whitebird.music.cutter.ringtone_lib.WaveformView;
import com.whitebird.music.cutter.ringtone_lib.soundfile.CheapSoundFile;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.widgets.PlayPauseButton;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SetRingtone {
    private Activity mcontext;
    private WaveformView mWaveformView;
    private CheapSoundFile mSoundFile;
    private Timer timer = null;
    private MediaPlayer mediaPlayer;

    private RangeBar rangebar;
    private boolean songPlaying = false;
    private int start = 0;
    private int end = 0;
    private SeekBar playerSeekBar;
    private boolean mediaPlyerReleased = false;
    private PlayPauseButton playpause;
    private TextView songduration, elapsedtime;
    long progress=0;

    public void open(final Activity context, final Song queue) {
        mcontext = context;

        View view =
                LayoutInflater.from(mcontext).inflate(R.layout.dialog_set_ringtone, null, false);
        mWaveformView = view.findViewById(R.id.waveform);
        RelativeLayout buttonlayout = view.findViewById(R.id.buttonlayout);

        rangebar = view.findViewById(R.id.rangebar);
        songduration = view.findViewById(R.id.song_duration);
        elapsedtime = view.findViewById(R.id.song_elapsed_time);
        songduration.setTextColor(Config.accentColor(context, Helpers.getATEKey(context)));
        elapsedtime.setTextColor(Config.accentColor(context, Helpers.getATEKey(context)));

        rangebar.setBarColor(ContextCompat.getColor(context, R.color.gray));
        rangebar.setPinColor(Config.accentColor(context, Helpers.getATEKey(context)));
        rangebar.setTickColor(ContextCompat.getColor(context, R.color.gray));
        rangebar.setDrawTicks(false);

        rangebar.setConnectingLineColor(Config.accentColor(context, Helpers.getATEKey(context)));
        rangebar.setSelectorColor(Config.accentColor(context, Helpers.getATEKey(context)));

        playerSeekBar = view.findViewById(R.id.playerSeekBar);

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


                        if (fromUser) {
                            mediaPlayer.seekTo(progress + start);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });


        playpause = view.findViewById(R.id.playpause);
        playpause.setColor(Config.accentColor(context, Helpers.getATEKey(context)));


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
        playerSeekBar.setMax(end - start);
        songduration.setText(MuzikoUtils.makeShortTimeString(context, (end - start) / 1000));
        elapsedtime.setText(MuzikoUtils.makeShortTimeString(context, 0));

        rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                start = leftPinIndex * 1000;
                end = rightPinIndex * 1000;
                playerSeekBar.setMax(end - start);
                songduration.setText(MuzikoUtils.makeShortTimeString(context, (end - start) / 1000));
                playerSeekBar.setProgress(0);

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
            public String format(String s) {
                double d = Double.parseDouble(s);
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
                   // mediaPlayer.seekTo(rangebar.getLeftIndex() * 1000);
                    playpause.setPlayed(false);
                    playpause.startAnimation();
                }


            }
        });


        if (MusicPlayer.isPlaying()) {
            songPlaying = true;
            if (MusicPlayer.isPlaying())
                MusicPlayer.playOrPause();
        } else {
            songPlaying = false;
        }

        new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .negativeColor(Config.accentColor(context, Helpers.getATEKey(context)))
                .titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                .neutralColorRes(R.color.dialog_negetive_button)
                .positiveColor(Config.accentColor(context, Helpers.getATEKey(context)))
                .customView(view, false)
                .autoDismiss(true)
                .positiveText("Set")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (Double.parseDouble(rangebar.getRightPinValue())
                                - Double.parseDouble(rangebar.getLeftPinValue())
                                < 10) {
                            MuzikoApp.toast(context, "Ringtone must be at least 10 seconds");
                        } else {

                            timerStop();
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlyerReleased = true;
                            dialog.dismiss();
                            new MaterialDialog.Builder(context)
                                    .theme(Theme.LIGHT)
                                    .negativeColor(Config.accentColor(context, Helpers.getATEKey(context)))
                                    .titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                                    .neutralColorRes(R.color.dialog_negetive_button)
                                    .positiveColor(Config.accentColor(context, Helpers.getATEKey(context)))
                                    .title("Save Ringtone")
                                    .positiveText("SAVE")
                                    .negativeText("CANCEL")
                                    .inputType(InputType.TYPE_CLASS_TEXT)
                                    .input(
                                            "Ringtone Name",
                                            "",
                                            new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                    // Do something
                                                    if (input.toString().length() > 0) {

                                                        saveRingtone(
                                                                context,
                                                                queue,
                                                                input,
                                                                rangebar.getLeftIndex(),
                                                                rangebar.getRightIndex());

                                                    } else {
                                                        MuzikoApp.toast(context, "Enter Something!");
                                                    }
                                                }
                                            })
                                    .show();
                        }
                    }
                })
                .neutralText("Cancel")
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .dismissListener(
                        new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                timerStop();
                                if (mediaPlayer != null) {
                                    if (!mediaPlyerReleased) {
                                        if (mediaPlayer.isPlaying()) {
                                            mediaPlayer.stop();
                                        }
                                        mediaPlayer.release();
                                    }
                                }

                               /* if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_STOPPED
                                        && songPlaying) {
                                    AppController.Instance().serviceResume(false);
                                }*/
                            }
                        })
                .show();
    }

    private void timerStart() {
        if (timer == null)
            timer = new Timer();

        timer.scheduleAtFixedRate(new PreviewTimerTask(), 0, 1000);
    }

    private void timerStop() {
        try {
            if (timer != null) timer.cancel();

            timer = null;
        } catch (Exception e) {

        }
    }

    private void saveRingtone(
            final Context context,
            final Song queueItem,
            final CharSequence title,
            final int start,
            final int end) {
        File mFile = new File(queueItem.data);
        String mExtension = getExtensionFromFilename(queueItem.data);

        final String outPath = makeRingtoneFilename(title);

        if (outPath == null) {

            MuzikoApp.toast(context, context.getString(R.string.no_unique_filename));
            return;
        }

        mSoundFile = null;
        try {
            mSoundFile = CheapSoundFile.create(mFile.getAbsolutePath(), null);
        } catch (IOException e) {
            e.printStackTrace();

            MuzikoApp.toast(mcontext, "Unable to create ringtone from file");
            return;
        }

        mWaveformView.setSoundFile(mSoundFile);
        final int startFrame = mWaveformView.secondsToFrames((double) start);
        final int endFrame = mWaveformView.secondsToFrames((double) end);
        final int duration = (int) ((double) end - (double) start + 0.5);

        //		createProgressDialog(context);

        new Thread() {
            public void run() {
                Handler mHandler = new Handler(context.getMainLooper());

                final File outFile = new File(outPath);
                try {

                    // Write the new file
                    mSoundFile.WriteFile(outFile, startFrame, endFrame - startFrame);

                    // Try to load the new file to make sure it worked
                    final CheapSoundFile.ProgressListener listener =
                            new CheapSoundFile.ProgressListener() {
                                @Override
                                public boolean reportProgress(double fractionComplete) {
                                    // Do nothing - we're not going to try to
                                    // estimate when reloading a saved sound
                                    // since it's usually fast, but hard to
                                    // estimate anyway.
                                    return true; // Keep going
                                }
                            };
                    CheapSoundFile.create(outPath, listener);
                } catch (Exception ex) {

                    //					mProgressDialogMaterial.dismiss();
                    CharSequence errorMessage;
                    if (ex.getMessage().equals("No space left on device")) {
                        errorMessage = context.getResources().getText(R.string.no_space_error);
                        ex = null;
                    } else {
                        errorMessage = context.getResources().getText(R.string.write_error);
                    }
                    MuzikoApp.toast(context, errorMessage.toString());
                    return;
                }

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        afterSavingRingtone(
                                context, title, outPath, outFile, duration * 1000);
                    }
                };

                mHandler.post(runnable);
            }
        }.start();
    }

    private void afterSavingRingtone(
            Context context, CharSequence title, String outPath, File outFile, int duration) {

        long length = outFile.length();
        if (length <= 512) {
            outFile.delete();
            new AlertDialog.Builder(context)
                    .setTitle(R.string.alert_title_failure)
                    .setMessage(R.string.too_small_error)
                    .setPositiveButton(R.string.alert_ok_button, null)
                    .setCancelable(false)
                    .show();
            return;
        }

        // Create the database record, pointing to the existing file path

        long fileSize = outFile.length();
        String mimeType = "audio/mp3";

        String artist = "" + context.getResources().getText(R.string.artist_name);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, outPath);
        values.put(MediaStore.MediaColumns.TITLE, title.toString());
        values.put(MediaStore.MediaColumns.SIZE, fileSize);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

        values.put(MediaStore.Audio.Media.ARTIST, artist);
        values.put(MediaStore.Audio.Media.DURATION, duration);

        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        // Insert it into the database
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(outPath);
        final Uri newUri = context.getContentResolver().insert(uri, values);

        // If Ringdroid was launched to get content, just return

        // There's nothing more to do with music or an alarm.  Show a
        // success message and then quit.

        try {
            RingtoneManager.setActualDefaultRingtoneUri(
                    MuzikoApp.getInstance().getApplicationContext(),
                    RingtoneManager.TYPE_RINGTONE,
                    newUri);
        } catch (Throwable t) {
            MuzikoApp.toast(context, context.getString(R.string.set_ringtone_failed));

        }
        MuzikoApp.toast(context, context.getResources().getString(R.string.save_success_message));

        /*if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_STOPPED && songPlaying) {
            AppController.Instance().serviceResume(false);
        }*/
    }

    private String getExtensionFromFilename(String filename) {
        try {
            return filename.substring(filename.lastIndexOf('.'), filename.length());
        } catch (Exception e) {
            return "mp3";
        }
    }

    private String makeRingtoneFilename(CharSequence title) {

        String subdir;
        String externalRootDir = Environment.getExternalStorageDirectory().getPath();
        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }
        subdir = "Muziko/";
        String parentdir = externalRootDir + subdir;

        // Create the parent directory
        File parentDirFile = new File(parentdir);
        if (!parentDirFile.exists()) {
            parentDirFile.mkdirs();
        }
        return parentdir + "/" + title + "." + "mp3";
    }

    private class PreviewTimerTask extends TimerTask {
        public void run() {

            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    int progress = 0;
                    final int position = mediaPlayer.getCurrentPosition();
                    playerSeekBar.setProgress(position - start);
                    mcontext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            elapsedtime.setText(MuzikoUtils.makeShortTimeString(mcontext, (position - start) / 1000));
                        }
                    });
                    if (position > end) {
                        mediaPlayer.pause();
                        mediaPlayer.seekTo(start);
                        playerSeekBar.setProgress(0);
                        mcontext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                elapsedtime.setText(MuzikoUtils.makeShortTimeString(mcontext, 0));
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

