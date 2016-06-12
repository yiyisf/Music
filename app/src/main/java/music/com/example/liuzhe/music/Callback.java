package music.com.example.liuzhe.music;

/**
 * Created by liuzhe on 2016/6/8.
 */
public interface Callback {

    /**
     *  当前音乐播放完成.
     */
    void onCompletion();
    /**
     * on Playback status changed
     * Implementations can use this callback to update
     * playback state on the media sessions.
     */
    void onPlaybackStatusChanged(int state);

    /**
     * @param error to be added to the PlaybackState
     */
    void onError(String error);
}
