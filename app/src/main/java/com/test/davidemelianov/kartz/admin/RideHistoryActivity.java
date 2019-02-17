package com.test.davidemelianov.kartz.admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.Ride;
import com.test.davidemelianov.kartz.RideHistoryAdapter;
import com.test.davidemelianov.kartz.RideStatus;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class RideHistoryActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Query mRidesQuery;
    private ChildEventListener mListener;

    private RideHistoryAdapter listAdapter;
    private ListView mList;

    private TextView mEmptyListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
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
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(RideHistoryActivity.this, IntroActivity.class);
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

        mRidesQuery = FirebaseDatabase.getInstance().getReference().child("rides");

        mListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if (dataSnapshot != null) {
                    Ride ride = dataSnapshot.getValue(Ride.class);
                    ride.key = dataSnapshot.getKey();
                    if ((ride.rideStatus != RideStatus.AVAILABLE.value()) && (ride.driverId != null)) {
                        listAdapter.addItem(ride);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                if (dataSnapshot != null) {
                    // TO DO
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
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
    }

    public void initializeActivity() {
        mList = (ListView) findViewById(R.id.list);
        listAdapter = new RideHistoryAdapter(new ArrayList<Ride>(), RideHistoryActivity.this);
        mList.setAdapter(listAdapter);
        mEmptyListView = (TextView) findViewById(R.id.text_empty_list);
        mEmptyListView.setText("There are no rides on record");
    }

    public void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            mEmptyListView.setVisibility(View.VISIBLE);
        } else {
            mEmptyListView.setVisibility(View.GONE);
        }
    }
}
