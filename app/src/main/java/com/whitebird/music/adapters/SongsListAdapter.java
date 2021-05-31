package com.whitebird.music.adapters;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.user.SongUploader;
import com.whitebird.music.dataloaders.PlaylistLoader;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.dialogs.PlayFrom;
import com.whitebird.music.dialogs.PreviewSong;
import com.whitebird.music.dialogs.SetRingtone;
import com.whitebird.music.dialogs.ShareRingtone;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Constants;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.widgets.FastScroller.views.FastScrollRecyclerView;
import com.whitebird.music.widgets.VuMeterView;

import java.util.ArrayList;
import java.util.List;

public class SongsListAdapter extends BaseSongAdapter<SongsListAdapter.ItemHolder> implements FastScrollRecyclerView.SectionedAdapter {

    public int currentlyPlayingPosition;
    ActionMode.Callback callback;
    private List<Song> arraylist;
    private AppCompatActivity mContext;
    private long[] songIDs;
    private boolean isPlaylist,isFavSong;
    private boolean animate;
    private int lastPosition = -1;
    private String ateKey;
    private long playlistId;
    private boolean isMultiSelect = false;
    private ArrayList<Song> selectedSong = new ArrayList<>();
    private ActionMode actionMode;
    private boolean isMp3Cutter = false;

    public SongsListAdapter(AppCompatActivity context, List<Song> arraylist, boolean isPlaylistSong, boolean animate, boolean isMp3Cutter, ActionMode.Callback callback) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.isPlaylist = isPlaylistSong;
        this.songIDs = getSongIds();
        this.ateKey = Helpers.getATEKey(context);
        this.animate = animate;
        this.isMp3Cutter = isMp3Cutter;
        this.callback = callback;
        this.isFavSong=false;
    }


    public void setIsFav(boolean isFavSong){
        this.isFavSong=isFavSong;
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

    public List<Song> getData() {
        return arraylist;
    }

    public void setData(List<Song> arraylist) {
        this.arraylist = arraylist;
        this.songIDs = getSongIds();
        notifyDataSetChanged();
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (isPlaylist) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song_playlist, null);
            ItemHolder ml = new ItemHolder(v);
            return ml;
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song, null);
            ItemHolder ml = new ItemHolder(v);
            return ml;
        }
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {
        Song localItem = arraylist.get(i);
        if (selectedSong.contains(localItem)) {
            itemHolder.viewOverlay.setVisibility(View.VISIBLE);
            itemHolder.viewOverlay.setAlpha(.5f);
            itemHolder.viewOverlay.setBackgroundColor(Config.primaryColor(mContext, Helpers.getATEKey(mContext)));
        } else {
            itemHolder.viewOverlay.setVisibility(View.GONE);
        }


        if (isMp3Cutter) {
            itemHolder.popupMenu.setVisibility(View.INVISIBLE);
        } else {
            itemHolder.popupMenu.setVisibility(View.VISIBLE);
        }


        itemHolder.title.setText(localItem.title);
        itemHolder.artist.setText(localItem.artistName);

        MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(localItem.albumId).toString(),
                itemHolder.albumArt, MuzikoApp.OPTIONS/*,new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        ImageView iv = (ImageView) view;
                        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        iv.setAdjustViewBounds(false);

                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                    }
                }*/);
        itemHolder.albumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (MusicPlayer.getCurrentAudioId() == localItem.id && !isPlaylist) {
            itemHolder.title.setTextColor(Config.accentColor(mContext, ateKey));
            if (MusicPlayer.isPlaying()) {
                itemHolder.visualizer.setColor(Config.accentColor(mContext, ateKey));
                itemHolder.visualizer.setVisibility(View.VISIBLE);
            } else {
                itemHolder.visualizer.setVisibility(View.GONE);
            }
        } else {
            itemHolder.visualizer.setVisibility(View.GONE);
            if (isPlaylist) {
                itemHolder.title.setTextColor(Color.WHITE);
            } else {
                itemHolder.title.setTextColor(Config.textColorPrimary(mContext, ateKey));
            }
        }


        if (animate && isPlaylist) {
            if (MuzikoUtils.isLollipop())
                setAnimation(itemHolder.itemView, i);
            else {
                if (i > 10)
                    setAnimation(itemHolder.itemView, i);
            }
        }


        setOnPopupMenuListener(itemHolder, i);

    }

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_song_remove_playlist:
                                //MuzikoUtils.removeFromPlaylist(mContext, arraylist.get(position).id, playlistId);
                                PlaylistLoader.removeSong(mContext.getIntent().getExtras().getString(Constants.PLAYLIST_NAME), arraylist.get(position), mContext);
                                removeSongAt(position);
                                notifyItemRemoved(position);
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
                            case R.id.popup_song_play:
                                ArrayList<Song> idsNew = new ArrayList<>();
                                idsNew.add(arraylist.get(position));
                                MusicPlayer.playAll(idsNew, 0, false);
                                break;

                            case R.id.popup_song_play_next:
                                ArrayList<Song> ids = new ArrayList<>();
                                ids.add(arraylist.get(position));
                                MusicPlayer.playNext(ids);
                                break;
                            case R.id.popup_song_goto_album:
                                NavigationUtils.goToAlbum(mContext, arraylist.get(position).albumName);
                                break;
                            case R.id.popup_song_goto_artist:
                                NavigationUtils.goToArtist(mContext, arraylist.get(position).artistId);
                                break;
                            case R.id.popup_song_addto_queue:
                                ArrayList<Song> id = new ArrayList<>();
                                id.add(arraylist.get(position));
                                MusicPlayer.addToQueue(id);
                                break;
                            case R.id.popup_song_addto_playlist:
                                AddPlaylistDialog.newInstance(arraylist.get(position)).show(mContext.getSupportFragmentManager(), "ADD_PLAYLIST");
                                break;
                            case R.id.preview_song:
                                PreviewSong previewSong = new PreviewSong();
                                previewSong.open(mContext, arraylist.get(position));
                                break;
                            case R.id.start_time:
                                PlayFrom play = new PlayFrom();
                                play.open(mContext, arraylist.get(position));
                                break;
                            case R.id.popup_cut:
                                MuzikoApp.getInstance().cutSong(arraylist.get(position));
                                break;
                            case R.id.send_au:
                                ShareRingtone shareRingtone = new ShareRingtone();
                                shareRingtone.open(mContext, arraylist.get(position));
                                break;
                            case R.id.popup_song_share:
                                MuzikoUtils.shareTrack(mContext, arraylist.get(position).id);
                                break;
                            case R.id.remove_to_fav:
                                MuzikoApp.removeFromFavorite(selectedSong);
                                arraylist=MuzikoApp.readFavoriteList();
                                notifyDataSetChanged();
                                //reloadAdapter();
                                break;
                            case R.id.tag_edit:
                                ArrayList<Song> song = new ArrayList<>();
                                song.add(arraylist.get(position));
                                NavigationUtils.multiTagEdit(mContext, song);
                                break;
                            case R.id.popup_song_delete:
                                ArrayList<Song> deleteIds = new ArrayList<>();
                                deleteIds.add(arraylist.get(position));
                                MuzikoUtils.showDeleteDialog(mContext, arraylist.get(position).title, deleteIds);
                                break;
                            case R.id.popup_song_details:

                                MuzikoApp.details(mContext, arraylist.get(position));
                                break;

                            case R.id.popup_song_upload:

                                SongUploader.UploadSong(mContext, arraylist.get(position), "Uploading..", 1);
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
                                                .content("Muziko requires the write settings permission to set your default ringtone. You need to enable it on the settings screen.")
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
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_song);
                menu.show();
                if (isPlaylist)
                    menu.getMenu().findItem(R.id.popup_song_remove_playlist).setVisible(true);
                if (isFavSong) {
                    menu.getMenu().findItem(R.id.add_to_fav).setVisible(false);
                    menu.getMenu().findItem(R.id.remove_to_fav).setVisible(true);
                }else{



                }
            }
        });
    }



    public long[] getSongIds() {
        long[] ret = new long[getItemCount()];
        for (int i = 0; i < getItemCount(); i++) {
            ret[i] = arraylist.get(i).id;
        }

        return ret;
    }


    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.abc_slide_in_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void updateDataSet(List<Song> arraylist) {
        this.arraylist = arraylist;
        this.songIDs = getSongIds();
    }

    public Song getSongAt(int i) {
        return arraylist.get(i);
    }

    public void addSongTo(int i, Song song) {
        arraylist.add(i, song);
    }

    @Override
    public void removeSongAt(int i) {
        arraylist.remove(i);
        updateDataSet(arraylist);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (arraylist == null || arraylist.size() == 0)
            return "";
        Character ch = arraylist.get(position).title.charAt(0);
        if (Character.isDigit(ch)) {
            return "#";
        } else
            return Character.toString(ch);
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


    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, artist;
        protected ImageView albumArt, popupMenu;
        private VuMeterView visualizer;
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
            this.viewOverlay = (View) view.findViewById(R.id.viewOverlay);
            this.title = (TextView) view.findViewById(R.id.song_title);
            this.artist = (TextView) view.findViewById(R.id.song_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            visualizer = (VuMeterView) view.findViewById(R.id.visualizer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (isMp3Cutter) {
                MuzikoApp.getInstance().cutSong(arraylist.get(getAdapterPosition()));
                return;
            }
            if (isMultiSelect) {
                toggleSelection(arraylist.get(getAdapterPosition()));
                return;
            }

            try {
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        playAll(mContext, (ArrayList<Song>) getData(), getAdapterPosition(), 0,
                                MuzikoUtils.IdType.Artist, false,
                                arraylist.get(getAdapterPosition()), false);
                        notifyItemChanged(currentlyPlayingPosition);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

}


