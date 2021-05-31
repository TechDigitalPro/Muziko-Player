package com.whitebird.music.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;


import androidx.recyclerview.widget.RecyclerView;

import com.whitebird.music.MusicPlayer;
import com.whitebird.music.activities.BaseActivity;

import com.whitebird.music.models.Song;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.MuzikoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by naman on 7/12/17.
 */

public class BaseSongAdapter<V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {

    @Override
    public V onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(V holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        public ItemHolder(View view) {
            super(view);
        }

    }

    public void playAll(final Activity context, final ArrayList<Song> list, int position,
                        final long sourceId, final MuzikoUtils.IdType sourceType,
                        final boolean forceShuffle, final Song currentSong, boolean navigateNowPlaying) {

        if (context instanceof BaseActivity) {
           /* CastSession castSession = ((BaseActivity) context).getCastSession();
            if (castSession != null) {
                navigateNowPlaying = false;
                MuzikoCastHelper.startCasting(castSession, currentSong);
            } else*/
                {
                MusicPlayer.playAll(  list, position,   false);
            }
        } else {
            MusicPlayer.playAll(  list, position,   false);
        }

        if (navigateNowPlaying) {
            NavigationUtils.navigateToNowplaying(context, true);
        }


    }
    public void removeSongAt(int i){}
    public void updateDataSet(List<Song> arraylist) {}

}
