package music.com.example.liuzhe.music;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import music.com.example.liuzhe.music.ui.SongListView;
import music.com.example.liuzhe.music.util.Artist;
import music.com.example.liuzhe.music.util.MediaIDHelper;
import music.com.example.liuzhe.music.util.Song;

public class SongsActivity extends AppCompatActivity {
    private static final String TAG = "SongActivity";

    private MediaBrowserCompat mBrowserCompat;
    FloatingActionButton fab;
    private Artist artist;
    private List<Song> songs;
    private SongAdapter adapter;
    private MediaBrowserCompat.ConnectionCallback mConnectCallBack = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            try {
                connectToSession(mBrowserCompat.getSessionToken());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConnectionFailed() {
            Log.e(TAG, "connect service error...");
        }
    };
    private final MediaControllerCompat.Callback ControlCallBack = new MediaControllerCompat.Callback() {
        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
            //播放列表变化
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            adapter.notifyDataSetChanged();
            //播放状态变化,更新浮动按钮的图标样式
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            adapter.notifyDataSetChanged();
        }
    };
    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, List<MediaBrowserCompat.MediaItem> children) {

            if (children.isEmpty()) {
                Log.e(TAG, "加载播放列表为空...");
            }
            adapter.clear();
            adapter.notifyDataSetInvalidated();
            for (MediaBrowserCompat.MediaItem item : children) {
//                adapter.add(new Song(item.getDescription().getTitle().toString(), ""));
                Log.i(TAG, "加载:" + item.getMediaId());
                adapter.add(item);
            }

            adapter.notifyDataSetChanged();

        }

        @Override
        public void onError(@NonNull String parentId) {
            super.onError(parentId);
            //加载出错
            Log.e(TAG, "加载播放列表出错...");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        artist = getIntent().getParcelableExtra("artist");
        setTitle(artist.getName());

        songs = artist.getSongs();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        SongListView song_list = (SongListView) findViewById(R.id.song_list);

        adapter = new SongAdapter(this);

        mBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), mConnectCallBack, null);

        Glide.with(this)
                .load(artist.getPhotoUrl())
                .into((ImageView) findViewById(R.id.toolbar_artist_photo));

        song_list.setAdapter(adapter);

        song_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //播放当前曲目
                MediaBrowserCompat.MediaItem item = adapter.getItem(position);
                Log.i(TAG, "播放:" + item.getMediaId());
                getSupportMediaController().getTransportControls().
                        playFromMediaId(item.getMediaId(), null);
            }
        });


        Log.i(TAG, String.valueOf(adapter.getCount()));

    }

    private class SongAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem> {

        public SongAdapter(Context context) {
            super(context, R.layout.songs_list_item, new ArrayList<MediaBrowserCompat.MediaItem>());
        }


        class ViewHolder {

            ImageView mImageView;
            TextView mTitleView;
            TextView mDescriptionView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.i(TAG, "get view position :" + position);
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(SongsActivity.this)
                        .inflate(R.layout.songs_list_item, parent, false);

                holder = new ViewHolder();
                holder.mImageView = (ImageView) convertView.findViewById(R.id.play_eq);
                holder.mTitleView = (TextView) convertView.findViewById(R.id.title);
                holder.mDescriptionView = (TextView) convertView.findViewById(R.id.description);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

//            Song song = getItem(position);
            MediaBrowserCompat.MediaItem item = getItem(position);
            holder.mTitleView.setText(item.getDescription().getTitle().toString());
            holder.mDescriptionView.setText(artist.getName());
            holder.mImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_play_arrow_red_24dp));

            MediaControllerCompat controller = getSupportMediaController();
            //如存在播放曲目及播放状态
            if (controller != null && controller.getMetadata() != null && controller.getPlaybackState() != null) {
                //获取到当前播放的曲目及id
                String currentPlaying = controller.getMetadata().getDescription().getMediaId();
                String musicId = MediaIDHelper.extractMusicIDFromMediaID(
                        item.getDescription().getMediaId());
                if (currentPlaying != null && currentPlaying.equals(musicId)) {
                    switch (controller.getPlaybackState().getState()) {
                        case PlaybackStateCompat.STATE_BUFFERING:  //正在加载
                            holder.mImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_autorenew_red_24dp));
//                            fab.setImageDrawable(getDrawable(R.drawable.ic_play_arrow_red_24dp));
                            break;
                        case PlaybackStateCompat.STATE_PLAYING:    //正在播放
                            holder.mImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_equalizer_red_24dp));
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:     //正在暂停
                            holder.mImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_play_arrow_red_24dp));
                            break;
                        case PlaybackStateCompat.STATE_NONE:       //未知状态
                            break;
                    }
                }
            }

            return convertView;
        }
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat controllerCompat = new MediaControllerCompat(this, token);

        setSupportMediaController(controllerCompat);

        controllerCompat.registerCallback(ControlCallBack);

        mBrowserCompat.unsubscribe(MusicService.MEDIA_ID_MUSICS_BY_ARTIST + "/" + artist.getName());

        mBrowserCompat.subscribe(MusicService.MEDIA_ID_MUSICS_BY_ARTIST + "/" + artist.getName(), mSubscriptionCallback);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mBrowserCompat.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getSupportMediaController() != null) {
            getSupportMediaController().unregisterCallback(ControlCallBack);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Log.i(TAG, "监听到返回按钮");
            finish();
        }

        return true;

//        return super.onOptionsItemSelected(item);
    }
}
