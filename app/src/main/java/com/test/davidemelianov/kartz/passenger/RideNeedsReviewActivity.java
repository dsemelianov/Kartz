package com.test.davidemelianov.kartz.passenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.RideStatusManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RideNeedsReviewActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_completed);
        try {
            getSupportActionBar().hide();
        } catch (Exception e) {

        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    RideStatusManager.getInstance().checkRideState(RideNeedsReviewActivity.this, false);
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(RideNeedsReviewActivity.this, IntroActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mIntent);
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
        Button mFinishButton = (Button) findViewById(R.id.button_finish);
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RideStatusManager.getInstance().setRideToCompleted();
            }
        });

    }
}