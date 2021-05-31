
package com.whitebird.music.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.AlbumArtFolderActivity;
import com.whitebird.music.activities.AlbumArtInternetActivity;
import com.whitebird.music.activities.BaseActivity;
import com.whitebird.music.adapters.AlbumSongsAdapter;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.listeners.MusicStateListener;
import com.whitebird.music.listeners.SimplelTransitionListener;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.ATEUtils;
import com.whitebird.music.utils.ArtworkHelper;
import com.whitebird.music.utils.Constants;
import com.whitebird.music.utils.FabAnimationUtils;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.ImageUtils;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.Utils;
import com.whitebird.music.widgets.DividerItemDecoration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.whitebird.music.MuzikoApp.ARTWORK_PICK_FROM_ALBUM_FOLDER;
import static com.whitebird.music.MuzikoApp.ARTWORK_PICK_FROM_GALLERY;
import static com.whitebird.music.MuzikoApp.ARTWORK_PICK_FROM_ID3_TAGS;
import static com.whitebird.music.MuzikoApp.ARTWORK_PICK_FROM_INTERNET;

public class AlbumDetailFragment extends Fragment implements MusicStateListener, ActionMode.Callback {

    public ActionMode actionMode = null;
    Receiver receiver;
    private String albumID = "";
    private ImageView albumArt;
    private TextView albumTitle, albumDetails;
    private AppCompatActivity mContext;
    private RecyclerView recyclerView;
    private AlbumSongsAdapter mAdapter;
    private Toolbar toolbar;
    private Song album;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton fab;
    private boolean loadFailed = false;
    private Context context;
    private int primaryColor = -1;

    public static AlbumDetailFragment newInstance(String name, boolean useTransition, String transitionName) {
        AlbumDetailFragment fragment = new AlbumDetailFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ALBUM_ID, name);
        args.putBoolean("transition", useTransition);
        if (useTransition)
            args.putString("transition_name", transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register();
        if (getArguments() != null) {
            albumID = getArguments().getString(Constants.ALBUM_ID);
        }
        context = getActivity();
        mContext = (AppCompatActivity) context;

    }

    @TargetApi(21)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(
                R.layout.fragment_album_detail, container, false);

        albumArt = (ImageView) rootView.findViewById(R.id.album_art);

        albumTitle = (TextView) rootView.findViewById(R.id.album_title);
        albumDetails = (TextView) rootView.findViewById(R.id.album_details);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        if (getArguments().getBoolean("transition")) {
            albumArt.setTransitionName(getArguments().getString("transition_name"));
        }
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);

        recyclerView.setEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        albumArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openArtMenu();
            }
        });

        AdView adView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("8401E8E9D1A58054CC5D94578030EA9E")
                .build();
        adView.loadAd(adRequest);

        setUpEverything();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AlbumSongsAdapter adapter = (AlbumSongsAdapter) recyclerView.getAdapter();
                        MusicPlayer.playAll( adapter.getSongList(), 0 , true);
                        NavigationUtils.navigateToNowplaying(getActivity(), false);
                    }
                }, 50);
            }
        });
        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);
        return rootView;
    }

    private void openArtMenu() {

        new MaterialDialog.Builder(getActivity())
                .theme(Theme.LIGHT).titleColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity()))).
                title(R.string.manage_art)
                .items(R.array.manage_artwork)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {

                        switch (position) {

                            /*  case 0:

                             *//*    artworkHelper.autoPickAlbumArt(getActivity(), selectedItem, false);
                                adapter.notifyItemChanged(selectedItemPosition);
                                if (mode) {
                                    ((PlayerListActivity) getActivity()).updateFragments();
                                } else {
                                    ((MainActivity) getActivity()).updateFragments();
                                }*//*
                                break;*/

                            case 0:

                                Intent internetintent = new Intent(getActivity(), AlbumArtInternetActivity.class);
                                internetintent.putExtra("tag", "TAG");
                                internetintent.putExtra("item", album);

                                getActivity().startActivityForResult(internetintent, ARTWORK_PICK_FROM_INTERNET);

                                break;

                            case 1:

                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");

                                try {
                                    getActivity().startActivityForResult(Intent.createChooser(intent, getString(R.string.select_album_art)), ARTWORK_PICK_FROM_GALLERY);
                                } catch (ActivityNotFoundException ex) {
                                    // Potentially direct the user to the Market with a Dialog
                                    MuzikoApp.toast(getActivity(), getString(R.string.install_file_manager));
                                }
                                break;

                            case 2:
                                Intent folderintent = new Intent(getActivity(), AlbumArtFolderActivity.class);
                                folderintent.putExtra("tag", "TAG");
                                folderintent.putExtra("item", album);

                                getActivity().startActivityForResult(folderintent, ARTWORK_PICK_FROM_ALBUM_FOLDER);

                                break;

                           /* case 4:

                                 Intent albumartIntent = new Intent(getActivity(), AlbumArtID3Activity.class);
                                albumartIntent.putExtra("tag", "TAG");
                                albumartIntent.putExtra("item", selectedItem);
                                albumartIntent.putExtra("index", selectedItemPosition);
                                getActivity().startActivityForResult(albumartIntent, ARTWORK_PICK_FROM_ID3_TAGS);

                                break;*/

                            case 3:

                                Uri myUri = Uri.parse("content://media/external/audio/albumart/" + album.albumId);
                                CropImage.activity(myUri)
                                        .setGuidelines(CropImageView.Guidelines.ON)
                                        .setActivityTitle(album.albumName)
                                        .start(getActivity());

                                break;

                            case 4:
                                Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(),
                                        R.drawable.ic_empty_music2);
                                Bitmap out = Utils.resize(icon, MuzikoApp.IMAGE_LARGE_SIZE, MuzikoApp.IMAGE_LARGE_SIZE);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                out.compress(Bitmap.CompressFormat.JPEG, MuzikoApp.JPEG_COMPRESS, stream);
                                icon.recycle();
                                out.recycle();
                                byte[] bitmapData = stream.toByteArray();
                                new ArtworkHelper().setArt(getActivity(), album, bitmapData);
                                setAlbumart();

                                break;

                        }
                    }
                })
                .show();
    }

    private void setupToolbar() {

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitle(album.albumName);

    }

    private void setAlbumart() {
        ImageUtils.loadAlbumArtIntoView(album.albumId, albumArt, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        loadFailed = true;
                        MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(context)
                                .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                                .setColor(MuzikoUtils.getBlackWhiteColor(Config.accentColor(context, Helpers.getATEKey(context))));
                        ATEUtils.setFabBackgroundTint(fab, Config.accentColor(context, Helpers.getATEKey(context)));
                        fab.setImageDrawable(builder.build());
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        try {
                            new Palette.Builder(loadedImage).generate(new Palette.PaletteAsyncListener() {
                                                                          @Override
                                                                          public void onGenerated(Palette palette) {
                                                                              Palette.Swatch swatch = palette.getVibrantSwatch();
                                                                              if (swatch != null) {
                                                                                  primaryColor = swatch.getRgb();
                                                                                  collapsingToolbarLayout.setContentScrimColor(primaryColor);
                                                                                  if (getActivity() != null)
                                                                                      ATEUtils.setStatusBarColor(getActivity(), Helpers.getATEKey(getActivity()), primaryColor);
                                                                              } else {
                                                                                  Palette.Swatch swatchMuted = palette.getMutedSwatch();
                                                                                  if (swatchMuted != null) {
                                                                                      primaryColor = swatchMuted.getRgb();
                                                                                      collapsingToolbarLayout.setContentScrimColor(primaryColor);
                                                                                      if (getActivity() != null)
                                                                                          ATEUtils.setStatusBarColor(getActivity(), Helpers.getATEKey(getActivity()), primaryColor);
                                                                                  }
                                                                              }

                                                                              if (getActivity() != null) {
                                                                                  MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(getActivity())
                                                                                          .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                                                                                          .setSizeDp(30);
                                                                                  if (primaryColor != -1) {
                                                                                      builder.setColor(MuzikoUtils.getBlackWhiteColor(primaryColor));
                                                                                      ATEUtils.setFabBackgroundTint(fab, primaryColor);
                                                                                      fab.setImageDrawable(builder.build());
                                                                                  } else {
                                                                                      if (context != null) {
                                                                                          ATEUtils.setFabBackgroundTint(fab, Config.accentColor(context, Helpers.getATEKey(context)));
                                                                                          builder.setColor(MuzikoUtils.getBlackWhiteColor(Config.accentColor(context, Helpers.getATEKey(context))));
                                                                                          fab.setImageDrawable(builder.build());
                                                                                      }
                                                                                  }
                                                                              }
                                                                          }
                                                                      }

                            );
                        } catch (
                                Exception ignored
                                )

                        {

                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                    }

                }

        );
    }

    private void setAlbumDetails() {

        String songCount = MuzikoUtils.makeLabel(getActivity(), R.plurals.Nsongs, album.songsCount);

        String year = (album.year != 0) ? (" - " + String.valueOf(album.year)) : "";

        albumTitle.setText(album.albumName);
        albumDetails.setText(album.artistName + " - " + songCount + year);


    }

    private void setUpAlbumSongs() {

        ArrayList<Song> songList = DataHelper.getSongsForAlbum(albumID);
        mAdapter = new AlbumSongsAdapter((AppCompatActivity) getActivity(), songList, songList.get(0).albumId, this);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(mAdapter);

        album = DataHelper.getAlbum(songList.get(0).albumId);

        setAlbumart();

    }

    private void setUpEverything() {
        setUpAlbumSongs();
        setupToolbar();
        setAlbumDetails();

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

 /*   public void restartLoader() {

    }

    public void onPlaylistChanged() {

    }

    public void onMetaChanged() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }*/

    private void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                List<Song> songList = DataHelper.getSongsForAlbum(albumID);
                mAdapter.updateDataSet(songList);
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
        inflater.inflate(R.menu.album_detail, menu);
        if (getActivity() != null)
            ATE.applyMenu(getActivity(), "dark_theme", menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_go_to_artist:
                NavigationUtils.goToArtist(getContext(), album.artistId);
                break;
            case R.id.popup_song_addto_queue:
                MusicPlayer.addToQueue( mAdapter.getSongList()   );
                break;
            case R.id.popup_song_addto_playlist:
                AddPlaylistDialog.newInstance(mAdapter.getSongList()).show(mContext.getSupportFragmentManager(), "ADD_PLAYLIST");
                break;
           /* case R.id.menu_sort_by_az:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_za:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_Z_A);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_year:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_YEAR);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_duration:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_DURATION);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_track_number:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
                reloadAdapter();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helpers.getATEKey(getActivity());
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        if (primaryColor != -1 && getActivity() != null) {
            collapsingToolbarLayout.setContentScrimColor(primaryColor);
            ATEUtils.setFabBackgroundTint(fab, primaryColor);
            ATEUtils.setStatusBarColor(getActivity(), ateKey, primaryColor);
        }


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


        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        ArrayList<Song> selectedSong = mAdapter.getSelectedItems();
       // long[] ids = MuzikoApp.getSongIds(mAdapter.getSelectedItems());
        if (selectedSong.size() > 0) {

            switch (item.getItemId()) {
                case R.id.play:
                    MusicPlayer.playAll(selectedSong, 0,   false);
                    break;
                case R.id.play_next:

                    MusicPlayer.playNext(selectedSong);
                    break;
                case R.id.add_to_fav:

                    MuzikoApp.moveToFavorite(selectedSong, getActivity());
                    break;
                case R.id.add_to_queue:

                    MusicPlayer.addToQueue(selectedSong);
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
                    onDestroyActionMode(AlbumDetailFragment.this.actionMode);
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARTWORK_PICK_FROM_GALLERY:
                if (resultCode == Activity.RESULT_OK) {

                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = Utils.getPath(getActivity(), uri);
                    if (path != null) {


                        Bitmap bMap = BitmapFactory.decodeFile(path);
                        Bitmap out = Utils.resize(bMap, MuzikoApp.IMAGE_LARGE_SIZE, MuzikoApp.IMAGE_LARGE_SIZE);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        out.compress(Bitmap.CompressFormat.JPEG, MuzikoApp.JPEG_COMPRESS, stream);
                        bMap.recycle();
                        out.recycle();
                        byte[] bitmapData = stream.toByteArray();

                        if (bitmapData != null) {
                            ArtworkHelper artworkHelper = new ArtworkHelper();
                            artworkHelper.setArt(getActivity(), album, bitmapData);
                            setAlbumart();
                           /* if (mode) {
                                ((PlayerListActivity) getActivity()).updateFragments();
                            } else {
                                ((MainActivity) getActivity()).updateFragments();
                            }*/
                        } else {
                            MuzikoApp.toast(getActivity(), "Unable to read file!");
                        }
                    } else {
                        MuzikoApp.toast(getActivity(), "File not available on device!");
                    }
                }

                break;

            case ARTWORK_PICK_FROM_INTERNET:
                if (resultCode == Activity.RESULT_OK) {


                    final Song queueItem = (Song) data.getSerializableExtra("item");
                    final int position = data.getIntExtra("index", 0);
                    byte[] byteArray = data.getByteArrayExtra("image");

                    if (byteArray != null) {
                        ArtworkHelper artworkHelper = new ArtworkHelper();
                        artworkHelper.setArt(getActivity(), queueItem, byteArray);
                        setAlbumart();
                     /*   if (mode) {
                            ((PlayerListActivity) getActivity()).updateFragments();
                        } else {
                            ((MainActivity) getActivity()).updateFragments();
                        }*/
                    } else {
                        MuzikoApp.toast(getActivity(), "Album Art not found");
                    }

                }
                break;

            case ARTWORK_PICK_FROM_ALBUM_FOLDER:
                if (resultCode == Activity.RESULT_OK) {
                    final Song queueItem = (Song) data.getSerializableExtra("item");
                    final int position = data.getIntExtra("index", 0);
                    String filePath = data.getStringExtra("filepath");

                    if (filePath != null) {
                        try {
                            Bitmap bMap = BitmapFactory.decodeFile(filePath);
                            Bitmap out = Utils.resize(bMap, MuzikoApp.IMAGE_LARGE_SIZE, MuzikoApp.IMAGE_LARGE_SIZE);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            out.compress(Bitmap.CompressFormat.JPEG, MuzikoApp.JPEG_COMPRESS, stream);
                            bMap.recycle();
                            out.recycle();
                            byte[] bitmapData = stream.toByteArray();
                            ArtworkHelper artworkHelper = new ArtworkHelper();
                            if (bitmapData != null) {
                                artworkHelper.setArt(getActivity(), album, bitmapData);
                                setAlbumart();
                           /* if (mode) {
                                ((PlayerListActivity) getActivity()).updateFragments();
                            } else {
                                ((MainActivity) getActivity()).updateFragments();
                            }*/
                            } else {
                                MuzikoApp.toast(getActivity(), "Unable to read file!");
                            }
                        } catch (Exception e) {
                            MuzikoApp.toast(getActivity(), "Unable to read file!");
                        }
                    } else {
                        MuzikoApp.toast(getActivity(), "Album Art not found");
                    }

                }

                break;

            case ARTWORK_PICK_FROM_ID3_TAGS:
                if (resultCode == Activity.RESULT_OK) {
                    ArtworkHelper artworkHelper = new ArtworkHelper();
                    Song queueItem = (Song) data.getSerializableExtra("item");
                    final int position = data.getIntExtra("index", 0);
                    byte[] bitmapData = artworkHelper.pickAlbumArt(getActivity(), queueItem);

                    if (queueItem != null && bitmapData != null) {
                        artworkHelper.setArt(getActivity(), queueItem, bitmapData);
                        setAlbumart();
                      /*if (mode) {
                            ((PlayerListActivity) getActivity()).updateFragments();
                        } else {
                            ((MainActivity) getActivity()).updateFragments();
                        }*/
                    } else {
                        MuzikoApp.toast(getActivity(), "Album Art not found");
                    }

                }

                break;


            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == Activity.RESULT_OK && album != null) {

                    Uri uri = result.getUri();
                    String path = Utils.getPath(getActivity(), uri);
                    if (path != null) {

                        Bitmap bMap = BitmapFactory.decodeFile(path);
                        Bitmap out = Utils.resize(bMap, MuzikoApp.IMAGE_LARGE_SIZE, MuzikoApp.IMAGE_LARGE_SIZE);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        out.compress(Bitmap.CompressFormat.JPEG, MuzikoApp.JPEG_COMPRESS, stream);
                        bMap.recycle();
                        out.recycle();
                        byte[] bitmapData = stream.toByteArray();
                        ArtworkHelper artworkHelper = new ArtworkHelper();
                        if (bitmapData != null) {
                            artworkHelper.setArt(getActivity(), album, bitmapData);
                            setAlbumart();
                           /* if (mode) {
                                ((PlayerListActivity) getActivity()).updateFragments();
                            } else {
                                ((MainActivity) getActivity()).updateFragments();
                            }*/
                        } else {
                            MuzikoApp.toast(getActivity(), "Unable to read file!");
                        }
                    } else {
                        MuzikoApp.toast(getActivity(), "File not available on device!");
                    }

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    error.printStackTrace();
                    MuzikoApp.toast(getActivity(), "Album art was not changed");
                }

                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
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

    private class EnterTransitionListener extends SimplelTransitionListener {

        @TargetApi(21)
        public void onTransitionEnd(Transition paramTransition) {
            FabAnimationUtils.scaleIn(fab);
        }

        public void onTransitionStart(Transition paramTransition) {
            FabAnimationUtils.scaleOut(fab, 0, null);
        }

    }


}
