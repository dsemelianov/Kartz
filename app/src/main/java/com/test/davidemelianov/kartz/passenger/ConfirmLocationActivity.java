package com.test.davidemelianov.kartz.passenger;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.test.davidemelianov.kartz.Analytics;
import com.test.davidemelianov.kartz.CurrentLocationHandler;
import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.Ride;
import com.test.davidemelianov.kartz.RideStatusManager;
import com.test.davidemelianov.kartz.UserManager;
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
import com.google.firebase.database.FirebaseDatabase;

public class ConfirmLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressBar mLoading;

    private boolean isRequesting = false;

    GoogleMap mMap;
    public CurrentLocationHandler mLocationHandler;

    Double mPricePerPassenger;
    Double mTotalPrice;
    Integer mNumberOfPassengers;
    float mLatitude;
    float mLongitude;
    float mRange;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passenger_activity_confirm_location);
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
                    RideStatusManager.getInstance().checkRideState(ConfirmLocationActivity.this, true);
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(ConfirmLocationActivity.this, IntroActivity.class);
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
            mLocationHandler = CurrentLocationHandler.getInstance(ConfirmLocationActivity.this);
        } else {
            ActivityCompat.requestPermissions(ConfirmLocationActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 69);
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
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras == null) {
            Intent mIntent = new Intent(ConfirmLocationActivity.this, SelectPassengersActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mIntent);
        } else {
            mPricePerPassenger = extras.getDouble("pricePerPassenger");
            mTotalPrice = extras.getDouble("totalPrice");
            mNumberOfPassengers = extras.getInt("numberOfPassengers");
            mLatitude = extras.getFloat("latitude");
            mLongitude = extras.getFloat("longitude");
            mRange = extras.getFloat("range");

            mLoading = (ProgressBar) findViewById(R.id.loading);

            Button mNextButton = (Button) findViewById(R.id.button_next);
            mNextButton.setText("REQUEST RIDE - $" + Double.toString(mTotalPrice) + "0");
            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLocationHandler == null) {
                        if (ContextCompat.checkSelfPermission(ConfirmLocationActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ConfirmLocationActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 69);
                        }
                    } else if (!isRequesting && (mLocationHandler.getCurrentLocation() != null) && checkLocationDistance()) {
                        isRequesting = true;
                        mLoading.setVisibility(View.VISIBLE);
                        UserManager.getInstance().checkForPaymentSources(ConfirmLocationActivity.this);
                    }
                }
            });

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng mDefaultLatLng = new LatLng(mLatitude, mLongitude);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mDefaultLatLng)      // Sets the center of the map to current location
                .zoom(17)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        updateMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 69: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationHandler = CurrentLocationHandler.getInstance(ConfirmLocationActivity.this);
                } else {
                    Toast.makeText(ConfirmLocationActivity.this, "Please give us location permissions!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    public void updateLocation(Location location) {
        updateMap();
    }

    public void updateMap() {
        if ((mMap != null) && (mLocationHandler != null) && (mLocationHandler.getCurrentLocation() != null)) {
            mLoading.setVisibility(View.GONE);

            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            LatLng mCurrentLatLng = new LatLng(mLocationHandler.getCurrentLocation().getLatitude(), mLocationHandler.getCurrentLocation().getLongitude());
            MarkerOptions marker = new MarkerOptions().position(mCurrentLatLng);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mCurrentLatLng)      // Sets the center of the map to current location
                    .zoom(17)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mMap.clear();
            mMap.addMarker(marker);
        } else {
            new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {

                    updateMap();
                }
            }.start();

        }
    }

    public Ride buildRide() {
        long mTimestamp = System.currentTimeMillis();

        FirebaseUser mCurrentUser = mAuth.getCurrentUser();
        String mPassengerId = mCurrentUser.getUid();
        String mPassengerName = mCurrentUser.getDisplayName();
        String mPassengerPhone = mCurrentUser.getPhoneNumber();

        Double mLatitude = mLocationHandler.getCurrentLocation().getLatitude();
        Double mLongitude = mLocationHandler.getCurrentLocation().getLongitude();

        Ride mRide = new Ride();
        mRide.createRide(mTimestamp, mPassengerId, mPassengerName, mPassengerPhone, mLatitude, mLongitude, mNumberOfPassengers, mPricePerPassenger, mTotalPrice);

        return mRide;
    }

    public void requestRide() {
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent(Analytics.Events.SET_LOCATION.value(), bundle);

        Ride mRide = buildRide();

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        mDatabase.getReference().child("rides").push().setValue(mRide);

        Intent mIntent = new Intent(ConfirmLocationActivity.this, RequestSentActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mIntent);
    }

    public void requestPayment() {
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent(Analytics.Events.REQUIRE_PAYMENT.value(), bundle);

        isRequesting = false;
        Intent mIntent = new Intent(ConfirmLocationActivity.this, AddPaymentActivity.class);
        startActivity(mIntent);
    }

    public void checkForPaymentSourcesFailed(String error) {
        Toast.makeText(ConfirmLocationActivity.this, error, Toast.LENGTH_LONG).show();
        mLoading.setVisibility(View.VISIBLE);
        isRequesting = false;
    }

    public boolean checkLocationDistance() {
        Location mCenterOfRange = new Location("center of range");
        mCenterOfRange.setLatitude(mLatitude);
        mCenterOfRange.setLongitude(mLongitude);
        Integer mDistanceValueInt = Math.round(mCenterOfRange.distanceTo(mLocationHandler.getCurrentLocation()));
        if (mDistanceValueInt < mRange) {
            return true;
        } else {
            Toast.makeText(ConfirmLocationActivity.this, "You are outside of our pickup area!", Toast.LENGTH_LONG).show();

            Bundle bundle = new Bundle();
            mFirebaseAnalytics.logEvent(Analytics.Events.OUTSIDE_OF_RANGE.value(), bundle);

            return false;
        }
    }

}