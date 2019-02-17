package com.test.davidemelianov.kartz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.test.davidemelianov.kartz.signin.PhoneNumberActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class IntroActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
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
                if (user != null) {
                    // User is signed in
                    mAuth.signOut();
                }
            }
        };

        Button mPassengerSignIn = (Button) findViewById(R.id.button_sign_in);
        mPassengerSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(IntroActivity.this, PhoneNumberActivity.class);
                startActivity(mIntent);
            }
        });

        TextView mTerms = (TextView) findViewById(R.id.label_terms);
        mTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String termsUrl = getResources().getString(R.string.terms_url);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl));
                startActivity(browserIntent);
            }
        });
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
}
