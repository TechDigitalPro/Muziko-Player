package com.whitebird.music.utils;

import android.content.Context;

import com.whitebird.music.R;
import com.whitebird.music.dataloaders.SongLoader;
import com.whitebird.music.fragments.DirectoryFragment;
import com.whitebird.music.models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.whitebird.music.fragments.DirectoryFragment.formatFileSize;

public class StaticData {

    public static final String BASE_URL="http://addonvisions.com/music/api/v1/";
    public static final String REGISTER="users/register";
    public static final String UPDATE_PROFILE="users/updateprofile";
    public static final String LOGIN="users/login";
    public static final String ADD_MUSIC="settings/addmusic";



    public static final int TYPE_SONG=0;
    public static final int TYPE_ALBUM=1;
    public static final int TYPE_ARTIST=2;
    public static final int TYPE_GENRE=3;
    public static  int PLAYLIST_POSITION = 0;
    private static String[] chhosefileType = {".mp3", ".acc", ".wav", ".ogg"};
    public static ArrayList<DirectoryFragment.ListItem> listFiles(File dir) {
        ArrayList<DirectoryFragment.ListItem> items=new ArrayList<>();


        File[] files = null;
        try {
            files = dir.listFiles();
        } catch (Exception e) {

        }


        items.clear();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.isDirectory() != rhs.isDirectory()) {
                    return lhs.isDirectory() ? -1 : 1;
                }
                return lhs.getName().compareToIgnoreCase(rhs.getName());
                /*
                 * long lm = lhs.lastModified(); long rm = lhs.lastModified();
                 * if (lm == rm) { return 0; } else if (lm > rm) { return -1; }
                 * else { return 1; }
                 */
            }
        });
        for (File file : files) {
            if (file.getName().startsWith(".")) {
                continue;
            }
            DirectoryFragment.ListItem item = new DirectoryFragment() .new ListItem();
            item.title = file.getName();
            item.file = file;
            if (file.isDirectory()) {
                item.icon = R.drawable.ic_directory;
                item.subtitle = "Folder";
            } else {
                String fname = file.getName();
                String[] sp = fname.split("\\.");
                item.ext = sp.length > 1 ? sp[sp.length - 1] : "?";
                item.subtitle = formatFileSize(file.length());
                fname = fname.toLowerCase();
                if (fname.endsWith(".jpg") || fname.endsWith(".png")
                        || fname.endsWith(".gif") || fname.endsWith(".jpeg")) {
                    item.thumb = file.getAbsolutePath();
                }
            }
            if (file.toString().contains(chhosefileType[0]) ||
                    file.toString().contains(chhosefileType[1]) ||
                    file.toString().contains(chhosefileType[2]) ||
                    file.toString().contains(chhosefileType[3]) || file.isDirectory())
                items.add(item);
        }
        DirectoryFragment.ListItem item =  new DirectoryFragment() .new ListItem();
        item.title = "..";
        item.subtitle = "Folder";
        item.icon = R.drawable.ic_directory;
        item.file = null;
        items.add(0, item);
        return items;
    }


    public static  ArrayList<Song> getSongsForFiles(List<DirectoryFragment.ListItem> files, Context context) {
        ArrayList<Song> mSongs=new ArrayList<>();
        try {
            mSongs.clear();
            for (DirectoryFragment.ListItem file : files) {
                try {
                    mSongs.add(SongLoader.getSongFromPath(file.file.getAbsolutePath(), context));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {

        }
        return mSongs;

    }



}
