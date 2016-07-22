package music.com.example.liuzhe.music.util;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import music.com.example.liuzhe.music.MusicProvider;
import music.com.example.liuzhe.music.MusicService;

/**
 * Created by liuzhe on 2016/6/8.
 */
public class QueueHelper {
    public static final String MEDIA_ID_MUSICS_BY_ARTIST = "__BY_ARTIST__";
    public static final String MEDIA_ID_MUSICS_BY_SEARCH = "__BY_SEARCH__";
    private static final String TAG = "QueueHelper";


    public static List<MediaSessionCompat.QueueItem> getRandomQueue(MusicProvider provider, String type) {
        //获取基本信息
        Iterator<String > iterator = provider.getArtists(type).iterator();
        //检查是否为空,若为空则返回一个空list
        if(!iterator.hasNext()){
            return Collections.emptyList();
        }

        String artistname = iterator.next();
        Log.i(TAG, "iterator gerner is :" + artistname);
        Iterable<MediaMetadataCompat> tracks = provider.getMusicsByArtistName(type, artistname);
        Log.i(TAG, "get all gener is :" + tracks);
        return convertToQueue(tracks, MEDIA_ID_MUSICS_BY_ARTIST, artistname);
    }

    private static List<MediaSessionCompat.QueueItem> convertToQueue(
            Iterable<MediaMetadataCompat> tracks, String... categories) {

        List<MediaSessionCompat.QueueItem> queue = new ArrayList<>();

        int count = 0;
        for(MediaMetadataCompat metadata : tracks){
            String id = MediaIDHelper.createMediaID(
                    metadata.getDescription().getMediaId(), categories
            );
            Log.i(TAG, "build a queuetem id is :" + id);
            MediaMetadataCompat newMeta = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                    .build();

            MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(
                    newMeta.getDescription(), count++
            );


            queue.add(queueItem);
        }

        return queue;
    }


    public static boolean isIndexPlayable(int index, List<MediaSessionCompat.QueueItem> queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }

    public static List<MediaSessionCompat.QueueItem> getPlayingQueue(String mediaId, MusicProvider provider, String type) {

        // extract the browsing hierarchy from the media ID:
        final String[] hierarchy = MediaIDHelper.getHierarchy(mediaId);

        if (hierarchy.length != 2) {
            Log.e(TAG, "Could not build a playing queue for this mediaId: " + mediaId);
            return null;
        }

        String categoryType = hierarchy[0];
        String categoryValue = hierarchy[1];
        Log.i(TAG, "Creating playing queue for " + categoryType + ",  " + categoryValue);

        Iterable<MediaMetadataCompat> tracks = null;
        // This sample only supports genre and by_search category types.
        if (categoryType.equals(MEDIA_ID_MUSICS_BY_ARTIST)) {
            switch (type){
                case MusicService.DISCO_MUSIC:
                    tracks = provider.getMusicsByArtistName(type, categoryValue);
                    break;
                case MusicService.CHINESE_MUSIC:
                    tracks = provider.getSongsQueueByArtistName(categoryValue);
                    break;
                default:
                    break;
            }
        }

        if (tracks == null) {
            Log.e(TAG, "Unrecognized category type: " + categoryType + " for media " + mediaId);
            return null;
        }
        return convertToQueue(tracks, hierarchy[0], hierarchy[1]);

    }

    public static int getMusicIndexOnQueue(List<MediaSessionCompat.QueueItem> queue, String mediaId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (mediaId.equals(item.getDescription().getMediaId())) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
