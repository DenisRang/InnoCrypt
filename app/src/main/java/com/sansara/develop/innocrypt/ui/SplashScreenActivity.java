package com.sansara.develop.innocrypt.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sansara.develop.innocrypt.MainActivity;
import com.sansara.develop.innocrypt.R;
import com.sansara.develop.innocrypt.data.SharedPreferenceHelper;
import com.sansara.develop.innocrypt.data.StaticConfig;
import com.sansara.develop.innocrypt.model.User;

import java.util.Arrays;
import java.util.HashMap;

public class SplashScreenActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
//                    StaticConfig.UID = user.getUid();
//                    Log.d("TAG", "onAuthStateChanged:signed_in:" + user.getUid());
//                    onSignedInInitialize(user.getDisplayName());
//                        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
//                    SplashScreenActivity.this.finish();
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            //new AuthUI.IdpConfig.PhoneBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        Button button=(Button)findViewById(R.id.button_get_started);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseAuth.addAuthStateListener(mAuthStateListener);
            }
       });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    // Sign-in succeeded, set up the UI
                    StaticConfig.UID = user.getUid();
                    onSignedInInitialize(user.getDisplayName());
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    SplashScreenActivity.this.finish();
                    Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
                } else {
                    // Sign in was canceled by the user, finish the activity
                    Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                   // finish();
                }
                break;
        }
    }

    private void onSignedInInitialize(String userName) {
        initNewUserInfo();
        saveUserInfo();
      //  mUsername = userName;
    }

    private void onSignedOutCleanup() {
      //  mUsername = ANONYMOUS;
    }

    private void saveUserInfo() {
        FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap hashUser = (HashMap) dataSnapshot.getValue();
                User userInfo = new User();
                userInfo.name = (String) hashUser.get("name");
                userInfo.email = (String) hashUser.get("email");
                userInfo.avata = (String) hashUser.get("avata");
                SharedPreferenceHelper.getInstance(SplashScreenActivity.this).saveUserInfo(userInfo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initNewUserInfo() {
        User newUser = new User();
        newUser.email = user.getEmail();
        newUser.name = user.getEmail().substring(0, user.getEmail().indexOf("@"));
        newUser.avata = StaticConfig.STR_DEFAULT_BASE64;
        FirebaseDatabase.getInstance().getReference().child("user/" + user.getUid()).setValue(newUser);
    }
}
