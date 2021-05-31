

package com.whitebird.music.lastfmapi.callbacks;

import com.whitebird.music.lastfmapi.models.LastfmAlbum;

public interface AlbumInfoListener {

    void albumInfoSuccess(LastfmAlbum album);

    void albumInfoFailed();

}
