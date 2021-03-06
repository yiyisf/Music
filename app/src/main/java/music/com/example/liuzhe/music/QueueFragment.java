/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package music.com.example.liuzhe.music;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ComponentName;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

/**
 * A class that shows the Media Queue to the user.
 */
public class QueueFragment extends Fragment {

    private static final String TAG = "QueueFragment";

    private ImageButton mSkipNext;
    private ImageButton mSkipPrevious;
    private ImageButton mPlayPause;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat.TransportControls mTransportControls;
    private MediaControllerCompat mMediaController;
    private PlaybackStateCompat mPlaybackState;

    private QueueAdapter mQueueAdapter;

    private static SetMediaconteller ControllerCallBack;
    private static String mMediaId;


    public interface SetMediaconteller {

        public void setContrller(MediaControllerCompat mediaControllerCompat);

        public MediaControllerCompat getContrller();

    }

    private MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onError(@NonNull String parentId) {
                    Toast.makeText(getActivity(), "Subscribe error", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildrenLoaded(@NonNull String parentId, List<MediaBrowserCompat.MediaItem> children) {
                    Log.i(TAG, "load child");
                }
            };

    private MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onConnected() {
                    Log.i(TAG, "onConnected: session token " + mMediaBrowser.getSessionToken());

                    if (mMediaBrowser.getSessionToken() == null) {
                        throw new IllegalArgumentException("No Session token");
                    }

                    mMediaBrowser.subscribe(mMediaId, mSubscriptionCallback);

                    try {
                        mMediaController = new MediaControllerCompat(getActivity(),
                                mMediaBrowser.getSessionToken());
                        Log.i(TAG, "controller is null ? " + String.valueOf(mMediaController == null));
                        Log.i(TAG, "callback is null" + String.valueOf(ControllerCallBack ==null));
                        ControllerCallBack.setContrller(mMediaController);
//                        getActivity().setMediaController(mediaControllerCompat);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    mTransportControls = mMediaController.getTransportControls();
                    mMediaController.registerCallback(mSessionCallback);
                    mPlaybackState = mMediaController.getPlaybackState();

                    List<MediaSessionCompat.QueueItem> queue = mMediaController.getQueue();

                    if (queue != null) {
                        mQueueAdapter.clear();
                        mQueueAdapter.notifyDataSetInvalidated();
                        mQueueAdapter.addAll(queue);
                        mQueueAdapter.notifyDataSetChanged();
                    }
                    onPlaybackStateChanged(mPlaybackState);
                }

                @Override
                public void onConnectionFailed() {
                    Log.e(TAG, "onConnectionFailed");
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onConnectionSuspended() {
                    Log.i(TAG, "onConnectionSuspended");
                    mMediaController.unregisterCallback(mSessionCallback);
                    mTransportControls = null;
                    mMediaController = null;
                    getActivity().setMediaController(null);
                }
            };

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private MediaControllerCompat.Callback mSessionCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onSessionDestroyed() {
            Log.i(TAG, "Session destroyed. Need to fetch a new Media Session");
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state == null) {
                Log.i(TAG, "Received playback state change to state " + state.getState());
                return;
            }
            mPlaybackState = state;
            QueueFragment.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            Log.i(TAG, "onQueueChanged " + queue);
            if (queue != null) {
                mQueueAdapter.clear();
                mQueueAdapter.notifyDataSetInvalidated();
                mQueueAdapter.addAll(queue);
                mQueueAdapter.notifyDataSetChanged();
            }
        }
    };

    public static QueueFragment newInstance(SetMediaconteller callback, String mediaId) {
        ControllerCallBack = callback;
        mMediaId = mediaId;
        return new QueueFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

//        mSkipPrevious = (ImageButton) rootView.findViewById(R.id.skip_previous);
//        mSkipPrevious.setEnabled(false);
//        mSkipPrevious.setOnClickListener(mButtonListener);
//
//        mSkipNext = (ImageButton) rootView.findViewById(R.id.skip_next);
        mSkipNext.setEnabled(false);
        mSkipNext.setOnClickListener(mButtonListener);

        mPlayPause = (ImageButton) rootView.findViewById(R.id.play_pause);
        mPlayPause.setEnabled(true);
        mPlayPause.setOnClickListener(mButtonListener);

        mQueueAdapter = new QueueAdapter(getActivity());

        ListView mListView = (ListView) rootView.findViewById(R.id.list_music);
        mListView.setAdapter(mQueueAdapter);
        mListView.setFocusable(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaSessionCompat.QueueItem item = mQueueAdapter.getItem(position);
                mTransportControls.skipToQueueItem(item.getQueueId());
            }
        });

        mMediaBrowser = new MediaBrowserCompat(getActivity(),
                new ComponentName(getActivity(), MusicService.class),
                mConnectionCallback, null);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "Queue fragment on Resume");
        if (mMediaBrowser != null) {
            mMediaBrowser.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mSessionCallback);
        }
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }
    }


    private void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.i(TAG, "onPlaybackStateChanged " + state);
        if (state == null) {
            return;
        }
        mQueueAdapter.setActiveQueueItemId(state.getActiveQueueItemId());
        mQueueAdapter.notifyDataSetChanged();
        boolean enablePlay = false;
        StringBuilder statusBuilder = new StringBuilder();
        Log.i(TAG, "current play state is :" + state.getState());
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                statusBuilder.append("playing");
                enablePlay = false;
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                statusBuilder.append("paused");
                enablePlay = true;
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                statusBuilder.append("ended");
                enablePlay = true;
                break;
            case PlaybackStateCompat.STATE_ERROR:
                statusBuilder.append("error: ").append(state.getErrorMessage());
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                statusBuilder.append("buffering");
                break;
            case PlaybackStateCompat.STATE_NONE:
                statusBuilder.append("none");
                enablePlay = false;
                break;
            case PlaybackStateCompat.STATE_CONNECTING:
                statusBuilder.append("connecting");
                break;
            default:
                statusBuilder.append(mPlaybackState);
        }
        statusBuilder.append(" -- At position: ").append(state.getPosition());
        Log.i(TAG, statusBuilder.toString());

        if (enablePlay) {
            mPlayPause.setImageDrawable(
                    getActivity().getResources().getDrawable(R.drawable.ic_play_arrow_red_24dp));
        } else {
            mPlayPause.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_pause_red_24dp));
        }

        mSkipPrevious.setEnabled((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0);
        mSkipNext.setEnabled((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0);

        Log.i(TAG, "Queue From MediaController *** Title " +
                mMediaController.getQueueTitle() + "\n: Queue: " + mMediaController.getQueue() +
                "\n Metadata " + mMediaController.getMetadata());
    }

    private View.OnClickListener mButtonListener = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onClick(View v) {
            final int state = mPlaybackState == null ?
                    PlaybackStateCompat.STATE_NONE : mPlaybackState.getState();
//            switch (v.getId()) {
//                case R.id.play_pause:
//                    Log.i(TAG, "Play button pressed, in state " + state);
//                    if (state == PlaybackState.STATE_PAUSED ||
//                            state == PlaybackState.STATE_STOPPED ||
//                            state == PlaybackState.STATE_NONE) {
//                        playMedia();
//                        mPlayPause.setImageDrawable(
//                                getActivity().getResources().getDrawable(R.drawable.ic_pause_red_24dp));
//                    } else if (state == PlaybackState.STATE_PLAYING) {
//                        pauseMedia();
//                        mPlayPause.setImageDrawable(
//                                getActivity().getResources().getDrawable(R.drawable.ic_play_arrow_red_24dp));
//                    }
//                    break;
//                case R.id.skip_previous:
//                    Log.d(TAG, "Start button pressed, in state " + state);
//                    skipToPrevious();
//                    break;
//                case R.id.skip_next:
//                    skipToNext();
//                    break;
//            }
        }
    };

    private void playMedia() {
        if (mTransportControls != null) {
            mTransportControls.play();
        }
    }

    private void pauseMedia() {
        if (mTransportControls != null) {
            mTransportControls.pause();
        }
    }

    private void skipToPrevious() {
        if (mTransportControls != null) {
            mTransportControls.skipToPrevious();
        }
    }

    private void skipToNext() {
        if (mTransportControls != null) {
            mTransportControls.skipToNext();
        }
    }

}
