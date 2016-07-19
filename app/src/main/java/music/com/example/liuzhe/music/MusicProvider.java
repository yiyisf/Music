package music.com.example.liuzhe.music;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import music.com.example.liuzhe.music.util.Artist;
import music.com.example.liuzhe.music.util.Song;

/**
 * Created by liuzhe on 2016/6/7.
 */
public class MusicProvider {
    private static final String TAG = "MusicProvider";
//    private static final String CATALOG_URL =
//            "https://glowing-fire-3217.firebaseio.com/music.json";

    public static final String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";

    //    private static final String JSON_MUSIC = "music";
//    private static final String JSON_TITLE = "title";
//    private static final String JSON_ALBUM = "album";
//    private static final String JSON_ARTIST = "artist";
    private static final String JSON_GENRE = "genre";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_NAME = "name";

    private static MusicProvider provider = new MusicProvider();

    private List<Artist> ArtistList;

//    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByArtist;
//    private ConcurrentMap<ArtistInfo, List<MediaMetadataCompat>> mMusicListByArtist;
    private ConcurrentMap<String , Artist> mMusicListByArtist;
    private final ConcurrentMap<String, MutableMediaMetadata> mMusicListById;
    private static int song_no = 0;

    public MediaMetadataCompat getMusic(String id) {
        Log.i(TAG, "getmusic id is:" + id);
        return mMusicListById.containsKey(id) ? mMusicListById.get(id).mediaMetadataCompat : null;

//        return null;
    }

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;


    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public static MusicProvider getInstence(){
        return provider;
    }

    private MusicProvider() {
        mMusicListById = new ConcurrentHashMap<>();
        mMusicListByArtist = new ConcurrentHashMap<>();
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    public Iterable<String > getArtists() {
        Log.i(TAG, "State is initialized ?" + String.valueOf((mCurrentState == State.INITIALIZED)));
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        //使用sortedset会将顺序排好，直接使用keyset顺序是乱的
        SortedSet<String > temp = new TreeSet<>();

        for (String  i : mMusicListByArtist.keySet()) {
            temp.add(i);
        }
        return temp;

//        return mMusicListByArtist.keySet();
    }

    public Uri getArtistImageUri(String artistName){
        if (mCurrentState != State.INITIALIZED || !mMusicListByArtist.containsKey(artistName)) {
            return null;
        }

        return Uri.parse(mMusicListByArtist.get(artistName).getPhotoUrl());
    }

    public Iterable<MediaMetadataCompat> getMusicsByArtistName(String artistName) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByArtist.containsKey(artistName)) {
            return Collections.emptyList();
        }
        Log.i(TAG, "get by : " + artistName);
        Artist a = mMusicListByArtist.get(artistName);
        List<MediaMetadataCompat> list = new ArrayList<>();
        for (Song song : a.getSongs()) {
            MediaMetadataCompat item = buildFromSong(song, a.getName(), a.getPhotoUrl());
            list.add(item);

            String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            MutableMediaMetadata temp = new MutableMediaMetadata(musicId, item);
            mMusicListById.put(musicId, temp);
        }

        return list;
    }

    public Artist getArtistByArtistName(String artistName) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByArtist.containsKey(artistName)) {
            return null;
        }

        return mMusicListByArtist.get(artistName);

    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    public void retrieveMediaAsync(final Callback callback) {
        Log.i(TAG, "retrieveMediaAsync called" + String.valueOf(mCurrentState == State.INITIALIZED));
        if (mCurrentState == State.INITIALIZED) {
            // Nothing to do, execute callback immediately
            callback.onMusicCatalogReady(true);
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();

//                Log.i(TAG, "cat status is begin listening");
                while (true) {
                    if (mCurrentState == State.INITIALIZED) {
//                        Log.i(TAG, "cat status is initialized");
                        return mCurrentState;
                    }
                }
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    //解析返回的JSON及组装mMusicListById
    private synchronized void retrieveMedia() {

        if (mCurrentState == State.NON_INITIALIZED) {
            mCurrentState = State.INITIALIZING;

//            fetchJSONFromUrl(null);

            fetchJSONFromUrl(new forFirebaseCallBack() {
                @Override
                public void onAdd(Artist a) {
                    buildListsByArtist(a);
                }
            });


//            //组装播放数据源
//            for(Artist a: ArtistList){
//                for(Song song:a.getSongs()) {
//                    MediaMetadataCompat item = buildFromSong(song, a.getName(), a.getPhotoUrl());
//                    list.add(item);
//
//                    String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
//                    MutableMediaMetadata temp = new MutableMediaMetadata(musicId, item);
//                    mMusicListById.put(musicId, temp);
//                }
//                buildListsByArtist(a.getName(), list);
//                Log.i(TAG, "put artist :" + a.getName());
//            }

//            mCurrentState = State.INITIALIZED; //完成数据源初始化



//                if (tracks.size() > 0) {
//                    Log.i(TAG, "All " + tracks.size() + "songs");
//                    for (int j = 0; j < tracks.size(); j++) {
//                        Log.i(TAG, "Song is Null? " + String.valueOf(tracks.get(j) == null));
//                        if (tracks.get(j) != null) {
//                            MediaMetadataCompat item = buildFromSong(tracks.get(j));
//                            String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
//                            mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));
//                        }
//                    }
//                    buildListsByArtist();
//                }
//            mCurrentState = State.INITIALIZED;
        }

//        if (mCurrentState != State.INITIALIZED) {
//            Log.i(TAG, "mCurrentState is bad happened");
//
//            // Something bad happened, so we reset state to NON_INITIALIZED to allow
//            // retries (eg if the network connection is temporary unavailable)
//            mCurrentState = State.NON_INITIALIZED;
//        }

    }

    private synchronized void buildListsByArtist(Artist a) {
        mMusicListByArtist.putIfAbsent(a.getName(), a);
    }

//    private synchronized void buildListsByGenre(MutableMediaMetadata compat) {
////        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicGenre =
////                new ConcurrentHashMap<>();
////        for(MutableMediaMetadata compat : mMusicListById.values()){
//        String genre = compat.mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
//
//        List<MediaMetadataCompat> list = null;
//
//        list = mMusicListByArtist.get(genre);
//        if (list == null) {
//            list = new ArrayList<>();
//            mMusicListByArtist.put(genre, list);
//        }
//        list.add(compat.mediaMetadataCompat);
//
////            if(list == null){
////                list = new ArrayList<>();
////                mMusicListByArtist.put(genre, list);
////            }
////             List<MediaMetadataCompat> list = newMusicGenre.get(genre);
////            if(list == null){
////                list = new ArrayList<>();
////                newMusicGenre.put(genre, list);
////            }
////
////            list.add(compat.mediaMetadataCompat);
////        }
////        mMusicListByArtist = newMusicGenre;
//    }

    private void fetchJSONFromUrl(final forFirebaseCallBack callBack) {
//        final List<Artist> list = new ArrayList<>();

//        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("artists");
        song_no = 0;

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Artist a = dataSnapshot.getValue(Artist.class);
                callBack.onAdd(a);
                if (++song_no >= 961) {
                    mCurrentState = State.INITIALIZED;
                    Log.i(TAG, "mCurrentState is INITIALIZED ");
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private interface forFirebaseCallBack {
        void onAdd(Artist a);
    }

    @NonNull
    private MediaMetadataCompat buildFromSong(Song song, String artist, String url) {
//        String name = json.getString(JSON_NAME);
        String name = song.getSong_name();
        String genre = "Disco";
        String title = name;
        String source = song.getSong_link();
//
//        // Media is stored relative to JSON file
//        if (!source.startsWith("http")) {
//            source = basePath + source;
//        }
//        if (!iconUrl.startsWith("http")) {
//            iconUrl = basePath + iconUrl;
//        }
        // Since we don't have a unique ID in the server, we fake one using the hashcode of
        // the music source. In a real world app, this could come from the server.
        String id = String.valueOf(source.hashCode());

        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // 生成media metadata,暂以METADATA_KEY_ALBUM_ART_URI保存播放数据源的地址
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, url)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, name)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, title)
                .build();
    }

}
