package music.com.example.liuzhe.music.util;

/**
 * Created by liuzhe on 2016/7/17.
 */
public class Artist {
    private String name;
    private String photoUrl;

    public Artist(String name, String photoUrl) {
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
