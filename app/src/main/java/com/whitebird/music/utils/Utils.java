package com.whitebird.music.utils;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.view.ViewGroup;


import com.whitebird.music.models.Song;

import org.apache.commons.io.FilenameUtils;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    private final static String[] genreNameColumns = {MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME};

    public static String getDuration(long milliseconds) {
        long sec = (milliseconds / 1000) % 60;
        long min = (milliseconds / (60 * 1000)) % 60;
        long hour = milliseconds / (60 * 60 * 1000);

        String s = (sec < 10) ? "0" + sec : "" + sec;
        String m = (min < 10) ? "0" + min : "" + min;
        String h = "" + hour;

        String time = "";
        if (hour > 0) {
            time = h + ":" + m + ":" + s;
        } else {
            time = m + ":" + s;
        }
        return time;
    }

    public static Spanned fromHtml(String source) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public static String getByteFormat(long data) {

        if ((float) data >= 1024 * 1024 * 1024)
            return String.format("%.2f ", (float) data / (1024 * 1024 * 1024)) + "GB";
        else if ((float) data >= 1024 * 1024)
            return String.format("%.2f ", (float) data / (1024 * 1024)) + "MB";
        else if ((float) data >= 1024)
            return String.format("%.2f ", (float) data / 1024) + "KB";
        else
            return String.format("%d ", data) + "B";
    }
    public static Bitmap decodeBitmapArray(byte[] data, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }


    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
    public static String getPath(Context context, Uri uri) {
        try {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                String[] projection = {"_data"};
                Cursor cursor = null;

                try {
                    cursor = context.getContentResolver().query(uri, projection, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index);
                    }
                    cursor.close();
                } catch (Exception e) {
                    // Eat it
                }
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public static boolean isServiceRunning(String serviceName, Context context) {

        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceName.equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
//        int childCount = viewGroup.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View view = viewGroup.getChildAt(i);
//            view.setEnabled(enabled);
//            if (view instanceof ViewGroup) {
//                enableDisableViewGroup((ViewGroup) view, enabled);
//            }
//        }
//
//        viewGroup.setAlpha(enabled ? 1.0f : 0.75f);

    }
    public static String convertMillisecondstoDuration(long seconds) {

        seconds = seconds / 1000;

        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static String toHtml(Spanned source) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.toHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.toHtml(source);
        }
    }



    public static int getInt(String input, int def) {
        int val = def;
        try {
            val = Integer.parseInt(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return val;
    }

    public static String trimString(String str, int count) {
        String string = str;

        if (string.length() > count) {
            string = string.substring(0, count) + "...";
        }

        return string;
    }

    public static String getLongDuration(long milliseconds) {
        String message = "";

        if (milliseconds >= 1000) {
            int seconds = (int) (milliseconds / 1000) % 60;
            int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
            int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
            int days = (int) (milliseconds / (1000 * 60 * 60 * 24));
            if ((days == 0) && (hours != 0)) {
                message = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            } else if ((hours == 0) && (minutes != 0)) {
                message = String.format("%02d:%02d", minutes, seconds);
            } else if ((days == 0) && (hours == 0) && (minutes == 0)) {
                message = String.format("%d second%s", seconds, seconds != 1 ? "s" : "");
            } else {
                message = String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
            }
        } else {
            message = "0 seconds";
        }
        return message;
    }
   /* public static DocumentFile getDocumentFile(final File file, final boolean isDirectory) {
        String baseFolder = getExtSdCardFolder(file);

        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            relativePath = fullPath.substring(baseFolder.length() + 1);
        } catch (IOException e) {
            return null;
        }

        Uri treeUri = Uri.parse(PrefsManager.Instance().getStoragePermsURi());

        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(MuzikoApp.getInstance().getApplicationContext(), treeUri);

        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("audio", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }*/


    public static void updateSongTags(Context context, Song queueItem) {
        ContentResolver res = context.getContentResolver();
        long genreId = 0;
        String genreName = null;

        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                queueItem.id);

        long current = System.currentTimeMillis();

        ContentValues values = new ContentValues();
//		values.put(MediaStore.Audio.Media._ID, queueItem.id);
//		values.put(MediaStore.Audio.Media.DATA, queueItem.data);
//		values.put(MediaStore.Audio.Media.ALBUM_ID, queueItem.album);
//		values.put(MediaStore.Audio.Media.ARTIST_ID, queueItem.artist);
        values.put(MediaStore.Audio.Media.TITLE, queueItem.title);
        values.put(MediaStore.Audio.Media.ARTIST, queueItem.artistName);
        values.put(MediaStore.Audio.Media.ALBUM, queueItem.albumName);
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.DATE_MODIFIED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.TRACK, queueItem.trackNumber);

        if (queueItem.year > 0) {
            String year = String.valueOf(queueItem.year);
            values.put(MediaStore.Audio.Media.YEAR, year);
        }
        // Insert it into the database
        res.update(uri, values, null, null);

//		if (newUri == null) {
//
//			Utils.toast(context, "There was a problem updating tags");
//
//		}
    }
    public static long checkGenre(Context context, Song queueItem) {
        ContentResolver res = context.getContentResolver();
        long genreId = 0;
        String genreName = null;

        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                queueItem.id);

        long current = System.currentTimeMillis();


        Uri audioGenresUri = MediaStore.Audio.Genres.getContentUriForAudioId("external", (int) queueItem.id);
        Cursor genresCursor = res.query(audioGenresUri,
                genreNameColumns, null, null, null);
        int genre_column_index = genresCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);


        if (genresCursor.moveToFirst()) {
            genreName = genresCursor.getString(genre_column_index);
        }

        HashMap<Long, String> genreList = getGenres(context);
        for (Map.Entry<Long, String> e : genreList.entrySet()) {
            long key = e.getKey();
            String value = e.getValue();

            if (value.equals(genreName)) {
                genreId = key;
            }
        }

        if (genreName != null) {
            if (!genreName.equals(queueItem.genre_name)) {

                // delete existing genre record
                res.delete(MediaStore.Audio.Genres.Members.getContentUri(
                        "external", genreId), MediaStore.Audio.Genres.Members.AUDIO_ID + " = " + queueItem.id, null);

            }

        }


        // check if correct genre exists
        if (genreList.containsValue(queueItem.genre_name)) {
            for (Map.Entry<Long, String> e : genreList.entrySet()) {
                long key = e.getKey();
                String value = e.getValue();

                if (value.equals(queueItem.genre_name)) {
                    genreId = key;
                }
            }
        } else {
            // create new genre
            ContentValues genreValues = new ContentValues();
            genreValues.put(MediaStore.Audio.Genres.NAME, queueItem.genre_name);
            Uri genreUri = res.insert(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, genreValues);
            if (genreUri != null) {
                genreId = ContentUris.parseId(genreUri);
            }

        }

        // add track to new genre

        ContentValues genreValues = new ContentValues();
        genreValues.put(MediaStore.Audio.Genres.Members.AUDIO_ID, queueItem.id);
        Uri membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId);
        res.insert(membersUri, genreValues);

        genresCursor.close();
        return genreId;
    }

    private static HashMap<Long, String> getGenres(Context context) {
        ContentResolver res = context.getContentResolver();

        HashMap<Long, String> genreNameMap = new HashMap<>();

        final String[] genreNameColumns = {MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME};
        Cursor cursor = res.query(MediaStore.Audio.Genres.getContentUri("external"), genreNameColumns, null, null, null);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            genreNameMap.put(cursor.getLong(0), cursor.getString(1));
        }
        cursor.close();

        return genreNameMap;
    }


    public static String isEditable(String path){
        return FilenameUtils.getExtension(path);

        }


}
