package music.com.example.liuzhe.music;

import android.app.Application;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by liuzhe on 2016/6/12.
 */
public class MusicApplication extends Application {
    private static MusicApplication context;
    private static GoogleApiClient mGoogleApiClient;
    private static GoogleSignInOptions googleSignInOptions;

    public static StorageReference getStorageReference() {
        return storageReference;
    }

    private static StorageReference storageReference;


    public static GoogleSignInAccount getGoogleSignInAccount() {
        return googleSignInAccount;
    }

    public static void setGoogleSignInAccount(GoogleSignInAccount googleSignInAccount) {
        MusicApplication.googleSignInAccount = googleSignInAccount;
    }

    public static GoogleSignInAccount googleSignInAccount;

    public static GoogleSignInOptions getGoogleSignInOptions() {
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        return googleSignInOptions;
    }

    public static GoogleApiClient getmGoogleApiClient() {

        return mGoogleApiClient;
    }


    public static MusicApplication getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, getGoogleSignInOptions())
                .build();
        storageReference = FirebaseStorage.getInstance().getReference();
    }
}
