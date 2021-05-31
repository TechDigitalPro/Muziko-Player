

package com.whitebird.music.subfragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.dataloaders.PlaylistLoader;
import com.whitebird.music.models.Playlist;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.Constants;
import com.whitebird.music.utils.MuzikoUtils;
import com.whitebird.music.utils.NavigationUtils;
import com.whitebird.music.utils.PreferencesUtility;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlaylistPagerFragment extends Fragment {

    private static final String ARG_PAGE_NUMBER = "pageNumber";
    private int[] foregroundColors = {R.color.pink_transparent, R.color.green_transparent, R.color.blue_transparent, R.color.red_transparent, R.color.purple_transparent};
    private int pageNumber, songCountInt, totalRuntime;
    private int foregroundColor;
    private long firstAlbumID = -1;
    private Playlist playlist;
    private TextView playlistame, songcount, playlistnumber, playlisttype, runtime;
    private ImageView playlistImage;
    private View foreground;
    private Context mContext;
    private boolean showAuto;

    public static PlaylistPagerFragment newInstance(int pageNumber) {
        PlaylistPagerFragment fragment = new PlaylistPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PAGE_NUMBER, pageNumber);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        showAuto = PreferencesUtility.getInstance(getActivity()).showAutoPlaylist();
        View rootView = inflater.inflate(R.layout.fragment_playlist_pager, container, false);

        final List<Playlist> playlists = PlaylistLoader.getPlayList();

        pageNumber = getArguments().getInt(ARG_PAGE_NUMBER);
        playlist = playlists.get(pageNumber);

        playlistame = (TextView) rootView.findViewById(R.id.name);
        playlistnumber = (TextView) rootView.findViewById(R.id.number);
        songcount = (TextView) rootView.findViewById(R.id.songcount);
        runtime = (TextView) rootView.findViewById(R.id.runtime);
        playlisttype = (TextView) rootView.findViewById(R.id.playlisttype);
        playlistImage = (ImageView) rootView.findViewById(R.id.playlist_image);
        foreground = rootView.findViewById(R.id.foreground);

        playlistImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Pair> tranitionViews = new ArrayList<>();
                tranitionViews.add(0, Pair.create((View) playlistame, "transition_playlist_name"));
                tranitionViews.add(1, Pair.create((View) playlistImage, "transition_album_art"));
                tranitionViews.add(2, Pair.create(foreground, "transition_foreground"));
                NavigationUtils.navigateToPlaylistDetail(getActivity(), getPlaylistType(), firstAlbumID, String.valueOf(playlistame.getText()), foregroundColor, pageNumber, tranitionViews);
            }
        });

        mContext = this.getContext();
        setUpPlaylistDetails();
        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedinstancestate) {
        new loadPlaylistImage().execute("");
    }

    private void setUpPlaylistDetails() {
        playlistame.setText(playlist.getName());

        int number = getArguments().getInt(ARG_PAGE_NUMBER) + 1;
        String playlistnumberstring;

        if (number > 9) {
            playlistnumberstring = String.valueOf(number);
        } else {
            playlistnumberstring = "0" + String.valueOf(number);
        }
        playlistnumber.setText(playlistnumberstring);

        Random random = new Random();
        int rndInt = random.nextInt(foregroundColors.length);

        foregroundColor = foregroundColors[rndInt];
        foreground.setBackgroundColor(foregroundColor);

        if (showAuto) {
            if (pageNumber <= 2)
                playlisttype.setVisibility(View.VISIBLE);
        }

    }

    private String getPlaylistType() {
        if (showAuto) {
            switch (pageNumber) {
                case 0:
                    return Constants.NAVIGATE_PLAYLIST_LASTADDED;
                case 1:
                    return Constants.NAVIGATE_PLAYLIST_RECENT;
                case 2:
                    return Constants.NAVIGATE_PLAYLIST_TOPTRACKS;
                default:
                    return Constants.NAVIGATE_PLAYLIST_USERCREATED;
            }
        } else return Constants.NAVIGATE_PLAYLIST_USERCREATED;
    }


    private class loadPlaylistImage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (getActivity() != null) {

                {
                    List<Song> playlistsongs = PlaylistLoader.getSongsInPlaylist(playlist.getName());
                    songCountInt = playlistsongs.size();
                    for (Song song : playlistsongs) {
                        totalRuntime += song.duration;
                    }
                    if (songCountInt != 0) {
                        firstAlbumID = playlistsongs.get(0).albumId;
                        return MuzikoUtils.getAlbumArtUri(firstAlbumID).toString();
                    } else return "nosongs";
                }
            } else return "context is null";

        }

        @Override
        protected void onPostExecute(String uri) {
            MuzikoApp.loader.displayImage(uri, playlistImage,
                    MuzikoApp.OPTIONS, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        }
                    });
            songcount.setText(" " + String.valueOf(songCountInt) + " " + mContext.getString(R.string.songs));
//            runtime.setText(" " + MuzikoUtils.makeShortTimeString(mContext, totalRuntime));
        }

        @Override
        protected void onPreExecute() {
        }
    }


}

