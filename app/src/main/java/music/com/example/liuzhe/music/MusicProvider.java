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

import music.com.example.liuzhe.music.Interface.forChSingerCallBack;
import music.com.example.liuzhe.music.Interface.forChsongCallBack;
import music.com.example.liuzhe.music.Interface.forFirebaseCallBack;
import music.com.example.liuzhe.music.model.Artist;
import music.com.example.liuzhe.music.model.ChSong;
import music.com.example.liuzhe.music.model.ChinaArtist;
import music.com.example.liuzhe.music.model.Song;
import music.com.example.liuzhe.music.util.GetSingerSongs;
import music.com.example.liuzhe.music.util.GetSongsUtil;

/**
 * Created by liuzhe on 2016/6/7.
 */
public class MusicProvider {
    private static final String TAG = "MusicProvider";

    public static final String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";

    private static final String JSON_GENRE = "genre";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_NAME = "name";

    private static MusicProvider provider = new MusicProvider();

    //    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByArtist;
//    private ConcurrentMap<ArtistInfo, List<MediaMetadataCompat>> mMusicListByArtist;
    private ConcurrentMap<String, Artist> mMusicListByArtist;
    private ConcurrentMap<String, ChinaArtist> mMusicListByChArtist;
    private ConcurrentHashMap<String, List<MediaMetadataCompat>> mChSongslist;
    private final ConcurrentMap<String, MutableMediaMetadata> mMusicListById;
    private static int song_no = 0;
    private GetSingerSongs getChSongs = GetSingerSongs.getInstance();

    public MediaMetadataCompat getMusic(String id) {
        Log.i(TAG, "getmusic id is:" + id);
        return mMusicListById.containsKey(id) ? mMusicListById.get(id).mediaMetadataCompat : null;

//        return null;
    }

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;
    private volatile State mCurrentState_C = State.NON_INITIALIZED;
    private volatile int temp_song_count = 0;
    private volatile boolean isEnding = false;


    public interface Callback {
        void onMusicCatalogReady(boolean success);
        void onMusicSongsReady(boolean success, Iterable<MediaMetadataCompat> iterable);
    }

    public static MusicProvider getInstence() {
        return provider;
    }

    private MusicProvider() {
        mMusicListById = new ConcurrentHashMap<>();
        mMusicListByArtist = new ConcurrentHashMap<>();
        mMusicListByChArtist = new ConcurrentHashMap<>();
        mChSongslist = new ConcurrentHashMap<>();
    }

    public boolean isInitialized(String type) {
        if (type.equals(MusicService.DISCO_MUSIC)) {
            return mCurrentState == State.INITIALIZED;
        } else {
            return mCurrentState_C == State.INITIALIZED;
        }

//        return false;
    }

    public Iterable<String> getArtists(String type) {
        //使用sortedset会将顺序排好，直接使用keyset顺序是乱的
        SortedSet<String> temp = new TreeSet<>();
        Log.i(TAG, "State is initialized ?" + String.valueOf((mCurrentState == State.INITIALIZED)));
        if(type.equals(MusicService.DISCO_MUSIC)) {
            if (mCurrentState != State.INITIALIZED) {
                return Collections.emptyList();
            }

            for (String i : mMusicListByArtist.keySet()) {
                temp.add(i);
            }
        }else {
            if (mCurrentState_C != State.INITIALIZED) {
                return Collections.emptyList();
            }

            for (String i : mMusicListByChArtist.keySet()) {
                temp.add(i);
            }
        }
        return temp;

//        return mMusicListByArtist.keySet();
    }

    public Uri getArtistImageUri(String type, String artistName) {
        if(type.equals(MusicService.DISCO_MUSIC)) {
            if (mCurrentState != State.INITIALIZED || !mMusicListByArtist.containsKey(artistName)) {
                return null;
            }

            return Uri.parse(mMusicListByArtist.get(artistName).getPhotoUrl());
        }else if(type.equals(MusicService.CHINESE_MUSIC)){
            if (mCurrentState_C != State.INITIALIZED || !mMusicListByChArtist.containsKey(artistName)) {
                return null;
            }

            return Uri.parse(mMusicListByChArtist.get(artistName).getImgurl());
        }else {
            return null;
        }
    }

    /**
     * 获取disco歌手的播放列表
     * @param type
     * @param artistName
     * @return
     */
    public Iterable<MediaMetadataCompat> getMusicsByArtistName(String type, String artistName) {
        List<MediaMetadataCompat> list = new ArrayList<>();
        if(type.equals(MusicService.DISCO_MUSIC)) {
            if (mCurrentState != State.INITIALIZED || !mMusicListByArtist.containsKey(artistName)) {
                return Collections.emptyList();
            }
            Log.i(TAG, "get by : " + artistName);
            Artist a = mMusicListByArtist.get(artistName);
            for (Song song : a.getSongs()) {
                MediaMetadataCompat item = buildFromSong(song, a.getName(), a.getPhotoUrl());
                list.add(item);

                String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                MutableMediaMetadata temp = new MutableMediaMetadata(musicId, item);
                mMusicListById.putIfAbsent(musicId, temp);
            }
        }else if(type.equals(MusicService.CHINESE_MUSIC)){

        }

        return list;
    }

    private synchronized List<ChSong> getChSongsList(final Integer singerid) {
        Log.i(TAG, "get songs for id:" + singerid);
        final List<ChSong> list = new ArrayList<>();
        isEnding = false;
        final int count = GetSongsUtil.getInstance().getCount(singerid);
        temp_song_count = 0;

        FetchSongs(singerid, new forChsongCallBack(){

            @Override
            public void onAdd(ChSong a) {
                if(!isEnding) {
                    list.add(a);
                    temp_song_count ++;
                    if(temp_song_count >= count){
                        isEnding = true;
                    }
                }
            }
        });


        while (true){
            if(isEnding) {
                return list;
            }else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return list;
                }
            }
        }

    }

    private void FetchSongs(Integer singerid, final forChsongCallBack callBack) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("kugousingerSongs").child(singerid.toString());

        Log.i(TAG, "connect to firebase....");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                callBack.onAdd(dataSnapshot.getValue(ChSong.class));
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

    public Artist getArtistByArtistName(String artistName) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByArtist.containsKey(artistName)) {
            return null;
        }

        return mMusicListByArtist.get(artistName);

    }


    public ChinaArtist getArtistByChArtistName(String name) {
        if (mCurrentState_C != State.INITIALIZED || !mMusicListByChArtist.containsKey(name)) {
            return null;
        }

        return mMusicListByChArtist.get(name);
    }

    /**
     * 异步加载歌手的歌曲列表
     * @param artistname
     * @param callback
     */

    public void retrieveSongsAsync(final String artistname, final Callback callback) {

        Log.i(TAG, "get by : " + artistname);
        if(!mChSongslist.isEmpty() && mChSongslist.containsKey(artistname)){
            callback.onMusicSongsReady(true, mChSongslist.get(artistname));
            return;
        }

        new AsyncTask<Void, Void, List<MediaMetadataCompat> >() {

            @Override
            protected List<MediaMetadataCompat> doInBackground(Void... params) {
                List<MediaMetadataCompat> list = new ArrayList<>();
                ChinaArtist a = mMusicListByChArtist.get(artistname);
                List<ChSong> list_songs = getChSongsList(a.getSingerid());

                for (ChSong song : list_songs) {
                    MediaMetadataCompat item = buildFromChSong(song, a.getSingername(), a.getImgurl());
                    list.add(item);

                    String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                    MutableMediaMetadata temp = new MutableMediaMetadata(musicId, item);
                    mMusicListById.putIfAbsent(musicId, temp);
                }
                return list;
            }

            @Override
            protected void onPostExecute(List<MediaMetadataCompat> list) {
                if(list!=null) {
                    mChSongslist.put(artistname, list);
                    callback.onMusicSongsReady(true, list);
                }else {
                    callback.onMusicSongsReady(false, null);
                }
            }
        }.execute();
    }


    public Iterable<MediaMetadataCompat> getSongsQueueByArtistName(String artistname){
        if(!mChSongslist.isEmpty() && mChSongslist.containsKey(artistname)){
            return mChSongslist.get(artistname);
        }
        return Collections.<MediaMetadataCompat>emptyList();
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId and grouping by genre.
     */
    public void retrieveMediaAsync(String type, final Callback callback) {
        Log.i(TAG, "retrieveMediaAsync called" + String.valueOf(mCurrentState == State.INITIALIZED));
        if (type.equals(MusicService.DISCO_MUSIC) && mCurrentState == State.INITIALIZED) {
            // Nothing to do, execute callback immediately
            callback.onMusicCatalogReady(true);
            return;
        }

        if (type.equals(MusicService.CHINESE_MUSIC) && mCurrentState_C == State.INITIALIZED) {
            // Nothing to do, execute callback immediately
            callback.onMusicCatalogReady(true);
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<String, Void, State>() {

            @Override
            protected State doInBackground(String... params) {
                String inType = params[0];
                retrieveMedia(inType);

//                Log.i(TAG, "cat status is begin listening");
                while (true) {
                    if (inType.equals(MusicService.DISCO_MUSIC) && mCurrentState == State.INITIALIZED) {
//                        Log.i(TAG, "cat status is initialized");
                        return mCurrentState;
                    }

                    if (inType.equals(MusicService.CHINESE_MUSIC) && mCurrentState_C == State.INITIALIZED) {
//                        Log.i(TAG, "cat status is initialized");
                        return mCurrentState_C;
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute(type);
    }

    //解析返回的JSON及组装mMusicListById
    private synchronized void retrieveMedia(String inType) {

        if (inType.equals(MusicService.DISCO_MUSIC) && mCurrentState == State.NON_INITIALIZED) {
            mCurrentState = State.INITIALIZING;
            fetchJSONForDisco(new forFirebaseCallBack() {
                @Override
                public void onAdd(Artist a) {
                    buildListsByArtist(a);
                }
            });
        }else if(inType.equals(MusicService.CHINESE_MUSIC) && mCurrentState_C == State.NON_INITIALIZED){
            mCurrentState_C = State.INITIALIZING;
            fetchJSONForChinese(new forChSingerCallBack() {
                @Override
                public void onadd(ChinaArtist a) {
                    buildListsByCHArtist(a);
                }
            });
        }
    }

    private synchronized void buildListsByArtist(Artist a) {
        mMusicListByArtist.putIfAbsent(a.getName(), a);
    }


    private synchronized void buildListsByCHArtist(ChinaArtist a) {
        mMusicListByChArtist.putIfAbsent(a.getSingername(), a);
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

    //Disco数据获取
    private void fetchJSONForDisco(final forFirebaseCallBack callBack) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("artists");
        song_no = 0;
        Log.i(TAG, "begin to connect to firebase....");
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

    //华语歌手数据获取
    private void fetchJSONForChinese(final forChSingerCallBack callBack) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("kugousinger");
        song_no = 0;
        Log.i(TAG, "begin to connect to firebase....");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChinaArtist a = dataSnapshot.getValue(ChinaArtist.class);
                callBack.onadd(a);
                if (++song_no >= 390) {
                    mCurrentState_C = State.INITIALIZED;
                    Log.i(TAG, "mCurrentState_C is INITIALIZED ");
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

    //
    @NonNull
    private MediaMetadataCompat buildFromChSong(ChSong song, String artist, String url) {
        String name = song.getFilename();
        String genre = artist;
        String title = name;
        String source = song.getHash();

        String id = String.valueOf(source.hashCode());

        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // 生成media metadata,暂以METADATA_KEY_ALBUM_ART_URI保存播放数据源的HASH,待播放时请求播放地址
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
