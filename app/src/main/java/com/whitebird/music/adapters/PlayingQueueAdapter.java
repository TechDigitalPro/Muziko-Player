

package com.whitebird.music.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
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
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.dialogs.PlayFrom;
import com.whitebird.music.dialogs.PreviewSong;
import com.whitebird.music.dialogs.ShareRingtone;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.widgets.VuMeterView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

public class PlayingQueueAdapter extends RecyclerView.Adapter<PlayingQueueAdapter.ItemHolder> {
    private static final String TAG = "PlayingQueueAdapter";

    public int currentlyPlayingPosition;
    private List<Song> arraylist;
    private AppCompatActivity mContext;
    private String ateKey;
    private ActionMode.Callback callback;
    private boolean isMultiSelect = false;
    private ActionMode actionMode;
    private ArrayList<Song> selectedSong = new ArrayList<>();
    ArrayList<Integer> selectedPosition=new ArrayList<>();

    public PlayingQueueAdapter(AppCompatActivity context, List<Song> arraylist,ActionMode.Callback callback) {
        this.callback=callback;
        this.arraylist = arraylist;
        this.mContext = context;
        this.currentlyPlayingPosition = MusicPlayer.getQueuePosition();
        this.ateKey = Helpers.getATEKey(context);
    }
    public void setMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
    }

    public void setActionMode(ActionMode actionMode) {
        this.actionMode = actionMode;
    }


    public void toggleSelection(Song song,int position) {
        if (selectedSong.contains(song)) {
            selectedSong.remove(song);
        } else {
            selectedSong.add(song);
        }
        if (selectedPosition.contains(position)) {
            selectedPosition.remove(song);
        } else {
            selectedPosition.add(position);
        }
        if (actionMode != null) {
            if (selectedSong.size() == 0)
                actionMode.setTitle("");
            else
                actionMode.setTitle(String.format("%d Song%s", selectedSong.size(), selectedSong.size() != 1 ? "s" : ""));
        }

        notifyItemChanged(arraylist.indexOf(song));
    }
    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_playing_queue, null);
        ItemHolder ml = new ItemHolder(v);
        return ml;
    }


    public ArrayList<Song> getSelectedItems() {
        return selectedSong;
    }
    public ArrayList<Integer> getSelectedPosition() {
        return selectedPosition;
    }

    public void clearSelection() {
        selectedSong.clear();
        selectedPosition.clear();
        notifyDataSetChanged();
    }    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {
        Song localItem = arraylist.get(i);


        if (selectedSong.contains(localItem)) {
            itemHolder.viewOverlay.setVisibility(View.VISIBLE);
            itemHolder.viewOverlay.setAlpha(.5f);
            itemHolder.viewOverlay.setBackgroundColor(Config.primaryColor(mContext, Helpers.getATEKey(mContext)));
        } else {
            itemHolder.viewOverlay.setVisibility(View.GONE);
        }

        itemHolder.title.setText(localItem.title);
        itemHolder.artist.setText(localItem.artistName);

        if (MusicPlayer.getQueuePosition() == i) {
            itemHolder.title.setTextColor(Config.accentColor(mContext, ateKey));
            if (MusicPlayer.isPlaying()) {
                itemHolder.visualizer.setColor(Config.accentColor(mContext, ateKey));
                itemHolder.visualizer.setVisibility(View.VISIBLE);
            } else {
                itemHolder.visualizer.setVisibility(View.GONE);
            }
        } else {
            itemHolder.title.setTextColor(Config.textColorPrimary(mContext, ateKey));
            itemHolder.visualizer.setVisibility(View.GONE);
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
        setOnPopupMenuListener(itemHolder, i);
    }
    private void showPlayDialog(final ArrayList<Song> songs) {
        new MaterialDialog.Builder(mContext)
                .title("Play Song")
                .content("are u sure u want to play the song?\n Queue will be updated " )
                .positiveText("Play")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MusicPlayer.playAll(songs, 0,  false);
                        mContext.sendBroadcast(new Intent(MuzikoApp.INTENT_RELOAD_QUEUE));
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
    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                           /* case R.id.popup_song_remove_playlist:
                                MuzikoUtils.removeFromPlaylist(mContext, arraylist.get(position).id, playlistId);

                                PlaylistLoader.removeSong(mContext.getIntent().getExtras().getString(Constants.PLAYLIST_NAME),arraylist.get(position),mContext);
                                removeSongAt(position);
                                notifyItemRemoved(position);
                                break;*/

                            case R.id.popup_song_play:
                                //long[] idsNew = new long[1];
//                                ArrayList
                                ArrayList<Song> songs=new ArrayList<>();
                                songs.add(arraylist.get(position));
                                showPlayDialog(songs);
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
                                ArrayList<Song> songs1=new ArrayList<>();
                                songs1.add(arraylist.get(position));
                                MusicPlayer.playNext(  songs1 );
                                break;
                            case R.id.popup_song_goto_album:
                                NavigationUtils.goToAlbum(mContext, arraylist.get(position).albumName);
                                break;
                            case R.id.popup_song_goto_artist:
                                NavigationUtils.goToArtist(mContext, arraylist.get(position).artistId);
                                break;
                            case R.id.popup_song_addto_queue:
                                ArrayList<Song> songsNew=new ArrayList<>();
                                songsNew.add(arraylist.get(position));
                                MusicPlayer.addToQueue(songsNew);
                                break;
                            case R.id.popup_song_addto_playlist:
                                AddPlaylistDialog.newInstance(DataHelper.getSong(arraylist.get(position).id)).show(mContext.getSupportFragmentManager(), "ADD_PLAYLIST");
                                break;
                            case R.id.preview_song:
                                PreviewSong previewSong = new PreviewSong();
                                previewSong.open(mContext, arraylist.get(position));
                                break;
                            case R.id.start_time:
                                PlayFrom play = new PlayFrom();
                                play.open(mContext, DataHelper.getSong(arraylist.get(position).id));
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
                            case R.id.tag_edit:
                                ArrayList<Song> song = new ArrayList<>();
                                song.add(DataHelper.getSong(arraylist.get(position).id));
                                NavigationUtils.multiTagEdit(mContext, song);
                                break;

                            case R.id.popup_song_remove_queue:
                                Log.v(TAG, "Removing " + position);
                                MusicPlayer.removeTrackAtPosition(  position);
                                removeSongAt(position);
                                notifyItemRemoved(position);
                                break;
                            case R.id.popup_song_details:
                                MuzikoApp.details(mContext, arraylist.get(position));
                                break;
                   /*         case R.id.popup_song_delete:
//                                long[] deleteIds = {arraylist.get(position).id};
//                                MuzikoUtils.showDeleteDialog(mContext, arraylist.get(position).title, deleteIds, PlayingQueueAdapter.this, position);
                                break;*/
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_playing_queue);
                menu.show();

            }
        });
    }


    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    public long[] getSongIds() {
        long[] ret = new long[getItemCount()];
        for (int i = 0; i < getItemCount(); i++) {
            ret[i] = arraylist.get(i).id;
        }

        return ret;
    }

    public Song getSongAt(int i) {
        return arraylist.get(i);
    }

    public void addSongTo(int i, Song song) {
        arraylist.add(i, song);
    }

    public void removeSongAt(int i) {
        arraylist.remove(i);
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, artist;
        protected ImageView albumArt, reorder, menu;
        private VuMeterView visualizer;
        private View viewOverlay;

        public ItemHolder(View view) {
            super(view);
            if (callback != null)
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!isMultiSelect) {
                            ((MainActivity) mContext).startSupportActionMode(callback);
                            isMultiSelect = true;
                            toggleSelection(arraylist.get(getAdapterPosition()),getAdapterPosition());
                        }

                        return true;
                    }
                });
            this.title = (TextView) view.findViewById(R.id.song_title);
            this.artist = (TextView) view.findViewById(R.id.song_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.menu = (ImageView) view.findViewById(R.id.popup_menu);
            this.reorder = (ImageView) view.findViewById(R.id.reorder);
            visualizer = (VuMeterView) view.findViewById(R.id.visualizer);
            viewOverlay =  view.findViewById(R.id.viewOverlay);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (isMultiSelect) {
                toggleSelection(arraylist.get(getAdapterPosition()),getAdapterPosition());
                return;
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.setQueuePosition(getAdapterPosition());
                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemChanged(currentlyPlayingPosition);
                            notifyItemChanged(getAdapterPosition());
                        }
                    }, 50);
                }
            }, 100);

        }

    }

}




