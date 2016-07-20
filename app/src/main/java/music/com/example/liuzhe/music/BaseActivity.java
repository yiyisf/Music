package music.com.example.liuzhe.music;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by liuzhe on 16/7/18.
 */
public class BaseActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private FirebaseUser user;

    private DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {

        }

        @Override
        public void onDrawerClosed(View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {
            Log.i("Base slide:", String.valueOf(newState));
        }
    };

    private android.app.FragmentManager.OnBackStackChangedListener mBackStackChangedListener =
            new android.app.FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    updateDrawerToggle();
                }
            };
    private int selectItem = -1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkLoginstatus();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.i("Base :", "fragment acount:" + fragmentManager.getBackStackEntryCount());
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getFragmentManager().removeOnBackStackChangedListener(mBackStackChangedListener);
    }

    private void checkLoginstatus() {
//        gso = MusicApplication.getGoogleSignInOptions();
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();
//
//        OptionalPendingResult<GoogleSignInResult> pendingResult =
//                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
//        if (pendingResult.isDone()) {
//            GoogleSignInAccount googleAccount = pendingResult.get().getSignInAccount();
//            Toast.makeText(this, "已登录google user：" + googleAccount.getDisplayName(), Toast.LENGTH_SHORT).show();
//            MusicApplication.setGoogleSignInAccount(googleAccount);
//        } else {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "可使用google或emal登录：", Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "已登录firebase user：" + auth.getAuth().get("token"), Toast.LENGTH_SHORT).show();
            startLogin();
        }
    }


    private void startLogin() {
//        Class classname = null;
//        if(MusicActivity.class.isAssignableFrom(getClass())){
//            classname = MusicActivity.class;
//        }
        Intent i = new Intent(BaseActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void initializeToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            throw new IllegalStateException("Layout is required to include a Toolbar with id " +
                    "'toolbar'");
        }
//        mToolbar.inflateMenu(R.menu.main);  //菜单工具栏,暂不添加,稍后增加相关功能
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            // Create an ActionBarDrawerToggle that will handle opening/closing of the drawer:
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    mToolbar, R.string.open_content_drawer, R.string.close_content_drawer);
//            mDrawerLayout.setDrawerListener(mDrawerListener);
            mDrawerLayout.addDrawerListener(mDrawerListener);
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

            if (navigationView != null) {
                Log.i("base :", "header count is:" + navigationView.getHeaderCount());
                if(navigationView.getHeaderCount() > 0) {
                    ((TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name)).setText(user.getEmail());
                }
                populateDrawerItems(navigationView);
            }
            setSupportActionBar(mToolbar);
            updateDrawerToggle();
        } else {
            setSupportActionBar(mToolbar);
        }
    }

    private void populateDrawerItems(NavigationView view) {
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);

                selectItem = item.getItemId();
//                mDrawerLayout.closeDrawers();    //关闭当前窗口
                onNavItemSelect(selectItem);
                return true;
            }
        });


        if (MusicActivity.class.isAssignableFrom(getClass())) {
            view.setCheckedItem(R.id.disco);
        }
    }

    protected void onNavItemSelect(int selectItem) {

    }

    private void updateDrawerToggle() {

        if (mDrawerToggle == null) {
            return;
        }
        boolean isRoot = getFragmentManager().getBackStackEntryCount() == 0;
        mDrawerToggle.setDrawerIndicatorEnabled(isRoot);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(!isRoot);
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRoot);
            getSupportActionBar().setHomeButtonEnabled(!isRoot);
        }
        if (isRoot) {
            mDrawerToggle.syncState();
        }
    }


    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mToolbar.setTitle(title);
    }

}
