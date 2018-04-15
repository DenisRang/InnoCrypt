package com.sansara.develop.innocrypt;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sansara.develop.innocrypt.data.SharedPreferenceHelper;
import com.sansara.develop.innocrypt.data.StaticConfig;
import com.sansara.develop.innocrypt.model.User;
import com.sansara.develop.innocrypt.service.ServiceUtils;
import com.sansara.develop.innocrypt.ui.FriendsFragment;
import com.sansara.develop.innocrypt.ui.GroupsFragment;
import com.sansara.develop.innocrypt.ui.LoginActivity;
import com.sansara.develop.innocrypt.ui.UserProfileFragment;
import com.sansara.develop.innocrypt.util.ImageUtils;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout drawer;
    private ImageView imageViewNavAvatar;
    private TextView textViewNavName;
    private TextView textViewNavEmail;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);

        initFirebase();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        imageViewNavAvatar = navigationView.getHeaderView(0).findViewById(R.id.image_nav_avatar);
        textViewNavName = navigationView.getHeaderView(0).findViewById(R.id.text_nav_name);
        textViewNavEmail = navigationView.getHeaderView(0).findViewById(R.id.text_nav_email);

        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User myAccount = dataSnapshot.getValue(User.class);

                if (textViewNavName != null) {
                    textViewNavName.setText(myAccount.name);
                }
                if (textViewNavEmail != null) {
                    textViewNavEmail.setText(myAccount.email);
                }

                setImageAvatar(getBaseContext(), myAccount.avata);
                SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(getBaseContext());
                preferenceHelper.saveUserInfo(myAccount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(UserProfileFragment.class.getName(), "loadPost:onCancelled", databaseError.toException());
            }
        });

        selectDrawerItem(navigationView.getMenu().getItem(0));
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    private void setImageAvatar(Context context, String imgBase64) {
        try {
            Resources res = getResources();
            Bitmap src;
            if (imgBase64.equals("default")) {
                src = BitmapFactory.decodeResource(res, R.drawable.default_avata);
            } else {
                byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
                src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }

            imageViewNavAvatar.setImageDrawable(ImageUtils.roundedImage(context, src));
        } catch (Exception e) {
        }
    }

    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.nav_chats:
                fragmentClass = FriendsFragment.class;
                break;
            case R.id.nav_group_chats:
                fragmentClass = GroupsFragment.class;
                break;
            case R.id.nav_settings:
                fragmentClass = UserProfileFragment.class;
                break;
            default:
                fragmentClass = FriendsFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_layout_content_main, fragment).commit();

        getSupportActionBar().setTitle(menuItem.getTitle());
        drawer.closeDrawer(GravityCompat.START);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        ServiceUtils.stopServiceFriendChat(getApplicationContext(), false);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onDestroy() {
        ServiceUtils.startServiceFriendChat(getApplicationContext());
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    StaticConfig.UID = user.getUid();
                } else {
                    MainActivity.this.finish();
                    // User is signed in
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }
}
