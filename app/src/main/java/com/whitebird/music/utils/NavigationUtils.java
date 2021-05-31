
package com.whitebird.music.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.R;
import com.whitebird.music.activities.MainActivity;
import com.whitebird.music.activities.MultiTagActivity;
import com.whitebird.music.activities.NowPlayingActivity;
import com.whitebird.music.activities.PlaylistDetailActivity;
import com.whitebird.music.activities.SearchActivity;
import com.whitebird.music.activities.SettingsActivity;
import com.whitebird.music.activities.TagsActivity;
import com.whitebird.music.fragments.AlbumDetailFragment;
import com.whitebird.music.fragments.ArtistDetailFragment;
import com.whitebird.music.models.Song;
import com.whitebird.music.nowplaying.Muziko5;
import com.whitebird.music.tageditor.metadata.MusicMetadata;
import com.whitebird.music.tageditor.metadata.MusicMetadataSet;
import com.whitebird.music.tageditor.myid3.MyID3;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;

public class NavigationUtils {

    @TargetApi(21)
    public static void navigateToAlbum(Activity context, String albumID, Pair<View, String> transitionViews) {

        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        Fragment fragment;

        transaction.setCustomAnimations(R.anim.activity_fade_in,
                R.anim.activity_fade_out, R.anim.activity_fade_in, R.anim.activity_fade_out);
        fragment = AlbumDetailFragment.newInstance(albumID, false, null);

        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack(null).commit();

    }

    @TargetApi(21)
    public static void navigateQueue(Activity context) {


        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_QUEUE);

        context.startActivity(intent);
        context.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);

    }

    @TargetApi(21)
    public static void navigateHome(Activity context) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_QUEUE);

        context.startActivity(intent);
        context.overridePendingTransition(R.anim.enter, R.anim.exit);

    }


    public static void navigateIgnore(Activity context) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_IGNORE);

        context.startActivity(intent);


    }

    public static void navigateFav(Activity context) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_FAVORITE);

        context.startActivity(intent);


    }

    @TargetApi(21)
    public static void navigateMp3Cutter(Activity context) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_MP3CUTTER);

        context.startActivity(intent);
        context.overridePendingTransition(R.anim.enter, R.anim.exit);

    }

    @TargetApi(21)
    public static void navigateToArtist(Activity context, long artistID, Pair<View, String> transitionViews) {

        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        Fragment fragment;

        transaction.setCustomAnimations(R.anim.activity_fade_in,
                R.anim.activity_fade_out, R.anim.activity_fade_in, R.anim.activity_fade_out);
        fragment = ArtistDetailFragment.newInstance(artistID, false, null);

        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack(null).commit();

    }

    public static void goToArtist(Context context, long artistId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_ARTIST);
        intent.putExtra(Constants.ARTIST_ID, artistId);
        context.startActivity(intent);
    }

    public static void goToAbout(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_ABOUT);

        context.startActivity(intent);
    }

    public static void goToPlayList(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_PLAYLIST);

        context.startActivity(intent);
    }
    public static void goToActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_ACTIVITY);

        context.startActivity(intent);
    }

    public static void goToAlbum(Context context, String albumId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_ALBUM);
        intent.putExtra(Constants.ALBUM_ID, albumId);
        context.startActivity(intent);
    }

    public static void goToGenre(Context context, String genreName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_GENRE);
        intent.putExtra(Constants.GENRE_ID, genreName);
        context.startActivity(intent);
    }

    public static void goToEqulizer(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_Equlizer);

        context.startActivity(intent);
    }

    public static void goToSleep(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_SLEEP);

        context.startActivity(intent);
    }
    public static void goToFolder(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_FOLDER);

        context.startActivity(intent);
    }
    public static void goToLyrics(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_LYRICS);
        context.startActivity(intent);
    }

    public static void navigateToNowplaying(Activity context, boolean withAnimations) {

        final Intent intent = new Intent(context, NowPlayingActivity.class);
        context.startActivity(intent);
    }

    public static Intent getNowPlayingIntent(Context context) {

        final Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_NOWPLAYING);
        return intent;
    }

    public static void navigateToSettings(Activity context) {
        final Intent intent = new Intent(context, SettingsActivity.class);
        intent.setAction(Constants.NAVIGATE_SETTINGS);
        context.startActivity(intent);
    }

    public static void navigateToSearch(Activity context) {
        final Intent intent = new Intent(context, SearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setAction(Constants.NAVIGATE_SEARCH);
        context.startActivity(intent);
    }


    @TargetApi(21)
    public static void navigateToPlaylistDetail(Activity context, String action, long firstAlbumID, String playlistName, int foregroundcolor, int playlistPosition, ArrayList<Pair> transitionViews) {
        final Intent intent = new Intent(context, PlaylistDetailActivity.class);
        intent.setAction(action);
        intent.putExtra(Constants.PLAYLIST_ID, playlistPosition);
        intent.putExtra(Constants.PLAYLIST_FOREGROUND_COLOR, foregroundcolor);
        intent.putExtra(Constants.ALBUM_ID, firstAlbumID);
        intent.putExtra(Constants.PLAYLIST_NAME, playlistName);
        intent.putExtra(Constants.ACTIVITY_TRANSITION, transitionViews != null);

        if (transitionViews != null && MuzikoUtils.isLollipop()) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context, transitionViews.get(0), transitionViews.get(1), transitionViews.get(2));
            context.startActivityForResult(intent, Constants.ACTION_DELETE_PLAYLIST, options.toBundle());
        } else {
            context.startActivityForResult(intent, Constants.ACTION_DELETE_PLAYLIST);
        }
    }


    public static Intent getNavigateToStyleSelectorIntent(Activity context, String what) {
        final Intent intent = new Intent(context, SettingsActivity.class);
        intent.setAction(Constants.SETTINGS_STYLE_SELECTOR);
        intent.putExtra(Constants.SETTINGS_STYLE_SELECTOR_WHAT, what);
        return intent;
    }

    public static Fragment getFragmentForNowplayingID(String fragmentID) {
    /*    switch (fragmentID) {
            case Constants.TIMBER1:
                return new Muziko1();
            case Constants.TIMBER2:
                return new Muziko2();
            case Constants.TIMBER3:
                return new Muziko3();
            case Constants.TIMBER4:
                return new Muziko4();
            case Constants.TIMBER5:
                return new Muziko5();
            case Constants.TIMBER6:
                return new Muziko6();
            default:
                return new Muziko1();
        }*/

        return new Muziko5();

    }


    public static void multiTagEdit(final Context context, final ArrayList<Song> queueItems) {
        MuzikoApp.multiTagList.clear();

        boolean hasSD = false;

        MuzikoApp.multiTagList.addAll(queueItems);

        if (hasSD && MuzikoApp.multiTagList.size() > 0) {
            new MaterialDialog.Builder(context)
                    .theme(Theme.LIGHT)

                    .negativeColor(Config.accentColor(context, Helpers.getATEKey(context)))
                    .titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                    .neutralColorRes(R.color.dialog_negetive_button)
                    .positiveColor(Config.accentColor(context, Helpers.getATEKey(context)))
                    .title("Multi Tag Edit")
                    .content(
                            MuzikoApp.getInstance()
                                    .getApplicationContext()
                                    .getString(R.string.multi_tagedit_warning))
                    .positiveText("OK")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (MuzikoApp.multiTagList.size() > 1) {
//                                    Intent intent = new Intent(context, MultiTagActivity.class);
//                                    context.startActivity(intent);
                            } else {
                                Song queueItem = new Song();
                                queueItem = MuzikoApp.multiTagList.get(0);

                                Intent intent = new Intent(context, TagsActivity.class);
//                                    intent.putExtra("tag", TAG);
                                intent.putExtra("item", queueItem);
                                intent.putExtra("index", 0);
                                context.startActivity(intent);
                            }
                        }
                    })
                    .negativeText("Cancel")
                    .show();
        } else if (hasSD && MuzikoApp.multiTagList.size() == 0) {

            new MaterialDialog.Builder(context)
                    .theme(Theme.LIGHT)

                    .negativeColor(Config.accentColor(context, Helpers.getATEKey(context)))
                    .titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                    .neutralColorRes(R.color.dialog_negetive_button)
                    .positiveColor(Config.accentColor(context, Helpers.getATEKey(context)))
                    .title("Multi Tag Edit")
                    .content(
                            MuzikoApp.getInstance()
                                    .getApplicationContext()
                                    .getString(R.string.multi_tagedit_error))
                    .positiveText("OK")
                    .show();
        } else {
            if (MuzikoApp.multiTagList.size() > 1) {

                ArrayList<Song> data = new ArrayList<>();
                for (int i = 0; i < MuzikoApp.multiTagList.size(); i++) {

                    Song multiQueueItem = MuzikoApp.multiTagList.get(i);
                    MusicMetadataSet itemDataset = readData(multiQueueItem);
                    if (!Utils.isEditable(multiQueueItem.data).equalsIgnoreCase("mp3")) {
                        data.add(multiQueueItem);
                        continue;
                    }

                    if (itemDataset == null) {
                        data.add(multiQueueItem);

                        continue;
                    }

                    MusicMetadata itemMetadata = (MusicMetadata) itemDataset.getSimplified();

                    if (itemMetadata == null) {
                        data.add(multiQueueItem);
                        continue;
                    }


                }


                if (data.size() > 0) {
                    String str = "";
                    for (Song song : data) {
                        str = str + song.title + "\n";
                    }
                    new MaterialDialog.Builder(context).theme(Theme.LIGHT)

                            .negativeColor(Config.accentColor(context, Helpers.getATEKey(context)))
                            .titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                            .neutralColorRes(R.color.dialog_negetive_button)
                            .positiveColor(Config.accentColor(context, Helpers.getATEKey(context)))
                            .title("Below song not support Tag Editing.\nDo you want to continue with other?")


                            .content(str).positiveText("continue").onPositive(new MaterialDialog.SingleButtonCallback() {


                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent intent = new Intent(context, MultiTagActivity.class);
                            context.startActivity(intent);

                        }
                    }).show();

                } else {
                    Intent intent = new Intent(context, MultiTagActivity.class);
                    context.startActivity(intent);
                }


            } else {
                try {


                    Song queueItem = new Song();
                    queueItem = MuzikoApp.multiTagList.get(0);
                    String extension = Utils.isEditable(queueItem.data);
                    if (extension.equalsIgnoreCase("mp3")) {
                        Intent intent = new Intent(context, TagsActivity.class);
//                                    intent.putExtra("tag", TAG);
                        intent.putExtra("item", queueItem);
                        intent.putExtra("index", 0);
                        context.startActivity(intent);
                    } else {
                        new MaterialDialog.Builder(context)
                                .negativeColor(Config.accentColor(context, Helpers.getATEKey(context)))
                                .titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                                .neutralColorRes(R.color.dialog_negetive_button)
                                .positiveColor(Config.accentColor(context, Helpers.getATEKey(context))).positiveColorRes(R.color.normal_blue).title("Invalid file format")
                                .content("This is a ." + extension + " file. Only mp3 files are supported.").positiveText("OK").onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        }).show();

                    }
                } catch (Exception e) {
                    new MaterialDialog.Builder(context).theme(Theme.LIGHT)
                            .negativeColor(Config.accentColor(context, Helpers.getATEKey(context)))
                            .titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                            .neutralColorRes(R.color.dialog_negetive_button)
                            .positiveColor(Config.accentColor(context, Helpers.getATEKey(context))).title("Invalid file format")
                            .content("Unable to edit Tag").positiveText("OK").onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        }
                    }).show();
                }

            }
        }
    }

    private static MusicMetadataSet readData(Song queueItem) {
        MyID3 id3 = new MyID3();
        MusicMetadataSet data = null;
        try {
            File from = new File(queueItem.data);
            data = id3.read(from);      //read metadata
        } catch (Exception ex) {
            String fileNameWithoutExt = FilenameUtils.getBaseName(queueItem.data);


        }

        return data;
    }

    public static int getIntForCurrentNowplaying(String nowPlaying) {
        switch (nowPlaying) {
            case Constants.TIMBER1:
                return 0;
            case Constants.TIMBER2:
                return 1;
            case Constants.TIMBER3:
                return 2;
            case Constants.TIMBER4:
                return 3;
            case Constants.TIMBER5:
                return 4;
            case Constants.TIMBER6:
                return 5;
            default:
                return 2;
        }

    }

}
