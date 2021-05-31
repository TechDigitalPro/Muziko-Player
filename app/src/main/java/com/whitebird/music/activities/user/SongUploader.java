package com.whitebird.music.activities.user;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.whitebird.music.MuzikoApp;
import com.whitebird.music.models.Song;
import com.whitebird.music.utils.MuzikoUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;

import static com.whitebird.music.utils.StaticData.ADD_MUSIC;
import static com.whitebird.music.utils.StaticData.BASE_URL;

public class SongUploader {


    public static void UploadSong(final Activity c, Song song, String loadingMsg/*, final ResponseCallback responseCallback*/, final int callNumber) {
        if (!MuzikoApp.readUser().IsLogin) {
            MuzikoApp.popMessage("", "Please login first!", c);
            return;
        }

        RequestParams params = new RequestParams();
        params.put("user_id", MuzikoApp.readUser().Id);
        params.put("FileName", song.name);
        try {
            params.put("imageArt", new File(MuzikoUtils.getAlbumArtUri(song.albumId).getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        params.put("AlbumName", song.albumName);
        params.put("ArtistName", song.artistName);
        params.put("GenerName", song.genre_name);
        params.put("song_id", song.id);
        try {
            File file = new File(song.data);
            params.put("file", file);
            params.put("size", file.length() / 1024);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        if (!MuzikoApp.isConnectingToInternet(c)) {
            Toast.makeText(c, "No internet connection !", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(loadingMsg))
            MuzikoApp.spinnerStart(c, loadingMsg);
        Log.e("URl:", BASE_URL + ADD_MUSIC);
        Log.e("Request:", params.toString());
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        //    client.addHeader("Content-Type","application/x-www-form-urlencoded");
        client.post(BASE_URL + ADD_MUSIC, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                MuzikoApp.spinnerStop();
                Toast.makeText(c, "Success", Toast.LENGTH_SHORT).show();
                // Log.d("Response:", response.toString());
                // responseCallback.onJsonObjectResponseReceived(response, callNumber);


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                MuzikoApp.spinnerStop();
                Log.d("error message:", throwable.getMessage());
                try {
                    //  responseCallback.onErrorReceived(errorResponse.toString());
                } catch (Exception e) {
                    Toast.makeText(c, "Slow internet connection ! \nPlease try again", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                MuzikoApp.spinnerStop();
                Log.d("error message:", throwable.getMessage());
                try {
                    // responseCallback.onErrorReceived(responseString);
                } catch (Exception e) {
                    Toast.makeText(c, "Slow internet connection ! \nPlease try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                long progressPercentage = (long) 100 * bytesWritten / totalSize;
                MuzikoApp.updateProgress("Uploading " + progressPercentage + " %");

            }



        });
    }

    public interface ResponseCallback {

        void onJsonObjectResponseReceived(JSONObject response, int callNumber);

        void onErrorReceived(String error);

    }


}
