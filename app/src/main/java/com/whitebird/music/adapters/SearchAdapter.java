

package com.whitebird.music.adapters;

import android.app.Activity;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.dialogs.PreviewSong;
import com.whitebird.music.dialogs.ShareRingtone;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchAdapter extends BaseSongAdapter<SearchAdapter.ItemHolder> {

    private Activity mContext;
    private List searchResults = Collections.emptyList();

    public SearchAdapter(Activity context) {
        this.mContext = context;

    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case 0:
                View v0 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song, null);
                ItemHolder ml0 = new ItemHolder(v0);
                return ml0;
            case 1:
                View v1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_album, null);
                ItemHolder ml1 = new ItemHolder(v1);
                return ml1;
            case 2:
                View v2 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist, null);
                ItemHolder ml2 = new ItemHolder(v2);
                return ml2;
            case 10:
                View v10 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_section_header, null);
                ItemHolder ml10 = new ItemHolder(v10);
                return ml10;
            default:
                View v3 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song, null);
                ItemHolder ml3 = new ItemHolder(v3);
                return ml3;
        }
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int i) {
        switch (getItemViewType(i)) {
            case 0:
                Song song = (Song) searchResults.get(i);
                itemHolder.title.setText(song.title);
                itemHolder.songartist.setText(song.albumName);
                MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(song.albumId).toString(), itemHolder.albumArt,
                        MuzikoApp.OPTIONS );
                setOnPopupMenuListener(itemHolder, i);
                itemHolder.albumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case 1:
                Song album = (Song) searchResults.get(i);
                itemHolder.artisttitle.setText(album.albumName);
                itemHolder.albumsongcount.setText(album.artistName);
                MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(album.albumId).toString(), itemHolder.artistImage,
                        MuzikoApp.OPTIONS );
                setOnPopupMenuListenerAlbum(itemHolder, i);
                itemHolder.artistImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case 2:
                Song artist = (Song) searchResults.get(i);
                itemHolder.artisttitle.setText(artist.artistName);

                String songCount = MuzikoUtils.makeLabel(mContext, R.plurals.Nsongs, artist.songsCount);
                itemHolder.albumsongcount.setText(MuzikoUtils.makeString(mContext, songCount));
                MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(artist.albumId).toString(), itemHolder.artistImage,
                        MuzikoApp.OPTIONS );
                itemHolder.artistImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                setOnPopupMenuListenerArtist(itemHolder, i);
                break;
            case 10:
                itemHolder.sectionHeader.setText((String) searchResults.get(i));
            case 3:
                break;
        }
    }

    @Override
    public void onViewRecycled(ItemHolder itemHolder) {

    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }



    @Override
    public int getItemViewType(int position) {

        if (searchResults.get(position) instanceof String)
            return 10;
        Song item = (Song) searchResults.get(position);

        if (searchResults.get(position) instanceof Song)
            return item.type;
       /* if (searchResults.get(position) instanceof Album)
            return 1;
        if (searchResults.get(position) instanceof Song)*/


        return 3;
    }

    public void updateSearchResults(List searchResults) {
        this.searchResults = searchResults;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, songartist,  artisttitle,  albumsongcount, sectionHeader;
        protected ImageView  albumArt, artistImage, menu;

        public ItemHolder(View view) {
            super(view);

            this.title = (TextView) view.findViewById(R.id.song_title);
            this.songartist = (TextView) view.findViewById(R.id.song_artist);
            this.albumsongcount = (TextView) view.findViewById(R.id.album_song_count);
            this.artisttitle = (TextView) view.findViewById(R.id.artist_name);



            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.artistImage = (ImageView) view.findViewById(R.id.artistImage);
            this.menu = (ImageView) view.findViewById(R.id.popup_menu);

            this.sectionHeader = (TextView) view.findViewById(R.id.section_header);


            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (getItemViewType()) {
                case 0:
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<Song> songs=new ArrayList<>();
                            songs.add(((Song) searchResults.get(getAdapterPosition())));
                         //   ret[0] = ((Song) searchResults.get(getAdapterPosition())).id;
                            playAll(mContext, songs, 0, -1, MuzikoUtils.IdType.NA,
                                    false, (Song) searchResults.get(getAdapterPosition()), false);
                        }
                    }, 100);

                    break;
                case 1:
                    NavigationUtils.goToAlbum(mContext, ((Song) searchResults.get(getAdapterPosition())).albumName);
                    break;
                case 2:
                    NavigationUtils.goToArtist(mContext, ((Song) searchResults.get(getAdapterPosition())).artistId);
                    break;
                case 3:
                    break;
                case 10:
                    break;
            }
        }

    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        ArrayList<Song> songs=new ArrayList<>();
                        songs.add((Song) searchResults.get(position)) ;
                        switch (item.getItemId()) {
                            case R.id.popup_song_play:
                                MusicPlayer.playAll(songs, 0,  false);
                                break;
                            case R.id.popup_song_play_next:
                                MusicPlayer.playNext(songs);
                                break;
                            case R.id.popup_song_goto_album:
                                NavigationUtils.goToAlbum(mContext, ((Song) searchResults.get(position)).albumName);
                                break;
                            case R.id.preview_song:
                                PreviewSong previewSong = new PreviewSong();
                                previewSong.open(mContext, ((Song) searchResults.get(position)));
                                break;
                            case R.id.popup_cut:
                                MuzikoApp.getInstance().cutSong(((Song) searchResults.get(position)));
                                break;
                            case R.id.send_au:
                                ShareRingtone shareRingtone = new ShareRingtone();
                                shareRingtone.open(mContext, ((Song) searchResults.get(position)));
                                break;
                            case R.id.popup_song_goto_artist:
                                NavigationUtils.goToArtist(mContext, ((Song) searchResults.get(position)).artistId);
                                break;
                            case R.id.popup_song_addto_queue:
                                MusicPlayer.addToQueue( songs);
                                break;
                            case R.id.popup_song_addto_playlist:
                                AddPlaylistDialog.newInstance(((Song) searchResults.get(position))).show(((AppCompatActivity) mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                                break;


                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_song);
                //Hide these because they aren't implemented
                menu.getMenu().findItem(R.id.start_time).setVisible(false);
                menu.getMenu().findItem(R.id.popup_set_ring).setVisible(false);
                menu.getMenu().findItem(R.id.popup_song_delete).setVisible(false);
                menu.getMenu().findItem(R.id.popup_song_share).setVisible(false);
                menu.getMenu().findItem(R.id.popup_song_details).setVisible(false);
                menu.getMenu().findItem(R.id.tag_edit).setVisible(false);
                menu.getMenu().findItem(R.id.popup_move_ignore).setVisible(false);
                menu.getMenu().findItem(R.id.add_to_fav).setVisible(false);
                menu.show();
            }
        });
    }


    private void setOnPopupMenuListenerAlbum(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        ArrayList<Song> songsAlbum= DataHelper.getSongsForAlbum(((Song) searchResults.get(position)).albumName);

                       /* long[] ids = new long[songsAlbum.size()];
                        for (int i=0;i<songsAlbum.size();i++)
                            ids[i] = songsAlbum.get(i).id;*/
                        switch (item.getItemId()) {

                            case R.id.popup_song_play:
                                MusicPlayer.playAll(songsAlbum,0,   false);
                                break;

                            case R.id.popup_song_play_next:
                                MusicPlayer.playNext(songsAlbum);
                                break;
                            case R.id.popup_song_addto_queue:
                                MusicPlayer.addToQueue(songsAlbum);
                                break;

                            case R.id.popup_song_addto_playlist:
                                AddPlaylistDialog.newInstance(songsAlbum).show(((FragmentActivity)mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                                break;

                            case R.id.popup_song_share:
                                MuzikoApp.getInstance().shareSongs(mContext, songsAlbum);
                                break;

                            case R.id.popup_song_delete:
                                MuzikoUtils.deleteMultipleSong(mContext, songsAlbum, "Songs");
                                break;

                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_album);
                menu.getMenu().findItem(R.id.popup_manage_art).setVisible(true);
                menu.getMenu().findItem(R.id.popup_move_ignore).setVisible(true);
                menu.getMenu().findItem(R.id.add_to_fav).setVisible(false);
                menu.show();

            }
        });
    }

    private void setOnPopupMenuListenerArtist(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        ArrayList<Song> songsArtist= DataHelper.getSongsForArtist(((Song) searchResults.get(position)).artistId);

                       /* long[] ids = new long[songsArtist.size()];
                        for (int i=0;i<songsArtist.size();i++)
                            ids[i] = songsArtist.get(i).id;*/
                        switch (item.getItemId()) {
                            case R.id.popup_song_play:
                                MusicPlayer.playAll(songsArtist, 0,  false);
                                break;
                            case R.id.popup_song_play_next:

                                MusicPlayer.playNext(songsArtist);
                                break;
                            case R.id.popup_song_addto_queue:
                                MusicPlayer.addToQueue(songsArtist);
                                break;



                            case R.id.popup_song_addto_playlist:
                                AddPlaylistDialog.newInstance(songsArtist).show(((FragmentActivity)mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                                break;

                            case R.id.popup_song_share:
                                MuzikoApp.getInstance().shareSongs(mContext, songsArtist);
                                break;

                            case R.id.popup_song_delete:
                                MuzikoUtils.deleteMultipleSong(mContext, songsArtist, "Songs");
                                break;

                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_album);
                menu.getMenu().findItem(R.id.popup_manage_art).setVisible(false);
                menu.show();

            }
        });
    }

}





