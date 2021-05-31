

package com.whitebird.music.adapters;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.dialogs.PlayFrom;
import com.whitebird.music.dialogs.PreviewSong;
import com.whitebird.music.dialogs.SetRingtone;
import com.whitebird.music.dialogs.ShareRingtone;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.widgets.VuMeterView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

public class ArtistSongAdapter extends BaseSongAdapter<ArtistSongAdapter.ItemHolder> {

    private ArrayList<Song> arraylist;
    private AppCompatActivity mContext;
    private long artistID;
    private long[] songIDs;
    private String ateKey;
    ActionMode.Callback callback;
    private boolean isMultiSelect = false;
    private ArrayList<Song> selectedSong = new ArrayList<>();
    private ActionMode actionMode;

    public ArtistSongAdapter(AppCompatActivity context, ArrayList<Song> arraylist, long artistID, ActionMode.Callback callback) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.artistID = artistID;
        this.songIDs = getSongIds();
        this.ateKey = Helpers.getATEKey(context);
        this.callback = callback;
    }

    public void setActionMode(ActionMode actionMode) {
        this.actionMode = actionMode;
    }

    public void setMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
    }

    public ArrayList<Song> getSelectedItems() {
        return selectedSong;
    }

    public void clearSelection() {
        selectedSong.clear();
        notifyDataSetChanged();
    }

    public void toggleSelection(Song song) {
        if (selectedSong.contains(song)) {
            selectedSong.remove(song);
        } else {
            selectedSong.add(song);
        }
        if (actionMode != null) {
            if (selectedSong.size() == 0) actionMode.setTitle("");
            else
                actionMode.setTitle(String.format("%d Song%s", selectedSong.size(), selectedSong.size() != 1 ? "s" : ""));
        }

        notifyItemChanged(arraylist.indexOf(song));
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == 0) {
            View v0 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.artist_detail_albums_header, null);
            ItemHolder ml = new ItemHolder(v0);
            return ml;
        } else {
            View v2 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist_song, null);
            ItemHolder ml = new ItemHolder(v2);
            return ml;
        }
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {

        if (getItemViewType(i) == 0) {
            //nothing
            setUpAlbums(itemHolder.albumsRecyclerView);
        } else {
            Song localItem = arraylist.get(i);

            if (selectedSong.contains(localItem)) {
                itemHolder.viewOverlay.setVisibility(View.VISIBLE);
                itemHolder.viewOverlay.setAlpha(.5f);
                itemHolder.viewOverlay.setBackgroundColor(Config.primaryColor(mContext, Helpers.getATEKey(mContext)));
            } else {
                itemHolder.viewOverlay.setVisibility(View.GONE);
            }
            itemHolder.title.setText(localItem.title);
            itemHolder.album.setText(localItem.albumName);
            if (MusicPlayer.getCurrentAudioId() == localItem.id) {
                itemHolder.title.setTextColor(Config.accentColor(mContext, ateKey));
                if (MusicPlayer.isPlaying()) {
                    itemHolder.visualizer.setColor(Config.accentColor(mContext, ateKey));
                    itemHolder.visualizer.setVisibility(View.VISIBLE);
                } else {
                    itemHolder.visualizer.setVisibility(View.GONE);
                }
            } else {
                itemHolder.visualizer.setVisibility(View.GONE);
           /* if (isPlaylist) {
                itemHolder.title.setTextColor(Color.WHITE);
            } else*/
                {
                    itemHolder.title.setTextColor(Config.textColorPrimary(mContext, ateKey));
                }
            }

            MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(localItem.albumId).toString(),
                    itemHolder.albumArt, MuzikoApp.OPTIONS, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            ImageView iv = (ImageView) view;
                            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            iv.setImageResource(R.drawable.ic_empty_music2);
                            iv.setAdjustViewBounds(true);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            ImageView iv = (ImageView) view;
                            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            iv.setAdjustViewBounds(false);
                            iv.setImageBitmap(loadedImage);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                        }
                    });
            setOnPopupMenuListener(itemHolder, i - 1);
        }

    }

    @Override
    public void onViewRecycled(ItemHolder itemHolder) {

        if (itemHolder.getItemViewType() == 0)
            clearExtraSpacingBetweenCards(itemHolder.albumsRecyclerView);

    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_song_play:
                                ArrayList<Song> songNew = new ArrayList<>();
                                songNew.add(arraylist.get(position + 1));
                                MusicPlayer.playAll(songNew, 0, false);
                                break;
                            case R.id.popup_move_ignore:
                                ArrayList<Song> ignore = new ArrayList();
                                ignore.add(arraylist.get(position));
                                MuzikoApp.moveToIgnore(ignore, mContext);
                                break;
                            case R.id.add_to_fav:
                                ArrayList<Song> fav = new ArrayList();
                                fav.add(arraylist.get(position));
                                MuzikoApp.moveToFavorite(fav, mContext);
                                break;
                            case R.id.popup_song_play_next:
                               /* long[] ids = new long[1];
                                ids[0] = arraylist.get(position + 1).id;*/

                                MusicPlayer.playNext(arraylist.get(position + 1));
                                break;
                            case R.id.popup_song_goto_album:
                                NavigationUtils.goToAlbum(mContext, arraylist.get(position + 1).albumName);
                                break;
                            case R.id.popup_song_goto_artist:
                                NavigationUtils.goToArtist(mContext, arraylist.get(position + 1).artistId);
                                break;
                            case R.id.popup_song_addto_queue:
                                ArrayList<Song> song = new ArrayList<>();
                                song.add(arraylist.get(position + 1));
                                MusicPlayer.addToQueue(song);
                                break;
                            case R.id.popup_song_addto_playlist:
                                AddPlaylistDialog.newInstance(arraylist.get(position + 1)).show(((AppCompatActivity) mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                                break;
                            case R.id.preview_song:
                                PreviewSong previewSong = new PreviewSong();
                                previewSong.open(mContext, arraylist.get(position));
                                break;
                            case R.id.start_time:
                                PlayFrom play = new PlayFrom();
                                play.open(mContext, arraylist.get(position));
                                break;
                            case R.id.send_au:
                                ShareRingtone shareRingtone = new ShareRingtone();
                                shareRingtone.open(mContext, arraylist.get(position));
                                break;
                            case R.id.popup_cut:
                                MuzikoApp.getInstance().cutSong(arraylist.get(position));
                                break;
                            case R.id.popup_song_share:
                                MuzikoUtils.shareTrack(mContext, arraylist.get(position + 1).id);
                                break;
                            case R.id.tag_edit:
                                ArrayList<Song> songY = new ArrayList<>();
                                songY.add(arraylist.get(position + 1));
                                NavigationUtils.multiTagEdit(mContext, songY);
                                break;
                            case R.id.popup_song_delete:
                                // long[] deleteIds = {arraylist.get(position + 1).id};
                                ArrayList<Song> songs = new ArrayList<>();
                                songs.add(arraylist.get(position + 1));
                                MuzikoUtils.showDeleteDialog(mContext, arraylist.get(position + 1).title, songs);
                                break;
                            case R.id.popup_set_ring:
                                boolean permission;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    permission = Settings.System.canWrite(mContext);
                                } else {
                                    permission =
                                            ContextCompat.checkSelfPermission(
                                                    mContext, Manifest.permission.WRITE_SETTINGS)
                                                    == PackageManager.PERMISSION_GRANTED;
                                }
                                if (!permission) {

                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                        new MaterialDialog.Builder(mContext)
                                                .theme(Theme.LIGHT)
                                                .negativeColor(Config.accentColor(mContext, Helpers.getATEKey(mContext)))
                                                .titleColor(Config.accentColor(mContext, Helpers.getATEKey(mContext)))
                                                .neutralColorRes(R.color.dialog_negetive_button)
                                                .positiveColor(Config.accentColor(mContext, Helpers.getATEKey(mContext)))
                                                .title("Permission required")
                                                .content(
                                                        "Muziko requires the write settings permission to set your default ringtone. You need to enable it on the settings screen.")
                                                .positiveText("Ok")
                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                                            intent.setData(Uri.parse("package:com.whitebird.music"));
                                                            mContext.startActivityForResult(intent, 1020);
                                                        } else {
                                                            ActivityCompat.requestPermissions(
                                                                    mContext,
                                                                    new String[]{Manifest.permission.WRITE_SETTINGS},
                                                                    1020);
                                                        }
                                                    }
                                                })
                                                .negativeText("Cancel")
                                                .show();
                                    } else {
                                        SetRingtone createRingtone = new SetRingtone();
                                        createRingtone.open(mContext, arraylist.get(position));
                                    }
                                } else {
                                    SetRingtone createRingtone = new SetRingtone();
                                    createRingtone.open(mContext, arraylist.get(position));
                                }

                                break;
                            case R.id.popup_song_details:

                                MuzikoApp.details(mContext, arraylist.get(position));
                                break;
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_song);
                menu.show();
            }
        });
    }

    private void setUpAlbums(RecyclerView albumsRecyclerview) {

        albumsRecyclerview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        albumsRecyclerview.setHasFixedSize(true);

        //to add spacing between cards
        int spacingInPixels = mContext.getResources().getDimensionPixelSize(R.dimen.spacing_card);
        albumsRecyclerview.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        albumsRecyclerview.setNestedScrollingEnabled(false);


        ArtistAlbumAdapter mAlbumAdapter = new ArtistAlbumAdapter(mContext, DataHelper.getAlbumsForArtist(artistID));
        albumsRecyclerview.setAdapter(mAlbumAdapter);
    }

    private void clearExtraSpacingBetweenCards(RecyclerView albumsRecyclerview) {
        //to clear any extra spacing between cards
        int spacingInPixelstoClear = -(mContext.getResources().getDimensionPixelSize(R.dimen.spacing_card));
        albumsRecyclerview.addItemDecoration(new SpacesItemDecoration(spacingInPixelstoClear));

    }

    public long[] getSongIds() {
        List<Song> actualArraylist = new ArrayList<Song>(arraylist);
        //actualArraylist.remove(0);
        long[] ret = new long[actualArraylist.size()];
        for (int i = 0; i < actualArraylist.size(); i++) {
            ret[i] = actualArraylist.get(i).id;
        }
        return ret;
    }

    public ArrayList<Song> getSongs() {
        List<Song> actualArraylist = new ArrayList<Song>(arraylist);

        return (ArrayList<Song>) actualArraylist;
    }

    public ArrayList<Song> getSongList() {
//        long[] ret = new long[getItemCount()];
//        for (int i = 0; i < getItemCount(); i++) {
//            ret[i] = arraylist.get(i).id;
//        }

        return arraylist;
    }

    @Override
    public void removeSongAt(int i) {
        arraylist.remove(i);
        updateDataSet(arraylist);
    }

    @Override
    public void updateDataSet(List<Song> arraylist) {
        this.arraylist = (ArrayList<Song>) arraylist;
        this.songIDs = getSongIds();


    }

    @Override
    public int getItemViewType(int position) {
        int viewType;
        if (position == 0) {
            viewType = 0;
        } else viewType = 1;
        return viewType;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, album;
        protected ImageView albumArt, menu;
        protected RecyclerView albumsRecyclerView;
        protected VuMeterView visualizer;
        private View viewOverlay;

        public ItemHolder(View view) {
            super(view);
            if (callback != null)
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!isMultiSelect) {
                            mContext.startSupportActionMode(callback);
                            isMultiSelect = true;
                            toggleSelection(arraylist.get(getAdapterPosition()));
                        }

                        return true;
                    }
                });
            this.albumsRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_album);

            this.title = (TextView) view.findViewById(R.id.song_title);
            this.album = (TextView) view.findViewById(R.id.song_album);
            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.menu = (ImageView) view.findViewById(R.id.popup_menu);
            this.visualizer = (VuMeterView) view.findViewById(R.id.visualizer);
            this.viewOverlay = (View) view.findViewById(R.id.viewOverlay);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (isMultiSelect) {
                toggleSelection(arraylist.get(getAdapterPosition()));
                return;
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        playAll(mContext, getSongs(), getAdapterPosition(), artistID,
                                MuzikoUtils.IdType.Artist, false,
                                arraylist.get(getAdapterPosition()), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0);

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

            //the padding from left
            outRect.left = space;


        }
    }
}




