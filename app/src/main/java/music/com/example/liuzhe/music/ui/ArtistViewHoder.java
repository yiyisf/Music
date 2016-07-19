package music.com.example.liuzhe.music.ui;

import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import music.com.example.liuzhe.music.R;
import music.com.example.liuzhe.music.util.Artist;

/**
 * Created by liuzhe on 2016/7/17.
 */
public class ArtistViewHoder extends RecyclerView.ViewHolder {
    public AppCompatImageView imageView;

    public AppCompatTextView nameView;

    public ArtistViewHoder(View itemView) {
        super(itemView);
        Log.i("ArtistViewhoder", "get hoder");
        imageView = (AppCompatImageView) itemView.findViewById(R.id.artist_photo);

        nameView = (AppCompatTextView) itemView.findViewById(R.id.artist_name);
    }

//    public void bindToItem(Artist artist){
//        nameView.setText(artist.getName());
//    }
}
