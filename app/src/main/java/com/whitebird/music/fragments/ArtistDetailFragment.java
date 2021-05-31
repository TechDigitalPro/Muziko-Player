

package com.whitebird.music.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.afollestad.appthemeengine.ATE;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.adapters.ArtistSongAdapter;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.lastfmapi.models.LastfmArtist;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.ATEUtils;
import com.whitebird.music.utils.Constants;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.ImageUtils;
import com.whitebird.music.utils.MuzikoUtils;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

public class ArtistDetailFragment extends Fragment {

    private long artistID = -1;
    private ImageView artistArt;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private boolean largeImageLoaded = false;
    private int primaryColor = -1;
   // private ArtistSongAdapter mAdapter;

    public static ArtistDetailFragment newInstance(long id, boolean useTransition, String transitionName) {
        ArtistDetailFragment fragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ARTIST_ID, id);
        args.putBoolean("transition", useTransition);
        if (useTransition)
            args.putString("transition_name", transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artistID = getArguments().getLong(Constants.ARTIST_ID);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_artist_detail, container, false);

        artistArt = (ImageView) rootView.findViewById(R.id.artist_art);

        collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        appBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar);

        if (getArguments().getBoolean("transition")) {
            artistArt.setTransitionName(getArguments().getString("transition_name"));
        }

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        setupToolbar();


        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                .build();
        adView.loadAd(adRequest);

        getChildFragmentManager().beginTransaction().replace(R.id.container, ArtistMusicFragment.newInstance(artistID)).commit();
        setUpArtistDetails();

        return rootView;
    }







    private void setupToolbar() {

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setUpArtistDetails() {

//        final Artist artist = ArtistLoader.getArtist(getActivity(), artistID);
        final ArrayList<Song> songList = DataHelper.getSongsForArtist(artistID);
       // mAdapter = (ArtistSongAdapter) ArtistMusicFragment.songsRecyclerview.getAdapter();

        collapsingToolbarLayout.setTitle(songList.get(0).artistName);

       /* LastFmClient.getInstance(getActivity()).getArtistInfo(new ArtistQuery(songList.get(0).artistName), new ArtistInfoListener() {
            @Override
            public void artistInfoSucess(final LastfmArtist artist) {
                if (artist != null) {


                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setBlurredPlaceholder(artist);
                        }
                    }, 100);

                }
            }

            @Override
            public void artistInfoFailed() {

            }
        });*/


        MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(songList.get(0).albumId).toString(), artistArt,
                MuzikoApp.OPTIONS, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        largeImageLoaded = true;
                        try {
                            new Palette.Builder(loadedImage).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                    if (swatch != null) {
                                        primaryColor = swatch.getRgb();
                                        collapsingToolbarLayout.setContentScrimColor(primaryColor);
                                        if (getActivity() != null)
                                            ATEUtils.setStatusBarColor(getActivity(), Helpers.getATEKey(getActivity()), primaryColor);
                                    } else {
                                        Palette.Swatch swatchMuted = palette.getMutedSwatch();
                                        if (swatchMuted != null) {
                                            primaryColor = swatchMuted.getRgb();
                                            collapsingToolbarLayout.setContentScrimColor(primaryColor);
                                            if (getActivity() != null)
                                                ATEUtils.setStatusBarColor(getActivity(), Helpers.getATEKey(getActivity()), primaryColor);
                                        }
                                    }

                                }
                            });
                        } catch (Exception ignored) {

                        }
                    }
                });

    }

    private void setBlurredPlaceholder(LastfmArtist artist) {
        MuzikoApp.loader.loadImage(artist.mArtwork.get(1).mUrl, MuzikoApp.OPTIONS, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (getActivity() != null && !largeImageLoaded)
                    new setBlurredAlbumArt().execute(loadedImage);

            }
        });
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.artist_detail, menu);
        if (getActivity() != null)
            ATE.applyMenu(getActivity(), "dark_theme", menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.popup_song_addto_queue:
                MusicPlayer.addToQueue( ( (ArtistSongAdapter) ArtistMusicFragment.songsRecyclerview.getAdapter()).getSongs() );
                break;
            case R.id.popup_song_addto_playlist:
                AddPlaylistDialog.newInstance(( (ArtistSongAdapter) ArtistMusicFragment.songsRecyclerview.getAdapter()).getSongList()).show(getActivity().getSupportFragmentManager(), "ADD_PLAYLIST");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        if (primaryColor != -1 && getActivity() != null) {
            collapsingToolbarLayout.setContentScrimColor(primaryColor);
            String ateKey = Helpers.getATEKey(getActivity());
            ATEUtils.setStatusBarColor(getActivity(), ateKey, primaryColor);
        }

    }

    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], getActivity(), 3);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null && !largeImageLoaded) {
                artistArt.setImageDrawable(result);
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }

}