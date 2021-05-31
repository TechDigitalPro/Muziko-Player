package com.whitebird.music.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.BaseActivity;
import com.whitebird.music.adapters.RingToneAdapter;
import com.whitebird.music.custom.AdvancedSearchButton;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.listeners.MusicStateListener;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.PreferencesUtility;
import com.whitebird.music.utils.SortOrder;
import com.whitebird.music.widgets.DividerItemDecoration;
import com.whitebird.music.widgets.FastScroller.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RingToneFragment extends Fragment implements MusicStateListener, View.OnClickListener, SearchView.OnCloseListener, SearchView.OnQueryTextListener{

    private final Executor mSearchExecutor = Executors.newSingleThreadExecutor();
    public ActionMode actionMode = null;
    ArrayList<Song> dataAll = new ArrayList<>();
    Receiver receiver;
    SearchView searchView;
    private RingToneAdapter mAdapter;
    private FastScrollRecyclerView recyclerView;
    private PreferencesUtility mPreferences;
    @Nullable
    private ImageButton advancedSearch;
    private AsyncTask mSearchTask = null;
    private InputMethodManager mImm;
    private String queryString = "";
    private boolean mode;
    private boolean IsMp3Cutter = false;


    public RingToneFragment() {
        IsMp3Cutter = false;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(getActivity());
        dataAll = sortList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_recyclerview, container, false);

        mImm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        recyclerView = rootView.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setEmptyView(getActivity(), rootView.findViewById(R.id.list_empty), "No media found");
//        FastScroller fastScroller =  rootView.findViewById(R.id.fastscroller);
//        fastScroller.setRecyclerView(recyclerView);

        mAdapter = new RingToneAdapter((AppCompatActivity) getActivity(), sortList());
        recyclerView.setAdapter(mAdapter);
        if (getActivity() != null)
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), 0));
        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);
        register();

        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                .build();
        adView.loadAd(adRequest);

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

    private void reloadAdapter() {
        new ReloadSongs().execute("");
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }


    public void setMp3Frament(boolean isMp3Cutter) {
        this.IsMp3Cutter = isMp3Cutter;
    }

    @Override

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.findItem(R.id.action_search).setVisible(false);

        inflater.inflate(R.menu.song_sort_by, menu);
        if (IsMp3Cutter)
            menu.findItem(R.id.menu_sort_by).setVisible(false);
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
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_za:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_Z_A);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_artist:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_album:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_year:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_YEAR);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_duration:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_DURATION);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_date:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_DATE_ADDED);
                reloadAdapter();
                return true;
            case R.id.menu_reverse:
                Collections.reverse(mAdapter.getData());
                mAdapter.notifyDataSetChanged();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onDestroy() {

        unregister();
        super.onDestroy();
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

    private ArrayList<Song> sortList() {
        ArrayList<Song> data = new ArrayList<>();
        if (queryString.equals(""))
            data.addAll(DataHelper.getAllRingtone());
        else
            data.addAll(DataHelper.searchRingTone(queryString, 1000));

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

                        case MuzikoApp.INTENT_TRACK_CHANGED:
                            new ReloadSongs().execute("");
                            break;
                        case MuzikoApp.INTENT_TRACK_DELETED:
                            new ReloadSongs().execute("");
                            break;

                        case MuzikoApp.INTENT_MEDIA_CHANGE:
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

    private class SearchTask extends AsyncTask<String, Void, ArrayList<Song>> {

        @Override
        protected ArrayList<Song> doInBackground(String... params) {

            if (params[0].length() == 0)
                return dataAll;

            ArrayList<Song> results = DataHelper.searchRingTone(params[0], 1000);
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

