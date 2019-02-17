package com.test.davidemelianov.kartz.driver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.test.davidemelianov.kartz.Analytics;
import com.test.davidemelianov.kartz.CurrentLocationHandler;
import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.Ride;
import com.test.davidemelianov.kartz.RideStatus;
import com.test.davidemelianov.kartz.RideStatusManager;
import com.test.davidemelianov.kartz.UserManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class BrowseRidesActivity extends AppCompatActivity {

    public FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Query mRidesQuery;
    private ChildEventListener mListener;

    private BrowseRidesAdapter listAdapter;
    private ListView mList;

    private TextView mEmptyListView;
    private ProgressBar mLoading;
    boolean isLoading = true;

    public CurrentLocationHandler mLocationHandler;

    private boolean isActive;
    private ProgressBar mActiveStatusLoading;
    private RelativeLayout mActiveStatusLayout;
    private Button mOnButton;
    private Button mOffButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_activity_browse_rides);
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
                    RideStatusManager.getInstance().checkRideState(BrowseRidesActivity.this, false);
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(BrowseRidesActivity.this, IntroActivity.class);
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
            mLocationHandler = CurrentLocationHandler.getInstance(BrowseRidesActivity.this);
        } else {
            ActivityCompat.requestPermissions(BrowseRidesActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 69);
        }

        mLoading = (ProgressBar) findViewById(R.id.loading);
        mLoading.setVisibility(View.VISIBLE);

        mRidesQuery = FirebaseDatabase.getInstance().getReference().child("rides").orderByChild("rideStatus").equalTo(RideStatus.AVAILABLE.value());
        mListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if (dataSnapshot != null) {
                    //updateState(dataSnapshot);
                    Ride ride = dataSnapshot.getValue(Ride.class);
                    ride.key = dataSnapshot.getKey();
                    listAdapter.addItem(ride);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                if (dataSnapshot != null) {
                    //updateState(dataSnapshot);
                    Ride ride = dataSnapshot.getValue(Ride.class);
                    ride.key = dataSnapshot.getKey();
                    if (ride.rideStatus != RideStatus.AVAILABLE.value()) {
                        listAdapter.removeItem(ride.key);
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    //updateState(dataSnapshot);
                    Ride ride = dataSnapshot.getValue(Ride.class);
                    ride.key = dataSnapshot.getKey();
                    listAdapter.removeItem(ride.key);
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mRidesQuery.addChildEventListener(mListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (mListener != null) {
            mRidesQuery.removeEventListener(mListener);
        }
        if (mLocationHandler != null) {
            mLocationHandler.unregisterLocationManager();
        }
    }

    public void initializeActivity() {
        mList = (ListView) findViewById(R.id.list);
        listAdapter = new BrowseRidesAdapter(new ArrayList<Ride>(), BrowseRidesActivity.this);
        mList.setAdapter(listAdapter);
        mEmptyListView = (TextView) findViewById(R.id.text_empty_list);
        mEmptyListView.setText("There are no available ride requests");

        mActiveStatusLoading = (ProgressBar) findViewById(R.id.loading_active_status);
        mActiveStatusLayout = (RelativeLayout) findViewById(R.id.layout_active_status);

        mOnButton = (Button) findViewById(R.id.button_on);
        mOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isActive) {
                    isActive = true;
                    toggleActiveStateButton();
                }
            }
        });

        mOffButton = (Button) findViewById(R.id.button_off);
        mOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isActive) {
                    isActive = false;
                    toggleActiveStateButton();
                }
            }
        });

        UserManager.getInstance().loadUserInfo(BrowseRidesActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 69: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationHandler = CurrentLocationHandler.getInstance(BrowseRidesActivity.this);
                } else {
                    Toast.makeText(BrowseRidesActivity.this, "Please give us location permissions!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    public void updateLocation(final Location location) {
        if (listAdapter != null) {
            listAdapter.updateLocation(location);
            stopLoading();
        } else {
            new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {

                    updateLocation(location);
                }
            }.start();

        }
    }

    public void claimRideCallback(boolean claimSuccessful) {
        stopLoading();
        if (claimSuccessful) {
            Intent mIntent = new Intent(BrowseRidesActivity.this, GiveRideActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mIntent);
        }
    }

    public void driverStatusCallback(boolean active) {
        isActive = active;
        toggleActiveStateButton();
        mActiveStatusLoading.setVisibility(View.GONE);
        mActiveStatusLayout.setVisibility(View.VISIBLE);
    }

    public void toggleActiveStateButton() {
        if (isActive) {
            mOnButton.setBackgroundResource(R.drawable.toggle_button_selected);
            mOnButton.setTextColor(BrowseRidesActivity.this.getResources().getColor(
                    R.color.colorAccent));

            mOffButton.setBackgroundResource(R.drawable.toggle_button_unselected);
            mOffButton.setTextColor(BrowseRidesActivity.this.getResources().getColor(
                    R.color.lightGray));
        } else {
            mOnButton.setBackgroundResource(R.drawable.toggle_button_unselected);
            mOnButton.setTextColor(BrowseRidesActivity.this.getResources().getColor(
                    R.color.lightGray));

            mOffButton.setBackgroundResource(R.drawable.toggle_button_selected);
            mOffButton.setTextColor(BrowseRidesActivity.this.getResources().getColor(
                    R.color.colorAccent));
        }

        UserManager.getInstance().setActiveState(isActive);
    }

    public void setLoading() {
        isLoading = true;
        mLoading.setVisibility(View.VISIBLE);
    }

    public void stopLoading() {
        isLoading = false;
        mLoading.setVisibility(View.GONE);
    }

    public void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            mEmptyListView.setVisibility(View.VISIBLE);
        } else {
            mEmptyListView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.history) {
            Bundle bundle = new Bundle();
            bundle.putString("user", mAuth.getCurrentUser().getUid());
            Intent mIntent = new Intent(BrowseRidesActivity.this, RideHistoryActivity.class);
            mIntent.putExtras(bundle);
            startActivity(mIntent);
        } else if (id == R.id.signout) {
            Bundle bundle = new Bundle();
            mFirebaseAnalytics.logEvent(Analytics.Events.SIGN_OUT.value(), bundle);
            mAuth.signOut();
        }

        return super.onOptionsItemSelected(item);
    }
}