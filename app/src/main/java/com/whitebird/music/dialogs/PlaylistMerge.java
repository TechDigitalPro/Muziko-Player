package com.whitebird.music.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.widget.CompoundButtonCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.adapters.PlayListDialogAdapter;
import com.whitebird.music.models.Playlist;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.widgets.DragSortRecycler;

import java.util.ArrayList;
import java.util.List;

public class PlaylistMerge implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    ArrayList<Playlist> data = new ArrayList<>();
    private Activity context;
    private List<Playlist> playList = new ArrayList<>();
    private PlayListDialogAdapter adapter;
    private RelativeLayout createNewButton;
    private MaterialDialog createDialog = null;
    private boolean removeExisting = false;

    public PlaylistMerge(List<Playlist> playlistItems) {
        playList.addAll(playlistItems);
    }

    public void open(Activity context) {
        this.context = context;

        adapter = new PlayListDialogAdapter(context, playList);


        View view = LayoutInflater.from(context).inflate(R.layout.dialog_merge_playlist, null, false);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        CheckBox removecheckbox = view.findViewById(R.id.removecheckbox);
        if (Build.VERSION.SDK_INT < 21) {
            CompoundButtonCompat.setButtonTintList(removecheckbox, ColorStateList.valueOf(Config.accentColor(context, Helpers.getATEKey(context))));//Use android.support.v4.widget.CompoundButtonCompat when necessary else
        } else {
            removecheckbox.setButtonTintList(ColorStateList.valueOf(Config.accentColor(context, Helpers.getATEKey(context))));//setButtonTintList is accessible directly on API>19
        }
        removecheckbox.setOnCheckedChangeListener(this);

        GridLayoutManager layoutList = new GridLayoutManager(context, 1);
        RecyclerView recyclerView = view.findViewById(R.id.itemList);
        recyclerView.setLayoutManager(layoutList);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);


        DragSortRecycler dragSortRecycler = new DragSortRecycler();
        dragSortRecycler.setViewHandleId(R.id.reorder);

        dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
            @Override
            public void onItemMoved(int from, int to) {
                Log.d("queue", "onItemMoved " + from + " to " + to);
                Playlist song = adapter.getItemAt(from);
                adapter.removeItemAt(from);
                adapter.addItemTo(to, song);
                adapter.notifyDataSetChanged();

            }
        });

        recyclerView.addItemDecoration(dragSortRecycler);
        recyclerView.addOnItemTouchListener(dragSortRecycler);
        recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());



        createNewButton = view.findViewById(R.id.createNewButton);
        createNewButton.setOnClickListener(this);
        createNewButton.setBackgroundColor(Config.accentColor(context, Helpers.getATEKey(context)));

        createDialog = new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT).titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                .negativeColorRes(R.color.dialog_negetive_button)
                .title("Merge Playlist")
                .customView(view, false)
                .build();

        createDialog.show();
    }

    public void close() {

        if (createDialog != null) {
            createDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == createNewButton) {
            playList.clear();
            playList.addAll(adapter.getItem());
            create();
        }
    }

    private void create() {
        MaterialDialog nameDialog = new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT).titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                .negativeColorRes(R.color.dialog_negetive_button)
                .title("Merge Playlist")
                .positiveText("CREATE")
                .negativeText("CANCEL")
                .inputType(InputType.TYPE_CLASS_TEXT).input("Playlist Name", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                        if (input.toString().length() > 0) {

                            data = MuzikoApp.readAllPlayLists();

                            for (Playlist playlistData : data) {
                                if (playlistData.name.equals(input.toString())) {
                                    MuzikoApp.toast(context, "Playlist " + input + " already exist !");
                                    return;
                                }
                            }
                            ArrayList<Song> dataSong = new ArrayList<>();
                            for (Playlist playlist : playList) {

                                dataSong.addAll(playlist.songList);
                                if (removeExisting)
                                    removeData(playlist);
                            }

                            Playlist newPlaylist = new Playlist(input.toString(), dataSong);
                            data.add(newPlaylist);
                            MuzikoApp.writeAllPlayLists(data);
                            createDialog.dismiss();
                            context.sendBroadcast(new Intent(MuzikoApp.INTENT_PLAYLIST_CHANGE));

                        } else {
                            MuzikoApp.toast(context, "Enter name playlist!");
                        }
                    }
                }).build();

        nameDialog.show();
    }

    private void removeData(Playlist playlist) {
        for (Playlist play : data) {
            if (play.name.equals(playlist.name)) {
                data.remove(play);
                return;
            }
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        removeExisting = isChecked;

    }


}
