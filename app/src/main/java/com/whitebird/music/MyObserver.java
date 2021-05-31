package com.whitebird.music;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.whitebird.music.dataloaders.SongLoaderNew;

public class MyObserver extends ContentObserver {
    Context context;

    public MyObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.e(">>>>>>>>>>>", "<<<<<<<<<<<<<<<<>>>>>>>>>>");

        new SongLoaderNew(context).execute();

        context.sendBroadcast(new Intent(MuzikoApp.INTENT_MEDIA_CHANGE));
    }
}
