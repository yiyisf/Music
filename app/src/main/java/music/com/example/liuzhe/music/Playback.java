package music.com.example.liuzhe.music;

import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import music.com.example.liuzhe.music.util.GetSongsUtil;
import music.com.example.liuzhe.music.util.MediaIDHelper;

/**
 * Created by liuzhe on 2016/6/8.
 */
public class Playback implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener {

    private static final String TAG ="Playback";

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce the volume instead of stopping playback.
    public static final float VOLUME_DUCK = 0.2f;
    // The volume we set the media player when we have audio focus.
    public static final float VOLUME_NORMAL = 1.0f;

    private final MusicService mMusicService;
    private final MusicProvider mMusicProvider;
    private boolean mPlayOnFocusGain;
    private Callback mCallback;
    private int mState;
    private long currentStreamPosition;
    private MediaPlayer mMediaPlayer;
    private volatile int mCurrentPosition;
    private volatile String mCurrentMediaId;


    public void setState(int mState) {
        this.mState = mState;
    }

    public void setCallback(Callback mCallback) {
        this.mCallback = mCallback;
    }

    public Playback(MusicService musicService, MusicProvider provider){
        this.mMusicProvider = provider;
        this.mMusicService = musicService;
    }

    public void start() {

    }

    public boolean isConnected() {
        return true;
    }

    public long getCurrentStreamPosition() {
        return mMediaPlayer!= null ?
                mMediaPlayer.getCurrentPosition() : mCurrentPosition;

    }

    public int getState() {
        return mState;
    }

    public void play(MediaSessionCompat.QueueItem queueItem, String mCurrentPlayType) {
        mPlayOnFocusGain = true;
//        tryToGetAudioFocus();
//        registerAudioNoisyReceiver();
        String mediaId = queueItem.getDescription().getMediaId();
        Log.i(TAG, "get media id is :" + mediaId);
        boolean mediaHasChanged = !TextUtils.equals(mediaId, mCurrentMediaId);
        if (mediaHasChanged) {
            mCurrentPosition = 0;
            mCurrentMediaId = mediaId;
        }
        //未更换曲目
        if (mState == PlaybackState.STATE_PAUSED && !mediaHasChanged && mMediaPlayer != null) {
            configMediaPlayerState();
        } else { //更换曲目
            mState = PlaybackStateCompat.STATE_STOPPED;
            relaxResources(false); // release everything except MediaPlayer
            MediaMetadataCompat track = mMusicProvider.getMusic(
                    MediaIDHelper.extractMusicIDFromMediaID(queueItem.getDescription().getMediaId()));
            Log.i(TAG, "provider retur track is null?" + track.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));

            String source = null;
            source = track.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);

            if(mCurrentPlayType.equals(MusicService.CHINESE_MUSIC)){
//                source = GetSongsUtil.getInstance().getPlayUrl(source);
                GetSongsUtil.getInstance().getPlayUrl(source, new GetSongsUtil.SongUtilCallBack() {
                    @Override
                    public void fetchCount(Integer integer) {

                    }

                    @Override
                    public void getchUrl(String s) {
                        startPlay(s);
                    }
                });
            }else {
                startPlay(source);
            }
        }
    }

    private void startPlay(String source) {
        try {
            Log.i(TAG, "加载播放器");
            createMediaPlayerIfNeeded();

            mState = PlaybackStateCompat.STATE_BUFFERING;

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(source);
            Log.i(TAG, "加载音乐前");

            // Starts preparing the media player in the background. When
            // it's done, it will call our OnPreparedListener (that is,
            // the onPrepared() method on this class, since we set the
            // listener to 'this'). Until the media player is prepared,
            // we *cannot* call start() on it!
            mMediaPlayer.prepareAsync();

            Log.i(TAG, "加载音乐后");
            // If we are streaming from the internet, we want to hold a
            // Wifi lock, which prevents the Wifi radio from going to
            // sleep while the song is playing.
//                mWifiLock.acquire();

            if (mCallback != null) {
                mCallback.onPlaybackStatusChanged(mState);
            }

        } catch (IOException ex) {
            Log.e(TAG, ex + "Exception playing song");
            if (mCallback != null) {
                mCallback.onError(ex.getMessage());
            }
        }
    }

    private void createMediaPlayerIfNeeded() {
        if(mMediaPlayer == null){
            mMediaPlayer = new MediaPlayer();
            //关屏幕后仍可继续播放
            mMediaPlayer.setWakeMode(mMusicService.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
        }else {
            mMediaPlayer.reset();
        }
    }

    private void relaxResources(boolean releasePlayer) {
        mMusicService.stopForeground(true);

        if(releasePlayer && mMediaPlayer!= null){
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer =null;
        }
    }

    private void configMediaPlayerState() {
        // If we don't have audio focus and can't duck, we have to pause,
        if (mState == PlaybackStateCompat.STATE_PLAYING) {
            pause();
        }
        if (mPlayOnFocusGain) {
            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                Log.i(TAG,"configMediaPlayerState startMediaPlayer. seeking to " +
                        mCurrentPosition);
                if (mCurrentPosition == mMediaPlayer.getCurrentPosition()) {
                    mMediaPlayer.start();
                    mState = PlaybackStateCompat.STATE_PLAYING;
                } else {
                    mMediaPlayer.seekTo(mCurrentPosition);
                    mState = PlaybackStateCompat.STATE_BUFFERING;
                }
            }
            mPlayOnFocusGain = false;
        }
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
    }

    public void pause() {
        if(mState == PlaybackStateCompat.STATE_PLAYING){
            if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
                mCurrentPosition = mMediaPlayer.getCurrentPosition();
            }

            mState = PlaybackStateCompat.STATE_PAUSED;
            if(mCallback!=null){
                mCallback.onPlaybackStatusChanged(mState);
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "准备数据...." + mp.isPlaying());
        configMediaPlayerState();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mCallback != null) {
            mCallback.onCompletion();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "播放错误");
        if(mCallback!=null){
            mCallback.onError("播放错误" + what + "(" + extra +")");
        }
        return true;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public boolean isPlaying() {
        return mPlayOnFocusGain || (mMediaPlayer!=null && mMediaPlayer.isPlaying());
    }
}
