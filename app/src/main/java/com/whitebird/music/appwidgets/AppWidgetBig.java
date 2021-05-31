package com.whitebird.music.appwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.whitebird.music.R;
import com.whitebird.music.activities.splash.SplashActivity;
import com.whitebird.music.models.Song;
import com.whitebird.music.service.MusicService;
import com.whitebird.music.utils.MuzikoUtil;
import com.nostra13.universalimageloader.core.ImageLoader;


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AppWidgetBig extends BaseAppWidget {
    public static final String NAME = "app_widget_big";

    private static AppWidgetBig mInstance;


    public static synchronized AppWidgetBig getInstance() {
        if (mInstance == null) {
            mInstance = new AppWidgetBig();
        }
        return mInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
                         final int[] appWidgetIds) {
        defaultAppWidget(context, appWidgetIds);
        final Intent updateIntent = new Intent(MusicService.APP_WIDGET_UPDATE);
        updateIntent.putExtra(MusicService.EXTRA_APP_WIDGET_NAME, NAME);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
    }

    /**
     * Initialize given widgets to default state, where we launch Music on
     * default click and hide actions if service not running.
     */
    private void defaultAppWidget(final Context context, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(context.getPackageName(), R.layout.app_widget_big);

        appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        appWidgetView.setViewVisibility(R.id.image, View.INVISIBLE);
        appWidgetView.setImageViewResource(R.id.button_next, R.drawable.ic_skip_next_white_36dp);
        appWidgetView.setImageViewResource(R.id.button_prev, R.drawable.ic_skip_previous_white_36dp);
        appWidgetView.setImageViewResource(R.id.button_toggle_play_pause, R.drawable.ic_play_white_36dp);

        linkButtons(context, appWidgetView);
        pushUpdate(context, appWidgetIds, appWidgetView);
    }

    private void pushUpdate(final Context context, final int[] appWidgetIds, final RemoteViews views) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        } else {
            appWidgetManager.updateAppWidget(new ComponentName(context, getClass()), views);
        }
    }

    /**
     * Check against {@link AppWidgetManager} if there are any instances of this
     * widget.
     */
    private boolean hasInstances(final Context context) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] mAppWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                getClass()));
        return mAppWidgetIds.length > 0;
    }

    /**
     * Handle a change notification coming over from
     * {@link MusicService}
     */
    public void notifyChange(final MusicService service, final String what) {
        if (hasInstances(service)) {
            if (MusicService.META_CHANGED.equals(what) || MusicService.PLAY_STATE_CHANGED.equals(what)) {
                performUpdate(service, null);
            }
        }
    }

    /**
     * Update all active widget instances by pushing changes
     */
    public void performUpdate(final MusicService service, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(service.getPackageName(), R.layout.app_widget_big);

        final boolean isPlaying = service.isPlaying();
        final Song song = service.getCurrentSong();

        // Set the titles and artwork
        if (TextUtils.isEmpty(song.title) && TextUtils.isEmpty(song.artistName)) {
            appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        } else {
            appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE);
            appWidgetView.setTextViewText(R.id.title, song.title);
            appWidgetView.setTextViewText(R.id.text, song.artistName);
        }

        // Set correct drawable for pause state
        int playPauseRes = isPlaying
                ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_white_36dp;
        appWidgetView.setImageViewResource(R.id.button_toggle_play_pause, playPauseRes);

        // Set prev/next button drawables
        appWidgetView.setImageViewResource(R.id.button_next, R.drawable.ic_skip_next_white_36dp);
        appWidgetView.setImageViewResource(R.id.button_prev, R.drawable.ic_skip_previous_white_36dp);

        // Link actions buttons to intents
        linkButtons(service, appWidgetView);

        // Load the album cover async and push the update on completion
        // Point p = Util.getScreenSize(service);
        //  final int widgetImageSize = Math.min(p.x, p.y);
        final Context appContext = service.getApplicationContext();
        service.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final Song song = service.getCurrentSong();
                Bitmap artwork;
                artwork = ImageLoader.getInstance().loadImageSync(MuzikoUtil.getAlbumArtUri(song.albumId).toString());

                if (artwork == null) {
                    artwork = ImageLoader.getInstance().loadImageSync("drawable://" + R.drawable.ic_empty_music2);
                }

                appWidgetView.setViewVisibility(R.id.image, View.VISIBLE);
                if (artwork == null) {
                    appWidgetView.setImageViewResource(R.id.image, R.drawable.ic_empty_music2);
                } else {
                    appWidgetView.setImageViewBitmap(R.id.image, artwork);
                }
                pushUpdate(appContext, appWidgetIds, appWidgetView);

            }
        });
    }

    /**
     * Link up various button actions using {@link PendingIntent}.
     */
    private void linkButtons(final Context context, final RemoteViews views) {
        Intent action;
        PendingIntent pendingIntent;

        final ComponentName serviceName = new ComponentName(context, MusicService.class);

        // Home
        action = new Intent(context, SplashActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
        views.setOnClickPendingIntent(R.id.clickable_area, pendingIntent);

        // Previous track
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_REWIND, serviceName);
        views.setOnClickPendingIntent(R.id.button_prev, pendingIntent);

        // Play and pause
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_TOGGLE_PAUSE, serviceName);
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent);

        // Next track
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_SKIP, serviceName);
        views.setOnClickPendingIntent(R.id.button_next, pendingIntent);
    }
}
