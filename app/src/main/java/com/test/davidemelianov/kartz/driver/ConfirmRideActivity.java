package com.test.davidemelianov.kartz.driver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.test.davidemelianov.kartz.Analytics;
import com.test.davidemelianov.kartz.ConfirmCancelDialog;
import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.RideStatusManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ConfirmRideActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_activity_confirm_ride);
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
                    RideStatusManager.getInstance().checkRideState(ConfirmRideActivity.this, false);
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(ConfirmRideActivity.this, IntroActivity.class);
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
        Button mCancelButton = (Button) findViewById(R.id.button_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmCancelDialog mDialogue = new ConfirmCancelDialog(ConfirmRideActivity.this);
                mDialogue.show();
            }
        });

        Button mConfirmButton = (Button) findViewById(R.id.button_confirm);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RideStatusManager.getInstance().setRideToNeedsPayment();
            }
        });

        TextView mChargePassengerText = (TextView) findViewById(R.id.text_charge_passenger);
        mChargePassengerText.setText("Should we charge " + RideStatusManager.getInstance().getPassengerName() + " for this ride?");

        TextView mTotalPrice = (TextView) findViewById(R.id.text_total_price);
        mTotalPrice.setText(RideStatusManager.getInstance().getTotalPrice());
    }

    public void cancel() {
        RideStatusManager.getInstance().setRideToCancelledByDriver(Analytics.Activities.DRIVER_CONFIRM_RIDE_COMPLETE.value());
    }
}