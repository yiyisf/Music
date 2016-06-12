package music.com.example.liuzhe.music;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import music.com.example.liuzhe.music.util.MediaIDHelper;
import music.com.example.liuzhe.music.util.NetworkHelper;

/**
 * Created by liuzhe on 2016/6/7.
 */
public class BrowseFragment extends Fragment implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "BrowseFragment";
    public static final String ARG_MEDIA_ID = "media_id";
    private String mMediaId;
    //    private MediaBrowserCompat mMediaBrowser;
    private BrowseAdapter mBrowseAdapter;
    private ProgressDialog mProgressDialog;
    private FragmentDataHelper mFragmentListenr;
    private View errorView;
    private TextView errorMsgView;


    private MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    super.onPlaybackStateChanged(state);
                    Log.i(TAG, "Received state change: " + state + "getactivity is :" + getActivity());
                    checkForUserVisibleErrors(false);
                    mBrowseAdapter.notifyDataSetChanged();
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    super.onMetadataChanged(metadata);
                    Log.i(TAG, "Received metadata change to media " + metadata.getDescription().getMediaId());
                    if (metadata == null) {
                        return;
                    }
                    mBrowseAdapter.notifyDataSetChanged();
                }
            };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i(TAG, "设置接口");
        mFragmentListenr = (FragmentDataHelper) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "fragment销毁");
        mFragmentListenr = null;
    }

    //链接成功处理
    public void onConnected() {
        if (isDetached()) {
            return;
        }
        mMediaId = getMediaId();
        if (mMediaId == null) {
            mMediaId = mFragmentListenr.getMediaBrowser().getRoot();
        }
        mFragmentListenr.getMediaBrowser().unsubscribe(mMediaId);
        mFragmentListenr.getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

        // Add MediaController callback so we can redraw the list when metadata changes:
        MediaControllerCompat controller = ((AppCompatActivity) getActivity())
                .getSupportMediaController();
        if (controller != null) {
            controller.registerCallback(mMediaControllerCallback);
        }

    }

    public void setMediaId(String mediaId) {
        Bundle args = new Bundle(1);
        args.putString(ARG_MEDIA_ID, mediaId);
        setArguments(args);
    }

    public String getMediaId() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(ARG_MEDIA_ID);
        }
        return null;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
    }


    //提供接口处理点击事件
    public interface FragmentDataHelper extends MediaBrowserProvider {
        void onMediaItemSelected(MediaBrowserCompat.MediaItem item);
    }

    private MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onError(@NonNull String parentId) {
                    Toast.makeText(getActivity(), "Subscribe error", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildrenLoaded(@NonNull String parentId, List<MediaBrowserCompat.MediaItem> children) {
                    mBrowseAdapter.clear();
                    mBrowseAdapter.notifyDataSetInvalidated();
                    checkForUserVisibleErrors(children.isEmpty());
                    for (MediaBrowserCompat.MediaItem item : children) {
                        mBrowseAdapter.add(item);
                    }

                    mBrowseAdapter.notifyDataSetChanged();
//                    hideProgressDialog();

                }
            };


    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "Starting.....");
        // fetch browsing information to fill the listview:
        MediaBrowserCompat mediaBrowser = mFragmentListenr.getMediaBrowser();

        Log.i(TAG, "fragment.onStart, mediaId=" + mMediaId +
                "  onConnected=" + mediaBrowser.isConnected());

        if (mediaBrowser.isConnected()) {
            onConnected();
        }
//        showProgressDialog();
    }

    @Override
    public void onStop() {
        super.onStop();
        //获取主activity中的mediabrowser并释放(如不为空)
        MediaBrowserCompat browserCompat = mFragmentListenr.getMediaBrowser();
        if (browserCompat != null && browserCompat.isConnected() && mMediaId != null) {
            browserCompat.unsubscribe(mMediaId);
        }
        //取消控制器回调
        MediaControllerCompat controller = ((AppCompatActivity) getActivity())
                .getSupportMediaController();
        if (controller != null) {
            controller.unregisterCallback(mMediaControllerCallback);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        mBrowseAdapter = new BrowseAdapter(getActivity());

        errorView = root.findViewById(R.id.playback_error);
        errorMsgView = (TextView) errorView.findViewById(R.id.error_message);
        errorView.setVisibility(View.GONE);

        ListView list_music = (ListView) root.findViewById(R.id.list_music);
        list_music.setAdapter(mBrowseAdapter);
        list_music.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "click for play, get activity is :" + getActivity());
                checkForUserVisibleErrors(false);
                MediaBrowserCompat.MediaItem item = mBrowseAdapter.getItem(position);
//                mFragmentListenr = (FragmentDataHelper) getActivity();
                mFragmentListenr.onMediaItemSelected(item);
            }
        });
        Log.i(TAG, "Created");
        return root;
    }


    // An adapter for showing the list of browsed MediaItem's
    private static class BrowseAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem> {


        public BrowseAdapter(Context context) {
            super(context, R.layout.media_list_item, new ArrayList<MediaBrowserCompat.MediaItem>());
        }

        static class ViewHolder {
            ImageView mImageView;
            TextView mTitleView;
            TextView mDescriptionView;
        }

        //        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.media_list_item, parent, false);
                holder = new ViewHolder();
                holder.mImageView = (ImageView) convertView.findViewById(R.id.play_eq);
                holder.mImageView.setVisibility(View.GONE);
                holder.mTitleView = (TextView) convertView.findViewById(R.id.title);
                holder.mDescriptionView = (TextView) convertView.findViewById(R.id.description);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            MediaBrowserCompat.MediaItem item = getItem(position);
            holder.mTitleView.setText(item.getDescription().getTitle());
            holder.mDescriptionView.setText(item.getDescription().getSubtitle());
            if (item.isPlayable()) {
                holder.mImageView.setImageDrawable(
                        getContext().getResources().getDrawable(R.drawable.ic_play_arrow_red_24dp));
                holder.mImageView.setVisibility(View.VISIBLE);

                MediaControllerCompat controller = ((AppCompatActivity) getContext())
                        .getSupportMediaController();
                if (controller != null && controller.getMetadata()!=null && controller.getPlaybackState() != null) {
                    String currentPlaying = controller.getMetadata().getDescription().getMediaId();
                    String musicId = MediaIDHelper.extractMusicIDFromMediaID(
                            item.getDescription().getMediaId());
                    if(currentPlaying!=null && currentPlaying.equals(musicId)) {
                        switch (controller.getPlaybackState().getState()) {
                            case PlaybackStateCompat.STATE_PLAYING:
                                holder.mImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_equalizer_red_24dp));
                                break;
                            case PlaybackStateCompat.STATE_PAUSED:
                                holder.mImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_play_arrow_red_24dp));
                                break;
                            case PlaybackStateCompat.STATE_BUFFERING:
                                holder.mImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_autorenew_red_24dp));
                                break;
                            default:
                                holder.mImageView.setVisibility(View.GONE);
                                break;
                        }
                    }
                }
            }
            return convertView;
        }
    }


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void checkForUserVisibleErrors(boolean forceError) {
        boolean showError = forceError;
        // If offline, message is about the lack of connectivity:
        if (!NetworkHelper.isOnline(MusicApplication.getContext())) {
            errorMsgView.setText(R.string.error_no_connection);
            showError = true;
        } else {
            // otherwise, if state is ERROR and metadata!=null, use playback state error message:
            MediaControllerCompat controller = ((AppCompatActivity) getActivity())
                    .getSupportMediaController();
            if (controller != null
                    && controller.getMetadata() != null
                    && controller.getPlaybackState() != null
                    && controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_ERROR
                    && controller.getPlaybackState().getErrorMessage() != null) {
                errorMsgView.setText(controller.getPlaybackState().getErrorMessage());
                showError = true;
            } else if (forceError) {
                // Finally, if the caller requested to show error, show a generic message:
                errorMsgView.setText(R.string.error_loading_media);
                showError = true;
            }
        }
        errorView.setVisibility(showError ? View.VISIBLE : View.GONE);
//        Log.i(TAG, "checkForUserVisibleErrors. forceError=" + forceError +
//                " showError=" + showError +
//                " isOnline=" + NetworkHelper.isOnline(getActivity()));
    }
}
