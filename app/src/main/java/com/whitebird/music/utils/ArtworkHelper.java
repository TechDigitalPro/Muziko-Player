package com.whitebird.music.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.whitebird.music.MuzikoApp;
import com.whitebird.music.dataloaders.DataHelper;
import com.whitebird.music.dataloaders.SongLoaderNew;
import com.whitebird.music.models.Song;
import com.whitebird.music.tageditor.metadata.ImageData;
import com.whitebird.music.tageditor.metadata.MusicMetadata;
import com.whitebird.music.tageditor.metadata.MusicMetadataSet;
import com.whitebird.music.tageditor.myid3.MyID3;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.util.Vector;

public class ArtworkHelper {


    public void setArt(Context context, Song queueItem, byte[] bitmapData) {

        for (Song queueItemAlbumid : DataHelper.getSongsForAlbum(queueItem.albumName)) {
            MediaStoreHelper.updateCoverArt(context, bitmapData, queueItemAlbumid);
        }

        MediaStoreHelper.updateCoverArt(context, bitmapData, queueItem);
        MemoryCacheUtils.removeFromCache(MuzikoUtils.getAlbumArtUri(queueItem.albumId).toString(),  MuzikoApp.loader.getMemoryCache());
        DiskCacheUtils.removeFromCache(MuzikoUtils.getAlbumArtUri(queueItem.albumId).toString(),  MuzikoApp.loader.getDiskCache());
        new SongLoaderNew(context).execute();

        context.sendBroadcast(new Intent(MuzikoApp.INTENT_MEDIA_CHANGE));

    }




    public void removeArt(Context context, Song queueItem) {

        for (Song queueItemAlbumid : DataHelper.getSongsForAlbum(queueItem.albumName)) {
            MediaStoreHelper.removeCoverArt(context, queueItemAlbumid.albumId);
        }

       // MediaStoreHelper.updateCoverArt(context, bitmapData, queueItem);
        MemoryCacheUtils.removeFromCache(MuzikoUtils.getAlbumArtUri(queueItem.albumId).toString(),  MuzikoApp.loader.getMemoryCache());
        DiskCacheUtils.removeFromCache(MuzikoUtils.getAlbumArtUri(queueItem.albumId).toString(),  MuzikoApp.loader.getDiskCache());
        new SongLoaderNew(context).execute();

        context.sendBroadcast(new Intent(MuzikoApp.INTENT_MEDIA_CHANGE));

    }
    public byte[] pickAlbumArt(final Context context, final Song queueItem) {


        MyID3 id3 = new MyID3();
        MusicMetadataSet dataset = null;
        MusicMetadata metadata = null;

        try {
            File from = new File(queueItem.data);
            dataset = id3.read(from);      //read metadata
        } catch (Exception e) {
           e.printStackTrace();
        }

        if (dataset == null) {
            MuzikoApp.toast(context, "Unable to read song metadata!");
            return null;
        }

        metadata = (MusicMetadata) dataset.getSimplified();
        if (metadata == null) {
            MuzikoApp.toast(context, "Unable to read song tags!");
            return null;
        }

        if (metadata != null) {

            Vector converart = metadata.getPictureList();

            if (converart.size() > 0) {
                byte[] bitmapData = ((ImageData) converart.get(0)).imageData;

                Bitmap bm = Utils.decodeBitmapArray(bitmapData, MuzikoApp.IMAGE_SMALL_SIZE, MuzikoApp.IMAGE_SMALL_SIZE);
                if (bm != null) {

                    return bitmapData;

                } else {
                    MuzikoApp.toast(context, "Album Art not found");
                    return null;
                }
            }
        }

        MuzikoApp.toast(context, "Album Art not found");
        return null;
    }
}
