package com.whitebird.music.fragments.homeList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.appthemeengine.Config;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.BaseActivity;
import com.whitebird.music.adapters.SongsListAdapter;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.dataloaders.PlaylistLoader;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.fragments.MainFragment;
import com.whitebird.music.listeners.MusicStateListener;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.PreferencesUtility;
import com.whitebird.music.utils.SortOrder;
import com.whitebird.music.widgets.BaseRecyclerView;
import com.whitebird.music.widgets.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//import com.whitebird.music.widgets.FastScroller;

public class HomeFragment extends Fragment implements MusicStateListener, View.OnClickListener, ActionMode.Callback {

    public ActionMode actionMode = null;
    Receiver receiver;
    ArrayList<Song> dataAll = new ArrayList<>();
    private SongsListAdapter mAdapter;
    private BaseRecyclerView recyclerView;
    LinearLayout ll1;
    private PreferencesUtility mPreferences;
    private CardView playlistsCard,activity_card;
    private TextView playlistsDetailText;
    NestedScrollView n_scroll;
    private boolean mode;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mPreferences = PreferencesUtility.getInstance(getActivity());
        dataAll = sortList();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.home_fragment, container, false);

        n_scroll = rootView.findViewById(R.id.n_scroll);
        ll1 = rootView.findViewById(R.id.ll1);
        recyclerView = rootView.findViewById(R.id.recyclerview);
        playlistsCard = rootView.findViewById(R.id.playlistsCard);
        playlistsDetailText = rootView.findViewById(R.id.playlistsDetailText);
        activity_card = rootView.findViewById(R.id.activity_card);
        playlistsCard.setOnClickListener(this);
        activity_card.setOnClickListener(this);

        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                .build();
        adView.loadAd(adRequest);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyView(getActivity(), rootView.findViewById(R.id.list_empty), "No media found");
//        FastScroller fastScroller = rootView.findViewById(R.id.fastscroller);
//        fastScroller.setRecyclerView(recyclerView);

//        new loadSongs().execute("");
        mAdapter = new SongsListAdapter((AppCompatActivity) getActivity(), dataAll, false, false, false,this);
        recyclerView.setAdapter(mAdapter);
        if (getActivity() != null)
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), 0));

        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);
        register();
        return rootView;
    }

  /*  public void restartLoader() {

    }

    public void onPlaylistChanged() {

    }

    public void onMetaChanged() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }*/

    public void onDestroy() {

        unregister();
        super.onDestroy();
    }

    private void reloadAdapter() {
        new ReloadSongs().execute("");
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.song_sort_by, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                mPreferences.setHomeSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_za:
                mPreferences.setHomeSortOrder(SortOrder.SongSortOrder.SONG_Z_A);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_artist:
                mPreferences.setHomeSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_album:
                mPreferences.setHomeSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_year:
                mPreferences.setHomeSortOrder(SortOrder.SongSortOrder.SONG_YEAR);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_duration:
                mPreferences.setHomeSortOrder(SortOrder.SongSortOrder.SONG_DURATION);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_date:
                mPreferences.setHomeSortOrder(SortOrder.SongSortOrder.SONG_DATE_ADDED);
                reloadAdapter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        if (view == playlistsCard) {

            NavigationUtils.goToPlayList(getActivity());

        } else if (view == activity_card) {

            NavigationUtils.goToActivity(getActivity());

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playlistsDetailText.setText(PlaylistLoader.getPlayList().size() + " playlists");
                recyclerView.setFocusable(false);
                ll1.requestFocus();

            }
        });

    }

    private void unregister() {
        try {
            if (receiver != null) {
                getActivity().unregisterReceiver(receiver);
                receiver = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void register() {
        receiver = new Receiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MuzikoApp.INTENT_TRACK_DELETED);
        filter.addAction(MuzikoApp.INTENT_TRACK_CHANGED);
        filter.addAction(MuzikoApp.INTENT_PLAYLIST_CHANGE);
        filter.addAction(MuzikoApp.INTENT_MEDIA_CHANGE);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Config.primaryColorDark(getActivity(), Helpers.getATEKey(getActivity())));
        }
        getActivity().getMenuInflater().inflate(R.menu.context_default, menu);
        ActionBarContextView actionBar = getActivity().getWindow().getDecorView().findViewById(R.id.action_mode_bar);
        actionBar.setBackgroundColor(Config.primaryColor(getActivity(), Helpers.getATEKey(getActivity())));

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;
        mAdapter.setActionMode(actionMode);
      /*  if (this.mode) {

            (((MainFragment) HomeFragment.this.getParentFragment())).enableTabs(false);
        } else {
            ((MainFragment) HomeFragment.this.getParentFragment()).enableTabs(false);
        }*/

        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        ArrayList<Song> selectedSong = mAdapter.getSelectedItems();
        //long[] ids = MuzikoApp.getSongIds(mAdapter.getSelectedItems());
        if (selectedSong.size() > 0) {
            switch (item.getItemId()) {
                case R.id.play:
                    MusicPlayer.playAll(selectedSong, 0, false);
                    break;
                case R.id.play_next:

                    MusicPlayer.playNext( selectedSong);
                    break;
                case R.id.add_to_queue:
                    MusicPlayer.addToQueue( selectedSong );
                    break;
                case R.id.add_to_fav:

                    MuzikoApp.moveToFavorite(selectedSong, getActivity());
                    break;
                case R.id.add_to_playlist:
                    AddPlaylistDialog.newInstance(selectedSong).show(getActivity().getSupportFragmentManager(), "ADD_PLAYLIST");
                    break;
                case R.id.share:
                    MuzikoApp.getInstance().shareSongs(getActivity(), mAdapter.getSelectedItems());
                    break;
                case R.id.multi_tag_edit:
                    NavigationUtils.multiTagEdit(getActivity(), selectedSong);
                    break;

                case R.id.delete:
                    MuzikoUtils.deleteMultipleSong(getActivity(), selectedSong, "Songs");
                    break;

                default:
                    return false;
            }
        }

        mode.finish();
        actionMode = null;
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (this.mode) {
            ((MainFragment) HomeFragment.this.getParentFragment()).enableTabs(true);
        } else {
            ((MainFragment) HomeFragment.this.getParentFragment()).enableTabs(true);
        }
        mAdapter.clearSelection();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!recyclerView.isComputingLayout()) {
                    mAdapter.setMultiSelect(false);
                    actionMode = null;
                } else {
                    onDestroyActionMode(HomeFragment.this.actionMode);
                }
            }
        });

    }

    private ArrayList<Song> sortList() {

        ArrayList<Song> data = DataHelper.getLastAddedSongs();
        Collections.sort(data, new Comparator<Song>() {
            public int compare(Song obj1, Song obj2) {
                return Long.valueOf(obj2.dateAdded).compareTo(obj1.dateAdded);
            }
        });
        return data;
       /* if (mPreferences.getHomeSortOrder().equalsIgnoreCase(SortOrder.SongSortOrder.SONG_A_Z)){
            Collections.sort(data, new Comparator<Song>(){
                public int compare(Song obj1, Song obj2) {

                    return obj1.title.compareToIgnoreCase(obj2.title);

                }
            });

        }else if (mPreferences.getHomeSortOrder().equalsIgnoreCase(SortOrder.SongSortOrder.SONG_Z_A)){
            Collections.sort(data, new Comparator<Song>(){
                public int compare(Song obj1, Song obj2) {
                    return obj2.title.compareToIgnoreCase(obj1.title);
                }
            });

        }else if (mPreferences.getHomeSortOrder().equalsIgnoreCase(SortOrder.SongSortOrder.SONG_ARTIST)){
            Collections.sort(data, new Comparator<Song>(){
                public int compare(Song obj1, Song obj2) {
                    return obj2.artistName.compareToIgnoreCase(obj1.artistName);
                }
            });
        }else if (mPreferences.getHomeSortOrder().equalsIgnoreCase(SortOrder.SongSortOrder.SONG_ALBUM)){
            Collections.sort(data, new Comparator<Song>(){
                public int compare(Song obj1, Song obj2) {
                    return obj2.albumName.compareToIgnoreCase(obj1.albumName);
                }
            });
        }else if (mPreferences.getHomeSortOrder().equalsIgnoreCase(SortOrder.SongSortOrder.SONG_YEAR)){
            Collections.sort(data, new Comparator<Song>(){
                public int compare(Song obj1, Song obj2) {
                    return Integer.valueOf(obj2.year).compareTo(obj1.year);
                }
            });
        }else if (mPreferences.getHomeSortOrder().equalsIgnoreCase(SortOrder.SongSortOrder.SONG_DURATION)){
            Collections.sort(data, new Comparator<Song>(){
                public int compare(Song obj1, Song obj2) {
                    return Long.valueOf(obj2.duration).compareTo(obj1.duration);
                }
            });
        }else if (mPreferences.getHomeSortOrder().equalsIgnoreCase(SortOrder.SongSortOrder.SONG_DATE_ADDED)){
            Collections.sort(data, new Comparator<Song>(){
                public int compare(Song obj1, Song obj2) {
                    return Long.valueOf(obj2.dateAdded).compareTo(obj1.dateAdded);
                }
            });
        }


        return data;*/
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayingMetaChanged() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayStateChanged() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
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

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    switch (action) {

                        case MuzikoApp.INTENT_MEDIA_CHANGE:
                            new ReloadSongs().execute("");
                            break;
                        case MuzikoApp.INTENT_TRACK_CHANGED:
                            new ReloadSongs().execute("");
                            break;
                        case MuzikoApp.INTENT_TRACK_DELETED:
                            new ReloadSongs().execute("");
                            break;
                        case MuzikoApp.INTENT_PLAYLIST_CHANGE:
                            try {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playlistsDetailText.setText(PlaylistLoader.getPlayList().size() + " playlists");
                                    }
                                });
                            } catch (Exception e) {

                            }
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ReloadSongs extends AsyncTask<String, Void, String> {

        ArrayList<Song> data;

        @Override
        protected String doInBackground(String... params) {
            if (getActivity() != null)
                data = sortList();

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            mAdapter.setData(data);

        }

        @Override
        protected void onPreExecute() {
        }
    }

}
