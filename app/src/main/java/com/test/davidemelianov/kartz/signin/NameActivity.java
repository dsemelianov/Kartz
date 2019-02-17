package com.test.davidemelianov.kartz.signin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.test.davidemelianov.kartz.Analytics;
import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NameActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_name);
        try {
            getSupportActionBar().hide();
        } catch(Exception e) {

        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // User is signed out
                    mAuth.signOut();
                    Intent mIntent = new Intent(NameActivity.this, IntroActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mIntent);
                } else {
                    initializeActivity();
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void initializeActivity() {
        final EditText mNameField = (EditText) findViewById(R.id.name_field);

        ImageView mContinueButton = (ImageView) findViewById(R.id.continue_button);
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mName = mNameField.getText().toString();
                mName = mName.trim();

                if (!mName.isEmpty()) {
                    Bundle bundle = new Bundle();
                    mFirebaseAnalytics.logEvent(Analytics.Events.ENTER_NAME.value(), bundle);

                    FirebaseUser user = mAuth.getCurrentUser();

                    //update auth object
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(mName)
                            .build();
                    user.updateProfile(profileUpdates);

                    DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
                    Map<String, Object> userUpdate = new HashMap<String, Object>();
                    userUpdate.put("name", mName);
                    userUpdate.put("android", true);
                    mUserReference.updateChildren(userUpdate);

                    Intent mIntent = new Intent(NameActivity.this, EmailActivity.class);
                    startActivity(mIntent);
                }

            }
        });
    }
}