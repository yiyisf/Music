package music.com.example.liuzhe.music.model;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by liuzhe on 2016/6/8.
 */
public class Song implements Parcelable {
    String song_name;
    String song_link;

    public Song() {
    }

    public Song(String song_name, String song_link) {
        this.song_name = song_name;
        this.song_link = song_link;
    }

    protected Song(Parcel in) {
        song_name = in.readString();
        song_link = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    public String getSong_link() {
        return song_link;
    }

    public void setSong_link(String song_link) {
        this.song_link = song_link;
    }

    @Override
    public String toString() {
        return "Song{" +
                "song_name='" + song_name + '\'' +
                ", song_link='" + song_link + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(song_name);
        dest.writeString(song_link);
    }
}
