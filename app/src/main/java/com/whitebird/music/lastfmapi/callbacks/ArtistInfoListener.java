

package com.whitebird.music.lastfmapi.callbacks;

import com.whitebird.music.lastfmapi.models.LastfmArtist;

public interface ArtistInfoListener {

    void artistInfoSucess(LastfmArtist artist);

    void artistInfoFailed();

}
