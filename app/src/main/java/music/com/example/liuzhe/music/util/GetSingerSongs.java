package music.com.example.liuzhe.music.util;


import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import music.com.example.liuzhe.music.model.ChSong;

/**
 * Created by liuzhe on 16/7/21.
 */
public class GetSingerSongs {
    private static GetSingerSongs ourInstance = new GetSingerSongs();
    private static final String getSongsUrl = "http://m.kugou.com/singer/info/?singerid=(*)&json=true";
    public static GetSingerSongs getInstance() {
        return ourInstance;
    }

    private GetSingerSongs() {
    }

    public List<ChSong> getList(String singerid) {
        List<ChSong> list = new ArrayList<>();
        boolean isEnd = false;
        String urls = getSongsUrl.replace("(*)", singerid);

        try {
            URL url = new URL(urls);
            BufferedInputStream is = null;
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            JsonReader jr = new JsonReader(new InputStreamReader(urlConnection.getErrorStream()));


        } catch (IOException e) {
            e.printStackTrace();
        }
//        is = new BufferedInputStream(new ByteArrayInputStream())


        return list;
    }
}
