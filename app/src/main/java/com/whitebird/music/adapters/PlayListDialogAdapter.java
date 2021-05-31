package com.whitebird.music.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.models.Playlist;
import com.whitebird.music.utils.MuzikoUtils;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

public class PlayListDialogAdapter extends RecyclerView.Adapter<PlayListDialogAdapter.ItemHolder> {


    int[] foregroundColors = {R.color.pink_transparent, R.color.green_transparent,
            R.color.blue_transparent, R.color.red_transparent, R.color.purple_transparent};

    private List<Playlist> arraylist=new ArrayList<>();

    private Activity mContext;

    private long firstAlbumID = -1;

    public PlayListDialogAdapter(Activity context, List<Playlist> arraylist) {
        this.arraylist .addAll(arraylist);
        this.mContext = context;
    }
    public Playlist getItemAt(int i) {
        return arraylist.get(i);
    }
    public void addItemTo(int i, Playlist song) {
        arraylist.add(i, song);
    }

    public void removeItemAt(int i) {
        arraylist.remove(i);
    }

    public void clearSelection() {

        notifyDataSetChanged();
    }

    public List<Playlist> getItem(){
        return arraylist;
    }


    @Override
    public PlayListDialogAdapter.ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_playing_queue, null);
            PlayListDialogAdapter.ItemHolder ml = new PlayListDialogAdapter.ItemHolder(v);
            return ml;

    }

    @Override
    public void onBindViewHolder(final PlayListDialogAdapter.ItemHolder itemHolder, int i) {


        final Playlist localItem = arraylist.get(i);

        itemHolder.title.setText(localItem.getName());

//        String s = getAlbumArtUri(i, localItem.id);
        long s = 0;
        if (localItem.getSongList().size() > 0)
            s = localItem.getSongList().get(0).albumId;


        itemHolder.albumArt.setTag(firstAlbumID);

        MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(s).toString(), itemHolder.albumArt,
                MuzikoApp.OPTIONS, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }//+ " - " + MuzikoUtils.makeShortTimeString(mContext,totalRuntime)
                });
        itemHolder.artist.setText(String.valueOf(arraylist.get(i).getSongList().size()) + " " + mContext.getString(R.string.songs));
        itemHolder.albumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (MuzikoUtils.isLollipop())
            itemHolder.albumArt.setTransitionName("transition_album_art" + i);




        {
            itemHolder.popupMenu.setVisibility(View.GONE);
        }

      //  setOnPopupMenuListener(itemHolder, i);
    }


    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    public void updateDataSet(List<Playlist> arraylist) {
        this.arraylist.clear();
        this.arraylist.addAll(arraylist);
        notifyDataSetChanged();
    }



    private void setOnPopupMenuListener(PlayListDialogAdapter.ItemHolder itemHolder, final int position) {

        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {

                            case R.id.popup_playlist_edit:
                                break;
                            case R.id.popup_playlist_delete:
                                break;
                            case R.id.popup_playlist_share:
                                break;
                            case R.id.popup_playlist_clone:
                                break;
                        }

                        return false;
                    }
                });
                menu.inflate(R.menu.popup_playlist);
                menu.show();

            }
        });
    }

    public class ItemHolder extends RecyclerView.ViewHolder  {
        protected TextView title, artist;
        protected ImageView albumArt, popupMenu;


        public ItemHolder(View view) {
            super(view);

            this.title = (TextView) view.findViewById(R.id.song_title);
            this.artist = (TextView) view.findViewById(R.id.song_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);


        }



    }

}


