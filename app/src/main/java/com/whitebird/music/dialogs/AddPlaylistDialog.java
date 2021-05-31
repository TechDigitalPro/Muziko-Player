package com.whitebird.music.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.dataloaders.PlaylistLoader;
import com.whitebird.music.models.Playlist;
import com.whitebird.music.models.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by naman on 20/12/15.
 */
public class AddPlaylistDialog extends DialogFragment {

    public static AddPlaylistDialog newInstance(Song song) {
//        long[] songs = new long[1];
//        songs[0] = song.id;
        ArrayList<Song> songs=new ArrayList<>();
        songs.add(song);
        return newInstance(songs);
    }

    public static AddPlaylistDialog newInstance(ArrayList<Song> songList) {
        AddPlaylistDialog dialog = new AddPlaylistDialog();
        Bundle bundle = new Bundle();
        ArrayList<Song> songs=new ArrayList<>();
        songs.addAll(songList);
        bundle.putSerializable("songs", songs);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final List<Playlist> playlists = PlaylistLoader.getPlayList();
        CharSequence[] chars = new CharSequence[playlists.size() + 1];
        chars[0] = "Create new playlist";

        for (int i = 0; i < playlists.size(); i++) {
            chars[i + 1] = playlists.get(i).getName();
        }
        return new MaterialDialog.Builder(getActivity()).title("Add to playlist").items(chars).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                ArrayList<Song> songs = (ArrayList<Song>) getArguments().getSerializable("songs");
                if (which == 0) {
                    CreatePlaylistDialog.newInstance(songs).show(getActivity().getSupportFragmentManager(), "CREATE_PLAYLIST");
                    return;
                }
                PlaylistLoader.saveToPlayList(playlists.get(which - 1).getName().toString(),songs,getActivity());

                MuzikoApp.toast(getActivity(),songs.size()+" Song Added to "+playlists.get(which - 1).getName().toString());
//                MusicPlayer.addToPlaylist(getActivity(), songs, playlists.get(which - 1).getName());
                dialog.dismiss();

            }
        }).build();
    }
}
