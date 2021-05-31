package com.whitebird.music.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.whitebird.music.MusicPlayer;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.models.Playlist;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Constants;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.PreferencesUtility;
import com.whitebird.music.utils.StaticData;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ItemHolder> {

    private final ActionMode.Callback callback;
    int[] foregroundColors = {R.color.pink_transparent, R.color.green_transparent, R.color.blue_transparent, R.color.red_transparent, R.color.purple_transparent};
    boolean isDialog;
    private List<Playlist> arraylist;
    private ActionMode actionMode;
    private Activity mContext;
    private boolean isGrid;
    //    private boolean showAuto;
    private int songCountInt;
    private long totalRuntime;
    private long firstAlbumID = -1;
    private int foregroundColor;
    private ArrayList<Playlist> selectedSong = new ArrayList<>();
    private boolean isMultiSelect;

    public PlaylistAdapter(Activity context, List<Playlist> arraylist, ActionMode.Callback callback) {
        this.arraylist = arraylist;
        this.mContext = context;
        isDialog = false;
        this.isGrid = PreferencesUtility.getInstance(mContext).getPlaylistView() == Constants.PLAYLIST_VIEW_GRID;
//        this.showAuto = PreferencesUtility.getInstance(mContext).showAutoPlaylist();
        Random random = new Random();
        int rndInt = random.nextInt(foregroundColors.length);
        foregroundColor = foregroundColors[rndInt];
        this.callback = callback;

    }

    public void setIsDialog(boolean isDialog) {
        this.isDialog = isDialog;
    }

    public void clearSelection() {
        selectedSong.clear();
        notifyDataSetChanged();
    }

    public void setMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
    }

    public void setActionMode(ActionMode actionMode) {
        this.actionMode = actionMode;
    }

    public ArrayList<Playlist> getSelectedItems() {
        return selectedSong;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (isGrid && !isDialog) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_playlist_grid, null);
            ItemHolder ml = new ItemHolder(v);
            return ml;
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_playlist_list, null);
            ItemHolder ml = new ItemHolder(v);
            return ml;
        }
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int i) {


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
                        if (isGrid && !isDialog) {
                            new Palette.Builder(loadedImage).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                    if (swatch != null) {
                                        int color = swatch.getRgb();
                                        itemHolder.footer.setBackgroundColor(color);
                                        int textColor = MuzikoUtils.getBlackWhiteColor(swatch.getTitleTextColor());
                                        itemHolder.title.setTextColor(textColor);
                                        itemHolder.artist.setTextColor(textColor);
                                    } else {
                                        Palette.Swatch mutedSwatch = palette.getMutedSwatch();
                                        if (mutedSwatch != null) {
                                            int color = mutedSwatch.getRgb();
                                            itemHolder.footer.setBackgroundColor(color);
                                            int textColor = MuzikoUtils.getBlackWhiteColor(mutedSwatch.getTitleTextColor());
                                            itemHolder.title.setTextColor(textColor);
                                            itemHolder.artist.setTextColor(textColor);
                                        }
                                    }


                                }
                            });


                        }

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        if (isGrid && !isDialog) {
                            itemHolder.footer.setBackgroundColor(0);
                            if (mContext != null) {
                                int textColorPrimary = Config.textColorPrimary(mContext, Helpers.getATEKey(mContext));
                                itemHolder.title.setTextColor(textColorPrimary);
                                itemHolder.artist.setTextColor(textColorPrimary);
                            }
                        }
                    }//+ " - " + MuzikoUtils.makeShortTimeString(mContext,totalRuntime)
                });
        itemHolder.artist.setText(String.valueOf(arraylist.get(i).getSongList().size()) + " " + mContext.getString(R.string.songs));
        itemHolder.albumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (MuzikoUtils.isLollipop())
            itemHolder.albumArt.setTransitionName("transition_album_art" + i);


        if (selectedSong.contains(localItem)) {
            itemHolder.viewOverlay.setVisibility(View.VISIBLE);
            itemHolder.viewOverlay.setAlpha(.5f);
            itemHolder.viewOverlay.setBackgroundColor(Config.primaryColor(mContext, Helpers.getATEKey(mContext)));
        } else {
            itemHolder.viewOverlay.setVisibility(View.GONE);
        }

        if (!isDialog) {
            itemHolder.popupMenu.setVisibility(View.VISIBLE);
        } else {
            itemHolder.popupMenu.setVisibility(View.GONE);
        }

        setOnPopupMenuListener(itemHolder, i);
    }

    public void toggleSelection(Playlist song) {
        if (selectedSong.contains(song)) {
            selectedSong.remove(song);
        } else {
            selectedSong.add(song);
        }
        if (actionMode != null) {
            if (selectedSong.size() == 0)
                actionMode.setTitle("");
            else
                actionMode.setTitle(String.format("%d PlayList%s", selectedSong.size(), selectedSong.size() != 1 ? "s" : ""));
        }

        notifyItemChanged(arraylist.indexOf(song));
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

    private String getPlaylistType(int position) {
     /*   if (showAuto) {
            switch (position) {
                case 0:
                    return Constants.NAVIGATE_PLAYLIST_LASTADDED;
                case 1:
                    return Constants.NAVIGATE_PLAYLIST_RECENT;
                case 2:
                    return Constants.NAVIGATE_PLAYLIST_TOPTRACKS;
                default:
                    return Constants.NAVIGATE_PLAYLIST_USERCREATED;
            }
        } else */


        return Constants.NAVIGATE_PLAYLIST_USERCREATED;
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        ArrayList<Song> selectedSong =  arraylist.get(position).getSongList();
                       // long[] ids = MuzikoApp.getSongIds(selectedSong);

                        switch (item.getItemId()) {
                            case R.id.play:
                                MusicPlayer.playAll(selectedSong, 0,   false);
                                break;

                            case R.id.play_next:
                                MusicPlayer.playNext(selectedSong);
                                break;
                            case R.id.add_to_queue:

                                MusicPlayer.addToQueue(selectedSong);
                                break;
                            case R.id.popup_playlist_edit:
                                editPlaylist(position);

                                break;
                            case R.id.popup_playlist_delete:
                                new MaterialDialog.Builder(mContext).theme(Theme.LIGHT)
                                        .negativeColor(Config.accentColor(mContext, Helpers.getATEKey(mContext)))
                                        .titleColor(Config.accentColor(mContext, Helpers.getATEKey(mContext)))
                                        .neutralColorRes(R.color.dialog_negetive_button)
                                        .positiveColor(Config.accentColor(mContext, Helpers.getATEKey(mContext)))
                                        .title("Dou you want to delete " + arraylist.get(position).name + " ?")
                                        .negativeText("Cancel")
                                        .positiveText("continue").onPositive(new MaterialDialog.SingleButtonCallback() {

                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        removeData(arraylist.get(position));
                                    }
                                }).show();
                                break;
                            case R.id.popup_playlist_share:
                                if (arraylist.get(position).getSongList().size() != 0)
                                    MuzikoApp.getInstance().shareSongs(mContext, arraylist.get(position).getSongList());

                                break;
                            case R.id.popup_playlist_clone:
                                clonePlaylist(position);
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


    private void editPlaylist(final int position) {
        MaterialDialog nameDialog = new MaterialDialog.Builder(mContext)
                .theme(Theme.LIGHT).titleColor(Config.accentColor(mContext, Helpers.getATEKey(mContext)))
                .negativeColorRes(R.color.dialog_negetive_button)
                .title("Edit Playlist")
                .positiveText("DONE")
                .negativeText("CANCEL")
                .inputType(InputType.TYPE_CLASS_TEXT).input("Playlist Name", arraylist.get(position).name, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input.toString().length() > 0 && !arraylist.get(position).name.equals(input.toString())) {


                            ArrayList<Playlist> data = MuzikoApp.readAllPlayLists();
                            data.get(position).setName(input.toString());


                            MuzikoApp.writeAllPlayLists(data);
                            mContext.sendBroadcast(new Intent(MuzikoApp.INTENT_PLAYLIST_CHANGE));

                        } else if (arraylist.get(position).name.equals(input.toString())) {
                            MuzikoApp.toast(mContext, "Playlist " + input + " already exist !");
                        } else {
                            MuzikoApp.toast(mContext, "Enter name playlist!");
                        }
                    }
                }).build();

        nameDialog.show();
    }


    private void clonePlaylist(final int position) {
        MaterialDialog nameDialog = new MaterialDialog.Builder(mContext)
                .theme(Theme.LIGHT).titleColor(Config.accentColor(mContext, Helpers.getATEKey(mContext)))
                .negativeColorRes(R.color.dialog_negetive_button)
                .title("Clone Playlist")
                .positiveText("DONE")
                .negativeText("CANCEL")
                .inputType(InputType.TYPE_CLASS_TEXT).input("Playlist Name", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input.toString().length() > 0 && !arraylist.get(position).name.equals(input.toString())) {


                            ArrayList<Playlist> data = MuzikoApp.readAllPlayLists();
                            ArrayList<Song> dataSong=new ArrayList<Song>();
                            dataSong.addAll(data.get(position).getSongList());
                            Playlist clone = new Playlist(input.toString(), dataSong);

                            data.add(clone);

                            MuzikoApp.writeAllPlayLists(data);
                            mContext.sendBroadcast(new Intent(MuzikoApp.INTENT_PLAYLIST_CHANGE));

                        } else if (arraylist.get(position).name.equals(input.toString())) {
                            MuzikoApp.toast(mContext, "Playlist " + input + " already exist !");
                        } else {
                            MuzikoApp.toast(mContext, "Enter name playlist!");
                        }
                    }
                }).build();

        nameDialog.show();
    }


    private void removeData(Playlist playlist) {
        ArrayList<Playlist> data = MuzikoApp.readAllPlayLists();
        for (Playlist play : data) {
            if (play.name.equals(playlist.name)) {
                data.remove(play);
break;
            }
        }
        MuzikoApp.writeAllPlayLists(data);

        mContext.sendBroadcast(new Intent(MuzikoApp.INTENT_PLAYLIST_CHANGE));
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, artist;
        protected ImageView albumArt, popupMenu;
        protected View footer, viewOverlay;

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
            this.title = (TextView) view.findViewById(R.id.album_title);
            this.artist = (TextView) view.findViewById(R.id.album_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.album_art);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            this.footer = view.findViewById(R.id.footer);
            this.viewOverlay = view.findViewById(R.id.viewOverlay);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (isDialog) {
                return;
            }
            if (isMultiSelect) {
                toggleSelection(arraylist.get(getAdapterPosition()));
                return;
            }
            StaticData.PLAYLIST_POSITION=getAdapterPosition();
            NavigationUtils.navigateToPlaylistDetail(mContext, getPlaylistType(getAdapterPosition()), (long) albumArt.getTag(), String.valueOf(title.getText()), foregroundColor, getAdapterPosition(), null);

        }

    }

}




