package music.com.example.liuzhe.music.util;


import android.util.Log;
import android.os.AsyncTask;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by liuzhe on 16/7/21.
 */
public class GetSongsUtil {
    private static GetSongsUtil ourInstance = new GetSongsUtil();
    private static final String getSongsUrl = "http://m.kugou.com/singer/info/?singerid=(id)&json=true";
    private static final String songInfoUrl = "http://m.kugou.com/app/i/getSongInfo.php?cmd=playInfo&hash=";
    public static GetSongsUtil getInstance() {
        return ourInstance;
    }

    private GetSongsUtil() {
    }


    public void getCount(final Integer singerid, final SongUtilCallBack callBack){

        new AsyncTask<Void, Void , Integer >(){

            @Override
            protected Integer doInBackground(Void... params) {
                String urls = getSongsUrl.replace("(id)", singerid.toString());
                Log.i("SongUtil", "songsUrl is " + urls);
                int total = 0;
                try {
                    URL url = new URL(urls);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    JsonReader jr = new JsonReader(new InputStreamReader(urlConnection.getInputStream()));

                    JsonElement element = new JsonParser().parse(jr);

                    JsonObject json = element.getAsJsonObject();

                    total = json.getAsJsonObject("songs").get("total").getAsInt();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return total;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                callBack.fetchCount(integer);
            }
        }.execute();
    }

    public void getPlayUrl(final String hash, final SongUtilCallBack callBack){
        new AsyncTask<Void, Void, String >(){

            @Override
            protected String doInBackground(Void... params) {
                String urls = songInfoUrl + hash;
                String playUrl = null;
                try {
                    URL url = new URL(urls);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    JsonReader jr = new JsonReader(new InputStreamReader(urlConnection.getInputStream()));

                    JsonElement element = new JsonParser().parse(jr);

                    JsonObject json = element.getAsJsonObject();

//                    playUrl = json.getAsJsonObject("url").getAsString();
                    playUrl = json.get("url").getAsString();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.i("get play url:", playUrl);
                return playUrl;
            }

            @Override
            protected void onPostExecute(String s) {
                callBack.getchUrl(s);
            }
        }.execute();
    }

    public int getCount(Integer singerid) {
        String urls = getSongsUrl.replace("(id)", singerid.toString());
        Log.i("SongUtil", "songsUrl is " + urls);
        int total = 0;
        try {
            URL url = new URL(urls);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            JsonReader jr = new JsonReader(new InputStreamReader(urlConnection.getInputStream()));

            JsonElement element = new JsonParser().parse(jr);

            JsonObject json = element.getAsJsonObject();

            total = json.getAsJsonObject("songs").get("total").getAsInt();
            Log.i("SongUtil", "all total " + total + " songs");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return total;
    }

    public String getPlayUrl(String hash) {
        String urls = songInfoUrl + hash;
        String playUrl = null;
        try {
            URL url = new URL(urls);
            BufferedInputStream is = null;
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            JsonReader jr = new JsonReader(new InputStreamReader(urlConnection.getInputStream()));

            JsonElement element = new JsonParser().parse(jr);

            JsonObject json = element.getAsJsonObject();

            playUrl = json.get("url").getAsString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("get play url:", playUrl);
        return playUrl;
    }

    public interface SongUtilCallBack {
        void fetchCount(Integer integer);

        void getchUrl(String s);
    }

}
