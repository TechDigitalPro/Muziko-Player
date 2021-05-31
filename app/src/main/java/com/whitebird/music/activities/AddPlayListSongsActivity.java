package com.whitebird.music.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.appthemeengine.Config;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.adapters.AddPlayListSongAdapter;
import com.whitebird.music.custom.AdvancedSearchButton;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.models.Playlist;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.PreferencesUtility;
import com.whitebird.music.utils.StaticData;
import com.whitebird.music.widgets.DividerItemDecoration;
import com.whitebird.music.widgets.FastScroller.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddPlayListSongsActivity extends BaseActivity implements View.OnClickListener, SearchView.OnCloseListener, SearchView.OnQueryTextListener {

    private final Executor mSearchExecutor = Executors.newSingleThreadExecutor();
    public ActionMode actionMode = null;


    SearchView searchView;
    ArrayList<Song> data = DataHelper.getAllTrack();
    private AddPlayListSongAdapter mAdapter;
    private FastScrollRecyclerView recyclerView;
    private PreferencesUtility mPreferences;
    @Nullable
    private ImageButton advancedSearch;
    private AsyncTask mSearchTask = null;
    private InputMethodManager mImm;
    private String queryString = "";

    private InterstitialAd interstitialAd;
    boolean isActivityIsVisible = true;

    @Override
    protected void onPause() {
        super.onPause();
        isActivityIsVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isActivityIsVisible = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_playlist_song);
        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new LinearLayoutManager(AddPlayListSongsActivity.this));
        recyclerView.setEmptyView(AddPlayListSongsActivity.this, findViewById(R.id.list_empty), "No media found");


        mAdapter = new AddPlayListSongAdapter((AppCompatActivity) AddPlayListSongsActivity.this, sortList());
        recyclerView.setAdapter(mAdapter);
        if (AddPlayListSongsActivity.this != null)
            recyclerView.addItemDecoration(new DividerItemDecoration(AddPlayListSongsActivity.this, 0));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("Add to '"+MuzikoApp.readAllPlayLists().get(StaticData.PLAYLIST_POSITION).name+"'");

        findViewById(R.id.btn_add).setBackgroundColor(Config.primaryColor(AddPlayListSongsActivity.this, Helpers.getATEKey(AddPlayListSongsActivity.this)));
        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getSelectedItems().size()==0){
                    MuzikoApp.toast(AddPlayListSongsActivity.this,"Select the song first!");
                    return;
                }
                ArrayList<Playlist> data=MuzikoApp.readAllPlayLists();

                data.get(StaticData.PLAYLIST_POSITION).getSongList().addAll(mAdapter.getSelectedItems());
                MuzikoApp.writeAllPlayLists(data);
                MuzikoApp.toast(AddPlayListSongsActivity.this,mAdapter.getSelectedItems().size()+" Song Added !");
                sendBroadcast(new Intent(MuzikoApp.INTENT_PLAYLIST_CHANGE));
                finish();


            }
        });

        try {
            interstitialAd = new InterstitialAd(this);
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

    }

    public void restartLoader() {

    }

    public void onPlaylistChanged() {

    }

    public void onMetaChanged() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

   /* @Override
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
    }*/

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_sort_by, menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.menu_sort_by).setVisible(false);


        MenuItem menuItemSearch = menu.findItem(R.id.player_search);

        if (menuItemSearch != null) {
            searchView = (SearchView) menuItemSearch.getActionView();
            searchView.setIconifiedByDefault(true);
            searchView.setIconified(false);

            AdvancedSearchButton advancedSearchButton = new AdvancedSearchButton();
            Resources resources = getResources();
            advancedSearch = advancedSearchButton.addButton(AddPlayListSongsActivity.this, resources, searchView);
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
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private ArrayList<Song> sortList() {
        ArrayList<Song> songs = new ArrayList<>();
        if (queryString.equals(""))
            songs.addAll(data);
        else {


            for (Song song : data) {
                if (songs.size() > 1000) {
                    return songs;
                }

                if (song.name.toLowerCase().contains(queryString.toLowerCase())) {
                    song.type = StaticData.TYPE_SONG;
                    songs.add(song);
                }

            }
        }

        return songs;
    }

    @Override
    public void onClick(View v) {

        if (v == advancedSearch) {
            NavigationUtils.navigateToSearch(AddPlayListSongsActivity.this);
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
            mAdapter.updateDataSet(data);
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


    private class SearchTask extends AsyncTask<String, Void, ArrayList<Song>> {

        @Override
        protected ArrayList<Song> doInBackground(String... params) {

            return sortList();
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
