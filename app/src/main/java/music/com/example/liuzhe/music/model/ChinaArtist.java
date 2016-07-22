package music.com.example.liuzhe.music.model;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by liuzhe on 16/7/21.
 */
public class ChinaArtist implements Parcelable {

    private Integer albumcount;
    private String intro;
    private Integer mvcount;
    private String imgurl;
    private Integer singerid;
    private String singername;
    private Integer songcount;

    public Integer getAlbumcount() {
        return albumcount;
    }

    public void setAlbumcount(Integer albumcount) {
        this.albumcount = albumcount;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public Integer getMvcount() {
        return mvcount;
    }

    public void setMvcount(Integer mvcount) {
        this.mvcount = mvcount;
    }

    public Integer getSongcount() {
        return songcount;
    }

    public void setSongcount(Integer songcount) {
        this.songcount = songcount;
    }

    public ChinaArtist() {
    }

    protected ChinaArtist(Parcel in) {
        imgurl = in.readString();
        singerid = in.readInt();
        singername = in.readString();
    }

    public static final Creator<ChinaArtist> CREATOR = new Creator<ChinaArtist>() {
        @Override
        public ChinaArtist createFromParcel(Parcel in) {
            return new ChinaArtist(in);
        }

        @Override
        public ChinaArtist[] newArray(int size) {
            return new ChinaArtist[size];
        }
    };

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public Integer getSingerid() {
        return singerid;
    }

    public void setSingerid(Integer singerid) {
        this.singerid = singerid;
    }

    public String getSingername() {
        return singername;
    }

    public void setSingername(String singername) {
        this.singername = singername;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imgurl);
        dest.writeInt(singerid);
        dest.writeString(singername);
    }

    @Override
    public String toString() {
        return "ChinaArtist{" +
                "imgurl='" + imgurl + '\'' +
                ", singerid=" + singerid +
                ", singername='" + singername + '\'' +
                '}';
    }
}
