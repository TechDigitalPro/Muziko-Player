

package com.whitebird.music.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.ATEActivity;
import com.whitebird.music.MusicPlayer;

import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.listeners.MusicStateListener;
import com.whitebird.music.service.MusicService;
import com.whitebird.music.slidinguppanel.SlidingUpPanelLayout;
import com.whitebird.music.subfragments.QuickControlsFragment;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.NavigationUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.whitebird.music.MusicPlayer.mService;

public class BaseActivity extends ATEActivity implements  MusicStateListener {

    private final ArrayList<MusicStateListener> mMusicStateListener = new ArrayList<>();
    private MusicPlayer.ServiceToken mToken;
    private MusicStateReceiver musicStateReceiver;
    private boolean receiverRegistered;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mToken = MusicPlayer.bindToService(this, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                BaseActivity.this.onServiceConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                BaseActivity.this.onServiceDisconnected();
            }
        });


        setVolumeControlStream(AudioManager.STREAM_MUSIC);

    }



    @Override
    public void onResume() {

        //For Android 8.0+: service may get destroyed if in background too long
        if(mService == null){
            mToken  = MusicPlayer.bindToService(this, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    BaseActivity.this.onServiceConnected();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    BaseActivity.this.onServiceDisconnected();
                }
            });
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mToken != null) {
            MusicPlayer.unbindFromService(mToken);
            mToken = null;
        }

        try {
            unregisterReceiver(musicStateReceiver);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
        mMusicStateListener.clear();
    }



    @Override
    public void onServiceConnected() {
        if (!receiverRegistered) {
            musicStateReceiver = new MusicStateReceiver(this);

            final IntentFilter filter = new IntentFilter();
            filter.addAction(MusicService.PLAY_STATE_CHANGED);
            filter.addAction(MusicService.SHUFFLE_MODE_CHANGED);
            filter.addAction(MusicService.REPEAT_MODE_CHANGED);
            filter.addAction(MusicService.META_CHANGED);
            filter.addAction(MusicService.QUEUE_CHANGED);
            filter.addAction(MusicService.MEDIA_STORE_CHANGED);

            registerReceiver(musicStateReceiver, filter);

            receiverRegistered = true;
        }

        for (MusicStateListener listener : mMusicStateListener) {
            if (listener != null) {
                listener.onServiceConnected();
            }
        }
    }



    @Override
    public void onServiceDisconnected() {
        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver);
            receiverRegistered = false;
        }

        for (MusicStateListener listener : mMusicStateListener) {
            if (listener != null) {
                listener.onServiceDisconnected();
            }
        }
    }

    @Override
    public void onPlayingMetaChanged() {
        for (MusicStateListener listener : mMusicStateListener) {
            if (listener != null) {
                listener.onPlayingMetaChanged();
            }
        }
    }

    @Override
    public void onQueueChanged() {
        for (MusicStateListener listener : mMusicStateListener) {
            if (listener != null) {
                listener.onQueueChanged();
            }
        }
    }

    @Override
    public void onPlayStateChanged() {
        for (MusicStateListener listener : mMusicStateListener) {
            if (listener != null) {
                listener.onPlayStateChanged();
            }
        }
    }

    @Override
    public void onMediaStoreChanged() {
        for (MusicStateListener listener : mMusicStateListener) {
            if (listener != null) {
                listener.onMediaStoreChanged();
            }
        }
    }

    @Override
    public void onRepeatModeChanged() {
        for (MusicStateListener listener : mMusicStateListener) {
            if (listener != null) {
                listener.onRepeatModeChanged();
            }
        }
    }

    @Override
    public void onShuffleModeChanged() {
        for (MusicStateListener listener : mMusicStateListener) {
            if (listener != null) {
                listener.onShuffleModeChanged();
            }
        }
    }

    public void setMusicStateListenerListener(final MusicStateListener status) {
        if (status == this) {
            throw new UnsupportedOperationException("Override the method, don't add a listener");
        }

        if (status != null) {
            mMusicStateListener.add(status);
        }
    }

    public void removeMusicStateListenerListener(final MusicStateListener status) {
        if (status != null) {
            mMusicStateListener.remove(status);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);


        ATE.applyMenu(this, getATEKey(), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!MainActivity.isPressDrawerOpen) {
                    MainActivity.isPressHome = true;
                    super.onBackPressed();
                }
                MainActivity.isPressDrawerOpen = false;

                return true;
            case R.id.action_settings:
                NavigationUtils.navigateToSettings(this);
                return true;
            case R.id.action_shuffle:
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.shuffleAll(BaseActivity.this);
                        sendBroadcast(new Intent(MuzikoApp.INTENT_SHUFFLE_ALL));
                    }
                }, 80);

                return true;
            case R.id.action_search:
                if (BaseActivity.this instanceof AlbumArtInternetActivity){
                    return true;
                }
                NavigationUtils.navigateToSearch(this);
                return true;

            case R.id.action_exit:
                finishAffinity();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public String getATEKey() {
        return Helpers.getATEKey(this);
    }

    public void setPanelSlideListeners(SlidingUpPanelLayout panelLayout) {
        panelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                View nowPlayingCard = QuickControlsFragment.topContainer;
                nowPlayingCard.setAlpha(1 - slideOffset);
            }

            @Override
            public void onPanelCollapsed(View panel) {
                View nowPlayingCard = QuickControlsFragment.topContainer;
                nowPlayingCard.setAlpha(1);
            }

            @Override
            public void onPanelExpanded(View panel) {
                View nowPlayingCard = QuickControlsFragment.topContainer;
                nowPlayingCard.setAlpha(0);
            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });
    }





    private static final class MusicStateReceiver extends BroadcastReceiver {

        private final WeakReference<BaseActivity> reference;

        public MusicStateReceiver(final BaseActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(final Context context, @NonNull final Intent intent) {
            final String action = intent.getAction();
            BaseActivity activity = reference.get();
            if (activity != null) {
                switch (action) {
                    case MusicService.META_CHANGED:
                        activity.onPlayingMetaChanged();
                        break;
                    case MusicService.QUEUE_CHANGED:
                        activity.onQueueChanged();
                        break;
                    case MusicService.PLAY_STATE_CHANGED:
                        activity.onPlayStateChanged();
                        break;
                    case MusicService.REPEAT_MODE_CHANGED:
                        activity.onRepeatModeChanged();
                        break;
                    case MusicService.SHUFFLE_MODE_CHANGED:
                        activity.onShuffleModeChanged();
                        break;
                    case MusicService.MEDIA_STORE_CHANGED:
                        activity.onMediaStoreChanged();
                        break;
                }
            }
        }
    }

    public class initQuickControls extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            QuickControlsFragment fragment1 = new QuickControlsFragment();
            FragmentManager fragmentManager1 = getSupportFragmentManager();
            fragmentManager1.beginTransaction()
                    .replace(R.id.quickcontrols_container, fragment1).commitAllowingStateLoss();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }
    }

    public void showCastMiniController() {
        //implement by overriding in activities
    }

    public void hideCastMiniController() {
        //implement by overriding in activities
    }


}

