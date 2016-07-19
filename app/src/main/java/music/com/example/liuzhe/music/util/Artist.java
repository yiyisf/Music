package music.com.example.liuzhe.music.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by liuzhe on 2016/7/17.
 */
public class Artist implements Parcelable {
    private String name;
    private String photoUrl;
    private List<Song> songs;



    public Artist(String name, String photoUrl, List<Song> songs) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.songs = songs;
    }


    protected Artist(Parcel in) {
        name = in.readString();
        photoUrl = in.readString();
        songs = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public Artist() {
    }

    @Override
    public String toString() {
        return "Artist{" +
                "name='" + name + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", songs=" + songs +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(photoUrl);
        dest.writeTypedList(songs);
    }
}
