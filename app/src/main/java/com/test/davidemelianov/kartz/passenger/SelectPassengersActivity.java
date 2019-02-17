package com.test.davidemelianov.kartz.passenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.test.davidemelianov.kartz.Analytics;
import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.MainActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.RideStatusManager;
import com.test.davidemelianov.kartz.Settings;
import com.test.davidemelianov.kartz.UserManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SelectPassengersActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private int mPassengers = 1;
    private double mPricePerPassenger;
    private int mPassengerLimit;
    private double mPriceMinimum;
    private float mLatitude;
    private float mLongitude;
    private float mRange;
    private boolean mClosed;

    private TextView mTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passenger_activity_select_passengers);
        try {
            if (UserManager.getInstance().userHasSpecialRoles()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
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
                    RideStatusManager.getInstance().checkRideState(SelectPassengersActivity.this, true);
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(SelectPassengersActivity.this, IntroActivity.class);
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
        DatabaseReference mRolesReference = FirebaseDatabase.getInstance().getReference().child("settings");

        mRolesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if they do, initialize the UI
                if ((dataSnapshot != null) && (dataSnapshot.getValue() != null)) {
                    Settings settings = dataSnapshot.getValue(Settings.class);
                    try {
                        mPassengerLimit = settings.passengerLimit;
                    } catch (Exception e) {}
                    try {
                        mPriceMinimum = settings.priceMinimum;
                    } catch (Exception e) {}
                    try {
                        mPricePerPassenger = settings.pricePerPassenger;
                    } catch (Exception e) {}
                    try {
                        mLatitude = settings.latitude;
                    } catch (Exception e) {}
                    try {
                        mLongitude = settings.longitude;
                    } catch (Exception e) {}
                    try {
                        mRange = settings.range;
                    } catch (Exception e) {}
                    try {
                        mClosed = settings.closed;
                    } catch (Exception e) {}

                    RelativeLayout mLoading = (RelativeLayout) findViewById(R.id.loading_screen);
                    mLoading.setVisibility(View.GONE);

                    initializeUI();
                } else {
                    Intent mIntent = new Intent(SelectPassengersActivity.this, MainActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void initializeUI() {
        mTotal = (TextView) findViewById(R.id.text_total);
        mTotal.setText("Loading...");

        setPrice();

        final TextView mNumberOfPassengers = (TextView) findViewById(R.id.text_number);
        mNumberOfPassengers.setText(Integer.toString(mPassengers));

        final ImageView mMinusIcon = (ImageView) findViewById(R.id.icon_minus);
        final ImageView mPlusIcon = (ImageView) findViewById(R.id.icon_plus);

        final RelativeLayout mMinusButton = (RelativeLayout) findViewById(R.id.button_minus);
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPassengers > 0) {
                    mPassengers--;
                    mNumberOfPassengers.setText(Integer.toString(mPassengers));

                    setPrice();
                }

                updateButtons(mMinusIcon, mPlusIcon);
            }
        });

        final RelativeLayout mPlusButton = (RelativeLayout) findViewById(R.id.button_plus);
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPassengers < mPassengerLimit) {
                    mPassengers++;
                    mNumberOfPassengers.setText(Integer.toString(mPassengers));

                    setPrice();
                }

                updateButtons(mMinusIcon, mPlusIcon);
            }
        });

        Button mNextButton = (Button) findViewById(R.id.button_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPassengers > 0) {
                    Bundle bundle = new Bundle();
                    mFirebaseAnalytics.logEvent(Analytics.Events.SET_PASSENGERS.value(), bundle);

                    Intent mIntent = new Intent(SelectPassengersActivity.this, ConfirmLocationActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putDouble("pricePerPassenger", mPricePerPassenger);
                    double total = mPricePerPassenger * mPassengers;
                    if (total < mPriceMinimum) {
                        total = mPriceMinimum;
                    }
                    mBundle.putDouble("totalPrice", total);
                    mBundle.putInt("numberOfPassengers", mPassengers);

                    mBundle.putFloat("latitude", mLatitude);
                    mBundle.putFloat("longitude", mLongitude);
                    mBundle.putFloat("range", mRange);

                    mIntent.putExtras(mBundle);
                    startActivity(mIntent);
                }
            }
        });

        RelativeLayout mClosedOverlay = (RelativeLayout) findViewById(R.id.closed_layout);
        if (mClosed) {
            mClosedOverlay.setVisibility(View.VISIBLE);
        } else {
            mClosedOverlay.setVisibility(View.GONE);
        }

    }

    private void updateButtons(ImageView mMinusIcon, ImageView mPlusIcon) {
        if (mPassengers > 0) {
            mMinusIcon.setVisibility(View.VISIBLE);
        } else {
            mMinusIcon.setVisibility(View.GONE);
        }
        if (mPassengers < mPassengerLimit) {
            mPlusIcon.setVisibility(View.VISIBLE);
        } else {
            mPlusIcon.setVisibility(View.GONE);
        }
    }

    private void setPrice() {
        double total = mPricePerPassenger * mPassengers;
        if (total == 0) {
            mTotal.setText("");
        } else {
            if (total < mPriceMinimum) {
                total = mPriceMinimum;
            }
            mTotal.setText("$" + Double.toString(total) + "0 total");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.passenger, menu);
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
        } else if (id == R.id.ride_history) {
            Bundle bundle = new Bundle();
            bundle.putString("user", mAuth.getCurrentUser().getUid());
            Intent mIntent = new Intent(SelectPassengersActivity.this, RideHistoryActivity.class);
            mIntent.putExtras(bundle);
            startActivity(mIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}
