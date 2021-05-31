

package com.whitebird.music.dataloaders;

import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.whitebird.music.MusicPlayer;

import java.util.Arrays;


public class NowPlayingCursor extends AbstractCursor {

    String[] PROJECTION = new String[]{"_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id",MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.DATE_ADDED,MediaStore.Audio.Media.DATE_MODIFIED,MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.TRACK,MediaStore.Audio.Media.YEAR};

    private final Context mContext;

    private long[] mNowPlaying;

    private long[] mCursorIndexes;

    private int mSize;

    private int mCurPos;

    private Cursor mQueueCursor;


    public NowPlayingCursor(final Context context) {
        mContext = context;
        makeNowPlayingCursor();
    }


    @Override
    public int getCount() {
        return mSize;
    }


    @Override
    public boolean onMove(final int oldPosition, final int newPosition) {
        if (oldPosition == newPosition) {
            return true;
        }
        if (mNowPlaying == null || mCursorIndexes == null || newPosition >= mNowPlaying.length) {
            return false;
        }
        final long id = mNowPlaying[newPosition];
        final int cursorIndex = Arrays.binarySearch(mCursorIndexes, id);
        mQueueCursor.moveToPosition(cursorIndex);
        mCurPos = newPosition;
        return true;
    }

    @Override
    public String getString(final int column) {
        try {
            return mQueueCursor.getString(column);
        } catch (final Exception ignored) {
            onChange(true);
            return "";
        }
    }


    @Override
    public short getShort(final int column) {
        return mQueueCursor.getShort(column);
    }


    @Override
    public int getInt(final int column) {
        try {
            return mQueueCursor.getInt(column);
        } catch (final Exception ignored) {
            onChange(true);
            return 0;
        }
    }


    @Override
    public long getLong(final int column) {
        try {
            return mQueueCursor.getLong(column);
        } catch (final Exception ignored) {
            onChange(true);
            return 0;
        }
    }


    @Override
    public float getFloat(final int column) {
        return mQueueCursor.getFloat(column);
    }


    @Override
    public double getDouble(final int column) {
        return mQueueCursor.getDouble(column);
    }


    @Override
    public int getType(final int column) {
        return mQueueCursor.getType(column);
    }

    @Override
    public boolean isNull(final int column) {
        return mQueueCursor.isNull(column);
    }


    @Override
    public String[] getColumnNames() {
        return PROJECTION;
    }


    @SuppressWarnings("deprecation")
    @Override
    public void deactivate() {
        if (mQueueCursor != null) {
            mQueueCursor.deactivate();
        }
    }

    @Override
    public boolean requery() {
        makeNowPlayingCursor();
        return true;
    }


    @Override
    public void close() {
        try {
            if (mQueueCursor != null) {
                mQueueCursor.close();
                mQueueCursor = null;
            }
        } catch (final Exception close) {
        }
        super.close();
    }


    private long[] getQueue(){
        long [] now=new long[MusicPlayer.getQueue().size()];
        for (int i=0;i<MusicPlayer.getQueue().size();i++){
            now[i]=MusicPlayer.getQueue().get(i).id;
        }
        return now;
    }
    private void makeNowPlayingCursor() {
        mQueueCursor = null;
        mNowPlaying =getQueue();
        Log.d("lol1", mNowPlaying.toString() + "   " + mNowPlaying.length);
        mSize = mNowPlaying.length;
        if (mSize == 0) {
            return;
        }

        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < mSize; i++) {
            selection.append(mNowPlaying[i]);
            if (i < mSize - 1) {
                selection.append(",");
            }
        }
        selection.append(")");

        mQueueCursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, selection.toString(),
                null, MediaStore.Audio.Media._ID);

        if (mQueueCursor == null) {
            mSize = 0;
            return;
        }

        final int playlistSize = mQueueCursor.getCount();
        mCursorIndexes = new long[playlistSize];
        mQueueCursor.moveToFirst();
        final int columnIndex = mQueueCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        for (int i = 0; i < playlistSize; i++) {
            mCursorIndexes[i] = mQueueCursor.getLong(columnIndex);
            mQueueCursor.moveToNext();
        }
        mQueueCursor.moveToFirst();
        mCurPos = -1;

        int removed = 0;
        for (int i = mNowPlaying.length - 1; i >= 0; i--) {
            final long trackId = mNowPlaying[i];
            final int cursorIndex = Arrays.binarySearch(mCursorIndexes, trackId);
            if (cursorIndex < 0) {
                removed += MusicPlayer.removeTrack(DataHelper.getSong(trackId));
            }
        }
        if (removed > 0) {
            mNowPlaying = getQueue();
            mSize = mNowPlaying.length;
            if (mSize == 0) {
                mCursorIndexes = null;
                return;
            }
        }
    }


/*    public boolean removeItem(final int which) {
        try {
            if (mService.removeTracks(which, which) == 0) {
                return false;
            }
            int i = which;
            mSize--;
            while (i < mSize) {
                mNowPlaying[i] = mNowPlaying[i + 1];
                i++;
            }
            onMove(-1, mCurPos);
        } catch (final RemoteException ignored) {
        }
        return true;
    }*/
}
