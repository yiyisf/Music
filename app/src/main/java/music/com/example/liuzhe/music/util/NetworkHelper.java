package music.com.example.liuzhe.music.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by liuzhe on 2016/6/11.
 * 检查是否已连接网络
 */
public class NetworkHelper {


    static final String TAG = "NetworkHelper";

    public static boolean isOnline(Context context){
        Log.i(TAG, "for networkHelper context is :" + context);
        ConnectivityManager managerCompat = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = managerCompat.getActiveNetworkInfo();

        return (info !=null && info.isConnected());
    }

}
