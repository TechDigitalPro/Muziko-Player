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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.appthemeengine.Config;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.BaseActivity;
import com.whitebird.music.adapters.IgnoreAdapter;
import com.whitebird.music.listeners.MusicStateListener;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.PreferencesUtility;
import com.whitebird.music.widgets.BaseRecyclerView;
import com.whitebird.music.widgets.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class IgnoreFragment extends Fragment implements MusicStateListener, ActionMode.Callback {

    public ActionMode actionMode = null;
    Receiver receiver;
    ArrayList<Song> dataAll = new ArrayList<>();
    LinearLayout ll1;
    NestedScrollView n_scroll;
    private IgnoreAdapter mAdapter;
    private BaseRecyclerView recyclerView;
    private PreferencesUtility mPreferences;
    private boolean mode;
    private Toolbar toolbar;

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
                R.layout.fragment_queue, container, false);

        n_scroll = rootView.findViewById(R.id.n_scroll);
        ll1 = rootView.findViewById(R.id.ll1);
        recyclerView = rootView.findViewById(R.id.recyclerview);


        toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyView(getActivity(), rootView.findViewById(R.id.list_empty), "No media found");

        mAdapter = new IgnoreAdapter((AppCompatActivity) getActivity(), dataAll, this);
        recyclerView.setAdapter(mAdapter);
        if (getActivity() != null)
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), 0));

        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                .build();
        adView.loadAd(adRequest);

        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);
        setupToolbar();
        return rootView;
    }


    private void setupToolbar() {

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Ignore");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MainActivity.isPressHome = true;
                getActivity().onBackPressed();
            }
        });
        register();

    }

   /* public void restartLoader() {

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

    }


    @Override
    public void onResume() {
        super.onResume();


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

        filter.addAction(MuzikoApp.INTENT_MEDIA_CHANGE);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Config.primaryColorDark(getActivity(), Helpers.getATEKey(getActivity())));
        }
        getActivity().getMenuInflater().inflate(R.menu.context_ignore, menu);
        ActionBarContextView actionBar = getActivity().getWindow().getDecorView().findViewById(R.id.action_mode_bar);
        actionBar.setBackgroundColor(Config.primaryColor(getActivity(), Helpers.getATEKey(getActivity())));

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;
        mAdapter.setActionMode(actionMode);

        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        ArrayList<Song> selectedSong = mAdapter.getSelectedItems();
       // long[] ids = MuzikoApp.getSongIds(mAdapter.getSelectedItems());
        if (selectedSong.size() > 0) {
            switch (item.getItemId()) {

                case R.id.restore_ignore:
                    ArrayList<Song> restore=new ArrayList<>();
                    restore.addAll(selectedSong);
                    MuzikoApp.removeFromIgnore(restore, getActivity());

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

        mAdapter.clearSelection();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!recyclerView.isComputingLayout()) {
                    mAdapter.setMultiSelect(false);
                    actionMode = null;
                } else {
                    onDestroyActionMode(IgnoreFragment.this.actionMode);
                }
            }
        });

    }

    private ArrayList<Song> sortList() {

        ArrayList<Song> data = MuzikoApp.readIgnoreList();
        Collections.sort(data, new Comparator<Song>() {
            public int compare(Song obj1, Song obj2) {
                return Long.valueOf(obj2.dateAdded).compareTo(obj1.dateAdded);
            }
        });
        return data;

    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {

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
                            new IgnoreFragment.ReloadSongs().execute("");
                            break;
                        case MuzikoApp.INTENT_TRACK_DELETED:
                            new ReloadSongs().execute("");
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
