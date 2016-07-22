package music.com.example.liuzhe.music.Interface;

import music.com.example.liuzhe.music.model.Song;

/**
 * Created by liuzhe on 16/7/18.
 */
public interface PlayControlHelper {
    Song getPlayingSong();

    void PlaySong(Song song);
}
