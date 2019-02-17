package com.test.davidemelianov.kartz.passenger;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.test.davidemelianov.kartz.Analytics;
import com.test.davidemelianov.kartz.ConfirmCancelDialog;
import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.RideStatusManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RideActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView mPrimaryText;

    private ProgressBar mLoading;

    GoogleMap mMap;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
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
                    RideStatusManager.getInstance().checkRideState(RideActivity.this, true);
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(RideActivity.this, IntroActivity.class);
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
        if (ContextCompat.checkSelfPermission(RideActivity.this, android.Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(RideActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 69);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void initializeActivity() {
        mLoading = (ProgressBar) findViewById(R.id.loading);

        mPrimaryText = (TextView) findViewById(R.id.text_primary);
        if (RideStatusManager.getInstance().isTransitMode()) {
            mPrimaryText.setText("You are on your way to your destination");
        } else {
            mPrimaryText.setText("Your driver is on their way");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final String driverNumber = RideStatusManager.getInstance().getDriverNumber();
        TextView mPhoneField = (TextView) findViewById(R.id.text_number);
        mPhoneField.setText(driverNumber);

        RelativeLayout mButtonCall = (RelativeLayout) findViewById(R.id.button_call);
        mButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(RideActivity.this, android.Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (driverNumber != null) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + driverNumber));
                        startActivity(intent);
                    }
                } else {
                    ActivityCompat.requestPermissions(RideActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 69);
                }
            }
        });

        RelativeLayout mButtonCancel = (RelativeLayout) findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmCancelDialog mDialogue = new ConfirmCancelDialog(RideActivity.this);
                mDialogue.show();
            }
        });

    }

    public void setTransitModeUI() {
        mPrimaryText.setText("You are on your way to your destination");

        try {
            mMap.clear();
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

            double latitude = RideStatusManager.getInstance().getCurrentRideDriverLocation().getLatitude();
            double longitude = RideStatusManager.getInstance().getCurrentRideDriverLocation().getLongitude();
            LatLng mDriverLocation = new LatLng(latitude, longitude);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mDriverLocation)        // Sets the center of the map to destination location
                    .zoom(17)                       // Sets the zoom
                    .build();                       // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mMap.clear();

            if (!RideStatusManager.getInstance().isTransitMode()) {
                mMap.addMarker(new MarkerOptions()
                        .position(mDriverLocation)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cart_pin))
                );
            }

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                ActivityCompat.requestPermissions(RideActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 69);
            }
        }
    }

    public void cancel() {
        RideStatusManager.getInstance().setRideToCancelledByPassenger(Analytics.Activities.PASSENGER_RIDE_ACCEPTED.value());
    }
}