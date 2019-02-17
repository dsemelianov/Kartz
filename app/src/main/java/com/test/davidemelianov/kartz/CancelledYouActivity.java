package com.test.davidemelianov.kartz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.test.davidemelianov.kartz.driver.BrowseRidesActivity;
import com.test.davidemelianov.kartz.passenger.SelectPassengersActivity;

public class CancelledYouActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancelled_you);
        try {
            getSupportActionBar().hide();
        } catch(Exception e) {

        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(CancelledYouActivity.this, com.test.davidemelianov.kartz.IntroActivity.class);
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
                Intent mIntent;
                if (com.test.davidemelianov.kartz.RideStatusManager.getInstance().isPassengerApp()) {
                    mIntent = new Intent(CancelledYouActivity.this, SelectPassengersActivity.class);
                } else {
                    mIntent = new Intent(CancelledYouActivity.this, BrowseRidesActivity.class);
                }
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mIntent);
            }
        });

        Button mReportButton = (Button) findViewById(R.id.button_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{getResources().getString(R.string.support_email_address)});
                i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.support_email_subject));
                i.putExtra(Intent.EXTRA_TEXT   , getResources().getString(R.string.support_email_body_first) + com.test.davidemelianov.kartz.RideStatusManager.getInstance().getCurrentRideID()
                        + getResources().getString(R.string.support_email_body_second) + mAuth.getCurrentUser().getUid());
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(CancelledYouActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}