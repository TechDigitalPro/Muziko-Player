package com.whitebird.music.service.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;


import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.whitebird.music.R;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.models.Song;
import com.whitebird.music.service.MusicService;
import com.whitebird.music.utils.MuzikoUtil;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.whitebird.music.service.MusicService.CHANNEL_ID;


public class PlayingNotificationImpl24 implements PlayingNotification {
    private static final int NOTIFY_MODE_FOREGROUND = 1;
    private static final int NOTIFY_MODE_BACKGROUND = 0;

    private MusicService service;

    private NotificationManager notificationManager;

    private int notifyMode = NOTIFY_MODE_BACKGROUND;

    private boolean stopped;

    @Override
    public synchronized void init(MusicService service) {
        this.service = service;
        notificationManager = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    private void createNotificationChannel() {
        if (MuzikoUtil.isOreo()) {
            CharSequence name = "Timber";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationManager manager = (NotificationManager)service. getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            manager.createNotificationChannel(mChannel);
        }
    }
    @Override
    public synchronized void update() {
        stopped = false;


        //   final int bigNotificationImageSize = service.getResources().getDimensionPixelSize(R.dimen.notification_big_image_size);
        service.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Song song = service.getCurrentSong();
                Bitmap artwork;
                artwork = ImageLoader.getInstance().loadImageSync(MuzikoUtil.getAlbumArtUri(song.albumId).toString());

                if (artwork == null) {
                    artwork = ImageLoader.getInstance().loadImageSync("drawable://" + R.mipmap.app_icon);
                }
                update(artwork, Palette.from(artwork).generate().getVibrantColor(Color.parseColor("#403f4d")));

            }
        });
    }

    void update(Bitmap bitmap, int color) {
        final Song song = service.getCurrentSong();

        final String albumName = song.albumName;
        final String artistName = song.artistName;
        final boolean isPlaying = service.isPlaying();
        final String text = TextUtils.isEmpty(albumName)
                ? artistName : artistName + " - " + albumName;

        final int playButtonResId = isPlaying
                ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_white_36dp;

        Intent action = new Intent(service, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent clickIntent = PendingIntent.getActivity(service, 0, action, 0);

        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(MusicService.ACTION_QUIT);
        intent.setComponent(serviceName);
        final PendingIntent deleteIntent = PendingIntent.getService(service, 0, intent, 0);

        if (bitmap == null)
            bitmap = BitmapFactory.decodeResource(service.getResources(), R.mipmap.app_icon);
        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(playButtonResId,
                service.getString(R.string.action_play_pause),
                retrievePlaybackAction(MusicService.ACTION_TOGGLE_PAUSE));
        NotificationCompat.Action previousAction = new NotificationCompat.Action(R.drawable.ic_skip_previous_white_36dp,
                service.getString(R.string.accessibility_prev),
                retrievePlaybackAction(MusicService.ACTION_REWIND));
        NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.ic_skip_next_white_36dp,
                service.getString(R.string.accessibility_next),
                retrievePlaybackAction(MusicService.ACTION_SKIP));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(bitmap)
                .setContentIntent(clickIntent)
                .setDeleteIntent(deleteIntent)
                .setContentTitle(song.title)
                .setContentText(text)
                .setOngoing(isPlaying)
                .setShowWhen(false)
                .addAction(previousAction)
                .addAction(playPauseAction)
                .addAction(nextAction);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
/*            builder.setStyle(new Notification.MediaStyle().setMediaSession(service.getMediaSession().getSessionToken()).setShowActionsInCompactView(0, 1, 2))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);*/
            if (true)
                builder.setColor(color);
        }

        if (stopped)
            return; // notification has been stopped before loading was finished
        updateNotifyModeAndPostNotification(builder.build());
    }

    private PendingIntent retrievePlaybackAction(final String action) {
        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);

        return PendingIntent.getService(service, 0, intent, 0);
    }

    private void updateNotifyModeAndPostNotification(Notification notification) {
        int newNotifyMode;
        if (service.isPlaying()) {
            newNotifyMode = NOTIFY_MODE_FOREGROUND;
        } else {
            newNotifyMode = NOTIFY_MODE_BACKGROUND;
        }

        if (notifyMode != newNotifyMode && newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            service.stopForeground(false);
        }

        if (newNotifyMode == NOTIFY_MODE_FOREGROUND) {
            service.startForeground(NOTIFICATION_ID, notification);
        } else if (newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        notifyMode = newNotifyMode;
    }

    @Override
    public synchronized void stop() {
        stopped = true;
        service.stopForeground(true);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
