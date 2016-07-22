package music.com.example.liuzhe.music.model;

/**
 * Created by liuzhe on 16/7/21.
 */
public class ChSong {

    String album_id;
    Integer bitrate;
    Integer duration;
    String extname;
    Integer feetype;
    String filename;
    Integer filesize;
    Integer has_accompany;
    String hash;
    Integer inlist;
    Integer m4afilesize;
    String mvhash;
    Integer privilege;
    Integer sqfilesize;
    String sqhash;
    Integer sqprivilege;

    public ChSong() {
    }

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getExtname() {
        return extname;
    }

    public void setExtname(String extname) {
        this.extname = extname;
    }

    public Integer getFeetype() {
        return feetype;
    }

    public void setFeetype(Integer feetype) {
        this.feetype = feetype;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getFilesize() {
        return filesize;
    }

    public void setFilesize(Integer filesize) {
        this.filesize = filesize;
    }

    public Integer getHas_accompany() {
        return has_accompany;
    }

    public void setHas_accompany(Integer has_accompany) {
        this.has_accompany = has_accompany;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getInlist() {
        return inlist;
    }

    public void setInlist(Integer inlist) {
        this.inlist = inlist;
    }

    public Integer getM4afilesize() {
        return m4afilesize;
    }

    public void setM4afilesize(Integer m4afilesize) {
        this.m4afilesize = m4afilesize;
    }

    public String getMvhash() {
        return mvhash;
    }

    public void setMvhash(String mvhash) {
        this.mvhash = mvhash;
    }

    public Integer getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Integer privilege) {
        this.privilege = privilege;
    }

    public Integer getSqfilesize() {
        return sqfilesize;
    }

    public void setSqfilesize(Integer sqfilesize) {
        this.sqfilesize = sqfilesize;
    }

    public String getSqhash() {
        return sqhash;
    }

    public void setSqhash(String sqhash) {
        this.sqhash = sqhash;
    }

    public Integer getSqprivilege() {
        return sqprivilege;
    }

    public void setSqprivilege(Integer sqprivilege) {
        this.sqprivilege = sqprivilege;
    }

    @Override
    public String toString() {
        return "ChSong{" +
                "album_id='" + album_id + '\'' +
                ", bitrate=" + bitrate +
                ", duration=" + duration +
                ", extname='" + extname + '\'' +
                ", feetype=" + feetype +
                ", filename='" + filename + '\'' +
                ", filesize=" + filesize +
                ", has_accompany=" + has_accompany +
                ", hash='" + hash + '\'' +
                ", inlist='" + inlist + '\'' +
                ", m4afilesize=" + m4afilesize +
                ", mvhash='" + mvhash + '\'' +
                ", privilege=" + privilege +
                ", sqfilesize=" + sqfilesize +
                ", sqhash='" + sqhash + '\'' +
                ", sqprivilege=" + sqprivilege +
                '}';
    }
}
