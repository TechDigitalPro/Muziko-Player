package com.whitebird.music.service.notification;


import com.whitebird.music.service.MusicService;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public interface PlayingNotification {
    int NOTIFICATION_ID = 1;

    void init(MusicService service);

    void update();

    void stop();
}
