package com.whitebird.music.adapters;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
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
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.dialogs.PlayRingTone;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.Utils;
import com.whitebird.music.widgets.FastScroller.views.FastScrollRecyclerView;
import com.whitebird.music.widgets.VuMeterView;

import java.util.ArrayList;
import java.util.List;

public class RingToneAdapter extends BaseSongAdapter<RingToneAdapter.ItemHolder> implements FastScrollRecyclerView.SectionedAdapter {

    public int currentlyPlayingPosition;
    ActionMode.Callback callback;

    private List<Song> arraylist;
    private AppCompatActivity mContext;
    private int lastPosition = -1;
    private String ateKey;
    private long playlistId;
    private boolean isMultiSelect = false;
    private ArrayList<Song> selectedSong = new ArrayList<>();
    private ActionMode actionMode;

    public RingToneAdapter(AppCompatActivity context, List<Song> arraylist) {
        this.arraylist = arraylist;
        this.mContext = context;

        this.ateKey = Helpers.getATEKey(context);


    }

    public List<Song> getData() {
        return arraylist;
    }

    public void setData(List<Song> arraylist) {
        this.arraylist = arraylist;

        notifyDataSetChanged();
    }

    @Override
    public RingToneAdapter.ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song, null);
        RingToneAdapter.ItemHolder ml = new RingToneAdapter.ItemHolder(v);
        return ml;

    }

    @Override
    public void onBindViewHolder(RingToneAdapter.ItemHolder itemHolder, int i) {
        Song localItem = arraylist.get(i);

        itemHolder.visualizer.setVisibility(View.GONE);
        itemHolder.viewOverlay.setVisibility(View.GONE);

        setOnPopupMenuListener(itemHolder, i);
        itemHolder.title.setText(localItem.title);

        String artistText =Utils.getLongDuration(localItem.duration);
        itemHolder.artist.setText(artistText);
    }


    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    private void setOnPopupMenuListener(RingToneAdapter.ItemHolder itemHolder, final int position) {

        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {

                            case R.id.popup_song_play:
                                PlayRingTone previewSong = new PlayRingTone();
                                previewSong.open(mContext, arraylist.get(position));
                                break;
                            case R.id.popup_move_ignore:
                                ArrayList<Song> ignore = new ArrayList();
                                ignore.add(arraylist.get(position));
                                MuzikoApp.moveToIgnore(ignore, mContext);
                                break;

                            case R.id.popup_song_delete:
                                ArrayList<Song> deleteIds = new ArrayList<>();
                                deleteIds.add(arraylist.get(position));
                                MuzikoUtils.showDeleteDialog(mContext, arraylist.get(position).title, deleteIds );
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
                                        ringtone(arraylist.get(position));
//                                        afterSavingRingtone(
//                                                mContext, arraylist.get(position).title, arraylist.get(position).data, new File(arraylist.get(position).data), (int) (arraylist.get(position).duration * 1000));
                                    }
                                } else {
                                    ringtone(arraylist.get(position));
//                                    afterSavingRingtone(
//                                            mContext, arraylist.get(position).title, arraylist.get(position).data, new File(arraylist.get(position).data), (int) (arraylist.get(position).duration * 1000));
                                }

                                break;
                        }
                        return false;
                    }
                });

                menu.inflate(R.menu.pop_up_ringtone);
                menu.show();

            }
        });
    }




    public void ringtone(Song ringtone) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, ringtone.data);
        values.put(MediaStore.MediaColumns.TITLE, ringtone.title);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.ARTIST, ringtone.artistName);
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.DURATION, ringtone.duration);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);
        //TODO values.put(MediaStore.Audio.Media.IS_MUSIC, false);
        //TODO values.put(MediaStore.MediaColumns.SIZE, 1024);

        try {
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(ringtone.data);
            mContext.getContentResolver()
                    .delete(uri, MediaStore.MediaColumns.DATA + "=\"" + ringtone.data + "\"", null);
            Uri newUri = mContext.getContentResolver().insert(uri, values);

            RingtoneManager.setActualDefaultRingtoneUri(
                    mContext, RingtoneManager.TYPE_RINGTONE, newUri);

            MuzikoApp.toast(mContext, "Ringtone set");

        } catch (Throwable t) {
            MuzikoApp.toast(mContext, "Unable to set ringtone!");
        }
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
        ;
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
                            ((MainActivity) mContext).startSupportActionMode(callback);
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
            PlayRingTone previewSong = new PlayRingTone();
            previewSong.open(mContext, arraylist.get(getAdapterPosition()));

        }


    }

}
