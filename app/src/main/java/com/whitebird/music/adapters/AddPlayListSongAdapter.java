package com.whitebird.music.adapters;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.Config;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.widgets.FastScroller.views.FastScrollRecyclerView;
import com.whitebird.music.widgets.VuMeterView;

import java.util.ArrayList;
import java.util.List;

public class AddPlayListSongAdapter extends BaseSongAdapter<AddPlayListSongAdapter.ItemHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private List<Song> arraylist;
    private AppCompatActivity mContext;



    private String ateKey;

    private ArrayList<Song> selectedSong = new ArrayList<>();


    public AddPlayListSongAdapter(AppCompatActivity context, List<Song> arraylist) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.ateKey = Helpers.getATEKey(context);

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
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_add_play_song, null);
        ItemHolder ml = new ItemHolder(v);
        return ml;

    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {
        Song localItem = arraylist.get(i);
        if (selectedSong.contains(localItem)) {
            itemHolder.img_select.setImageResource(R.drawable.ic_check_blue);
            itemHolder.img_select.setColorFilter(new PorterDuffColorFilter(Config.accentColor(mContext, Helpers.getATEKey(mContext)), PorterDuff.Mode.SRC_IN));

        } else {
            itemHolder.img_select.setImageResource(R.drawable.ic_check_white);
            itemHolder.img_select.setColorFilter(null);
        }

        itemHolder.title.setText(localItem.title);
        itemHolder.artist.setText(localItem.artistName);

        MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(localItem.albumId).toString(),
                itemHolder.albumArt, MuzikoApp.OPTIONS);
        itemHolder.albumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);

      /*  if (MusicPlayer.getCurrentAudioId() == localItem.id) {
            itemHolder.title.setTextColor(Config.accentColor(mContext, ateKey));
            if (MusicPlayer.isPlaying()) {
                itemHolder.visualizer.setColor(Config.accentColor(mContext, ateKey));
                itemHolder.visualizer.setVisibility(View.VISIBLE);
            } else {
                itemHolder.visualizer.setVisibility(View.GONE);
            }
        }*/


    }


    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }


    @Override
    public void updateDataSet(List<Song> arraylist) {
        this.arraylist = arraylist;

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


        notifyItemChanged(arraylist.indexOf(song));
    }


    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, artist;
        protected ImageView albumArt, img_select;
        private VuMeterView visualizer;
        private View viewOverlay;

        public ItemHolder(View view) {
            super(view);
            this.viewOverlay = (View) view.findViewById(R.id.viewOverlay);
            this.title = (TextView) view.findViewById(R.id.song_title);
            this.artist = (TextView) view.findViewById(R.id.song_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.img_select = (ImageView) view.findViewById(R.id.img_select);
            visualizer = (VuMeterView) view.findViewById(R.id.visualizer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            toggleSelection(arraylist.get(getAdapterPosition()));

        }


    }


}


