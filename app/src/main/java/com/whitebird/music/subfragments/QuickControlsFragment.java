

package com.whitebird.music.subfragments;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.appthemeengine.Config;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.BaseActivity;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.listeners.MusicStateListener;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.ImageUtils;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.PreferencesUtility;
import com.whitebird.music.utils.SlideTrackSwitcher;
import com.whitebird.music.widgets.PlayPauseButton;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.steamcrafted.materialiconlib.MaterialIconView;

import static android.graphics.Color.WHITE;

public class QuickControlsFragment extends Fragment implements  MusicStateListener{


    public static View topContainer;
    private ProgressBar mProgress;
    private SeekBar mSeekBar;
    private int overflowcounter = 0;
    private PlayPauseButton mPlayPause, mPlayPauseExpanded;
    private TextView mTitle, mTitleExpanded;
    private TextView mArtist, mArtistExpanded;
    private ImageView mAlbumArt, mBlurredArt;
    private View rootView;
    private View playPauseWrapper, playPauseWrapperExpanded;
    private MaterialIconView next, next_close, previous/*,list_open*/;
    private ImageView list;
    private boolean duetoplaypause = false;
    private boolean fragmentPaused = false;
    public static boolean isQueueOpen = false;

    /* private PlayingQueueAdapter mAdapter;
     private BaseRecyclerView recyclerView;*/
    public Runnable mUpdateProgress = new Runnable() {

        @Override
        public void run() {

            long position = MusicPlayer.getSongProgressMillis();
            mProgress.setProgress((int) position);
            mSeekBar.setProgress((int) position);

            overflowcounter--;
            if (MusicPlayer.isPlaying()) {
                int delay = (int) (1500 - (position % 1000));
                if (overflowcounter < 0 && !fragmentPaused) {
                    overflowcounter++;
                    mProgress.postDelayed(mUpdateProgress, delay);
                }
            } else mProgress.removeCallbacks(this);

        }
    };

    private final View.OnClickListener mPlayPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            duetoplaypause = true;
            if (!mPlayPause.isPlayed()) {
                mPlayPause.setPlayed(true);
                mPlayPause.startAnimation();
            } else {
                mPlayPause.setPlayed(false);
                mPlayPause.startAnimation();
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.playOrPause();
                }
            }, 200);

        }
    };

    private final View.OnClickListener mPlayPauseExpandedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            duetoplaypause = true;
            if (!mPlayPauseExpanded.isPlayed()) {
                mPlayPauseExpanded.setPlayed(true);
                mPlayPauseExpanded.startAnimation();
            } else {
                mPlayPauseExpanded.setPlayed(false);
                mPlayPauseExpanded.startAnimation();
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.playOrPause();
                }
            }, 200);

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);
        this.rootView = rootView;

        mPlayPause = (PlayPauseButton) rootView.findViewById(R.id.play_pause);
        mPlayPauseExpanded = (PlayPauseButton) rootView.findViewById(R.id.playpause);
        playPauseWrapper = rootView.findViewById(R.id.play_pause_wrapper);
        playPauseWrapperExpanded = rootView.findViewById(R.id.playpausewrapper);
        playPauseWrapper.setOnClickListener(mPlayPauseListener);
        playPauseWrapperExpanded.setOnClickListener(mPlayPauseExpandedListener);
        mProgress = (ProgressBar) rootView.findViewById(R.id.song_progress_normal);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.song_progress);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mArtist = (TextView) rootView.findViewById(R.id.artist);
        mTitleExpanded = (TextView) rootView.findViewById(R.id.song_title);
        mArtistExpanded = (TextView) rootView.findViewById(R.id.song_artist);
        mAlbumArt = (ImageView) rootView.findViewById(R.id.album_art_nowplayingcard);
        mBlurredArt = (ImageView) rootView.findViewById(R.id.blurredAlbumart);
        next = (MaterialIconView) rootView.findViewById(R.id.next);
        next_close = (MaterialIconView) rootView.findViewById(R.id.next_close);
        list = (ImageView) rootView.findViewById(R.id.list);
//        list_open = (MaterialIconView) rootView.findViewById(R.id.list_open);
        previous = (MaterialIconView) rootView.findViewById(R.id.previous);
        topContainer = rootView.findViewById(R.id.topContainer);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mProgress.getLayoutParams();
        mProgress.measure(0, 0);
        layoutParams.setMargins(0, -(mProgress.getMeasuredHeight() / 2), 0, 0);
        mProgress.setLayoutParams(layoutParams);


        if (mProgress != null) {
            mProgress.getProgressDrawable().setColorFilter(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())), PorterDuff.Mode.SRC_IN);

        }

        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M&&mProgress != null){
            mSeekBar.setProgressDrawable(getActivity().getDrawable(R.drawable.seekbar_bg));
            mSeekBar.setTag("tint_accent_color");
        }*/
        if (mSeekBar != null) {

            mSeekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())), PorterDuff.Mode.SRC_IN));
            mSeekBar.getThumb().setColorFilter(new PorterDuffColorFilter(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())), PorterDuff.Mode.SRC_IN));

        }
        //if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
        {
            LayerDrawable progressBarDrawable = (LayerDrawable) mSeekBar.getProgressDrawable();
            Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
            Drawable progressDrawable = progressBarDrawable.getDrawable(1);

            backgroundDrawable.setColorFilter(ContextCompat.getColor(this.getContext(), R.color.gray), PorterDuff.Mode.SRC_IN);
            progressDrawable.setColorFilter(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())), PorterDuff.Mode.SRC_IN);
        }


        mPlayPause.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        mPlayPauseExpanded.setColor(WHITE);
//
//        next.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        list.setColorFilter(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
//        list_open.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        next_close.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));

/////////PLayList

       /* recyclerView = rootView.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(null);
        recyclerView.setEmptyView(getActivity(), rootView.findViewById(R.id.list_empty), "No songs in queue");
*/
//        new loadQueueSongs().execute("");
//        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    MusicPlayer.seek((long) i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.next();
                    }
                }, 200);

            }
        });
        next_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.next();
                    }
                }, 200);

            }
        });
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() instanceof MainActivity) {
                            if (((MainActivity) getActivity()).panelLayout.isPanelExpanded())
                                return;

                            if (!isQueueOpen) {
                                NavigationUtils.navigateQueue(getActivity());
                                isQueueOpen = true;
                            } else {
                                ((MainActivity) getActivity()).onBackPressed();
                                isQueueOpen = false;
                            }
//                           ((MainActivity)getActivity()).panelLayout.expandPanel();
                        }


                    }
                }, 200);

            }
        });
     /*   list_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       if (getActivity() instanceof MainActivity){
                           ((MainActivity)getActivity()).panelLayout.collapsePanel();
                       }



                    }
                }, 200);

            }
        });*/

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.previous(getActivity(), false);
                    }
                }, 200);

            }
        });

        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);

        if (PreferencesUtility.getInstance(getActivity()).isGesturesEnabled()) {
            new SlideTrackSwitcher() {
                @Override
                public void onClick() {
                    NavigationUtils.navigateToNowplaying(getActivity(), false);
                }
            }.attach(rootView.findViewById(R.id.root_view));
        }


        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentPaused = true;
    }

   /* private class loadQueueSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            mAdapter = new PlayingQueueAdapter(getActivity(), QueueLoader.getQueueSongs(getActivity()));
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);
            DragSortRecycler dragSortRecycler = new DragSortRecycler();
            dragSortRecycler.setViewHandleId(R.id.reorder);

            dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
                @Override
                public void onItemMoved(int from, int to) {
                    Log.d("queue", "onItemMoved " + from + " to " + to);
                    Song song = mAdapter.getSongAt(from);
                    mAdapter.removeSongAt(from);
                    mAdapter.addSongTo(to, song);
                    mAdapter.notifyDataSetChanged();
                    MusicPlayer.moveQueueItem(from, to);
                }
            });

            recyclerView.addItemDecoration(dragSortRecycler);
            recyclerView.addOnItemTouchListener(dragSortRecycler);
            recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());

            recyclerView.getLayoutManager().scrollToPosition(mAdapter.currentlyPlayingPosition);

        }

        @Override
        protected void onPreExecute() {
        }
    }*/


    public void updateNowplayingCard() {
        mTitle.setText(MusicPlayer.getTrackName());
        mArtist.setText(MusicPlayer.getArtistName());
        mTitleExpanded.setText(MusicPlayer.getTrackName());
        mArtistExpanded.setText(MusicPlayer.getArtistName());
        if (!duetoplaypause) {
            MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(MusicPlayer.getCurrentAlbumId()).toString(), mAlbumArt,
                    MuzikoApp.OPTIONS, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            Bitmap failedBitmap = MuzikoApp.loader.loadImageSync("drawable://" + R.drawable.ic_empty_music2);
                            if (getActivity() != null)
                                new setBlurredAlbumArt().execute(failedBitmap);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            if (getActivity() != null)
                                new setBlurredAlbumArt().execute(loadedImage);

                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
        }
        duetoplaypause = false;
        mProgress.setMax((int) MusicPlayer.duration());
        mSeekBar.setMax((int) MusicPlayer.duration());
        mProgress.postDelayed(mUpdateProgress, 10);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        topContainer = rootView.findViewById(R.id.topContainer);
        fragmentPaused = false;
        if (mProgress != null)
            mProgress.postDelayed(mUpdateProgress, 10);

    }

    public void updateState() {
        if (MusicPlayer.isPlaying()) {
            if (!mPlayPause.isPlayed()) {
                mPlayPause.setPlayed(true);
                mPlayPause.startAnimation();
            }
            if (!mPlayPauseExpanded.isPlayed()) {
                mPlayPauseExpanded.setPlayed(true);
                mPlayPauseExpanded.startAnimation();
            }
        } else {
            if (mPlayPause.isPlayed()) {
                mPlayPause.setPlayed(false);
                mPlayPause.startAnimation();
            }
            if (mPlayPauseExpanded.isPlayed()) {
                mPlayPauseExpanded.setPlayed(false);
                mPlayPauseExpanded.startAnimation();
            }
        }
    }



    @Override
    public void onServiceConnected() {
        updateNowplayingCard();
        updateState();
    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {
        updateNowplayingCard();
        updateState();
    }

    @Override
    public void onPlayingMetaChanged() {
        updateNowplayingCard();
        updateState();
    }

    @Override
    public void onPlayStateChanged() {
        updateNowplayingCard();
        updateState();

    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {

    }

    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], getActivity(), 6);
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
                    td.startTransition(400);

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
