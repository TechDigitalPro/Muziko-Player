package com.whitebird.music;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.whitebird.music.activities.user.UserData;
import com.whitebird.music.dataloaders.SongLoaderNew;
import com.whitebird.music.models.Playlist;
import com.whitebird.music.models.Song;
import com.whitebird.music.models.StartTimeWrapper;
import com.whitebird.music.permissions.Nammu;
import com.whitebird.music.utils.Helpers;
import com.whitebird.music.utils.PreferencesUtility;
import com.whitebird.music.utils.Utils;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.L;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MuzikoApp extends MultiDexApplication {


    public static final String INTENT_TRACK_CHANGED = "com.whitebird.music.intent.track.changed";
    public static final String INTENT_RELOAD_QUEUE = "com.whitebird.music.intent.reload.queue";
    public static final String INTENT_TRACK_DELETED = "com.whitebird.music.intent.track.deleted";
    public static final String INTENT_PLAYLIST_CHANGE = "com.whitebird.music.intent.playlist.change";
    public static final String INTENT_SHUFFLE_ALL = "com.whitebird.music.intent.shuffle.all";
    public static final String INTENT_CLEAR_QUQUE = "com.whitebird.music.intent.clear.queue";
    public static final String INTENT_MEDIA_CHANGE = "com.whitebird.music.intent.media.change";
    public static final String INTENT_ACTIVITY_CHANGE = "com.whitebird.music.intent.activity.change";

    public final static int ARTWORK_PICK_FROM_INTERNET = 7100;
    public final static int ARTWORK_PICK_FROM_GALLERY = 7200;
    public final static int ARTWORK_PICK_FROM_ALBUM_FOLDER = 7300;
    public final static int ARTWORK_PICK_FROM_ID3_TAGS = 7400;
    public static final int IMAGE_SMALL_SIZE = 100;
    public static final int IMAGE_MEDIUM_SIZE = 300;
    public static final int IMAGE_LARGE_SIZE = 800;
    public static final int MAX_ACTIVITY_ITEMS = 20;
    public static final int JPEG_COMPRESS = 40;
    public static final ArrayList<Song> multiTagList = new ArrayList<>();
    public static long SLEEP_TIMER = 0;
    public static long START_TIME = 0;
    public static boolean isActiveLast = false;
    public static ImageLoader loader;
    public static DisplayImageOptions OPTIONS = new DisplayImageOptions.Builder().cacheInMemory(true)
            .showImageOnLoading(R.mipmap.app_icon)
            .resetViewBeforeLoading(true).build();
    private static ProgressDialog dialog;
    private static MuzikoApp mInstance;
    private static int[] rates = null;
    public ImageLoaderConfiguration config;

    public static void spinnerStart(Context context, String text) {
        String pleaseWait = text;
        dialog = ProgressDialog.show(context, "", pleaseWait, true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }





    public static void updateProgress(String message) {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.setMessage(message);
            }
        }

    }
    public static void spinnerStop() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

    }


    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public static void checkTime() {
        if (PreferencesUtility.getInstance(mInstance).isTimer())
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (SLEEP_TIMER != 0 && System.currentTimeMillis() > SLEEP_TIMER) {

                        if (MusicPlayer.isPlaying()) {
                            if (PreferencesUtility.getInstance(mInstance).getSleepTimeLastSong() && !isActiveLast) {
                                SLEEP_TIMER = System.currentTimeMillis() + MusicPlayer.duration() - MusicPlayer.getSongProgressMillis();

                                isActiveLast = true;
                                checkTime();
                                return;
                            }
                            MusicPlayer.playOrPause();
                            SLEEP_TIMER = 0;
                            START_TIME = 0;
                            isActiveLast = false;
                            PreferencesUtility.getInstance(mInstance).setTimer(false);
                        } else
                            checkTime();
                    } else
                        checkTime();
                }
            }, 1000);


    }

    public static synchronized MuzikoApp getInstance() {
        return mInstance;
    }

    public static void writeUser(UserData userData) {
        try {
            String path = "/data/data/" + mInstance.getPackageName()
                    + "/userData.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(userData);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static UserData readUser() {
        String path = "/data/data/" + mInstance.getPackageName()
                + "/userData.ser";
        File f = new File(path);
        UserData userData = new UserData();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                userData = (UserData) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return userData;
    }


    public static void writeHome(ArrayList<Song> homeWrappers) {
        try {
            String path = "/data/data/" + mInstance.getPackageName()
                    + "/Home.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(homeWrappers);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Song> readHome() {
        String path = "/data/data/" + mInstance.getPackageName()
                + "/Home.ser";
        File f = new File(path);
        ArrayList<Song> homeWrappers = new ArrayList<>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                homeWrappers = (ArrayList<Song>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return homeWrappers;
    }

    public static long[] getSongIds(ArrayList<Song> data) {
        long[] ret = new long[data.size()];
        for (int i = 0; i < data.size(); i++) {
            ret[i] = data.get(i).id;
        }

        return ret;
    }

    public static void writeAllSong(HashMap<String, Song> allSong) {
        try {
            String path = "/data/data/" + mInstance.getPackageName()
                    + "/AllSong.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(allSong);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, Song> readAllSong() {
        String path = "/data/data/" + mInstance.getPackageName()
                + "/AllSong.ser";
        File f = new File(path);
        HashMap<String, Song> allSong = new HashMap<String, Song>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                allSong = (HashMap<String, Song>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return allSong;
    }

    public static void writeAllRingtong(HashMap<String, Song> allSong) {
        try {
            String path = "/data/data/" + mInstance.getPackageName()
                    + "/AllRingtong.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(allSong);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, Song> readAllRingtong() {
        String path = "/data/data/" + mInstance.getPackageName()
                + "/AllRingtong.ser";
        File f = new File(path);
        HashMap<String, Song> allSong = new HashMap<String, Song>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                allSong = (HashMap<String, Song>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return allSong;
    }


    public static void writeAllTime(HashMap<String, StartTimeWrapper> allSong) {
        try {
            String path = "/data/data/" + mInstance.getPackageName()
                    + "/AllTime.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(allSong);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, StartTimeWrapper> readAllTime() {
        String path = "/data/data/" + mInstance.getPackageName()
                + "/AllTime.ser";
        File f = new File(path);
        HashMap<String, StartTimeWrapper> allTime = new HashMap<String, StartTimeWrapper>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                allTime = (HashMap<String, StartTimeWrapper>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return allTime;
    }


    public static void writeAllPlayLists(ArrayList<Playlist> playList) {
        try {
            String path = "/data/data/" + mInstance.getPackageName()
                    + "/PlayList.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(playList);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void popMessage(final String titleMsg, final String errorMsg,
                                  final Activity context) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // pop error message
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(titleMsg).setMessage(errorMsg)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }

    public static ArrayList<Playlist> readAllPlayLists() {
        String path = "/data/data/" + mInstance.getPackageName()
                + "/PlayList.ser";
        File f = new File(path);
        ArrayList<Playlist> allSong = new ArrayList<Playlist>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                allSong = (ArrayList<Playlist>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return allSong;
    }

    public static int getDisplayWidth() {
        WindowManager wm = (WindowManager) mInstance
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    public static int getDisplayHeight() {
        WindowManager wm = (WindowManager) mInstance
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        return height;
    }

    public static void toast(Context context, String message) {

        try {


            new StyleableToast
                    .Builder(context)
                    .text(message)
                    .textColor(Color.WHITE)

                    .backgroundColor(Config.primaryColor(context, Helpers.getATEKey(context)))
                    .show();

//            styleableToast.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void writeActivity(ArrayList<Song> playList) {
        try {
            String path = "/data/data/" + mInstance.getPackageName()
                    + "/Activity.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(playList);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Song> readAllActivity() {
        String path = "/data/data/" + mInstance.getPackageName()
                + "/Activity.ser";
        File f = new File(path);
        ArrayList<Song> allSong = new ArrayList<Song>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                allSong = (ArrayList<Song>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return allSong;
    }


    public int NoOfColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 180);
        return noOfColumns;
    }

    public static void writeIgnoreList(ArrayList<Song> IgnoreList) {
        try {
            String path = "/data/data/" + mInstance.getPackageName()
                    + "/Ignore.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(IgnoreList);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Song> readIgnoreList() {
        String path = "/data/data/" + mInstance.getPackageName()
                + "/Ignore.ser";
        File f = new File(path);
        ArrayList<Song> IgnoreList = new ArrayList<Song>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                IgnoreList = (ArrayList<Song>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return IgnoreList;
    }


    public static void writeFavoriteList(ArrayList<Song> IgnoreList) {
        try {
            String path = "/data/data/" + mInstance.getPackageName()
                    + "/favorite.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(IgnoreList);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Song> readFavoriteList() {
        String path = "/data/data/" + mInstance.getPackageName()
                + "/favorite.ser";
        File f = new File(path);
        ArrayList<Song> IgnoreList = new ArrayList<Song>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                IgnoreList = (ArrayList<Song>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return IgnoreList;
    }





    public static void savePlayingQueue(ArrayList<Song> playingQueue) {
        try {
            String path = "/data/data/" + mInstance.getPackageName()
                    + "/playingQueue.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(playingQueue);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void saveOriginalQueue(ArrayList<Song> originalQueue) {
        try {
            String path = "/data/data/" + mInstance.getPackageName()
                    + "/originalQueue.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(originalQueue);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Song> getSavedPlayingQueue() {
        String path = "/data/data/" + mInstance.getPackageName()
                + "/playingQueue.ser";
        File f = new File(path);
        ArrayList<Song> queueList = new ArrayList<Song>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                queueList = (ArrayList<Song>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return queueList;
    }

    public static ArrayList<Song> getSavedOriginalPlayingQueue() {
        String path = "/data/data/" + mInstance.getPackageName()
                + "/originalQueue.ser";
        File f = new File(path);
        ArrayList<Song> queueList = new ArrayList<Song>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                queueList = (ArrayList<Song>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return queueList;
    }
    public static boolean isConnectingToInternet( ) {
        ConnectivityManager cm = (ConnectivityManager) mInstance.getSystemService(mInstance.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
    public static void makeShuffleList(@NonNull List<Song> listToShuffle, final int current) {
        if (listToShuffle.isEmpty()) return;
        if (current >= 0) {
            Song song = listToShuffle.remove(current);
            Collections.shuffle(listToShuffle);
            listToShuffle.add(0, song);
        } else {
            Collections.shuffle(listToShuffle);
        }
    }
    public static void details(final Activity context, final Song queueItem) {

    /*    if (queue.storage != 0 && queue.storage != 1 && queue.storage != 2) {
            isCloud = true;
            progressDialog = new MaterialDialog.Builder(context)
                    .title("Reading track data")
                    .content("Please wait")
                    .cancelable(false)
                    .progress(true, 0)
                    .build();
            progressDialog.show();
        }*/


        final File file = new File(queueItem.data);

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MediaExtractor mediaExtractor = new MediaExtractor();
                try {
                    mediaExtractor.setDataSource(queueItem.data);
                    int numTracks = mediaExtractor.getTrackCount();
                    for (int i = 0; i < numTracks; i++) {
                        MediaFormat format = mediaExtractor.getTrackFormat(i);
                        if (format == null) continue;

                        String mime = format.getString(MediaFormat.KEY_MIME);

                        int bitRate = format.containsKey(android.media.MediaFormat.KEY_BIT_RATE) ? format.getInteger(MediaFormat.KEY_BIT_RATE) : 0;
                        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                        long duration = format.containsKey(android.media.MediaFormat.KEY_DURATION) ? format.getLong(android.media.MediaFormat.KEY_DURATION) / 1000 : 0;
                        rates = new int[]{bitRate, sampleRate, (int) duration};
                    }
                    mediaExtractor.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final boolean result = true;
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int duration = 0;
                        if (rates != null) {
//        int duration = Utils.getInt(queue.duration, 0);
                            duration = rates[2];

                            if (duration == 0) {
                                int bitrate = rates[0] / 1000;
                                duration = (int) ((queueItem.duration * 8 / 1000) / bitrate) * 1000;
                            }
                        }

                        String msg = msg = "<b>Title</b>:  " + queueItem.title;
                        msg += "<br/><br/><b>Artist</b>:  " + queueItem.artistName;
                        msg += "<br/><br/><b>Album</b>: " + queueItem.albumName;
                        msg += "<br/><br/><b>Genre</b>: " + queueItem.genre_name;
                        msg += "<br/><br/><b>Duration</b>: " + Utils.getDuration(duration);
                        msg += "<br/><br/><b>Size</b>: " + Utils.getByteFormat(file.length());

                        if (rates != null) {
                            msg += "<br/><br/><b>Bitrate</b>: " + String.format(Locale.ENGLISH, "%dkbps", rates[0] / 1000);
                            msg += "<br/><br/><b>Sampling rate</b>: " + String.format(Locale.ENGLISH, "%dHz", rates[1]);
                        }
                     /*   if (queueItem.md5 == null) {
                            queueItem.md5 = MD5.calculateMD5(new File(queueItem.data));
                            TrackRealmHelper.updateMD5Hash(queueItem);
                        }
                        msg += "<br/><br/><b>MD5</b>: " + queueItem.md5;*/

                        msg += "<br/><br/><b>Path</b>: " + queueItem.data + "\n\n";
                        final String message = msg;

                        //msg += "\n\nTrack: " + queue.track;
                        //msg += "\n\nYear: " + queue.year;
                        new MaterialDialog.Builder(context)
                                .theme(Theme.LIGHT)
                                .negativeColor(Config.accentColor(context, Helpers.getATEKey(context)))
                                .titleColor(Config.accentColor(context, Helpers.getATEKey(context)))
                                .neutralColorRes(R.color.dialog_negetive_button)
                                .positiveColor(Config.accentColor(context, Helpers.getATEKey(context)))
                                .title("Details")
                                .content(Utils.fromHtml(message))
                                .positiveText("OK")

                                /*.neutralText("Go to Path")
                                .onNeutral(
                                        new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    Intent folderIntent = new Intent(context, StorageActivity.class);
                                                    folderIntent.putExtra(MyApplication.ARG_DATA, queue.data);
                                                    ActivityCompat.startActivity(context, folderIntent, null);
                                            }
                                        } )*/
                                .show();
                    }
                });

            }
        });

    }

    public static void moveToIgnore(final ArrayList<Song> songs, final Activity mContext) {
        new MaterialDialog.Builder(mContext)
                .title("Move to Ignore Folder")
                .content("You can later restore it from ignore folder,Are yo sure you want to ignore it?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ArrayList<Song> data = readIgnoreList();
                        data.addAll(songs);
                        writeIgnoreList(data);
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<Song> activity = readAllActivity();
                                for (Song song : songs) {
                                    for (int i = 0; i < activity.size(); i++) {
                                        if (activity.get(i).id == song.id) {
                                            activity.remove(i);
                                            i--;
                                        }
                                    }

                                }
                                writeActivity(activity);


                                new SongLoaderNew(mContext).execute();
                                // mContext.sendBroadcast(new Intent(MuzikoApp.INTENT_MEDIA_CHANGE));
                                mContext.getContentResolver().notifyChange(Uri.parse("content://media"), null);
                                MusicPlayer.refresh();
                            }
                        });
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }


    public static void moveToFavorite(final ArrayList<Song> songs, final Activity mContext) {

        ArrayList<Song> data = readFavoriteList();

        if (data.size()==0){
            data.addAll(songs);
        }else {
            for (int i = 0; i < songs.size(); i++) {
                boolean isFound = false;
                for (int j = 0; j < data.size(); j++) {
                    if (data.get(j).id == songs.get(i).id) {
                        isFound = true;
                    }

                }
                if (!isFound) {
                    data.add(songs.get(i));
                }
            }
        }

        writeFavoriteList(data);

    }


    public static void removeFromFavorite(final ArrayList<Song> songs) {

        ArrayList<Song> data = readFavoriteList();
        for (Song song : songs) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).id == song.id) {
                    data.remove(i);
                    i--;
                }
            }
        }

        writeFavoriteList(data);

    }

    public static void removeFromIgnore(final ArrayList<Song> songs, final Activity mContext) {

        new MaterialDialog.Builder(mContext)
                .title("Restore Song")
                .content(" Are yo sure you want to restore ?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MuzikoApp.spinnerStart(mContext, "Restoring song .....");

                            }
                        });

                        ArrayList<Song> data = MuzikoApp.readIgnoreList();
                        for (Song song : songs) {
                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).id == song.id) {
                                    data.remove(i);
                                    i--;
                                }
                            }
                        }
                        MuzikoApp.writeIgnoreList(data);

                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                new SongLoaderNew(mContext).execute();
                                // mContext.sendBroadcast(new Intent(MuzikoApp.INTENT_MEDIA_CHANGE));
                                mContext.getContentResolver().notifyChange(Uri.parse("content://media"), null);
                                MusicPlayer.refresh();
                                MuzikoApp.spinnerStop();
                            }
                        });
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }



    public static void removeFromIgnoreDelete(final ArrayList<Song> songs, final Activity mContext) {


        ArrayList<Song> data = MuzikoApp.readIgnoreList();
        for (Song song : songs) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).id == song.id) {
                    data.remove(i);
                    i--;
                }
            }
        }
        MuzikoApp.writeIgnoreList(data);


    }

    public void shareSongs(Activity activity, ArrayList<Song> queuelist) {
        try {
            ArrayList<Uri> arrayUri = new ArrayList<>();
            for (Song queue : queuelist) {
                arrayUri.add(Uri.parse("file://" + queue.data));
            }

            Intent intentShareFile = new Intent();
            intentShareFile.setAction(Intent.ACTION_SEND_MULTIPLE);
            intentShareFile.putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayUri);
            intentShareFile.setType("audio/*");
            activity.startActivity(intentShareFile);
        } catch (Exception ex) {
            toast(activity, "Error sharing file!");

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

      /*  ImagePipelineConfig config =
                ImagePipelineConfig.newBuilder(mInstance).setDownsampleEnabled(true).build();
        Fresco.initialize(mInstance, config);*/


        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ImageLoaderConfiguration localImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(this).imageDownloader(new BaseImageDownloader(this) {
            PreferencesUtility prefs = PreferencesUtility.getInstance(MuzikoApp.this);

            @Override
            protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
                if (prefs.loadArtistAndAlbumImages())
                    return super.getStreamFromNetwork(imageUri, extra);
                throw new IOException();
            }
        }).build();


        L.writeLogs(false);
        L.disableLogging();
        L.writeDebugLogs(false);

        loader = ImageLoader.getInstance();
//        loader.init(ImageLoaderConfiguration.createDefault(this));
        setImageLoaderConfig();

        Nammu.init(this);

        if (!ATE.config(this, "light_theme").isConfigured()) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppThemeLight)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .coloredNavigationBar(false)
                    .lightToolbarMode(Config.LIGHT_TOOLBAR_AUTO)
                    .usingMaterialDialogs(true)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme").isConfigured()) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppThemeDark)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .coloredNavigationBar(false)
                    .usingMaterialDialogs(true)
                    .commit();
        }
        if (!ATE.config(this, "light_theme_notoolbar").isConfigured()) {
            ATE.config(this, "light_theme_notoolbar")
                    .activityTheme(R.style.AppThemeLight)
                    .coloredActionBar(false)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .lightToolbarMode(Config.LIGHT_TOOLBAR_AUTO)
                    .coloredNavigationBar(false)
                    .usingMaterialDialogs(true)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme_notoolbar").isConfigured()) {
            ATE.config(this, "dark_theme_notoolbar")
                    .activityTheme(R.style.AppThemeDark)
                    .coloredActionBar(false)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .coloredNavigationBar(true)
                    .usingMaterialDialogs(true)
                    .commit();
        }


    }

    public void setImageLoaderConfig() {

        config = new ImageLoaderConfiguration.Builder(getApplicationContext())

                .memoryCacheSize(50 * 1024 * 1024)

                // 20 Mb
                .memoryCache(new LruMemoryCache(50 * 1024 * 1024))
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();

        // Initialize ImageLoader with configuration.
        loader.init(config);
    }

    public void cutSong(Song queue) {
        try {

            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(queue.data));
            intent.putExtra("SONG_NAME", queue.title);
            intent.putExtra("was_get_content_intent", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(
                    "com.whitebird.music", "com.whitebird.music.cutter.ringtone_lib.RingdroidEditActivity");
            mInstance.startActivity(intent);

        } catch (Exception ex) {
            System.out.println("Could not start editor");

            toast(mInstance, "Unable to start mp3 edit!");

        }
    }
}
