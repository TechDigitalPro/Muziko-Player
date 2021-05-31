package com.whitebird.music.nowplaying;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.whitebird.music.MusicPlayer;

import com.whitebird.music.R;
import com.whitebird.music.adapters.SlidingQueueAdapter;
import com.whitebird.music.dataloaders.QueueLoader;
import com.whitebird.music.service.MusicService;
import com.whitebird.music.utils.ImageUtils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

/**
 * Created by naman on 22/02/17.
 */

public class Muziko5 extends BaseNowplayingFragment {

    ImageView mBlurredArt;
    RecyclerView recyclerView;
    SlidingQueueAdapter adapter;
    private InterstitialAd interstitialAd;
    boolean isActivityIsVisible = true;

    @Override
    public void onPause() {
        super.onPause();
        isActivityIsVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isActivityIsVisible = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_timber5, container, false);

        setMusicStateListener();
        setSongDetails(rootView);

        mBlurredArt = (ImageView) rootView.findViewById(R.id.album_art_blurred);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.queue_recyclerview_horizontal) ;
        initGestures(mBlurredArt);

        try {
            interstitialAd = new InterstitialAd(getContext());
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstial_id));

            AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                    .build();

            interstitialAd.loadAd(adRequest);
            interstitialAd.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    if (interstitialAd.isLoaded() && isActivityIsVisible) {
                        interstitialAd.show();
                    } else {
                        Log.d("Interstitial", "Interstitial ad was not ready to be shown.");
                    }
                }

                @Override
                public void onAdFailedToLoad(int error_code) {

                }

            });
        }catch (Exception e){
            e.printStackTrace();
        }

        setupSlidingQueue();

        return rootView;
    }

    @Override
    public void updateShuffleState() {
        if (shuffle != null && getActivity() != null) {
            MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(getActivity())
                    .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                    .setSizeDp(30);

            if (MusicPlayer.getShuffleMode() == 0) {
                builder.setColor(Color.WHITE);
            } else builder.setColor(accentColor);

            shuffle.setImageDrawable(builder.build());
            shuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MusicPlayer.cycleShuffle();
                    updateShuffleState();
                    updateRepeatState();
                }
            });
        }
    }

    @Override
    public void updateRepeatState() {
        if (repeat != null && getActivity() != null) {
            MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(getActivity())
                    .setSizeDp(30);

            if (MusicPlayer.getRepeatMode() == 0) {
                builder.setColor(Color.WHITE);
            } else builder.setColor(accentColor);

            if (MusicPlayer.getRepeatMode() == MusicService.REPEAT_MODE_NONE) {
                builder.setIcon(MaterialDrawableBuilder.IconValue.REPEAT);
                builder.setColor(Color.WHITE);
            } else if (MusicPlayer.getRepeatMode() == MusicService.REPEAT_MODE_THIS) {
                builder.setIcon(MaterialDrawableBuilder.IconValue.REPEAT_ONCE);
                builder.setColor(accentColor);
            } else if (MusicPlayer.getRepeatMode() == MusicService.REPEAT_MODE_ALL) {
                builder.setColor(accentColor);
                builder.setIcon(MaterialDrawableBuilder.IconValue.REPEAT);
            }

            repeat.setImageDrawable(builder.build());
            repeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MusicPlayer.cycleRepeat();
                    updateRepeatState();
                    updateShuffleState();
                }
            });
        }
    }

    @Override
    public void doAlbumArtStuff(Bitmap loadedImage) {
        setBlurredAlbumArt blurredAlbumArt = new setBlurredAlbumArt();
        blurredAlbumArt.execute(loadedImage);
    }

    private void setupSlidingQueue() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new SlidingQueueAdapter((AppCompatActivity) getActivity(), QueueLoader.getQueueSongs(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(MusicPlayer.getQueuePosition() - 3);
    }


    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], getActivity(), 12);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                if (mBlurredArt.getDrawable() != null) {
                    final TransitionDrawable td =
                            new TransitionDrawable(new Drawable[]{
                                    mBlurredArt.getDrawable(),
                                    result
                            });
                    mBlurredArt.setImageDrawable(td);
                    td.startTransition(200);

                } else {
                    mBlurredArt.setImageDrawable(result);
                }
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }

}
