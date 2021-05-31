

package com.whitebird.music.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.appthemeengine.customizers.ATEToolbarCustomizer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.adapters.SongsListAdapter;
import com.whitebird.music.dataloaders.PlaylistLoader;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.listeners.SimplelTransitionListener;
import com.whitebird.music.models.Playlist;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Constants;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.widgets.DividerItemDecoration;
import com.whitebird.music.widgets.DragSortRecycler;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailActivity extends BaseActivity implements ATEActivityThemeCustomizer, ATEToolbarCustomizer, ActionMode.Callback {

    private String action;
    private int playlistID;
    public ActionMode actionMode = null;

    private AppCompatActivity mContext = PlaylistDetailActivity.this;
    private SongsListAdapter mAdapter;
    private RecyclerView recyclerView;
    private ImageView blurFrame;
    private TextView playlistname;
    private View foreground;
    private boolean animate;


    private Runnable playlistUsercreated = new Runnable() {
        @Override
        public void run() {
            new loadUserCreatedPlaylist().execute("");

        }
    };

    @TargetApi(21)
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        action = getIntent().getAction();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");


        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        blurFrame = (ImageView) findViewById(R.id.blurFrame);
        playlistname = (TextView) findViewById(R.id.name);
        foreground = findViewById(R.id.foreground);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setAlbumart();

        animate = getIntent().getBooleanExtra(Constants.ACTIVITY_TRANSITION, false);
        if (animate && MuzikoUtils.isLollipop()) {
            getWindow().getEnterTransition().addListener(new EnterTransitionListener());
        } else {
            setUpSongs();
        }

    }

    private void setAlbumart() {
        playlistname.setText(getIntent().getExtras().getString(Constants.PLAYLIST_NAME));
        foreground.setBackgroundColor(getIntent().getExtras().getInt(Constants.PLAYLIST_FOREGROUND_COLOR));
        loadBitmap(MuzikoUtils.getAlbumArtUri(getIntent().getExtras().getLong(Constants.ALBUM_ID)).toString());
        register();
    }
    public void onDestroy() {

        unregister();
        super.onDestroy();
    }
    private void setUpSongs() {
        Runnable navigation = playlistUsercreated;
        if (navigation != null) {
            navigation.run();

            DragSortRecycler dragSortRecycler = new DragSortRecycler();
            dragSortRecycler.setViewHandleId(R.id.reorder);

            dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
                @Override
                public void onItemMoved(int from, int to) {
                    Log.d("playlist", "onItemMoved " + from + " to " + to);
                    Song song = mAdapter.getSongAt(from);
                    mAdapter.removeSongAt(from);
                    mAdapter.addSongTo(to, song);
                    mAdapter.notifyDataSetChanged();
                    MediaStore.Audio.Playlists.Members.moveItem(getContentResolver(),
                            playlistID, from, to);


                    ArrayList<Playlist> playLists=MuzikoApp.readAllPlayLists();
                    playLists.get(playlistID).setSongList((ArrayList<Song>) mAdapter.getData());
                    MuzikoApp.writeAllPlayLists(playLists);



                }
            });

            recyclerView.addItemDecoration(dragSortRecycler);
            recyclerView.addOnItemTouchListener(dragSortRecycler);
            recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());

        } else {
            Log.d("PlaylistDetail", "mo action specified");
        }
    }

    private void loadBitmap(String uri) {
        MuzikoApp.loader.displayImage(uri, blurFrame,
                MuzikoApp.OPTIONS);
    }

    Receiver receiver;


    private void unregister() {
        try {
            if (receiver != null) {
               unregisterReceiver(receiver);
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
        registerReceiver(receiver, filter);
    }
    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    String action = intent.getAction();
                    switch (action) {

                        case MuzikoApp.INTENT_MEDIA_CHANGE:
                            new loadUserCreatedPlaylist().execute("");
                            break;
                        case MuzikoApp.INTENT_TRACK_CHANGED:
                            new loadUserCreatedPlaylist().execute("");
                            break;
                        case MuzikoApp.INTENT_TRACK_DELETED:
                            new loadUserCreatedPlaylist().execute("");
                            break;

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setRecyclerViewAapter() {
        recyclerView.setAdapter(mAdapter);
        if (animate && MuzikoUtils.isLollipop()) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST, R.drawable.item_divider_white));
                }
            }, 250);
        } else
            recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST, R.drawable.item_divider_white));
    }

    @StyleRes
    @Override
    public int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ? R.style.AppTheme_FullScreen_Dark : R.style.AppTheme_FullScreen_Light;

    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_playlist_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_delete_playlist:
                showDeletePlaylistDialog();
                break;
            case R.id.action_clear_auto_playlist:
                clearAutoPlaylists();
                break;
            case R.id.action_add:
                 startActivity(new Intent(PlaylistDetailActivity.this,AddPlayListSongsActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeletePlaylistDialog() {
        new MaterialDialog.Builder(this)
                .title("Delete playlist?")
                .content("Are you sure you want to delete playlist " + playlistname.getText().toString() + " ?")
                .positiveText("Delete")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PlaylistLoader.deletePlaylists(playlistname.getText().toString(), PlaylistDetailActivity.this);
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
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

    private void clearAutoPlaylists() {
        switch (action) {
            case Constants.NAVIGATE_PLAYLIST_LASTADDED:
                MuzikoUtils.clearLastAdded(this);
                break;
            case Constants.NAVIGATE_PLAYLIST_RECENT:
                MuzikoUtils.clearRecent(this);
                break;
            case Constants.NAVIGATE_PLAYLIST_TOPTRACKS:
                MuzikoUtils.clearTopTracks(this);
                break;
        }
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public int getToolbarColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public int getLightToolbarMode() {
        return Config.LIGHT_TOOLBAR_AUTO;
    }

    private class loadUserCreatedPlaylist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            playlistID = getIntent().getExtras().getInt(Constants.PLAYLIST_ID);
            List<Song> playlistsongs = PlaylistLoader.getPlayList().get(playlistID).getSongList();
            mAdapter = new SongsListAdapter(mContext, playlistsongs, true, animate, false, PlaylistDetailActivity.this);
            mAdapter.setPlaylistId(playlistID);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            setRecyclerViewAapter();
        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class EnterTransitionListener extends SimplelTransitionListener {

        @TargetApi(21)
        public void onTransitionEnd(Transition paramTransition) {
            setUpSongs();
        }

        public void onTransitionStart(Transition paramTransition) {
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        setUpSongs();
    }


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             getWindow().setStatusBarColor(Config.primaryColorDark(PlaylistDetailActivity.this, Helpers.getATEKey(PlaylistDetailActivity.this)));
        }
        PlaylistDetailActivity.this.getMenuInflater().inflate(R.menu.context_playlist, menu);
        ActionBarContextView actionBar = PlaylistDetailActivity.this.getWindow().getDecorView().findViewById(R.id.action_mode_bar);
        actionBar.setBackgroundColor(Config.primaryColor(PlaylistDetailActivity.this, Helpers.getATEKey(PlaylistDetailActivity.this)));

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

                case R.id.remove_playlist:

                    ArrayList<Song> selectedSongN=new ArrayList<>();
                    selectedSongN.addAll(mAdapter.getSelectedItems());
                    List<Song> playlistSongs = PlaylistLoader.getPlayList().get(playlistID).getSongList();

                    for (Song s:selectedSong){
                        for (int i=0;i<playlistSongs.size();i++){
                            if (s.id==playlistSongs.get(i).id){
                                MuzikoUtils.removeFromPlaylist(mContext, s.id, playlistID);
                                PlaylistLoader.removeSong(mContext.getIntent().getExtras().getString(Constants.PLAYLIST_NAME), s, mContext);
                                mAdapter.removeSongAt(i);
                                mAdapter.notifyItemRemoved(i);
                                playlistSongs.remove(i);
                                break;
                            }
                        }
                    }


                    break;

                case R.id.play:
                    MusicPlayer.playAll(selectedSong, 0,   false);
                    PlaylistDetailActivity.this.sendBroadcast(new Intent(MuzikoApp.INTENT_RELOAD_QUEUE));
                    break;
                case R.id.play_next:

                    MusicPlayer.playNext(selectedSong);
                    PlaylistDetailActivity.this.sendBroadcast(new Intent(MuzikoApp.INTENT_RELOAD_QUEUE));
                    break;
                case R.id.add_to_queue:

                    MusicPlayer.addToQueue(selectedSong);
                    PlaylistDetailActivity.this.sendBroadcast(new Intent(MuzikoApp.INTENT_RELOAD_QUEUE));
                    break;
                case R.id.share:
                    MuzikoApp.getInstance().shareSongs(PlaylistDetailActivity.this, mAdapter.getSelectedItems());
                    break;
                case R.id.add_to_playlist:
                    AddPlaylistDialog.newInstance(selectedSong).show(PlaylistDetailActivity.this.getSupportFragmentManager(), "ADD_PLAYLIST");
                    break;

                case R.id.multi_tag_edit:
                    NavigationUtils.multiTagEdit(PlaylistDetailActivity.this, selectedSong);
                    break;
                case R.id.delete:
                    MuzikoUtils.deleteMultipleSong(PlaylistDetailActivity.this, selectedSong, "Songs");
                    PlaylistDetailActivity.this.sendBroadcast(new Intent(MuzikoApp.INTENT_RELOAD_QUEUE));
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
                    onDestroyActionMode(PlaylistDetailActivity.this.actionMode);
                }
            }
        });

    }
    
}
