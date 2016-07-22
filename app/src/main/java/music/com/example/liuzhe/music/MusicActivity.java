package music.com.example.liuzhe.music;

import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

import music.com.example.liuzhe.music.ui.PlaybackControlsFragment;
import music.com.example.liuzhe.music.util.NetworkHelper;

public class MusicActivity extends BaseActivity implements Thread.UncaughtExceptionHandler,
        BrowseFragment.FragmentDataHelper {

    private static final String TAG = "Main Activity";
    private static final String FRAGMENT_TAG = "DISCO container";
    private static final String FRAGMENT_TAG1 = "CH container";
    private static final String SAVED_MEDIA_ID = "music.com.example.liuzhe.music.MEDIA_ID";
    private static final String SAVED_MEDIA_TYPE = "music.com.example.liuzhe.music.MEDIA_TYPE";
    private MediaBrowserCompat mMediaBrowser;
    private PlaybackControlsFragment mControlsFragment;
    private String type;
    private MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {

                @Override
                public void onConnected() {
                    Log.i(TAG, "connect media browser success");
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Log.e(TAG, "could not connect media controller");
                        hidePlaybackControls();
                    }
                }

                @Override
                public void onConnectionSuspended() {
                    super.onConnectionSuspended();
                }

                @Override
                public void onConnectionFailed() {
                    super.onConnectionFailed();
                    Log.e(TAG, "coneect error");
                }
            };
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
                    super.onQueueChanged(queue);
                    Log.i(TAG, "Queue changed");
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    Log.i(TAG, "播放状态改变为： " + state);
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        hidePlaybackControls();
                    }
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    Log.i(TAG, "播放内容改变为： " + metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        hidePlaybackControls();
                    }
                }
            };



    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            FirebaseAuth.getInstance().signOut();
//        }
//
//        if (MusicApplication.getGoogleSignInAccount() != null && MusicApplication.getmGoogleApiClient().isConnected()) {
//            Auth.GoogleSignInApi.signOut(MusicApplication.getmGoogleApiClient());
//        }

    }

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaControllerCompat = new MediaControllerCompat(this, token);
        Log.i(TAG, "注册播放监听器");
        setSupportMediaController(mediaControllerCompat);
        mediaControllerCompat.registerCallback(mMediaControllerCallback);

        if (shouldShowControls()) {
            showPlaybackControls();
        } else {
            hidePlaybackControls();
        }

        if (mControlsFragment != null) {
            mControlsFragment.onConnected();
        }

        getBrowseFragment().onConnected();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i("主界面：","起来了");
//        checkLoginstatus();
        Log.i(TAG, "SAVED_INSTANCE is null" + String.valueOf(savedInstanceState==null));
        setContentView(R.layout.activity_main);

        initializeToolbar();

        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);

        initializeFromParams(savedInstanceState, getIntent());
        Log.i(TAG, "after create view");
    }


    protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
        String mediaId = null;
        type = MusicService.DISCO_MUSIC;
        // check if we were started from a "Play XYZ" voice search. If so, we save the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.

        if (savedInstanceState != null) {
            // If there is a saved media ID, use it
            mediaId = savedInstanceState.getString(SAVED_MEDIA_ID);
            type = savedInstanceState.getString(SAVED_MEDIA_TYPE);
        }
        navigateToBrowser(mediaId, type);
//        navigateToArtist();
    }

//    private void navigateToArtist() {
//        ArtistFragment fragment = getArtistFragment();
//        if(fragment == null){
//            fragment = new ArtistFragment();
//            FragmentTransaction transaction = getFragmentManager().beginTransaction();
//            transaction.setCustomAnimations(
//                    R.animator.slide_in_from_right, R.animator.slide_out_to_left,
//                    R.animator.slide_in_from_left, R.animator.slide_out_to_right);
//            transaction.replace(R.id.container, fragment, FRAGMENT_TAG);
//
//            transaction.commit();
//        }
//    }

    private void navigateToBrowser(String mediaId, String type) {
        Log.i(TAG, "to browser meids : " + type + mediaId);

//        if (type.equals(MusicService.DISCO_MUSIC)) {
//            fragment = getBrowseFragment();
//        }else {
//            fragment =
//        }


//        if (fragment == null ||
//                !TextUtils.equals(fragment.getMediaId(), mediaId) ||
//                !TextUtils.equals(fragment.getType(), type)) {
//        BrowseFragment fragment = getBrowseFragment();
        BrowseFragment fragment = new BrowseFragment();
        fragment.setMediaId(mediaId, type);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.animator.slide_in_from_right, R.animator.slide_out_to_left,
                R.animator.slide_in_from_left, R.animator.slide_out_to_right);
        transaction.replace(R.id.container, fragment, FRAGMENT_TAG);
//        if (mediaId != null) {
//            transaction.addToBackStack(null);
//        }
        transaction.commit();
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "save instance");
        outState.putString(SAVED_MEDIA_TYPE, type);
        outState.putString(SAVED_MEDIA_ID, null);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "restore instance");
        type = savedInstanceState.getString(SAVED_MEDIA_TYPE);
        super.onRestoreInstanceState(savedInstanceState);
//        type = savedInstanceState.getString(SAVED_MEDIA_TYPE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("主界面", "start了");
        mControlsFragment = (PlaybackControlsFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_playback_controls);

        if (mControlsFragment == null) {
            throw new IllegalArgumentException("Mising fragment with id 'controls'. Cannot continue.");
        }

        hidePlaybackControls();
        Log.i(TAG, "befor connect browser");

        mMediaBrowser.connect();
        Log.i(TAG, "after connect browser");

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "主界面stop");
        if (getSupportMediaController() != null) {
            getSupportMediaController().unregisterCallback(mMediaControllerCallback);
        }
        mMediaBrowser.disconnect();
    }


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
    }


    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {
        //浏览处理,继续使用Browserfragment
        if (item.isBrowsable()) {
            navigateToBrowser(item.getMediaId(), type);
        } else if (item.isPlayable()) {
            //直接播放，activity中已设置监听处理
            getSupportMediaController().getTransportControls().
                    playFromMediaId(item.getMediaId(), null);
        }
    }

    @Override
    public void setToolbarTitle(CharSequence title) {
        if (title == null) {
            title = getString(R.string.app_name);
        }
        Log.i(TAG, "set title is:" + title);
        setTitle(title);
    }

    protected void showPlaybackControls() {
        Log.i(TAG, "showPlaybackControls");
        if (NetworkHelper.isOnline(this)) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
                            R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
                    .show(mControlsFragment)
                    .commit();
        }
    }

    protected void hidePlaybackControls() {
        getFragmentManager().beginTransaction()
                .hide(mControlsFragment)
                .commit();
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected boolean shouldShowControls() {
        MediaControllerCompat mediaController = getSupportMediaController();
        Log.i(TAG, "播放控制器当前播放状态为： " + mediaController.getPlaybackState().getState());
        if (mediaController == null ||
                mediaController.getMetadata() == null ||
                mediaController.getPlaybackState() == null) {
            return false;
        }

        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_ERROR:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                return false;
            default:
                return true;
        }
    }

    public BrowseFragment getBrowseFragment() {
        return (BrowseFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    public BrowseFragment getBrowseChFragment() {
        return (BrowseFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG1);
    }

    @Override
    protected void onNavItemSelect(int selectItem) {
        switch (selectItem) {
            case R.id.disco:
                type = MusicService.DISCO_MUSIC;
//                navigateToBrowser(null, type);
                break;
            case R.id.Chinese:
                type = MusicService.CHINESE_MUSIC;
                Log.i(TAG, "处理华人歌手");
                break;
        }

        navigateToBrowser(null, type);

    }
}
