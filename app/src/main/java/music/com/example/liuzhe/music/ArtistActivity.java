package music.com.example.liuzhe.music;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import music.com.example.liuzhe.music.model.Artist;

public class ArtistActivity extends AppCompatActivity implements Thread.UncaughtExceptionHandler {
    private ListViewCompat listViewCompat;
    private RecyclerView recycler;
    private ProgressBar progressBar;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!=null) {
            getSupportActionBar().setTitle("Disco歌手");
        }

//        Use listview
        listViewCompat = (ListViewCompat) findViewById(R.id.artist_list);
        final FirebaseListAdapter<Artist> adapter;

//        USE RecyclerView
//        recycler = (RecyclerView) findViewById(R.id.artist_list);
//        recycler.setHasFixedSize(true);
//        recycler.setLayoutManager(new LinearLayoutManager(this));
//        FirebaseRecyclerAdapter<Artist, ArtistViewHoder> adapter;

        Query ref = FirebaseDatabase.getInstance().getReference().child("Artist");

//        use listView
        adapter = new FirebaseListAdapter<Artist>(this, Artist.class, R.layout.artist_item, ref) {
            @Override
            protected void populateView(final View v, Artist model, int position) {
                progressBar.setVisibility(View.INVISIBLE);
                final String name = model.getName();
                String url = model.getPhotoUrl();
                AppCompatTextView nameText = (AppCompatTextView) v.findViewById(R.id.artist_name);
                AppCompatImageView image = (AppCompatImageView) v.findViewById(R.id.artist_photo);
                nameText.setText(name);
                Glide.with(ArtistActivity.this)
                        .load(url)
                        .into(image);
            }
        };
//        USE RecyclerView
//        adapter = new FirebaseRecyclerAdapter<Artist, ArtistViewHoder>(
//                Artist.class,
//                R.layout.artist_item,
//                ArtistViewHoder.class,
//                ref) {
//
//            @Override
//            protected void populateViewHolder(ArtistViewHoder viewHolder, Artist artist, int position) {
//                progressBar.setVisibility(ProgressBar.INVISIBLE);
//                viewHolder.nameView.setText(artist.getName());
//
//                Glide.with(ArtistActivity.this)
//                        .load(artist.getPhotoUrl())
//                        .into(viewHolder.imageView);
//            }
//        };

        listViewCompat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = adapter.getItem(position);

                Intent i = new Intent(ArtistActivity.this, SongsActivity.class);
                i.putExtra("artist", artist);
                startActivity(i);
            }
        });

        listViewCompat.setAdapter(adapter);

//        recycler.setAdapter(adapter);

    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
    }
}
