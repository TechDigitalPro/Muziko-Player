package com.whitebird.music.service.notification;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import com.whitebird.music.R;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.models.Song;
import com.whitebird.music.service.MusicService;
import com.whitebird.music.utils.MuzikoUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Collections;
import java.util.Comparator;


public class PlayingNotificationImpl implements PlayingNotification {

    private MusicService service;

    //  private Target<BitmapPaletteWrapper> target;

    private boolean stopped;

    public static int getColor(@Nullable Palette palette, int fallback) {
        if (palette != null) {
            if (palette.getVibrantSwatch() != null) {
                return palette.getVibrantSwatch().getRgb();
            } else if (palette.getMutedSwatch() != null) {
                return palette.getMutedSwatch().getRgb();
            } else if (palette.getDarkVibrantSwatch() != null) {
                return palette.getDarkVibrantSwatch().getRgb();
            } else if (palette.getDarkMutedSwatch() != null) {
                return palette.getDarkMutedSwatch().getRgb();
            } else if (palette.getLightVibrantSwatch() != null) {
                return palette.getLightVibrantSwatch().getRgb();
            } else if (palette.getLightMutedSwatch() != null) {
                return palette.getLightMutedSwatch().getRgb();
            } else if (!palette.getSwatches().isEmpty()) {
                return Collections.max(palette.getSwatches(), SwatchComparator.getInstance()).getRgb();
            }
        }
        return fallback;
    }

    private static Bitmap createBitmap(Drawable drawable, float sizeMultiplier) {
        Bitmap bitmap = Bitmap.createBitmap((int) (drawable.getIntrinsicWidth() * sizeMultiplier), (int) (drawable.getIntrinsicHeight() * sizeMultiplier), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        drawable.setBounds(0, 0, c.getWidth(), c.getHeight());
        drawable.draw(c);
        return bitmap;
    }

    @Override
    public synchronized void init(MusicService service) {
        this.service = service;
    }

    RemoteViews notificationLayout;
    RemoteViews notificationLayoutBig;
    Notification notification;
      boolean isPlaying;
    @Override
    public synchronized void update() {
        stopped = false;

        final Song song = service.getCurrentSong();

          isPlaying = service.isPlaying();

        notificationLayout = new RemoteViews(service.getPackageName(), R.layout.notification);
        notificationLayoutBig = new RemoteViews(service.getPackageName(), R.layout.notification_big);

        if (TextUtils.isEmpty(song.title) && TextUtils.isEmpty(song.artistName)) {
            notificationLayout.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        } else {
            notificationLayout.setViewVisibility(R.id.media_titles, View.VISIBLE);
            notificationLayout.setTextViewText(R.id.title, song.title);
            notificationLayout.setTextViewText(R.id.text, song.artistName);
        }

        if (TextUtils.isEmpty(song.title) && TextUtils.isEmpty(song.artistName) && TextUtils.isEmpty(song.albumName)) {
            notificationLayoutBig.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        } else {
            notificationLayoutBig.setViewVisibility(R.id.media_titles, View.VISIBLE);
            notificationLayoutBig.setTextViewText(R.id.title, song.title);
            notificationLayoutBig.setTextViewText(R.id.text, song.artistName);
            notificationLayoutBig.setTextViewText(R.id.text2, song.albumName);
        }

        linkButtons(notificationLayout, notificationLayoutBig);

        Intent action = new Intent(service, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(service, 0, action, 0);

        notification = new NotificationCompat.Builder(service)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(openAppPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContent(notificationLayout)
                .setCustomBigContentView(notificationLayoutBig)
                .build();

        final int bigNotificationImageSize = service.getResources().getDimensionPixelSize(R.dimen.notification_big_image_size);
        service.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap artwork;
                artwork = ImageLoader.getInstance().loadImageSync(MuzikoUtil.getAlbumArtUri(song.albumId).toString());

                if (artwork == null) {
                    artwork = ImageLoader.getInstance().loadImageSync("drawable://" + R.mipmap.app_icon);
                }
                update(artwork, Palette.from(artwork).generate().getVibrantColor(Color.parseColor("#403f4d")));


            }
        });
    }

    private void update(@Nullable Bitmap bitmap, int bgColor) {
        if (bitmap != null) {
            notificationLayout.setImageViewBitmap(R.id.image, bitmap);
            notificationLayoutBig.setImageViewBitmap(R.id.image, bitmap);
        } else {
            notificationLayout.setImageViewResource(R.id.image, R.mipmap.app_icon);
            notificationLayoutBig.setImageViewResource(R.id.image, R.mipmap.app_icon);
        }

                               /* if (!PreferenceUtil.getInstance(service).coloredNotification()) {
                                    bgColor = Color.TRANSPARENT;
                                }*/
        setBackgroundColor(bgColor);
        setNotificationContent(true);/*bgColor == *//*Color.TRANSPARENT )*/
        ;//? Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP : ColorUtil.isColorLight(bgColor));

        if (stopped)
            return; // notification has been stopped before loading was finished
        service.startForeground(NOTIFICATION_ID, notification);
    }

    private void setBackgroundColor(int color) {
        notificationLayout.setInt(R.id.root, "setBackgroundColor", color);
        notificationLayoutBig.setInt(R.id.root, "setBackgroundColor", color);
    }

    private void setNotificationContent(boolean dark) {

        int primary = service.getResources().getColor(R.color.white_translucent);
        int secondary = service.getResources().getColor(R.color.white_translucent);

                              /*  Bitmap prev = createBitmap(Util.getTintedVectorDrawable(service, R.drawable.ic_skip_previous_white_24dp, primary), 1.5f);
                                Bitmap next = createBitmap(Util.getTintedVectorDrawable(service, R.drawable.ic_skip_next_white_24dp, primary), 1.5f);
                                Bitmap playPause = createBitmap(Util.getTintedVectorDrawable(service, isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp, primary), 1.5f);
                                Bitmap close = createBitmap(Util.getTintedVectorDrawable(service, R.drawable.ic_close_white_24dp, secondary), 1f);*/

        notificationLayout.setTextColor(R.id.title, primary);
        notificationLayout.setTextColor(R.id.text, secondary);
        notificationLayout.setImageViewResource(R.id.action_prev, R.drawable.ic_skip_previous_white_36dp);
        notificationLayout.setImageViewResource(R.id.action_next, R.drawable.ic_skip_next_white_36dp);
        notificationLayout.setImageViewResource(R.id.action_play_pause, isPlaying
                ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_white_36dp);

        notificationLayoutBig.setTextColor(R.id.title, primary);
        notificationLayoutBig.setTextColor(R.id.text, secondary);
        notificationLayoutBig.setTextColor(R.id.text2, secondary);
        notificationLayoutBig.setImageViewResource(R.id.action_prev, R.drawable.ic_skip_previous_white_36dp);
        notificationLayoutBig.setImageViewResource(R.id.action_next, R.drawable.ic_skip_next_white_36dp);
        notificationLayoutBig.setImageViewResource(R.id.action_play_pause, isPlaying
                ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_white_36dp);
        notificationLayoutBig.setImageViewResource(R.id.action_quit, R.drawable.ic_close_white_24dp);


    }

    @Override
    public synchronized void stop() {
        stopped = true;
        service.stopForeground(true);
    }

    private void linkButtons(final RemoteViews notificationLayout, final RemoteViews notificationLayoutBig) {
        PendingIntent pendingIntent;

        final ComponentName serviceName = new ComponentName(service, MusicService.class);

        // Previous track
        pendingIntent = buildPendingIntent(service, MusicService.ACTION_REWIND, serviceName);
        notificationLayout.setOnClickPendingIntent(R.id.action_prev, pendingIntent);
        notificationLayoutBig.setOnClickPendingIntent(R.id.action_prev, pendingIntent);

        // Play and pause
        pendingIntent = buildPendingIntent(service, MusicService.ACTION_TOGGLE_PAUSE, serviceName);
        notificationLayout.setOnClickPendingIntent(R.id.action_play_pause, pendingIntent);
        notificationLayoutBig.setOnClickPendingIntent(R.id.action_play_pause, pendingIntent);

        // Next track
        pendingIntent = buildPendingIntent(service, MusicService.ACTION_SKIP, serviceName);
        notificationLayout.setOnClickPendingIntent(R.id.action_next, pendingIntent);
        notificationLayoutBig.setOnClickPendingIntent(R.id.action_next, pendingIntent);

        // Quit
        pendingIntent = buildPendingIntent(service, MusicService.ACTION_QUIT, serviceName);
        notificationLayoutBig.setOnClickPendingIntent(R.id.action_quit, pendingIntent);
    }

    private PendingIntent buildPendingIntent(Context context, final String action, final ComponentName serviceName) {
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    private static class SwatchComparator implements Comparator<Palette.Swatch> {
        private static SwatchComparator sInstance;

        static SwatchComparator getInstance() {
            if (sInstance == null) {
                sInstance = new SwatchComparator();
            }
            return sInstance;
        }

        @Override
        public int compare(Palette.Swatch lhs, Palette.Swatch rhs) {
            return lhs.getPopulation() - rhs.getPopulation();
        }
    }
}
