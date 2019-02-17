package com.test.davidemelianov.kartz.driver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.test.davidemelianov.kartz.Analytics;
import com.test.davidemelianov.kartz.ConfirmCancelDialog;
import com.test.davidemelianov.kartz.CurrentLocationHandler;
import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.RideStatusManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GiveRideActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressBar mLoading;

    private Button mNextButton;

    GoogleMap mMap;

    boolean isTaken = false;

    public CurrentLocationHandler mLocationHandler;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_activity_give_ride);
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
                    RideStatusManager.getInstance().checkRideState(GiveRideActivity.this, false);
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(GiveRideActivity.this, IntroActivity.class);
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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationHandler = CurrentLocationHandler.getInstance(GiveRideActivity.this);
        } else {
            ActivityCompat.requestPermissions(GiveRideActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 69);
        }
        if (ContextCompat.checkSelfPermission(GiveRideActivity.this, android.Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(GiveRideActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 69);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (mLocationHandler != null) {
            mLocationHandler.unregisterLocationManager();
        }
    }

    public void initializeActivity() {
        mLoading = (ProgressBar) findViewById(R.id.loading);

        TextView mPrimaryText = (TextView) findViewById(R.id.text_primary);
        mPrimaryText.setText(RideStatusManager.getInstance().getPassengerName());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final String passengerNumber = RideStatusManager.getInstance().getPassengerNumber();
        TextView mPhoneField = (TextView) findViewById(R.id.text_number);
        mPhoneField.setText(passengerNumber);

        mNextButton = (Button) findViewById(R.id.button_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTaken) {
                    Intent mIntent = new Intent(GiveRideActivity.this, BrowseRidesActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mIntent);
                } else if (RideStatusManager.getInstance().isTransitMode()) {
                    Intent mIntent = new Intent(GiveRideActivity.this, ConfirmRideActivity.class);
                    startActivity(mIntent);
                } else {
                    RideStatusManager.getInstance().setRideToInTransit();
                }
            }
        });

        RelativeLayout mButtonCall = (RelativeLayout) findViewById(R.id.button_call);
        mButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(GiveRideActivity.this, android.Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (passengerNumber != null) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + passengerNumber));
                        startActivity(intent);
                    }
                } else {
                    ActivityCompat.requestPermissions(GiveRideActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 69);
                }
            }
        });

        RelativeLayout mButtonCancel = (RelativeLayout) findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmCancelDialog mDialogue = new ConfirmCancelDialog(GiveRideActivity.this);
                mDialogue.show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 69: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationHandler = CurrentLocationHandler.getInstance(GiveRideActivity.this);
                    updateMap();
                } else {
                    Toast.makeText(GiveRideActivity.this, "Please give us location permissions!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    public void setClaimedMode() {
        mNextButton.setText("Pick up " + RideStatusManager.getInstance().getPassengerName());
    }

    public void setTransitModeUI() {
        mNextButton.setText("Drop off " + RideStatusManager.getInstance().getPassengerName());

        try {
            updateMap();
        } catch (Exception e) {}
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        updateMap();
    }

    public void updateMap() {
        if ((mMap != null)) {
            mLoading.setVisibility(View.GONE);

            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            double latitude;
            double longitude;
            if (!RideStatusManager.getInstance().isTransitMode()) {
                latitude = RideStatusManager.getInstance().getCurrentRideLocation().getLatitude();
                longitude = RideStatusManager.getInstance().getCurrentRideLocation().getLongitude();
            } else {
                latitude = mLocationHandler.getCurrentLocation().getLatitude();
                longitude = mLocationHandler.getCurrentLocation().getLongitude();
            }

            LatLng mMapFocus = new LatLng(latitude, longitude);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mMapFocus)      // Sets the center of the map to destination location
                    .zoom(17)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mMap.clear();

            if (!RideStatusManager.getInstance().isTransitMode()) {
                mMap.addMarker(new MarkerOptions().position(mMapFocus));
            }

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                ActivityCompat.requestPermissions(GiveRideActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 69);
            }
        }
    }

    public void rideTaken() {
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent(Analytics.Events.CLAIM_STOLEN.value(), bundle);

        isTaken = true;

        TextView mPrimaryText = (TextView) findViewById(R.id.text_primary);
        mPrimaryText.setText("Sorry!");

        Button mNextButton = (Button) findViewById(R.id.button_next);
        mNextButton.setText("Look for more rides");

        RelativeLayout mRideTaken = (RelativeLayout) findViewById(R.id.layout_taken);
        mRideTaken.setVisibility(View.VISIBLE);
    }

    public void cancel() {
        RideStatusManager.getInstance().setRideToCancelledByDriver(Analytics.Activities.DRIVER_GIVE_RIDE.value());
    }
}