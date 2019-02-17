package com.test.davidemelianov.kartz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.test.davidemelianov.kartz.admin.AdminActivity;
import com.test.davidemelianov.kartz.driver.BrowseRidesActivity;
import com.test.davidemelianov.kartz.passenger.SelectPassengersActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    UserManager.getInstance().loadUserInfo(MainActivity.this);
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(MainActivity.this, IntroActivity.class);
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

        try {
            RelativeLayout mLoading = (RelativeLayout) findViewById(R.id.loading_screen);
            mLoading.setVisibility(View.VISIBLE);
        } catch (Exception e) {

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void initializeActivity(final boolean driver, final boolean admin) {
        TextView mWelcomeText = (TextView) findViewById(R.id.text_welcome);
        mWelcomeText.setText("Welcome back, " + mAuth.getCurrentUser().getDisplayName());

        Button mPassenger = (Button) findViewById(R.id.button_passenger);
        Button mDriver = (Button) findViewById(R.id.button_driver);
        Button mAdmin = (Button) findViewById(R.id.button_admin);

        mPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(MainActivity.this, SelectPassengersActivity.class);
                startActivity(mIntent);
            }
        });

        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (driver) {
                    Intent mIntent = new Intent(MainActivity.this, BrowseRidesActivity.class);
                    startActivity(mIntent);
                }
            }
        });

        mAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (admin) {
                    Intent mIntent = new Intent(MainActivity.this, AdminActivity.class);
                    startActivity(mIntent);
                }
            }
        });

        if (!driver) {
            mDriver.setVisibility(View.INVISIBLE);
        }
        if (!admin) {
            mAdmin.setVisibility(View.INVISIBLE);
        }

        RelativeLayout mLoading = (RelativeLayout) findViewById(R.id.loading_screen);
        mLoading.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.signout) {
            Bundle bundle = new Bundle();
            mFirebaseAnalytics.logEvent(Analytics.Events.SIGN_OUT.value(), bundle);
            mAuth.signOut();
        }

        return super.onOptionsItemSelected(item);
    }
}
