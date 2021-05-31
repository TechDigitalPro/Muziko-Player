package com.whitebird.music.fragments;

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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.Config;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.BaseActivity;
import com.whitebird.music.adapters.GenerSongAdapter;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.listeners.MusicStateListener;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Constants;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.widgets.DividerItemDecoration;

import java.util.ArrayList;

public class GenreMusicFragment extends Fragment implements MusicStateListener, ActionMode.Callback  {

    public static RecyclerView songsRecyclerview;
    private String genreName = "";
    private GenerSongAdapter mSongAdapter;
    public ActionMode actionMode = null;
    public static GenreMusicFragment newInstance(String id) {
        GenreMusicFragment fragment = new GenreMusicFragment();
        Bundle args = new Bundle();
        args.putString(Constants.GENRE_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            genreName = getArguments().getString(Constants.GENRE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_artist_music, container, false);

        songsRecyclerview = (RecyclerView) rootView.findViewById(R.id.recycler_view_songs);

        setUpSongs();
register();
        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void setUpSongs() {
        songsRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        ArrayList<Song> songList;
        songList = DataHelper.getSongsForGenre(genreName);

        // adding one dummy song to top of arraylist
        //there will be albums header at this position in recyclerview
        songList.add(0, new Song(-1, -1, -1, "dummy", "dummy", "dummy", -1, -1,""));

        mSongAdapter = new GenerSongAdapter((AppCompatActivity)getActivity(), songList,genreName,this);
        songsRecyclerview.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        songsRecyclerview.setAdapter(mSongAdapter);
    }
  /*  public void restartLoader() {

    }

    public void onPlaylistChanged() {

    }

    public void onMetaChanged() {
        if (mSongAdapter != null)
            mSongAdapter.notifyDataSetChanged();
    }*/
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
        mSongAdapter.setActionMode(actionMode);




        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        ArrayList<Song> selectedSong = mSongAdapter.getSelectedItems();
      //  long[] ids = MuzikoApp.getSongIds(mSongAdapter.getSelectedItems());
        if (selectedSong.size() > 0) {

            switch (item.getItemId()) {
                case R.id.play:
                    MusicPlayer.playAll(selectedSong, 0,   false);
                    break;
                case R.id.play_next:

                    MusicPlayer.playNext(selectedSong);
                    break;
                case R.id.add_to_queue:

                    MusicPlayer.addToQueue(selectedSong);
                    break;
                case R.id.add_to_fav:

                    MuzikoApp.moveToFavorite(selectedSong, getActivity());
                    break;
                case R.id.share:
                    MuzikoApp.getInstance().shareSongs(getActivity(), selectedSong);
                    break;
                case R.id.add_to_playlist:
                    AddPlaylistDialog.newInstance(selectedSong).show(getActivity().getSupportFragmentManager(), "ADD_PLAYLIST");
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

        mSongAdapter.clearSelection();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!songsRecyclerview.isComputingLayout()) {
                    mSongAdapter.setMultiSelect(false);
                    actionMode = null;
                } else {
                    onDestroyActionMode(GenreMusicFragment.this.actionMode);
                }
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

    @Override
    public void onDestroy() {

        unregister();
        super.onDestroy();
    }
    Receiver receiver;

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {
        if (mSongAdapter != null)
            mSongAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayingMetaChanged() {
        if (mSongAdapter != null)
            mSongAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayStateChanged() {
        if (mSongAdapter != null)
            mSongAdapter.notifyDataSetChanged();
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

                     /*   case MuzikoApp.INTENT_TRACK_CHANGED:
                            setUpSongs();
                            break;
                        case MuzikoApp.INTENT_TRACK_DELETED:
                            setUpSongs();
                            break;*/
                        case MuzikoApp.INTENT_MEDIA_CHANGE:
                            reloadAdapter();

                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void register() {
        receiver = new Receiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MuzikoApp.INTENT_TRACK_DELETED);
        filter.addAction(MuzikoApp.INTENT_TRACK_CHANGED);
        filter.addAction(MuzikoApp.INTENT_MEDIA_CHANGE);
        getActivity().registerReceiver(receiver, filter);
    }
    private void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                ArrayList<Song> songList;
                songList = DataHelper.getSongsForGenre(genreName);

                // adding one dummy song to top of arraylist
                //there will be albums header at this position in recyclerview
                songList.add(0, new Song(-1, -1, -1, "dummy", "dummy", "dummy", -1, -1,""));
                mSongAdapter.updateDataSet(songList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mSongAdapter.notifyDataSetChanged();
            }
        }.execute();
    }
}

