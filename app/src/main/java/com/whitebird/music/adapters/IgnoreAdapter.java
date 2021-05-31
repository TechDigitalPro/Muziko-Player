package com.whitebird.music.adapters;

import android.os.Handler;
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
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.Config;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.widgets.FastScroller.views.FastScrollRecyclerView;
import com.whitebird.music.widgets.VuMeterView;

import java.util.ArrayList;
import java.util.List;

public class IgnoreAdapter extends BaseSongAdapter<IgnoreAdapter.ItemHolder> implements FastScrollRecyclerView.SectionedAdapter {

    public int currentlyPlayingPosition;
    ActionMode.Callback callback;
    private List<Song> arraylist;
    private AppCompatActivity mContext;
    private int lastPosition = -1;
    private String ateKey;
    private boolean isMultiSelect = false;
    private ArrayList<Song> selectedSong = new ArrayList<>();
    private ActionMode actionMode;



    public IgnoreAdapter(AppCompatActivity context, List<Song> arraylist , ActionMode.Callback callback) {
        this.arraylist = arraylist;
        this.mContext = context;

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

    public List<Song> getData() {
        return arraylist;
    }

    public void setData(List<Song> arraylist) {
        this.arraylist = arraylist;

        notifyDataSetChanged();
    }

    @Override
    public IgnoreAdapter.ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song, null);
        IgnoreAdapter.ItemHolder ml = new IgnoreAdapter.ItemHolder(v);
        return ml;

    }

    @Override
    public void onBindViewHolder(IgnoreAdapter.ItemHolder itemHolder, int i) {
        Song localItem = arraylist.get(i);
        if (selectedSong.contains(localItem)) {
            itemHolder.viewOverlay.setVisibility(View.VISIBLE);
            itemHolder.viewOverlay.setAlpha(.5f);
            itemHolder.viewOverlay.setBackgroundColor(Config.primaryColor(mContext, Helpers.getATEKey(mContext)));
        } else {
            itemHolder.viewOverlay.setVisibility(View.GONE);
        }


       {
            itemHolder.popupMenu.setVisibility(View.VISIBLE);
        }


        itemHolder.title.setText(localItem.title);
        itemHolder.artist.setText(localItem.artistName);

        MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(localItem.albumId).toString(),
                itemHolder.albumArt, MuzikoApp.OPTIONS);
        itemHolder.albumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
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

            itemHolder.title.setTextColor(Config.textColorPrimary(mContext, ateKey));

        }


        setOnPopupMenuListener(itemHolder, i);

    }


    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    private void setOnPopupMenuListener(IgnoreAdapter.ItemHolder itemHolder, final int position) {

        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                //long[] deleteIds = {arraylist.get(position).id};
                                ArrayList<Song> data=new ArrayList<>();
                                data.add(arraylist.get(position));
                                MuzikoUtils.showDeleteDialog(mContext, arraylist.get(position).title, data);
                                break;
                            case R.id.restore_ignore:
                                ArrayList<Song> restore=new ArrayList<>();
                                restore.add(arraylist.get(position));

                                MuzikoApp.removeFromIgnore(restore, mContext);
                                break;


                            default:
                                return false;
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.context_ignore);
                menu.show();

            }
        });
    }

    public ArrayList<Song> getSongIds() {
        ArrayList<Song> ret = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            ret.add(arraylist.get(i));
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

            if (isMultiSelect) {
                toggleSelection(arraylist.get(getAdapterPosition()));
                return;
            }

            try {
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {


                        MusicPlayer.playAll( getSongIds(), getAdapterPosition() , false);
                        notifyItemChanged(currentlyPlayingPosition);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

}


