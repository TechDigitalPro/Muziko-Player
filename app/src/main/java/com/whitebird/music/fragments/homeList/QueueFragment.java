
package com.whitebird.music.fragments.homeList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.BaseActivity;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.adapters.PlayingQueueAdapter;
import com.whitebird.music.dataloaders.QueueLoader;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.listeners.MusicStateListener;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.widgets.BaseRecyclerView;
import com.whitebird.music.widgets.DragSortRecycler;

import java.util.ArrayList;

import static com.whitebird.music.subfragments.QuickControlsFragment.isQueueOpen;

public class QueueFragment extends Fragment implements MusicStateListener, ActionMode.Callback {

    private static QueueFragment queueFragment;
    public ActionMode actionMode = null;
    Receiver receiver;
    private PlayingQueueAdapter mAdapter;
    private BaseRecyclerView recyclerView;
    private Toolbar toolbar;

    public static QueueFragment getInstance() {
        if (queueFragment == null)
            queueFragment = new QueueFragment();
        return queueFragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_queue, container, false);

        toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

//        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
//        ab.setDisplayHomeAsUpEnabled(true);
//        ab.setTitle(R.string.playing_queue);

        recyclerView = rootView.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(null);
        recyclerView.setEmptyView(getActivity(), rootView.findViewById(R.id.list_empty), "No songs in queue");

        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                .build();
        adView.loadAd(adRequest);

        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);
        setupToolbar();
        new loadQueueSongs().execute("");
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //  new loadQueueSongs().execute("");
    }

    private void setupToolbar() {

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Playing Queue");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.isPressHome = true;
                getActivity().onBackPressed();
            }
        });
        register();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("dark_theme", false)) {
            ATE.apply(this, "dark_theme");
        } else {
            ATE.apply(this, "light_theme");
        }
    }

  /*  public void restartLoader() {

    }

    public void onPlaylistChanged() {

    }

    public void onMetaChanged() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }*/

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Config.primaryColorDark(getActivity(), Helpers.getATEKey(getActivity())));
        }
        getActivity().getMenuInflater().inflate(R.menu.context_queue, menu);
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
        ArrayList<Integer> selectedPosition = mAdapter.getSelectedPosition();

       // long[] ids = MuzikoApp.getSongIds(mAdapter.getSelectedItems());
        if (selectedSong.size() > 0) {

            switch (item.getItemId()) {

                case R.id.remove_playlist:
                    ArrayList<Song> selectedSongN=new ArrayList<>();
                    selectedSongN.addAll(mAdapter.getSelectedItems());
                    ArrayList<Song> allSong=new ArrayList<>();
                    allSong.addAll(QueueLoader.getQueueSongs(getActivity()));

                    for (Integer i:selectedPosition){
                        MusicPlayer.removeTrackAtPosition(/*allSong.get(i).id,*/ i);
                        mAdapter.removeSongAt(i);
                        mAdapter.notifyItemRemoved(i);
                        allSong.remove(i);
                    }


                  /*  for (Song s:selectedSong){
                        for (int i=0;i<allSong.size();i++){
                            if (s.id==allSong.get(i).id){
                                MusicPlayer.removeTrackAtPosition(s.id, i);
                                mAdapter.removeSongAt(i);
                                mAdapter.notifyItemRemoved(i);
                                allSong.remove(i);
                               break;
                            }
                        }
                    }*/

                    break;

                case R.id.play:
                    MusicPlayer.playAll(selectedSong, 0,  false);
                    getActivity().sendBroadcast(new Intent(MuzikoApp.INTENT_RELOAD_QUEUE));
                    break;
                case R.id.play_next:

                    MusicPlayer.playNext(selectedSong);
                    getActivity().sendBroadcast(new Intent(MuzikoApp.INTENT_RELOAD_QUEUE));
                    break;
                case R.id.add_to_queue:

                    MusicPlayer.addToQueue(selectedSong);
                    getActivity().sendBroadcast(new Intent(MuzikoApp.INTENT_RELOAD_QUEUE));
                    break;
                case R.id.share:
                    MuzikoApp.getInstance().shareSongs(getActivity(), mAdapter.getSelectedItems());
                    break;
                case R.id.add_to_playlist:
                    AddPlaylistDialog.newInstance(selectedSong).show(getActivity().getSupportFragmentManager(), "ADD_PLAYLIST");
                    break;

                case R.id.multi_tag_edit:
                    NavigationUtils.multiTagEdit(getActivity(), selectedSong);
                    break;
                case R.id.delete:
                    MuzikoUtils.deleteMultipleSong(getActivity(), selectedSong, "Songs");
                    getActivity().sendBroadcast(new Intent(MuzikoApp.INTENT_RELOAD_QUEUE));
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
                    onDestroyActionMode(QueueFragment.this.actionMode);
                }
            }
        });

    }

    private void showClearQueuetDialog() {
        new MaterialDialog.Builder(getActivity())
                .title("Clear Queue?")
                .content("Are you sure you want to clear queue ?")
                .positiveText("Clear")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MusicPlayer.clearQueue();
                        new loadQueueSongs().execute("");
                        ((MainActivity) getActivity()).panelLayout.hidePanel();
                        getActivity().sendBroadcast(new Intent(MuzikoApp.INTENT_CLEAR_QUQUE));

                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
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
        isQueueOpen = false;
        super.onDestroy();
    }

    private void register() {
        receiver = new Receiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MuzikoApp.INTENT_SHUFFLE_ALL);
        filter.addAction(MuzikoApp.INTENT_RELOAD_QUEUE);

        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.clear_queue, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == R.id.clear_view) {
            showClearQueuetDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private class loadQueueSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            mAdapter = new PlayingQueueAdapter((AppCompatActivity) getActivity(), QueueLoader.getQueueSongs(getActivity()), QueueFragment.this);
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
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    switch (action) {

                        case MuzikoApp.INTENT_SHUFFLE_ALL:
                            new loadQueueSongs().execute("");
                            break;
                        case MuzikoApp.INTENT_RELOAD_QUEUE:
                            new loadQueueSongs().execute("");
                            break;

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

