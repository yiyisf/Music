package music.com.example.liuzhe.music.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import music.com.example.liuzhe.music.MediaBrowserProvider;
import music.com.example.liuzhe.music.MusicApplication;
import music.com.example.liuzhe.music.R;
import music.com.example.liuzhe.music.util.AlbumArtCache;
import music.com.example.liuzhe.music.util.Artist;
import music.com.example.liuzhe.music.util.MediaIDHelper;
import music.com.example.liuzhe.music.util.NetworkHelper;

/**
 * Created by liuzhe on 2016/6/7.
 */
public class ArtistFragment extends Fragment {
    private static final String TAG = "ArtistFragment";
    //    private MediaBrowserCompat mMediaBrowser;
    private View errorView;
    private TextView errorMsgView;
    private ProgressBar mProgressBar;
    private AlbumArtCache cache;
    private DatabaseReference mData;
    private RecyclerView recycler;
    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter mAdapter;
    private Query Aquery;

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "fragment销毁");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "Starting.....");
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.activity_artist, container, false);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        mData = FirebaseDatabase.getInstance().getReference().child("Artist");

        Aquery = FirebaseDatabase.getInstance().getReference().child("Artist").limitToFirst(100);

        recycler = (RecyclerView) root.findViewById(R.id.artist_list);
        recycler.setHasFixedSize(true);
//        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_container);
//        toolbar.setTitle("歌手");
        Log.i(TAG, "Created");
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        recycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
//        Query postsQuery = getQuery(mData);
        mAdapter = new FirebaseRecyclerAdapter<Artist, ArtistViewHoder>(Artist.class, R.layout.activity_artist,
                ArtistViewHoder.class, mData) {

            @Override
            protected void populateViewHolder(ArtistViewHoder viewHolder, Artist artist, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.bindToItem(artist);
            }
        };
        recycler.setAdapter(mAdapter);
    }


    // An adapter for showing the list of browsed MediaItem's
    private class BrowseAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem> {


        public BrowseAdapter(Context context) {
            super(context, R.layout.media_list_item, new ArrayList<MediaBrowserCompat.MediaItem>());
        }

        class ViewHolder {
            ImageView mImageView;
            TextView mTitleView;
            TextView mDescriptionView;
        }

        //        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.media_list_item, parent, false);
                holder = new ViewHolder();
                holder.mImageView = (ImageView) convertView.findViewById(R.id.play_eq);
//                holder.mImageView.setVisibility(View.GONE);
                holder.mTitleView = (TextView) convertView.findViewById(R.id.title);
                holder.mDescriptionView = (TextView) convertView.findViewById(R.id.description);
                holder.mDescriptionView.setVisibility(View.GONE);
                convertView.setTag(holder);
            } else {
//                Log.i(TAG, "加载已创建的view：" + position);
                holder = (ViewHolder) convertView.getTag();
            }


            MediaBrowserCompat.MediaItem item = getItem(position);
            holder.mTitleView.setText(item.getDescription().getTitle());
            Log.i(TAG, "list view 记载第" + position + "各item" + item.getDescription().getTitle() + (holder.mImageView.getDrawable() == null));
            if (item.isPlayable()) {
                holder.mDescriptionView.setVisibility(View.VISIBLE);
                holder.mDescriptionView.setText(item.getDescription().getSubtitle());
                ViewGroup.LayoutParams params = holder.mImageView.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.mImageView.setLayoutParams(params);

                holder.mImageView.setImageDrawable(
                        getContext().getResources().getDrawable(R.drawable.ic_play_arrow_red_24dp));
                holder.mImageView.setVisibility(View.VISIBLE);

                MediaControllerCompat controller = ((AppCompatActivity) getContext())
                        .getSupportMediaController();
                if (controller != null && controller.getMetadata() != null && controller.getPlaybackState() != null) {
                    String currentPlaying = controller.getMetadata().getDescription().getMediaId();
                    String musicId = MediaIDHelper.extractMusicIDFromMediaID(
                            item.getDescription().getMediaId());
                    if (currentPlaying != null && currentPlaying.equals(musicId)) {
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
            } else {
//                holder.mImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_default_artist));
                String mArtUrl = item.getDescription().getTitle().toString();
//                new ImageDownloaderTask(holder.mImageView).execute(mArtUrl);
//                显示歌手头像
                if (holder.mImageView.getDrawable() == null) {
//                if (art == null) {
//                    final String mArtUrl = item.getDescription().getTitle().toString();
                    Bitmap art = cache.getIconImage(mArtUrl);
//                }
                    final ImageView mView = holder.mImageView;
                    if (art != null) {
                        mView.setImageBitmap(art);
                    } else {
//                        cache.fetch(mArtUrl, new AlbumArtCache.FetchListener() {
//
//                                    @Override
//                                    public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
//                                        if (icon != null) {
//                                            Log.i(TAG, "设置" + artUrl + "的图片" + "postion:" + position);
////                                        if (isActive()) {
//                                            mView.setImageBitmap(icon);
////                                        }
//                                        }
//                                    }
//                                    @Override
//                                    public void onError(String artUrl, Exception e) {
//                                        mView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_default_artist));
//                                    }
//                                }
//                        );
                    }
                }

            }
            return convertView;
        }


    }

    private void checkForUserVisibleErrors(boolean forceError) {
        boolean showError = forceError;
        // If offline, message is about the lack of connectivity:
        if (!NetworkHelper.isOnline(MusicApplication.getContext())) {
            errorMsgView.setText(R.string.error_no_connection);
            showError = true;
        } else {
            showError = false;
        }
        errorView.setVisibility(showError ? View.VISIBLE : View.GONE);
    }
}
