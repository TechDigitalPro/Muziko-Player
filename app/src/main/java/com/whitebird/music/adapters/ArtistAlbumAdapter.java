

package com.whitebird.music.adapters;

import android.app.Activity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.MuzikoUtils;

import java.util.List;

public class ArtistAlbumAdapter extends RecyclerView.Adapter<ArtistAlbumAdapter.ItemHolder> {

    private List<Song> arraylist;
    private Activity mContext;

    public ArtistAlbumAdapter(Activity context, List<Song> arraylist) {
        this.arraylist = arraylist;
        this.mContext = context;

    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist_album, null);
        ItemHolder ml = new ItemHolder(v);
        return ml;
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {

        Song localItem = arraylist.get(i);

        itemHolder.title.setText(localItem.albumName);
        String songCount = MuzikoUtils.makeLabel(mContext, R.plurals.Nsongs, localItem.songsCount);
        itemHolder.details.setText(songCount);

//        ImageUtils.loadAlbumArtIntoView(localItem.albumId, itemHolder.albumArt);
        MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(localItem.albumId).toString(), itemHolder.albumArt,
                MuzikoApp.OPTIONS/*,new ImageLoadingListener() {
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
        if (MuzikoUtils.isLollipop())
            itemHolder.albumArt.setTransitionName("transition_album_art" + i);

    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }


    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, details;
        protected ImageView albumArt;
        protected CardView rootView;

        public ItemHolder(View view) {
            super(view);
            this.rootView = (CardView) view.findViewById(R.id.root_view);
            this.title = (TextView) view.findViewById(R.id.album_title);
            this.details = (TextView) view.findViewById(R.id.album_details);
            this.albumArt = (ImageView) view.findViewById(R.id.album_art);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavigationUtils.navigateToAlbum(mContext, arraylist.get(getAdapterPosition()).albumName,
                    new Pair<View, String>(albumArt, "transition_album_art" + getAdapterPosition()));
        }

    }
}




