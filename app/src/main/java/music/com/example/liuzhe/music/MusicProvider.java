package music.com.example.liuzhe.music;

import android.os.AsyncTask;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    private List<Song> Songlist;

    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByGenre;
    private final ConcurrentMap<String, MutableMediaMetadata> mMusicListById;
    private int song_no = 0;

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

    public MusicProvider() {
        mMusicListById = new ConcurrentHashMap<>();
        mMusicListByGenre = new ConcurrentHashMap<>();
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    public Iterable<String> getGenres() {
        Log.i(TAG, "State is initialized ?" + String.valueOf((mCurrentState == State.INITIALIZED)));
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        //使用sortedset会将顺序排好，直接使用keyset顺序是乱的
        SortedSet<String> temp = new TreeSet<>();

        for(String t : mMusicListByGenre.keySet()){
            temp.add(t);
        }
        return temp;

//        return mMusicListByGenre.keySet();
    }

    public Iterable<MediaMetadataCompat> getMusicsByGenre(String gener) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByGenre.containsKey(gener)) {
            return Collections.emptyList();
        }

        return mMusicListByGenre.get(gener);
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
                    if(mCurrentState == State.INITIALIZED) {
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
            List<Song> tracks = fetchJSONFromUrl(new forFirebaseCallBack() {
                @Override
                public void onAdd(Song song) {
                    MediaMetadataCompat item = buildFromSong(song);
                    String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                    MutableMediaMetadata temp = new MutableMediaMetadata(musicId, item);
                    Log.i(TAG, "put song id :" + musicId);
                    mMusicListById.put(musicId, temp);
                    buildListsByGenre(temp);
                }
            });


            Log.i(TAG, "mCurrentState is INITIALIZED ");

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
//                    buildListsByGenre();
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

    private synchronized void buildListsByGenre(MutableMediaMetadata compat) {
//        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicGenre =
//                new ConcurrentHashMap<>();
//        for(MutableMediaMetadata compat : mMusicListById.values()){
        String genre = compat.mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_GENRE);

        List<MediaMetadataCompat> list = null;

        list = mMusicListByGenre.get(genre);
        if (list == null) {
            list = new ArrayList<>();
            mMusicListByGenre.put(genre, list);
        }
        list.add(compat.mediaMetadataCompat);

//            if(list == null){
//                list = new ArrayList<>();
//                mMusicListByGenre.put(genre, list);
//            }
//             List<MediaMetadataCompat> list = newMusicGenre.get(genre);
//            if(list == null){
//                list = new ArrayList<>();
//                newMusicGenre.put(genre, list);
//            }
//
//            list.add(compat.mediaMetadataCompat);
//        }
//        mMusicListByGenre = newMusicGenre;
    }

    private List<Song> fetchJSONFromUrl(final forFirebaseCallBack callBack) {
        Songlist = new ArrayList<>();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("music");
        Query query = ref.limitToFirst(1000);
        Log.i(TAG, "link to firebase.......");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.i(TAG, "the Song no :" + dataSnapshot.getKey());
                song_no ++ ;
                Song song = dataSnapshot.getValue(Song.class);
//                Songlist.add(song);
                callBack.onAdd(song);

                if(song_no >= 1000){
                    mCurrentState = State.INITIALIZED;
                }
//                MediaMetadataCompat item = buildFromSong(song);
//                String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
//                MutableMediaMetadata temp = new MutableMediaMetadata(musicId, item);
//                mMusicListById.put(musicId, temp);
//                buildListsByGenre(temp);
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

        return Songlist;
    }

    private interface forFirebaseCallBack {
        void onAdd(Song song);
    }

    private MediaMetadataCompat buildFromSong(Song song) {
//        String name = json.getString(JSON_NAME);
        String name = song.getName();
        String[] temp = name.split("-");
        String artist = temp[0];
        String genre = temp[0];
        String title = temp[1];
        String source = song.getLink();
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
        // sample for convenience only.
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, title)
                .build();
    }

}
