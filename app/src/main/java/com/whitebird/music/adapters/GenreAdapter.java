package com.whitebird.music.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.FragmentActivity;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.Config;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.dialogs.AddPlaylistDialog;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.PreferencesUtility;
import com.whitebird.music.widgets.FastScroller.views.FastScrollRecyclerView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ItemHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private final ActionMode.Callback callback;
    private List<Song> arraylist;
    private Activity mContext;
    private boolean isGrid;
    private ActionMode actionMode;
    private ArrayList<Song> selectedSong = new ArrayList<>();
    private boolean isMultiSelect;
    public GenreAdapter(Activity context, List<Song> arraylist, ActionMode.Callback callback) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.isGrid = PreferencesUtility.getInstance(mContext).isGenreInGrid();
        this.callback = callback;
    }
    public void clearSelection() {
        selectedSong.clear();
        notifyDataSetChanged();
    }
    public void setActionMode(ActionMode actionMode) {
        this.actionMode = actionMode;
    }
    public static int getOpaqueColor(@ColorInt int paramInt) {
        return 0xFF000000 | paramInt;
    }
    public List<Song> getData(){
        return  arraylist;
    }
    public void setMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
    }
    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (isGrid) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist_grid, null);
            ItemHolder ml = new ItemHolder(v);
            return ml;
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist, null);
            ItemHolder ml = new ItemHolder(v);
            return ml;
        }
    }
    public ArrayList<Song> getSelectedItems() {
        return selectedSong;
    }
    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int i) {



        final Song localItem = arraylist.get(i);

        itemHolder.name.setText(localItem.genre_name);
        String albumNmber = MuzikoUtils.makeLabel(mContext, R.plurals.Nalbums, 0);
        String songCount = MuzikoUtils.makeLabel(mContext, R.plurals.Nsongs, localItem.songsCount);
        itemHolder.albums.setText(MuzikoUtils.makeString(mContext, songCount));

        if (isGrid) {
            MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(localItem.albumId).toString(), itemHolder.artistImage,
                    MuzikoApp.OPTIONS, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            if (isGrid && loadedImage != null) {
                                new Palette.Builder(loadedImage).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        int color = palette.getVibrantColor(Color.parseColor("#66000000"));
                                        itemHolder.footer.setBackgroundColor(color);
                                        Palette.Swatch swatch = palette.getVibrantSwatch();
                                        int textColor;
                                        if (swatch != null) {
                                            textColor = getOpaqueColor(swatch.getTitleTextColor());
                                        } else textColor = Color.parseColor("#ffffff");

                                        itemHolder.name.setTextColor(textColor);
                                        itemHolder.albums.setTextColor(textColor);
                                    }
                                });

                                if (selectedSong.contains(localItem)) {
                                    itemHolder.viewOverlay.setVisibility(View.VISIBLE);
                                    itemHolder.viewOverlay.setAlpha(.5f);
                                    itemHolder.viewOverlay.setBackgroundColor(Config.primaryColor(mContext, Helpers.getATEKey(mContext)));
                                } else {
                                    itemHolder.viewOverlay.setVisibility(View.GONE);
                                }

                            }

                            ImageView iv = (ImageView) view;
                            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            iv.setAdjustViewBounds(false);
                            iv.setImageBitmap(loadedImage);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            if (isGrid) {
                                itemHolder.footer.setBackgroundColor(0);
                                if (mContext != null) {
                                    int textColorPrimary = Config.textColorPrimary(mContext, Helpers.getATEKey(mContext));
                                    itemHolder.name.setTextColor(textColorPrimary);
                                    itemHolder.albums.setTextColor(textColorPrimary);
                                    if (selectedSong.contains(localItem)) {
                                        itemHolder.viewOverlay.setVisibility(View.VISIBLE);
                                        itemHolder.viewOverlay.setAlpha(.5f);
                                        itemHolder.viewOverlay.setBackgroundColor(Config.primaryColor(mContext, Helpers.getATEKey(mContext)));
                                    } else {
                                        itemHolder.viewOverlay.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                    });
        } else {
            MuzikoApp.loader.displayImage(MuzikoUtils.getAlbumArtUri(localItem.albumId).toString(), itemHolder.artistImage,
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
            itemHolder.artistImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (selectedSong.contains(localItem)) {
                itemHolder.viewOverlay.setVisibility(View.VISIBLE);
                itemHolder.viewOverlay.setAlpha(.5f);
                itemHolder.viewOverlay.setBackgroundColor(Config.primaryColor(mContext, Helpers.getATEKey(mContext)));
            } else {
                itemHolder.viewOverlay.setVisibility(View.GONE);
            }
        }        if (MuzikoUtils.isLollipop())
            itemHolder.artistImage.setTransitionName("transition_artist_art" + i);
        setOnPopupMenuListener(itemHolder, i);
    }
    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        ArrayList<Song> songsGenre= DataHelper.getSongsForGenre(arraylist.get(position).genre_name);

                       /* long[] ids = new long[songsGenre.size()];
                        for (int i=0;i<songsGenre.size();i++)
                            ids[i] = songsGenre.get(i).id;*/
                        switch (item.getItemId()) {
                            case R.id.popup_song_play:
                                MusicPlayer.playAll(songsGenre, 0,  false);
                                break;
                            case R.id.popup_move_ignore:
                                ArrayList<Song> ignore = new ArrayList();
                                ignore.addAll(songsGenre);
                                MuzikoApp.moveToIgnore(ignore, mContext);
                                break;
                            case R.id.add_to_fav:
                                ArrayList<Song> fav = new ArrayList();
                                fav.add(arraylist.get(position));
                                MuzikoApp.moveToFavorite(fav, mContext);
                                break;
                            case R.id.popup_song_play_next:

                                MusicPlayer.playNext(songsGenre);
                                break;
                            case R.id.popup_song_addto_queue:
                                MusicPlayer.addToQueue(  songsGenre );
                                break;



                            case R.id.popup_song_addto_playlist:
                                AddPlaylistDialog.newInstance(songsGenre).show(((FragmentActivity)mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                                break;

                            case R.id.popup_song_share:
                                MuzikoApp.getInstance().shareSongs(mContext, songsGenre);
                                break;

                            case R.id.popup_song_delete:
                                MuzikoUtils.deleteMultipleSong(mContext, songsGenre, "Songs");
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
    @Override
    public long getItemId(int position) {
        return arraylist.get(position).id;
    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }


    public void updateDataSet(List<Song> arrayList) {
        this.arraylist = arrayList;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (arraylist == null || arraylist.size() == 0)
            return "";
        Character ch = arraylist.get(position).genre_name.charAt(0);
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
                actionMode.setTitle(String.format("%d Genre%s", selectedSong.size(), selectedSong.size() != 1 ? "s" : ""));
        }

        notifyItemChanged(arraylist.indexOf(song));
    }
    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView name, albums;
        protected ImageView artistImage,popupMenu;
        protected View footer,viewOverlay;

        public ItemHolder(View view) {
            super(view);
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
            this.name = (TextView) view.findViewById(R.id.artist_name);
            this.albums = (TextView) view.findViewById(R.id.album_song_count);
            this.artistImage = (ImageView) view.findViewById(R.id.artistImage);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            this.footer = view.findViewById(R.id.footer);
            this.viewOverlay = view.findViewById(R.id.viewOverlay);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (isMultiSelect) {
                toggleSelection(arraylist.get(getAdapterPosition()));
                return;
            }
            NavigationUtils.goToGenre(mContext, arraylist.get(getAdapterPosition()).genre_name);
//            NavigationUtils.navigateToArtist(mContext, arraylist.get(getAdapterPosition()).id,
//                    new Pair<View, String>(artistImage, "transition_artist_art" + getAdapterPosition()));
        }

    }
}




