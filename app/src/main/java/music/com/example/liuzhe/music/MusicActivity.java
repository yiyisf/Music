package music.com.example.liuzhe.music;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import music.com.example.liuzhe.music.ui.PlaybackControlsFragment;

public class MusicActivity extends AppCompatActivity implements Thread.UncaughtExceptionHandler,
        BrowseFragment.FragmentDataHelper {

    private static final String TAG = "Main Activity";
    private static final String FRAGMENT_TAG = "container";
    private static final java.lang.String SAVED_MEDIA_ID = "music.com.example.liuzhe.music.MEDIA_ID";
    private MediaBrowserCompat mMediaBrowser;
    private PlaybackControlsFragment mControlsFragment;
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
                    Log.i(TAG, "播放状态改变为： "+ state);
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        hidePlaybackControls();
                    }
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    Log.i(TAG, "播放内容改变为： "+ metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        hidePlaybackControls();
                    }
                }
            };
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
//            FirebaseAuth.getInstance().signOut();
//        }
//
//        if(MusicApplication.getGoogleSignInAccount()!=null && MusicApplication.getmGoogleApiClient().isConnected()){
//            Auth.GoogleSignInApi.signOut(MusicApplication.getmGoogleApiClient());
//        }
    }

    private DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {

        }

        @Override
        public void onDrawerClosed(View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };
    private FragmentManager.OnBackStackChangedListener mBackStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            updateDrawerToggle();
        }
    };
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

        if(mControlsFragment!= null){
            mControlsFragment.onConnected();
        }

        getBrowseFragment().onConnected();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.i("主界面：","起来了");
//        checkLoginstatus();
        setContentView(R.layout.activity_main);
        initializeToolbar();

        mMediaBrowser = new MediaBrowserCompat(this,
                    new ComponentName(this, MusicService.class), mConnectionCallback, null);

        initializeFromParams(savedInstanceState, getIntent());
        Log.i(TAG, "after create view");
    }

    private void checkLoginstatus() {
        gso = MusicApplication.getGoogleSignInOptions();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            GoogleSignInAccount googleAccount = pendingResult.get().getSignInAccount();
            Toast.makeText(this, "已登录google user：" + googleAccount.getDisplayName(), Toast.LENGTH_SHORT).show();
            MusicApplication.setGoogleSignInAccount(googleAccount);
            startLogin();
        } else {
            Toast.makeText(this, "可使用google登录：", Toast.LENGTH_SHORT).show();
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
//            Toast.makeText(this, "已登录firebase user：" + auth.getAuth().get("token"), Toast.LENGTH_SHORT).show();
            startLogin();
        }
    }

    private void startLogin() {
        Intent i = new Intent(MusicActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void initializeToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new IllegalStateException("Layout is required to include a Toolbar with id " +
                    "'toolbar'");
        }
//        mToolbar.inflateMenu(R.menu.main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(mDrawerLayout!=null){
            // Create an ActionBarDrawerToggle that will handle opening/closing of the drawer:
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    mToolbar, R.string.open_content_drawer, R.string.close_content_drawer);
            mDrawerLayout.setDrawerListener(mDrawerListener);
//            populateDrawerItems(navigationView);
            setSupportActionBar(mToolbar);
            updateDrawerToggle();
        }else {
            setSupportActionBar(mToolbar);
        }

    }

    private void updateDrawerToggle() {
        if (mDrawerToggle == null) {
            return;
        }
        boolean isRoot = getFragmentManager().getBackStackEntryCount() == 0;
        mDrawerToggle.setDrawerIndicatorEnabled(isRoot);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(!isRoot);
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRoot);
            getSupportActionBar().setHomeButtonEnabled(!isRoot);
        }
        if (isRoot) {
            mDrawerToggle.syncState();
        }
    }

    protected void initializeFromParams(Bundle savedInstanceState, Intent intent) {
        String mediaId = null;
        // check if we were started from a "Play XYZ" voice search. If so, we save the extras
        // (which contain the query details) in a parameter, so we can reuse it later, when the
        // MediaSession is connected.

        if (savedInstanceState != null) {
            // If there is a saved media ID, use it
            mediaId = savedInstanceState.getString(SAVED_MEDIA_ID);
        }
        navigateToBrowser(mediaId);
    }

    private void navigateToBrowser(String mediaId) {
        Log.i(TAG, "to browser meids : " + mediaId);
        BrowseFragment fragment = getBrowseFragment();
        if(fragment == null || !TextUtils.equals(fragment.getMediaId(), mediaId)){
            fragment = new BrowseFragment();
            fragment.setMediaId(mediaId);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    R.animator.slide_in_from_right, R.animator.slide_out_to_left,
                    R.animator.slide_in_from_left, R.animator.slide_out_to_right);
            transaction.replace(R.id.container, fragment, FRAGMENT_TAG);
            if(mediaId!= null){
                transaction.addToBackStack(null);
            }
            transaction.commit();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("主界面","start了");
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
        getFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i(TAG, "点击率返回键");
    }

    @Override
    protected void onPause() {
        super.onPause();
        getFragmentManager().removeOnBackStackChangedListener(mBackStackChangedListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(getSupportMediaController()!=null){
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
            navigateToBrowser(item.getMediaId());
        } else if (item.isPlayable()) {
            //直接播放，activity中已设置监听处理
            Log.i(TAG, "播放前状态是：" + getSupportMediaController().getPlaybackState().getState());
            Log.i(TAG, "启动播放" + item.getDescription().getTitle());
            getSupportMediaController().getTransportControls().playFromMediaId(item.getMediaId(), null);
            Log.i(TAG, "播放后状态是：" + getSupportMediaController().getPlaybackState().getState());;
        }
    }

    protected void showPlaybackControls() {
        Log.i(TAG, "showPlaybackControls");
//        if (NetworkHelper.isOnline(this)) {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
                        R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
                .show(mControlsFragment)
                .commit();
//        }
    }

    protected void hidePlaybackControls() {
        Log.i(TAG, "hidePlaybackControls");
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
        Log.i(TAG, "播放控制器当前播放状态为： "+ mediaController.getPlaybackState().getState());
        if (mediaController == null ||
                mediaController.getMetadata() == null ||
                mediaController.getPlaybackState() == null) {
//            Log.i(TAG, "播放内容为null? "+ String.valueOf(mediaController == null));
//            Log.i(TAG, "播放状态为null? "+ String.valueOf(mediaController == null));
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
}
