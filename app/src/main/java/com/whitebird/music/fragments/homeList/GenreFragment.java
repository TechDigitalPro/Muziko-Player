package com.whitebird.music.fragments.homeList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.Config;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.adapters.GenreAdapter;
import com.whitebird.music.custom.AdvancedSearchButton;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.fragments.MainFragment;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GenreFragment extends Fragment implements View.OnClickListener, SearchView.OnCloseListener, SearchView.OnQueryTextListener, ActionMode.Callback {

    private final Executor mSearchExecutor = Executors.newSingleThreadExecutor();
    public ActionMode actionMode = null;
    ArrayList<Song> dataAll = new ArrayList<>();
    Receiver receiver;
    SearchView searchView;
    private GenreAdapter mAdapter;
    private BaseRecyclerView recyclerView;
    //    private FastScroller fastScroller;
    private GridLayoutManager layoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    private PreferencesUtility mPreferences;
    private boolean isGrid;
    private ImageButton advancedSearch;
    private AsyncTask mSearchTask = null;
    private InputMethodManager mImm;
    private String queryString = "";
    private boolean mode;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(getActivity());
        isGrid = mPreferences.isGenreInGrid();
        register();
        dataAll = sortList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_recyclerview, container, false);
        mImm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        recyclerView = rootView.findViewById(R.id.recyclerview);
//        fastScroller = rootView.findViewById(R.id.fastscroller);

        recyclerView.setEmptyView(getActivity(), rootView.findViewById(R.id.list_empty), "No media found");

        setLayoutManager();

        if (getActivity() != null) {
            mAdapter = new GenreAdapter(getActivity(), dataAll, GenreFragment.this);
            if (mAdapter != null) {
                mAdapter.setHasStableIds(true);
                recyclerView.setAdapter(mAdapter);
            }
            if (getActivity() != null) {
                setItemDecoration();
            }
        }

        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                .build();
        adView.loadAd(adRequest);

        return rootView;
    }

    private void setLayoutManager() {
        int Orientation = getResources().getConfiguration().orientation;


        if (isGrid) {
            if (Orientation == Configuration.ORIENTATION_PORTRAIT)
                layoutManager = new GridLayoutManager(getActivity(), MuzikoApp.getInstance().NoOfColumns());
            else
                layoutManager = new GridLayoutManager(getActivity(), MuzikoApp.getInstance().NoOfColumns());
//            fastScroller.setVisibility(View.GONE);
        } else {
            layoutManager = new GridLayoutManager(getActivity(), 1);
//            fastScroller.setVisibility(View.VISIBLE);
//            fastScroller.setRecyclerView(recyclerView);
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
        mAdapter = new GenreAdapter(getActivity(), sortList(), GenreFragment.this);
        recyclerView.setAdapter(mAdapter);
        layoutManager.setSpanCount(column);
        layoutManager.requestLayout();
        setItemDecoration();
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

                mAdapter.updateDataSet(sortList());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_search).setVisible(false);
        inflater.inflate(R.menu.album_sort_by, menu);
        inflater.inflate(R.menu.menu_show_as, menu);
        menu.findItem(R.id.menu_reverse).setVisible(true);
        MenuItem menuItemSearch = menu.findItem(R.id.player_search);
        if (menuItemSearch != null) {
            searchView = (SearchView) menuItemSearch.getActionView();
            searchView.setIconifiedByDefault(true);
            searchView.setIconified(false);

            AdvancedSearchButton advancedSearchButton = new AdvancedSearchButton();
            Resources resources = getResources();
            advancedSearch = advancedSearchButton.addButton(getActivity(), resources, searchView);
            advancedSearch.setOnClickListener(this);


            ActionBar.LayoutParams searchviewParams =
                    new ActionBar.LayoutParams(
                            ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT);
            searchView.setLayoutParams(searchviewParams);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            searchView.setOnQueryTextListener(this);
            searchView.setQueryHint("Search");
            EditText searchEditText = searchView.findViewById(R.id.search_src_text);
            searchEditText.setTextColor(Color.WHITE);
            searchEditText.setHintTextColor(Color.WHITE);
            ImageView imvClose = searchView.findViewById(R.id
                    .search_close_btn);
            ImageView magClose = searchView.findViewById(R.id
                    .search_mag_icon);
            imvClose.setImageResource(R.drawable.ic_close);
            magClose.setImageResource(R.drawable.ic_action_search);

            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    advancedSearch.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                mPreferences.setGenreSortOrder(SortOrder.AlbumSortOrder.ALBUM_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_za:
                mPreferences.setGenreSortOrder(SortOrder.AlbumSortOrder.ALBUM_Z_A);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_year:
                mPreferences.setGenreSortOrder(SortOrder.AlbumSortOrder.ALBUM_YEAR);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_date:
                mPreferences.setGenreSortOrder(SortOrder.AlbumSortOrder.ALBUM_DATE_ADDED);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_artist:
                mPreferences.setGenreSortOrder(SortOrder.AlbumSortOrder.ALBUM_ARTIST);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_number_of_songs:
                mPreferences.setGenreSortOrder(SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS);
                reloadAdapter();
                return true;
            case R.id.menu_show_as_list:
                mPreferences.setGenreInGrid(false);
                isGrid = false;
                updateLayoutManager(1);
                return true;
            case R.id.menu_show_as_grid:
                mPreferences.setGenreInGrid(true);
                isGrid = true;
                updateLayoutManager(2);
                return true;
            case R.id.menu_reverse:
                Collections.reverse(mAdapter.getData());
                mAdapter.notifyDataSetChanged();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<Song> sortList() {
        ArrayList<Song> data = new ArrayList<>();
        if (queryString.equals(""))
            data.addAll(DataHelper.getAllGenre());
        else
            data.addAll(DataHelper.searchGenre(queryString, 1000));


        if (mPreferences.getGenreSortOrder().equalsIgnoreCase(SortOrder.AlbumSortOrder.ALBUM_A_Z)) {
            Collections.sort(data, new Comparator<Song>() {
                public int compare(Song obj1, Song obj2) {

                    return obj1.genre_name.compareToIgnoreCase(obj2.genre_name);

                }
            });

        } else if (mPreferences.getGenreSortOrder().equalsIgnoreCase(SortOrder.AlbumSortOrder.ALBUM_Z_A)) {
            Collections.sort(data, new Comparator<Song>() {
                public int compare(Song obj1, Song obj2) {
                    return obj2.genre_name.compareToIgnoreCase(obj1.genre_name);
                }
            });

        } else if (mPreferences.getGenreSortOrder().equalsIgnoreCase(SortOrder.AlbumSortOrder.ALBUM_ARTIST)) {
            Collections.sort(data, new Comparator<Song>() {
                public int compare(Song obj1, Song obj2) {
                    return obj2.artistName.compareToIgnoreCase(obj1.artistName);
                }
            });

        } else if (mPreferences.getGenreSortOrder().equalsIgnoreCase(SortOrder.AlbumSortOrder.ALBUM_YEAR)) {
            Collections.sort(data, new Comparator<Song>() {
                public int compare(Song obj1, Song obj2) {
                    return Integer.valueOf(obj2.year).compareTo(obj1.year);
                }
            });
        } else if (mPreferences.getGenreSortOrder().equalsIgnoreCase(SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS)) {
            Collections.sort(data, new Comparator<Song>() {
                public int compare(Song obj1, Song obj2) {
                    return Integer.valueOf(obj2.songsCount).compareTo(obj1.songsCount);
                }
            });
        } else if (mPreferences.getGenreSortOrder().equalsIgnoreCase(SortOrder.AlbumSortOrder.ALBUM_DATE_ADDED)) {
            Collections.sort(data, new Comparator<Song>() {
                public int compare(Song obj1, Song obj2) {
                    return Long.valueOf(obj2.dateAdded).compareTo(obj1.dateAdded);
                }
            });
        }


        return data;
    }

    @Override
    public void onClick(View v) {

        if (v == advancedSearch) {

            NavigationUtils.navigateToSearch(getActivity());
        }

    }

    @Override
    public boolean onClose() {
        advancedSearch.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        onQueryTextChange(query);
        hideInputManager();

        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {

        if (newText.equals(queryString)) {
            return true;
        }
        if (mSearchTask != null) {
            mSearchTask.cancel(false);
            mSearchTask = null;
        }
        queryString = newText;
        if (queryString.trim().equals("")) {
            mAdapter.updateDataSet(dataAll);
            mAdapter.notifyDataSetChanged();
        } else {
            mSearchTask = new SearchTask().executeOnExecutor(mSearchExecutor, queryString);
        }

        return true;
    }

    public void hideInputManager() {
        if (searchView != null) {
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }
            searchView.clearFocus();


        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLayoutManager();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Config.primaryColorDark(getActivity(), Helpers.getATEKey(getActivity())));
        }
        getActivity().getMenuInflater().inflate(R.menu.context_groups, menu);
        ActionBarContextView actionBar = getActivity().getWindow().getDecorView().findViewById(R.id.action_mode_bar);
        actionBar.setBackgroundColor(Config.primaryColor(getActivity(), Helpers.getATEKey(getActivity())));

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        actionMode = mode;
        mAdapter.setActionMode(actionMode);
        if (this.mode) {

            (((MainFragment) GenreFragment.this.getParentFragment())).enableTabs(false);
        } else {
            ((MainFragment) GenreFragment.this.getParentFragment()).enableTabs(false);
        }

        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ArrayList<Song> selectedItem = mAdapter.getSelectedItems();
        ArrayList<Song> songs = new ArrayList<>();
        for (Song s : selectedItem) {
            songs.addAll(DataHelper.getSongsForGenre(s.genre_name));
        }
       // long[] ids = MuzikoApp.getSongIds(songs);
        if (selectedItem.size() > 0) {
            switch (item.getItemId()) {
                case R.id.play:
                    MusicPlayer.playAll(songs, 0,   false);
                    break;

                case R.id.play_next:
                    MusicPlayer.playNext(songs);
                    break;
                case R.id.add_to_queue:
                    MusicPlayer.addToQueue(songs);
                    break;
                case R.id.add_to_playlist:
                    AddPlaylistDialog.newInstance(songs).show(getActivity().getSupportFragmentManager(), "ADD_PLAYLIST");
                    break;
                case R.id.share:
                    MuzikoApp.getInstance().shareSongs(getActivity(), songs);
                    break;
                case R.id.delete:
                    MuzikoUtils.deleteMultipleSong(getActivity(), songs, "Songs");
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
            ((MainFragment) GenreFragment.this.getParentFragment()).enableTabs(true);
        } else {
            ((MainFragment) GenreFragment.this.getParentFragment()).enableTabs(true);
        }
        mAdapter.clearSelection();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!recyclerView.isComputingLayout()) {
                    mAdapter.setMultiSelect(false);
                    actionMode = null;
                } else {
                    onDestroyActionMode(GenreFragment.this.actionMode);
                }
            }
        });

    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    switch (action) {

                        case MuzikoApp.INTENT_TRACK_CHANGED:
                            reloadAdapter();
                            break;
                        case MuzikoApp.INTENT_TRACK_DELETED:
                            reloadAdapter();
                            break;
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

    private class SearchTask extends AsyncTask<String, Void, ArrayList<Song>> {

        @Override
        protected ArrayList<Song> doInBackground(String... params) {

            if (params[0].length() == 0)
                return dataAll;

            ArrayList<Song> results = DataHelper.searchGenre(params[0], 1000);
          /*  if (!results.isEmpty()) {
                results.addAll(results);
            }*/
            return results;
        }

        @Override
        protected void onPostExecute(ArrayList<Song> objects) {
            super.onPostExecute(objects);
            mSearchTask = null;
            if (objects != null) {
                mAdapter.updateDataSet(objects);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}

