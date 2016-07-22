package music.com.example.liuzhe.music.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import music.com.example.liuzhe.music.MusicApplication;
import music.com.example.liuzhe.music.R;
import music.com.example.liuzhe.music.util.AlbumArtCache;
import music.com.example.liuzhe.music.model.Artist;
import music.com.example.liuzhe.music.util.NetworkHelper;

/**
 * Created by liuzhe on 2016/6/7.
 */
public class ArtistFragment extends Fragment {
    private static final String TAG = "ArtistFragment";
    //    private MediaBrowserCompat mMediaBrowser;
    private View errorView;
    private TextView errorMsgView;
    private ProgressBar mProgressBar;
    private AlbumArtCache cache;
    private DatabaseReference mData;
    private RecyclerView recycler;
    private LinearLayoutManager mManager;
    private Query Aquery;
    private FirebaseRecyclerAdapter<Artist, ArtistViewHoder> mAdapter;

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "fragment销毁");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "Starting.....");
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.activity_artist, container, false);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        mData = FirebaseDatabase.getInstance().getReference().child("Artist");

//        Aquery = FirebaseDatabase.getInstance().getReference().child("Artist").limitToFirst(100);

        recycler = (RecyclerView) root.findViewById(R.id.artist_list);
        recycler.setHasFixedSize(true);
        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
//        mManager.setReverseLayout(true);
//        mManager.setStackFromEnd(true);


        // Set up FirebaseRecyclerAdapter with the Query
//        Query postsQuery = getQuery(mData);
        Log.i(TAG, "connect to firebase...");
        Log.i(TAG, FirebaseAuth.getInstance().getCurrentUser().getEmail());

        mAdapter = new FirebaseRecyclerAdapter<Artist, ArtistViewHoder>(
                Artist.class,
                R.layout.artist_item,
                ArtistViewHoder.class,
                mData) {
            @Override
            protected void populateViewHolder(ArtistViewHoder viewHolder, Artist model, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                Log.i(TAG, getItem(position).toString());
                Log.i(TAG, getItem(position).getName());
                viewHolder.nameView.setText(model.getName());
//                viewHolder.bindToItem(model);

            }

            @Override
            public ArtistViewHoder onCreateViewHolder(ViewGroup parent, int viewType) {
                Log.i(TAG, "create view hoder");
                return super.onCreateViewHolder(parent, viewType);
            }

            @Override
            public void onBindViewHolder(ArtistViewHoder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
            }
        };

//        mAdapter = new FirebaseRecyclerAdapter<Artist, ArtistViewHoder>(
//                Artist.class,
//                R.layout.activity_artist,
//                ArtistViewHoder.class,
//                mData) {
//
//            @Override
//            protected void populateViewHolder(ArtistViewHoder viewHolder, Artist artist, int position) {
//                Log.i(TAG, artist.getName());
////                viewHolder.bindToItem(artist);
//            }
//        };

        recycler.setLayoutManager(mManager);
        recycler.setAdapter(mAdapter);
        Log.i(TAG, "Created");
        return root;
    }

    private void checkForUserVisibleErrors(boolean forceError) {
        boolean showError = forceError;
        // If offline, message is about the lack of connectivity:
        if (!NetworkHelper.isOnline(MusicApplication.getContext())) {
            errorMsgView.setText(R.string.error_no_connection);
            showError = true;
        } else {
            showError = false;
        }
        errorView.setVisibility(showError ? View.VISIBLE : View.GONE);
    }
}
