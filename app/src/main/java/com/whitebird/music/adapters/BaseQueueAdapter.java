

package com.whitebird.music.adapters;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.whitebird.music.dialogs.PlayFrom;
import com.whitebird.music.dialogs.PreviewSong;
import com.whitebird.music.dialogs.SetRingtone;
import com.whitebird.music.dialogs.ShareRingtone;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.widgets.VuMeterView;

import java.util.ArrayList;
import java.util.List;

public class BaseQueueAdapter extends RecyclerView.Adapter<BaseQueueAdapter.ItemHolder> {

    public static int currentlyPlayingPosition;
    private List<Song> arraylist;
    private AppCompatActivity mContext;
    private String ateKey;

    public BaseQueueAdapter(AppCompatActivity context, List<Song> arraylist) {
        this.arraylist = arraylist;
        this.mContext = context;
        currentlyPlayingPosition = MusicPlayer.getQueuePosition();
        this.ateKey = Helpers.getATEKey(context);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song_timber1, null);
        ItemHolder ml = new ItemHolder(v);
        return ml;
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {
        Song localItem = arraylist.get(i);

        itemHolder.title.setText(localItem.title);
        itemHolder.artist.setText(localItem.artistName);

        if (MusicPlayer.getCurrentAudioId() == localItem.id) {
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
                itemHolder.albumArt, MuzikoApp.OPTIONS/*,new ImageLoadingListener() {
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
                }*/);
        itemHolder.albumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        setOnPopupMenuListener(itemHolder, i);
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
                            case R.id.popup_song_play:
                                MusicPlayer.playAll( getSongList(), position, false);
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
                               // long[] ids = new long[1];
                                //ids[0] = arraylist.get(position).id;
                                MusicPlayer.playNext(arraylist.get(position));
                                break;
                            case R.id.popup_song_goto_album:
                                NavigationUtils.goToAlbum(mContext, arraylist.get(position).albumName);
                                break;
                            case R.id.popup_song_goto_artist:
                                NavigationUtils.goToArtist(mContext, arraylist.get(position).artistId);
                                break;
                            case R.id.popup_song_addto_queue:
                             //   long[] id = new long[1];
                             //   id[0] = arraylist.get(position).id;
                                MusicPlayer.addToQueue( arraylist.get(position));
                                break;
                            case R.id.popup_song_addto_playlist:
                                AddPlaylistDialog.newInstance(arraylist.get(position)).show(mContext.getSupportFragmentManager(), "ADD_PLAYLIST");
                                break;
                            case R.id.preview_song:
                                PreviewSong previewSong=new PreviewSong();
                                previewSong.open(mContext,arraylist.get(position));
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
                            case R.id.tag_edit:
                                ArrayList<Song> song=new ArrayList<>();
                                song.add(arraylist.get(position));
                                NavigationUtils.multiTagEdit(mContext, song);
                                break;
                            case R.id.popup_song_delete:
                                long[] deleteIds = {arraylist.get(position).id};
                                ArrayList<Song> songs=new ArrayList<>();
                                songs.add(arraylist.get(position));

                                MuzikoUtils.showDeleteDialog(mContext,arraylist.get(position).title, songs, BaseQueueAdapter.this, position);
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
                                                            intent.setData(Uri.parse("package:com.whitebird.music"  ));
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

    public long[] getSongIds() {
        long[] ret = new long[getItemCount()];
        for (int i = 0; i < getItemCount(); i++) {
            ret[i] = arraylist.get(i).id;
        }

        return ret;
    }

    public ArrayList<Song> getSongList() {


        return (ArrayList<Song>) arraylist;
    }

    public void removeSongAt(int i){
        arraylist.remove(i);
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, artist;
        protected ImageView albumArt, popupMenu;
        private VuMeterView visualizer;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.song_title);
            this.artist = (TextView) view.findViewById(R.id.song_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            visualizer = (VuMeterView) view.findViewById(R.id.visualizer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
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



