package com.whitebird.music.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.whitebird.music.dataloaders.PlaylistLoader;
import com.whitebird.music.models.Song;

import java.util.ArrayList;

/**
 * Created by naman on 20/12/15.
 */
public class CreatePlaylistDialog extends DialogFragment {

    public static CreatePlaylistDialog newInstance() {
        return newInstance(new ArrayList<Song>());
    }

    public static CreatePlaylistDialog newInstance(Song song) {
      ArrayList<Song> songs=new ArrayList<>();
      songs.add(song);
        return newInstance(songs);
    }

    public static CreatePlaylistDialog newInstance(ArrayList<Song> songList) {
        CreatePlaylistDialog dialog = new CreatePlaylistDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable("songs", songList);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity()).positiveText("Create").negativeText("Cancel").input("Enter playlist name", "", false, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                ArrayList<Song> songs = (ArrayList<Song>) getArguments().getSerializable("songs");
//                long playistId = MusicPlayer.createPlaylist(getActivity(), input.toString());

//                if (playistId != -1) {
//                    if (songs != null && songs.length != 0)
//                        MusicPlayer.addToPlaylist(getActivity(), songs, playistId);
//                    else
//                        Toast.makeText(getActivity(), "Created playlist", Toast.LENGTH_SHORT).show();
//                    if (getParentFragment() instanceof PlaylistFragment) {
//                        ((PlaylistFragment) getParentFragment()).updatePlaylists(playistId);
//                    }
//                } else
                    {
//                    Toast.makeText(getActivity(), "Unable to create playlist", Toast.LENGTH_SHORT).show();
                        PlaylistLoader.saveToPlayList(input.toString(),songs,getActivity());

                }

            }
        }).build();
    }
}
