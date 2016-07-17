package music.com.example.liuzhe.music;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class ArtistActivity extends AppCompatActivity {
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        recycler = (RecyclerView) findViewById(R.id.artist_list);
        recycler.setHasFixedSize(true);

        recycler.setLayoutManager(new LinearLayoutManager(this));



    }
}
