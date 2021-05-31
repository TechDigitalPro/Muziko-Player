

package com.whitebird.music.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.adapters.PlaylistAdapter;
import com.whitebird.music.dataloaders.PlaylistLoader;
import com.whitebird.music.dialogs.CreatePlaylistDialog;
import com.whitebird.music.dialogs.PlaylistMerge;
import com.whitebird.music.models.Playlist;
import com.whitebird.music.models.Song;
import com.whitebird.music.subfragments.PlaylistPagerFragment;
import com.whitebird.music.utils.Constants;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.PreferencesUtility;
import com.whitebird.music.widgets.DividerItemDecoration;
import com.whitebird.music.widgets.MultiViewPager;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment implements ActionMode.Callback {

    public ActionMode actionMode = null;
    Receiver receiver;
    Toolbar toolbar;
    ArrayList<Playlist> dataPlayList;
    private int playlistcount;
    private FragmentStatePagerAdapter adapter;
    private MultiViewPager pager;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    private PreferencesUtility mPreferences;
    private boolean isGrid;
    private boolean isDefault;
    private boolean showAuto;
    private PlaylistAdapter mAdapter;
    private List<Playlist> playlists = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mPreferences = PreferencesUtility.getInstance(getActivity());
        isGrid = mPreferences.getPlaylistView() == Constants.PLAYLIST_VIEW_GRID;
        isDefault = mPreferences.getPlaylistView() == Constants.PLAYLIST_VIEW_DEFAULT;
        showAuto = mPreferences.showAutoPlaylist();
        register();
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
        filter.addAction(MuzikoApp.INTENT_PLAYLIST_CHANGE);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_playlist, container, false);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        pager = (MultiViewPager) rootView.findViewById(R.id.playlistpager);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);

        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                .build();
        adView.loadAd(adRequest);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);


        playlists = PlaylistLoader.getPlayList();
        playlistcount = playlists.size();

        if (isDefault) {
            initPager();
        } else {
            initRecyclerView();
        }

        setupToolbar();
        return rootView;

    }

    public void onDestroy() {

        unregister();
        super.onDestroy();
    }

    private void setupToolbar() {

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.playlists);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.isPressHome = true;
                getActivity().onBackPressed();
            }
        });


    }

    private void initPager() {
        pager.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setAdapter(null);
        adapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {

            @Override
            public int getCount() {
                return playlistcount;
            }

            @Override
            public Fragment getItem(int position) {
                return PlaylistPagerFragment.newInstance(position);
            }

        };
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);
    }

    private void initRecyclerView() {
        recyclerView.setVisibility(View.VISIBLE);
        pager.setVisibility(View.GONE);
        setLayoutManager();
        mAdapter = new PlaylistAdapter(getActivity(), playlists, PlaylistFragment.this);

        recyclerView.setAdapter(mAdapter);
        //to add spacing between cards
        if (getActivity() != null) {
            setItemDecoration();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLayoutManager();
    }

    private void setLayoutManager() {

        if (isGrid) {
            int Orientation = getResources().getConfiguration().orientation;
            if (Orientation == Configuration.ORIENTATION_PORTRAIT)
                layoutManager = new GridLayoutManager(getActivity(), MuzikoApp.getInstance().NoOfColumns());
            else
                layoutManager = new GridLayoutManager(getActivity(), MuzikoApp.getInstance().NoOfColumns());
        } else {
            layoutManager = new GridLayoutManager(getActivity(), 1);
        }
        recyclerView.setLayoutManager(layoutManager);
    }

    private void setItemDecoration() {
        if (isGrid) {
            int spacingInPixels = getActivity().getResources().getDimensionPixelSize(R.dimen.spacing_card_album_grid);
            itemDecoration = new SpacesItemDecoration(spacingInPixels);
        } else {
            itemDecoration = new DividerItemDecoration(getActivity(), 0);
        }
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void updateLayoutManager(int column) {
        recyclerView.removeItemDecoration(itemDecoration);
        recyclerView.setAdapter(new PlaylistAdapter(getActivity(), PlaylistLoader.getPlayList(), PlaylistFragment.this));
        layoutManager.setSpanCount(column);
        layoutManager.requestLayout();
        setItemDecoration();
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

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_playlist, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (showAuto) {
            menu.findItem(R.id.action_view_auto_playlists).setTitle("Hide auto playlists");
        } else menu.findItem(R.id.action_view_auto_playlists).setTitle("Show auto playlists");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_playlist:
                CreatePlaylistDialog.newInstance().show(getChildFragmentManager(), "CREATE_PLAYLIST");
                return true;
            case R.id.menu_show_as_list:
                mPreferences.setPlaylistView(Constants.PLAYLIST_VIEW_LIST);
                isGrid = false;
                isDefault = false;
                initRecyclerView();
                updateLayoutManager(1);
                return true;
            case R.id.menu_show_as_grid:
                mPreferences.setPlaylistView(Constants.PLAYLIST_VIEW_GRID);
                isGrid = true;
                isDefault = false;
                initRecyclerView();
                updateLayoutManager(2);
                return true;
            case R.id.menu_show_as_default:
                mPreferences.setPlaylistView(Constants.PLAYLIST_VIEW_DEFAULT);
                isDefault = true;
                initPager();
                return true;
            case R.id.action_view_auto_playlists:
                if (showAuto) {
                    showAuto = false;
                    mPreferences.setToggleShowAutoPlaylist(false);
                } else {
                    showAuto = true;
                    mPreferences.setToggleShowAutoPlaylist(true);
                }
                reloadPlaylists();
                getActivity().invalidateOptionsMenu();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void updatePlaylists(final String name) {
        playlists = PlaylistLoader.getPlayList();
        playlistcount = playlists.size();

        if (isDefault) {
            adapter.notifyDataSetChanged();
            if (name != null) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < playlists.size(); i++) {
                            String playlistname = playlists.get(i).getName();
                            if (playlistname.equals(name)) {
                                pager.setCurrentItem(i);
                                break;
                            }
                        }
                    }
                }, 200);
            }

        } else {
            mAdapter.updateDataSet(playlists);
        }
    }

    public void reloadPlaylists() {
        playlists = PlaylistLoader.getPlayList();
        playlistcount = playlists.size();

        if (isDefault) {
            initPager();
        } else {
            initRecyclerView();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.ACTION_DELETE_PLAYLIST) {
            if (resultCode == Activity.RESULT_OK) {
                reloadPlaylists();
            }

        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Config.primaryColorDark(getActivity(), Helpers.getATEKey(getActivity())));
        }
        getActivity().getMenuInflater().inflate(R.menu.playlist_action_menu, menu);
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
        final ArrayList<Playlist> selectedItem = new ArrayList<>();
        selectedItem.addAll(mAdapter.getSelectedItems());

        if (selectedItem.size() > 0) {
            ArrayList<Song> songData = new ArrayList<>();
            for (Playlist playlist : selectedItem) {
                songData.addAll(playlist.getSongList());
            }

          //  long[] ids = MuzikoApp.getSongIds(songData);
            switch (item.getItemId()) {
                case R.id.play:
                    MusicPlayer.playAll(songData, 0,   false);
                    break;

                case R.id.add_to_queue:
                    MusicPlayer.addToQueue(songData);
                    break;

                case R.id.detete:

                    new MaterialDialog.Builder(getActivity()).theme(Theme.LIGHT)
                            .negativeColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())))
                            .titleColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())))
                            .neutralColorRes(R.color.dialog_negetive_button)
                            .positiveColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())))
                            .title("Dou you want to delete selected Playlist ?")
                            .negativeText("Cancel")
                            .positiveText("continue").onPositive(new MaterialDialog.SingleButtonCallback() {


                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dataPlayList = MuzikoApp.readAllPlayLists();
                            for (Playlist playlist : selectedItem) {
                                removeData(playlist);
                            }
                            MuzikoApp.writeAllPlayLists(dataPlayList);
                            getActivity().sendBroadcast(new Intent(MuzikoApp.INTENT_PLAYLIST_CHANGE));
                        }
                    }).show();

                    break;
                case R.id.merge:
                    new PlaylistMerge(selectedItem).open(getActivity());
                    break;

            }
        }
        mode.finish();
        actionMode = null;
        return false;
    }

    private void removeData(Playlist playlist) {

        for (Playlist play : dataPlayList) {
            if (play.name.equals(playlist.name)) {
                dataPlayList.remove(play);
                return;
            }
        }

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
                    onDestroyActionMode(PlaylistFragment.this.actionMode);
                }
            }
        });
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {


            outRect.left = space;
            outRect.top = space;
            outRect.right = space;
            outRect.bottom = space;

        }
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    switch (action) {


                        case MuzikoApp.INTENT_PLAYLIST_CHANGE:
                            try {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playlists = PlaylistLoader.getPlayList();
                                        playlistcount = playlists.size();
                                        if (isDefault) {
                                            initPager();
                                        } else {
                                            initRecyclerView();
                                        }
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


}

