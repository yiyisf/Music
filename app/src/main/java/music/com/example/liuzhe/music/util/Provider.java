package music.com.example.liuzhe.music.util;

/**
 * Created by liuzhe on 16/7/19.
 */
public class Provider {
    private static Provider ourInstance = new Provider();

    public static Provider getInstance() {
        return ourInstance;
    }

    private Provider() {
    }
}
