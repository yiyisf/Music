package music.com.example.liuzhe.music;

import android.app.Application;

/**
 * Created by liuzhe on 2016/6/12.
 */
public class MusicApplication extends Application {
    private static MusicApplication context;

    public static MusicApplication getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
