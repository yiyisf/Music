package music.com.example.liuzhe.music;

import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

/**
 * Created by liuzhe on 2016/6/7.
 */
public class MutableMediaMetadata {
    public MediaMetadataCompat mediaMetadataCompat;
    public final String trackId;

    public MutableMediaMetadata(String trackId, MediaMetadataCompat mediaMetadataCompat){
        this.mediaMetadataCompat = mediaMetadataCompat;
        this.trackId = trackId;
    }

    @Override
    public int hashCode() {
        return trackId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if(o == null || o.getClass()!= MediaMetadataCompat.class){
            return false;
        }
        MutableMediaMetadata that = (MutableMediaMetadata) o;

        return TextUtils.equals(trackId, that.trackId);
    }
}
