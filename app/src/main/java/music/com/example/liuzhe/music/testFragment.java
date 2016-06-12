package music.com.example.liuzhe.music;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by liuzhe on 2016/6/8.
 */
public class testFragment extends Fragment {

    public static Fragment newInstence(){
        testFragment test = new testFragment();
        return test;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);
        return root;
    }
}
