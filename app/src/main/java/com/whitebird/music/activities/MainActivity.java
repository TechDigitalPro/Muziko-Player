

package com.whitebird.music.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.MyObserver;
import com.whitebird.music.R;
import com.whitebird.music.dialogs.RateUs;
import com.whitebird.music.fragments.AlbumDetailFragment;
import com.whitebird.music.fragments.AllActivity.AllActivityFragment;
import com.whitebird.music.fragments.ArtistDetailFragment;
import com.whitebird.music.fragments.DirectoryFragment;
import com.whitebird.music.fragments.Equalizer.EqualizerFragment;
import com.whitebird.music.fragments.GenreDetailFragment;
import com.whitebird.music.fragments.MainFragment;
import com.whitebird.music.fragments.Mp3CutterList;
import com.whitebird.music.fragments.PlaylistFragment;
import com.whitebird.music.fragments.SleepFragment;
import com.whitebird.music.fragments.homeList.FavFragment;
import com.whitebird.music.fragments.homeList.IgnoreFragment;
import com.whitebird.music.fragments.homeList.QueueFragment;
import com.whitebird.music.permissions.Nammu;
import com.whitebird.music.permissions.PermissionCallback;
import com.whitebird.music.slidinguppanel.SlidingUpPanelLayout;
import com.whitebird.music.subfragments.LyricsFragment;
import com.whitebird.music.utils.Constants;
import com.whitebird.music.utils.ImageUtils;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.HashMap;
import java.util.Map;

import static com.whitebird.music.fragments.Equalizer.EqualizerFragment.bassStrength;
import static com.whitebird.music.fragments.Equalizer.EqualizerFragment.presetPos;
import static com.whitebird.music.fragments.Equalizer.EqualizerFragment.reverbPreset;
import static com.whitebird.music.subfragments.QuickControlsFragment.isQueueOpen;

public class MainActivity extends BaseActivity implements ATEActivityThemeCustomizer, EqualizerFragment.onCheckChangedListener {

    public static boolean isPressHome = false;
    public static boolean isPressDrawerOpen = false;
     private TextView songtitle, songartist;
   private ImageView albumart;
    private final PermissionCallback permissionRecordAudio = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            NavigationUtils.goToEqulizer(MainActivity.this);
        }

        @Override
        public void permissionRefused() {
            finish();
        }
    };
    public SlidingUpPanelLayout panelLayout;
    Receiver receiver;
    MyObserver observer;
    private NavigationView navigationView;
    // private TextView songtitle, songartist;
    // private ImageView albumart;

    private ImageView img_blurred;
    /* private RoundedImageView img_user;
     private TextView user_name;*/
    private String action;
    private Map<String, Runnable> navigationMap = new HashMap<String, Runnable>();
    private Handler navDrawerRunnable = new Handler();
    private Runnable runnable;
    private DrawerLayout mDrawerLayout;
    private boolean isDarkTheme;
    private Runnable navigateLibrary = new Runnable() {
        public void run() {
//            ArrayList<Genre> data=new GenreLoader(MainActivity.this).getAllGenre();
//            new GenreLoader(MainActivity.this).getSongGenre(data.get(0).getCount());


            navigationView.getMenu().findItem(R.id.nav_library).setChecked(true);
            Fragment fragment = new MainFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();

        }
    };
    private final PermissionCallback permissionReadstorageCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            loadEverything();
        }

        @Override
        public void permissionRefused() {
            finish();
        }
    };
    private Runnable navigatePlaylist = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_playlists).setChecked(true);
            Fragment fragment = new PlaylistFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    private Runnable navigateActivity = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_playlists).setChecked(true);
            Fragment fragment = new AllActivityFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    private Runnable navigateSleep = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_sleep).setChecked(true);
            Fragment fragment = new SleepFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    private Runnable navigateFolder = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_folders).setChecked(false);
            Fragment fragment = new DirectoryFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    private Runnable navigateQueue = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_queue).setChecked(false);
            Fragment fragment = new QueueFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    private Runnable navigateIgnore = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_queue).setChecked(false);
            Fragment fragment = new IgnoreFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };

    private Runnable nav_fav = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_queue).setChecked(false);
            Fragment fragment = new FavFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    private Runnable navigatecutter = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_queue).setChecked(false);
            Fragment fragment = new Mp3CutterList();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    private Runnable navigateAlbum = new Runnable() {
        public void run() {
            String albumID = getIntent().getExtras().getString(Constants.ALBUM_ID);
            Fragment fragment = AlbumDetailFragment.newInstance(albumID, false, null);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };
    private Runnable navigateGenre = new Runnable() {
        public void run() {
            String genreName = getIntent().getExtras().getString(Constants.GENRE_ID);
            Fragment fragment = GenreDetailFragment.newInstance(genreName, false, null);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };
    private Runnable navigateEqu = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_equ).setChecked(true);
            Fragment fragment = new EqualizerFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };
    private Runnable navigateArtist = new Runnable() {
        public void run() {
            long artistID = getIntent().getExtras().getLong(Constants.ARTIST_ID);
            Fragment fragment = ArtistDetailFragment.newInstance(artistID, false, null);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };
    private Runnable navigateLyrics = new Runnable() {
        public void run() {
            Fragment fragment = new LyricsFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };
    /* @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);

     }*/
    private Runnable navigateNowplaying = new Runnable() {
        public void run() {
            navigateLibrary.run();
            startActivity(new Intent(MainActivity.this, NowPlayingActivity.class));
        }
    };
    private int backCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        action = getIntent().getAction();

        isDarkTheme = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        navigationMap.put(Constants.NAVIGATE_LIBRARY, navigateLibrary);
        navigationMap.put(Constants.NAVIGATE_PLAYLIST, navigatePlaylist);
        navigationMap.put(Constants.NAVIGATE_ACTIVITY, navigateActivity);
        navigationMap.put(Constants.NAVIGATE_QUEUE, navigateQueue);
        navigationMap.put(Constants.NAVIGATE_IGNORE, navigateIgnore);
        navigationMap.put(Constants.NAVIGATE_FAVORITE, nav_fav);
        navigationMap.put(Constants.NAVIGATE_MP3CUTTER, navigatecutter);
        navigationMap.put(Constants.NAVIGATE_NOWPLAYING, navigateNowplaying);
        navigationMap.put(Constants.NAVIGATE_ALBUM, navigateAlbum);
        navigationMap.put(Constants.NAVIGATE_GENRE, navigateGenre);
        navigationMap.put(Constants.NAVIGATE_ARTIST, navigateArtist);
        navigationMap.put(Constants.NAVIGATE_LYRICS, navigateLyrics);
        navigationMap.put(Constants.NAVIGATE_Equlizer, navigateEqu);
        navigationMap.put(Constants.NAVIGATE_SLEEP, navigateSleep);
        navigationMap.put(Constants.NAVIGATE_FOLDER, navigateFolder);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.inflateHeaderView(R.layout.nav_header);


        img_blurred = (ImageView) header.findViewById(R.id.img_blurred);
       /* img_user = (RoundedImageView) header.findViewById(R.id.img_user);
        user_name = (TextView) header.findViewById(R.id.user_name);*/

         albumart = (ImageView) header.findViewById(R.id.album_art);
         songtitle = (TextView) header.findViewById(R.id.song_title);
         songartist = (TextView) header.findViewById(R.id.song_artist);

        setDetailsToHeader();

       /* img_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MuzikoApp.readUser().IsLogin) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                }
            }
        });*/

        setPanelSlideListeners(panelLayout);

        navDrawerRunnable.postDelayed(new Runnable() {
            @Override
            public void run() {
                setupDrawerContent(navigationView);
                setupNavigationIcons(navigationView);
            }
        }, 700);


        if (MuzikoUtils.isMarshmallow()) {
            checkPermissionAndThenLoad();
            //checkWritePermissions();
        } else {
            loadEverything();
        }

        addBackstackListener();

        if (Intent.ACTION_VIEW.equals(action)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.clearQueue();
                    MusicPlayer.openFile(getIntent().getData().getPath());
                    MusicPlayer.playOrPause();
                    navigateNowplaying.run();
                }
            }, 350);
        }

        if (!panelLayout.isPanelHidden() && MusicPlayer.getTrackName() == null) {
            panelLayout.hidePanel();
        }


        register();


        Handler handler;

        handler = new Handler();
        observer = new MyObserver(handler, MainActivity.this);
        this.getContentResolver().
                registerContentObserver(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        true,
                        observer);


    }

    private void loadEverything() {
        Runnable navigation = navigationMap.get(action);
        if (navigation != null) {
            navigation.run();
        } else {
            navigateLibrary.run();
        }

        new initQuickControls().execute("");
    }

    private void checkPermissionAndThenLoad() {
        //check for permission
        if (Nammu.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            loadEverything();
        } else {
            if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(panelLayout, "Muziko will need to read external storage to display songs on your device.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Nammu.askForPermission(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionReadstorageCallback);
                            }
                        }).show();
            } else {
                Nammu.askForPermission(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionReadstorageCallback);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                isPressDrawerOpen = isNavigatingMain();
                if (isPressDrawerOpen) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    isPressHome = true;
                    onBackPressed();
                    return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (panelLayout.isPanelExpanded()) {
            panelLayout.collapsePanel();
        } else if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (isPressHome || !isNavigatingMain()) {
            super.onBackPressed();
            isPressHome = false;
        } else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof QueueFragment) {
            isQueueOpen = false;
            super.onBackPressed();

        } else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof DirectoryFragment) {
            if (((DirectoryFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container)).onBackPressed_()) {
                super.onBackPressed();
            }
        } else {
            if (backCount == 1) {

                super.onBackPressed();

            } else {
                backCount++;

                MuzikoApp.toast(MainActivity.this, "Press back once more to exit");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        backCount = 0;
                    }
                }, 3000);

            }

        }


    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(final MenuItem menuItem) {
                        updatePosition(menuItem);
                        return true;

                    }
                });
    }

    private void setupNavigationIcons(NavigationView navigationView) {

        //material-icon-lib currently doesn't work with navigationview of design support library 22.2.0+
        //set icons manually for now
        //https://github.com/code-mc/material-icon-lib/issues/15

        if (!isDarkTheme) {
            navigationView.getMenu().findItem(R.id.nav_library).setIcon(R.drawable.library_music_black);
            navigationView.getMenu().findItem(R.id.nav_playlists).setIcon(R.drawable.playlist_play_black);
            navigationView.getMenu().findItem(R.id.nav_queue).setIcon(R.drawable.music_note_black);
            navigationView.getMenu().findItem(R.id.nav_ignore).setIcon(R.drawable.ic_ignore_black);
            navigationView.getMenu().findItem(R.id.nav_fav).setIcon(R.drawable.ic_favorite_black);
            navigationView.getMenu().findItem(R.id.nav_folders).setIcon(R.drawable.ic_folder_open_black_24dp);
            navigationView.getMenu().findItem(R.id.nav_sleep).setIcon(R.drawable.ic_timer_black);
            navigationView.getMenu().findItem(R.id.nav_nowplaying).setIcon(R.drawable.bookmark_music_black);
            navigationView.getMenu().findItem(R.id.nav_cutter).setIcon(R.drawable.nav_cutter_black);
            navigationView.getMenu().findItem(R.id.nav_equ).setIcon(R.drawable.menu_equalizer_black);
//          navigationView.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.settings);
            navigationView.getMenu().findItem(R.id.nav_about).setIcon(R.drawable.ic_information_black);
//          navigationView.getMenu().findItem(R.id.nav_donate).setIcon(R.drawable.payment_black);
        } else {
            navigationView.getMenu().findItem(R.id.nav_library).setIcon(R.drawable.library_music_white);
            navigationView.getMenu().findItem(R.id.nav_playlists).setIcon(R.drawable.playlist_play_white);
            navigationView.getMenu().findItem(R.id.nav_queue).setIcon(R.drawable.music_note_white);
            navigationView.getMenu().findItem(R.id.nav_ignore).setIcon(R.drawable.ic_ignore_white);
            navigationView.getMenu().findItem(R.id.nav_fav).setIcon(R.drawable.ic_favorite_white);
            navigationView.getMenu().findItem(R.id.nav_folders).setIcon(R.drawable.ic_folder_open_white_24dp);
            navigationView.getMenu().findItem(R.id.nav_sleep).setIcon(R.drawable.ic_timer_white);
            navigationView.getMenu().findItem(R.id.nav_nowplaying).setIcon(R.drawable.bookmark_music_white);
            navigationView.getMenu().findItem(R.id.nav_cutter).setIcon(R.drawable.nav_cutter_white);
            navigationView.getMenu().findItem(R.id.nav_equ).setIcon(R.drawable.menu_equalizer_white);
//          navigationView.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.settings);
            navigationView.getMenu().findItem(R.id.nav_about).setIcon(R.drawable.ic_information_white);
//          navigationView.getMenu().findItem(R.id.nav_donate).setIcon(R.drawable.payment_black);
        }

     /* try {
            if (!BillingProcessor.isIabServiceAvailable(this)) {
                navigationView.getMenu().removeItem(R.id.nav_donate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    private void updatePosition(final MenuItem menuItem) {
        runnable = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_library:
                runnable = navigateLibrary;

                break;
            case R.id.nav_playlists:
                runnable = navigatePlaylist;

                break;
            case R.id.nav_folders:
                //runnable = navigateFolder;
                NavigationUtils.goToFolder(MainActivity.this);
                break;
            case R.id.nav_sleep:
                NavigationUtils.goToSleep(MainActivity.this);
                break;
            case R.id.nav_nowplaying:
                NavigationUtils.navigateToNowplaying(MainActivity.this, false);
                break;
            case R.id.nav_equ:
                if (MuzikoUtils.isMarshmallow()) {
                    if (Nammu.checkPermission(Manifest.permission.RECORD_AUDIO)) {
                        NavigationUtils.goToEqulizer(MainActivity.this);
                    } else {
                        if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                            Snackbar.make(panelLayout, "Muziko will need permission for Equalizer !",
                                    Snackbar.LENGTH_INDEFINITE)
                                    .setAction("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Nammu.askForPermission(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, permissionRecordAudio);
                                        }
                                    }).show();
                        } else {
                            Nammu.askForPermission(this, new String[]{Manifest.permission.RECORD_AUDIO}, permissionRecordAudio);
                        }
                    }
                } else {
                    NavigationUtils.goToEqulizer(MainActivity.this);
                }

                break;
            case R.id.nav_queue:
                NavigationUtils.navigateHome(MainActivity.this);
                isQueueOpen = true;
                break;

            case R.id.nav_ignore:
                NavigationUtils.navigateIgnore(MainActivity.this);

                break;

            case R.id.nav_fav:
                NavigationUtils.navigateFav(MainActivity.this);

                break;
            case R.id.nav_cutter:
                NavigationUtils.navigateMp3Cutter(MainActivity.this);

                break;
            case R.id.menu_rateus:
                mDrawerLayout.closeDrawers();
                RateUs rateUs = new RateUs();
                rateUs.open(this);
                break;
            case R.id.nav_about:
                Intent activityIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                break;

        }

        if (runnable != null) {
            menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }, 350);
        } else
            menuItem.setChecked(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setDetailsToHeader();

            }
        });
    }

    public void setDetailsToHeader() {


            MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(MusicPlayer.getCurrentAlbumId()).toString(), albumart,
                    MuzikoApp.OPTIONS, new SimpleImageLoadingListener() {

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            setBlurredAlbumArt blurredAlbumArt = new setBlurredAlbumArt();
                            blurredAlbumArt.execute(loadedImage);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                            setBlurredAlbumArt blurredAlbumArt = new setBlurredAlbumArt();
                            blurredAlbumArt.execute(albumart.getDrawingCache());
                        }

                    });




     /*   String name = MusicPlayer.getTrackName();
        String artist = MusicPlayer.getArtistName();

        if (name != null && artist != null) {
            songtitle.setText(name);
            songartist.setText(artist);
        }
        ImageLoader.getInstance().displayImage(MuzikoUtils.getAlbumArtUri(MusicPlayer.getCurrentAlbumId()).toString(), albumart,
                new DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnFail(R.mipmap.app_icon)
                        .resetViewBeforeLoading(true)
                        .build());*/

    }


    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
         setDetailsToHeader();

        if (panelLayout.isPanelHidden() && MusicPlayer.getTrackName() != null) {
            panelLayout.showPanel();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean isNavigatingMain() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        return (currentFragment instanceof MainFragment || currentFragment instanceof QueueFragment || currentFragment instanceof DirectoryFragment
        );
    }

    private void addBackstackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                getSupportFragmentManager().findFragmentById(R.id.fragment_container).onResume();
            }
        });
    }

    @Override
    public int getActivityTheme() {
        return isDarkTheme ? R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        getSupportFragmentManager().findFragmentById(R.id.fragment_container).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showCastMiniController() {
        findViewById(R.id.castMiniController).setVisibility(View.VISIBLE);
        findViewById(R.id.quickcontrols_container).setVisibility(View.GONE);
        panelLayout.hidePanel();
    }

    @Override
    public void hideCastMiniController() {

        findViewById(R.id.castMiniController).setVisibility(View.GONE);
        findViewById(R.id.quickcontrols_container).setVisibility(View.VISIBLE);

        panelLayout.showPanel();
    }


    @Override
    public void onCheckChanged(boolean isChecked) {

        if (isChecked) {
            try {
                EqualizerFragment.setBlockerVisibility(View.GONE);
//                isEqualizerEnabled = true;
                int pos = presetPos;
                if (pos != 0) {
                    EqualizerFragment.mEqualizer.usePreset((short) (pos - 1));
                } else {
                    for (short i = 0; i < 5; i++) {
                        EqualizerFragment.mEqualizer.setBandLevel(i, (short) EqualizerFragment.seekbarpos[i]);
                    }
                }
                if (bassStrength != -1 && reverbPreset != -1) {
                    EqualizerFragment.bassBoost.setEnabled(true);
                    EqualizerFragment.bassBoost.setStrength(bassStrength);
                    EqualizerFragment.presetReverb.setEnabled(true);
                    EqualizerFragment.presetReverb.setPreset(reverbPreset);
                }
                //  MusicPlayer.getPlayer().setAuxEffectSendLevel(1.0f);


            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                EqualizerFragment.setBlockerVisibility(View.VISIBLE);
                EqualizerFragment.mEqualizer.usePreset((short) 0);
                EqualizerFragment.bassBoost.setStrength((short) (((float) 1000 / 19) * (1)));
                EqualizerFragment.presetReverb.setPreset((short) 0);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        equalizerModel.isEqualizerEnabled = isChecked;
    }

    private void unregister() {
        try {
            if (receiver != null) {
                unregisterReceiver(receiver);
                receiver = null;
            }

            this.getContentResolver().unregisterContentObserver(observer);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {

        unregister();


        super.onDestroy();
    }

    private void register() {
        receiver = new Receiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MuzikoApp.INTENT_CLEAR_QUQUE);

        registerReceiver(receiver, filter);
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    switch (action) {

                        case MuzikoApp.INTENT_CLEAR_QUQUE:
                            panelLayout.hidePanel();
                            break;

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


     private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], MainActivity.this, 12);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                if (img_blurred.getDrawable() != null) {
                    final TransitionDrawable td =
                            new TransitionDrawable(new Drawable[]{
                                    img_blurred.getDrawable(),
                                    result
                            });
                    img_blurred.setImageDrawable(td);
                    td.startTransition(200);

                } else {
                    img_blurred.setImageDrawable(result);
                }
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }
}


