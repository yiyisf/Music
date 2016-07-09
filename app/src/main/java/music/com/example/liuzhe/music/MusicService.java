package music.com.example.liuzhe.music;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import music.com.example.liuzhe.music.util.MediaIDHelper;
import music.com.example.liuzhe.music.util.QueueHelper;

public class MusicService extends MediaBrowserServiceCompat implements Callback, Thread.UncaughtExceptionHandler {
    private static final String TAG = "MUSIC Service";
    public static final String MEDIA_ID_ROOT = "__ROOT__";
    public static final String MEDIA_ID_MUSICS_BY_GENRE = "__BY_GENRE__";

    // Music catalog manager
    private MusicProvider mMusicProvider;
    private MediaSessionCompat mSession;
    // "Now playing" queue:
    private List<MediaSessionCompat.QueueItem> mPlayingQueue;
    private Playback mPlayback;
    private PackageValidator mPackageValidator;
    private int mCurrentIndexOnQueue;
//    private MediaNotificationManager mMediaNotificationManager;
    private long availableActions;
    private boolean mServiceStarted;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind intent is null ? " + String.valueOf(intent == null));
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Created");
        mMusicProvider = new MusicProvider();
        mPackageValidator = new PackageValidator(this);
        mPlayingQueue = new ArrayList<>();
        Thread.setDefaultUncaughtExceptionHandler(this);

        //Start a new MediaSessionCompat
        mSession = new MediaSessionCompat(getApplicationContext(), "MusicService");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Log.i(TAG, "Created -- 01");
        mPlayback = new Playback(this, mMusicProvider);
        mPlayback.setState(PlaybackStateCompat.STATE_NONE);
        mPlayback.setCallback(this);
        mPlayback.start();

        Log.i(TAG, "Created -- 02");
        Context context = getApplicationContext();
        Intent intent = new Intent(context, MusicActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);
        Log.i(TAG, "Created -- 03");

//        Bundle extras = new Bundle();
//        CarHelper.setSlotReservationFlags(extras, true, true, true);
//        mSession.setExtras(extras);

        updatePlaybackState(null);

//        mMediaNotificationManager = new MediaNotificationManager(this);


    }

    private void updatePlaybackState(String error) {
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(mPlayback.isPlaying()
                        ? PlaybackStateCompat.ACTION_PAUSE
                        : PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_NEXT|PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
//                .setActions(getAvailableActions());

//        setCustomAction(stateBuilder);
        int state = mPlayback.getState();
        Log.i(TAG, "获取到playback状态为： " + state);

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }

        Log.i(TAG, "status is :" + state);

        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());
//        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        Log.i(TAG, "mPlayingQueue is null ?" + String.valueOf(mPlayingQueue == null));
        Log.i(TAG, "mPlayingQueue is empty ?" + String.valueOf(mPlayingQueue.isEmpty()));
        // Set the activeQueueItemId if the current index is valid.
        if (QueueHelper.isIndexPlayable(mCurrentIndexOnQueue, mPlayingQueue)) {
            MediaSessionCompat.QueueItem item = mPlayingQueue.get(mCurrentIndexOnQueue);
            stateBuilder.setActiveQueueItemId(item.getQueueId());
        }

        mSession.setPlaybackState(stateBuilder.build());

//        if (state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED) {
//            mMediaNotificationManager.startNotification();
//        }

    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Log.d(TAG, "OnGetRoot: clientPackageName=" + clientPackageName +
                "; clientUid=" + clientUid + " ; rootHints=" + rootHints);
        // To ensure you are not allowing any arbitrary app to browse your app's contents, you
        // need to check the origin:
        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // If the request comes from an untrusted package, return null. No further calls will
            // be made to other media browsing methods.
            Log.w(TAG, "OnGetRoot: IGNORING request from untrusted package "
                    + clientPackageName);
            return null;
        }
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {

        Log.i(TAG, "provider is inited ?" + String.valueOf(mMusicProvider.isInitialized()));

        if (!mMusicProvider.isInitialized()) {
            result.detach();
            mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                @Override
                public void onMusicCatalogReady(boolean success) {
                    if (success) {
                        Log.i(TAG, "Music catalog ready" + String.valueOf(success));
                        Log.i(TAG, "OnLoadChildren: parentMediaId=" + parentId);
                        loadChildrenImpl(parentId, result);
                    } else {
                        result.sendResult(Collections.<MediaBrowserCompat.MediaItem>emptyList());
                    }
                }
            });
        } else {
            loadChildrenImpl(parentId, result);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "destroy");
    }


    private void loadChildrenImpl(String parentId,
                                  Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.i(TAG, "OnLoadChildren: parentMediaId=" + parentId);

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        if (MEDIA_ID_ROOT.equals(parentId)) {
            Log.i(TAG, "OnLoadChildren.ROOT");
            for (String genre : mMusicProvider.getGenres()) {
                MediaBrowserCompat.MediaItem item = new MediaBrowserCompat.MediaItem(
                        new MediaDescriptionCompat.Builder()
//                                .setMediaId(createBrowseCategoryMediaID(MEDIA_ID_MUSICS_BY_GENRE, genre))
                                .setMediaId(MEDIA_ID_MUSICS_BY_GENRE + "/" + genre)
                                .setTitle(genre)
                                .setSubtitle("gener sub title")
                                .build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                );
                mediaItems.add(item);
            }
        } else if (parentId.startsWith(MEDIA_ID_MUSICS_BY_GENRE)) {
            Log.i(TAG, "OnLoadChildren.GENRES");
            String genre = parentId.split("/")[1];

            for (MediaMetadataCompat compat : mMusicProvider.getMusicsByGenre(genre)){
                String hierarchyAwareMediaID = MediaIDHelper.createMediaID(
                        compat.getDescription().getMediaId(), MEDIA_ID_MUSICS_BY_GENRE, genre);
                MediaMetadataCompat trackCopy = new MediaMetadataCompat.Builder(compat)
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                        .build();
                MediaBrowserCompat.MediaItem bItem = new MediaBrowserCompat.MediaItem(
                        trackCopy.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
                mediaItems.add(bItem);
            }


//            for (String genre : mMusicProvider.getGenres()) {
//                MediaBrowserCompat.MediaItem item = new MediaBrowserCompat.MediaItem(
//                        new MediaDescriptionCompat.Builder()
////                                .setMediaId(createBrowseCategoryMediaID(MEDIA_ID_MUSICS_BY_GENRE, genre))
//                                .setMediaId(MEDIA_ID_MUSICS_BY_GENRE + "/" + genre)
//                                .setTitle(genre)
//                                .setSubtitle("gener sub title")
//                                .build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
//                );
//                mediaItems.add(item);
//            }

        }

        result.sendResult(mediaItems);
    }

    @Override
    public void onCompletion() {
        mCurrentIndexOnQueue ++ ;
        if(mCurrentIndexOnQueue < 0){
            mCurrentIndexOnQueue = 0;
        }else {
            mCurrentIndexOnQueue %= mPlayingQueue.size();
        }

        if(!QueueHelper.isIndexPlayable(mCurrentIndexOnQueue, mPlayingQueue)){
            handlePauseRequest();
        }else {
            handlePlayRequest();
            updateMetadata();
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    public long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
    }

    public void setQueueFromMusic(String mediaId) {
        // selected from.
        boolean canReuseQueue = false;
//        if (isSameBrowsingCategory(mediaId)) {
//            canReuseQueue = setCurrentQueueItem(mediaId);
//        }
//        if (!canReuseQueue) {
//            String queueTitle = mResources.getString(R.string.browse_musics_by_genre_subtitle,
//                    MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaId));
            setCurrentQueue(null,
                    QueueHelper.getPlayingQueue(mediaId, mMusicProvider), mediaId);
//        }
        updateMetadata();
    }

    private void updateMetadata() {
        MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            ;updatePlaybackState("没有metadata");
            return;
        }
        final String musicId = MediaIDHelper.extractMusicIDFromMediaID(
                currentMusic.getDescription().getMediaId());
        MediaMetadataCompat metadata = mMusicProvider.getMusic(musicId);
        if (metadata == null) {
            throw new IllegalArgumentException("Invalid musicId " + musicId);
        }

        mSession.setMetadata(metadata);
    }

    private MediaSessionCompat.QueueItem getCurrentMusic() {
        if (!QueueHelper.isIndexPlayable(mCurrentIndexOnQueue, mPlayingQueue)) {
            return null;
        }
        return mPlayingQueue.get(mCurrentIndexOnQueue);
    }

    private void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue, String initialMediaId) {
        mPlayingQueue = newQueue;
        int index = 0;
        if (initialMediaId != null) {
            index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, initialMediaId);
        }
        mCurrentIndexOnQueue = Math.max(index, 0);
        mSession.setQueue(newQueue);
    }


    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            Log.i(TAG, "handle ply from mediaId :" + mediaId);
//            mPlayingQueue = QueueHelper.getPlayingQueue(mediaId, mMusicProvider);
//
//            mSession.setQueue(mPlayingQueue);
            setQueueFromMusic(mediaId);
            handlePlayRequest();

        }

        @Override
        public void onSeekTo(long pos) {

        }

        @Override
        public void onPlay() {
            Log.i(TAG, "Play");

            if(mPlayingQueue == null || mPlayingQueue.isEmpty()){
                mPlayingQueue = QueueHelper.getRandomQueue(mMusicProvider);
                mSession.setQueue(mPlayingQueue);
                mSession.setQueueTitle(getString(R.string.random_queue_title));
                // start playing from the beginning of the queue
                mCurrentIndexOnQueue = 0;
            }

            if(mPlayingQueue!=null && !mPlayingQueue.isEmpty()){
                handlePlayRequest();
            }

        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        }

        @Override
        public void onPause() {
            handlePauseRequest();
        }
    }

    private void handlePauseRequest() {
        mPlayback.pause();
    }

    private void handlePlayRequest() {
        if (!mServiceStarted) {
            Log.i(TAG, "Starting service");
            // The MusicService needs to keep running even after the calling MediaBrowser
            // is disconnected. Call startService(Intent) and then stopSelf(..) when we no longer
            // need to play media.
            startService(new Intent(getApplicationContext(), MusicService.class));
            mServiceStarted = true;
        }

        if (!mSession.isActive()) {
            mSession.setActive(true);
        }

        Log.i(TAG, "PlayingQueue id is : " + mPlayingQueue.get(0).getDescription().getMediaId());
        Log.i(TAG, "current play queue size is :" + mPlayingQueue.size());
        if (QueueHelper.isIndexPlayable(mCurrentIndexOnQueue, mPlayingQueue)) {
//            updateMetadata();
            mPlayback.play(mPlayingQueue.get(mCurrentIndexOnQueue));
        }
    }
}
