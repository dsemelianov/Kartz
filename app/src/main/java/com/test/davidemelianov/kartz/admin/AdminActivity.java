package com.test.davidemelianov.kartz.admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.test.davidemelianov.kartz.Analytics;
import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.RideStatusManager;
import com.test.davidemelianov.kartz.Settings;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class AdminActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private boolean isClosed;
    private ProgressBar mClosedStatusLoading;
    private RelativeLayout mClosedStatusLayout;
    private Button mOpenButton;
    private Button mClosedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
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
                    RideStatusManager.getInstance().checkRideState(AdminActivity.this, false);
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(AdminActivity.this, IntroActivity.class);
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
        TextView mWelcomeText = (TextView) findViewById(R.id.text_welcome);
        mWelcomeText.setText("Welcome back, " + mAuth.getCurrentUser().getDisplayName());

        Button mManageDrivers = (Button) findViewById(R.id.button_manage_drivers);
        mManageDrivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(AdminActivity.this, com.test.davidemelianov.kartz.admin.ManageDriversActivity.class);
                startActivity(mIntent);
            }
        });

        Button mRideHistory = (Button) findViewById(R.id.button_ride_history);
        mRideHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(AdminActivity.this, RideHistoryActivity.class);
                startActivity(mIntent);
            }
        });

        Button mSetRules = (Button) findViewById(R.id.button_settings);
        mSetRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(AdminActivity.this, com.test.davidemelianov.kartz.admin.SettingsActivity.class);
                startActivity(mIntent);
            }
        });

        mClosedStatusLoading = (ProgressBar) findViewById(R.id.loading_closed_status);
        mClosedStatusLayout = (RelativeLayout) findViewById(R.id.layout_closed_status);

        mOpenButton = (Button) findViewById(R.id.button_open);
        mOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClosed) {
                    isClosed = false;
                    toggleClosedStateButton();
                }
            }
        });

        mClosedButton = (Button) findViewById(R.id.button_closed);
        mClosedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClosed) {
                    isClosed = true;
                    toggleClosedStateButton();
                }
            }
        });

        loadClosedStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.contact) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{getResources().getString(R.string.dev_email_address)});
            i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.dev_email_subject));
            i.putExtra(Intent.EXTRA_TEXT   , getResources().getString(R.string.dev_email_body_first) + getResources().getString(R.string.dev_email_body_first)
                    + getResources().getString(R.string.dev_email_body_second));
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(AdminActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.signout) {
            Bundle bundle = new Bundle();
            mFirebaseAnalytics.logEvent(Analytics.Events.SIGN_OUT.value(), bundle);
            mAuth.signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadClosedStatus() {
        DatabaseReference mSettingsReference = FirebaseDatabase.getInstance().getReference().child("settings");

        mSettingsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if they do, initialize the UI
                if ((dataSnapshot != null) && (dataSnapshot.getValue() != null)) {
                    Settings settings = dataSnapshot.getValue(Settings.class);
                    try {
                        isClosed = settings.closed;
                        toggleClosedStateButton();
                    } catch (Exception e) {
                        Toast.makeText(AdminActivity.this, "Error loading closed status!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminActivity.this, "Error loading closed status!", Toast.LENGTH_SHORT).show();
                }
                mClosedStatusLoading.setVisibility(View.GONE);
                mClosedStatusLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void toggleClosedStateButton() {
        if (!isClosed) {
            mOpenButton.setBackgroundResource(R.drawable.toggle_button_selected);
            mOpenButton.setTextColor(AdminActivity.this.getResources().getColor(
                    R.color.colorAccent));

            mClosedButton.setBackgroundResource(R.drawable.toggle_button_unselected);
            mClosedButton.setTextColor(AdminActivity.this.getResources().getColor(
                    R.color.lightGray));
        } else {
            mOpenButton.setBackgroundResource(R.drawable.toggle_button_unselected);
            mOpenButton.setTextColor(AdminActivity.this.getResources().getColor(
                    R.color.lightGray));

            mClosedButton.setBackgroundResource(R.drawable.toggle_button_selected);
            mClosedButton.setTextColor(AdminActivity.this.getResources().getColor(
                    R.color.colorAccent));
        }

        FirebaseDatabase.getInstance().getReference().child("settings").child("closed").setValue(isClosed);
    }
}
