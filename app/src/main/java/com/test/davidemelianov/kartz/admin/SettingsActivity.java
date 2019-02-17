package com.test.davidemelianov.kartz.admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.Settings;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Double mPricePerPassenger;
    private Integer mPassengerLimit;
    private Double mPriceMinimum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_settings);
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
                    Intent mIntent = new Intent(SettingsActivity.this, IntroActivity.class);
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
                if (dataSnapshot != null) {
                        Settings settings = dataSnapshot.getValue(Settings.class);
                        mPassengerLimit = ((settings == null) || (settings.passengerLimit == null)) ? 4 : settings.passengerLimit;
                        mPriceMinimum = ((settings == null) || (settings.priceMinimum == null)) ? 5 : settings.priceMinimum;
                        mPricePerPassenger = ((settings == null) || (settings.pricePerPassenger == null)) ? 2 : settings.pricePerPassenger;

                        RelativeLayout mLoading = (RelativeLayout) findViewById(R.id.loading_screen);
                        mLoading.setVisibility(View.GONE);

                        setPricePerPassengerLayout();
                        setMinimumPriceLayout();
                        setPassengerLimitLayout();

                        Button mNextButton = (Button) findViewById(R.id.button_save);
                        mNextButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Settings settings = new Settings();
                                settings.passengerLimit = mPassengerLimit;
                                settings.priceMinimum = mPriceMinimum;
                                settings.pricePerPassenger = mPricePerPassenger;

                                //Create or update the roles object
                                HashMap<String, Object> settingsUpdate = new HashMap<>();
                                settingsUpdate.put("passengerLimit", mPassengerLimit);
                                settingsUpdate.put("priceMinimum", mPriceMinimum);
                                settingsUpdate.put("pricePerPassenger", mPricePerPassenger);
                                FirebaseDatabase.getInstance().getReference().child("settings").updateChildren(settingsUpdate);
                                finish();
                            }
                        });
                } else {
                    mPassengerLimit = 0;
                    mPriceMinimum = 0.0;
                    mPricePerPassenger = 0.0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setPassengerLimitLayout() {
        final TextView mNumber = (TextView) findViewById(R.id.text_number_passenger_limit);
        mNumber.setText(Integer.toString(mPassengerLimit));

        final RelativeLayout mMinusButton = (RelativeLayout) findViewById(R.id.button_minus_passenger_limit);
        final ImageView mMinusIcon = (ImageView) findViewById(R.id.icon_minus_passenger_limit);
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPassengerLimit > 1) {
                    mPassengerLimit--;
                    mNumber.setText(Integer.toString(mPassengerLimit));
                }
                updatePassengerLimitButtons(mMinusIcon);
            }
        });

        final RelativeLayout mPlusButton = (RelativeLayout) findViewById(R.id.button_plus_passenger_limit);
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPassengerLimit++;
                mNumber.setText(Integer.toString(mPassengerLimit));
                updatePassengerLimitButtons(mMinusIcon);
            }
        });
    }

    private void updatePassengerLimitButtons(ImageView mMinusIcon) {
        if (mPassengerLimit > 1) {
            mMinusIcon.setVisibility(View.VISIBLE);
        } else {
            mMinusIcon.setVisibility(View.GONE);
        }
    }

    private void setMinimumPriceLayout() {
        final TextView mNumber = (TextView) findViewById(R.id.text_number_minimum_price);
        mNumber.setText(Integer.toString(mPriceMinimum.intValue()));

        final RelativeLayout mMinusButton = (RelativeLayout) findViewById(R.id.button_minus_minimum_price);
        final ImageView mMinusIcon = (ImageView) findViewById(R.id.icon_minus_minimum_price);
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPriceMinimum > 0) {
                    mPriceMinimum--;
                    mNumber.setText(Integer.toString(mPriceMinimum.intValue()));
                }
                updateMinimumPriceButtons(mMinusIcon);
            }
        });

        final RelativeLayout mPlusButton = (RelativeLayout) findViewById(R.id.button_plus_minimum_price);
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPriceMinimum++;
                mNumber.setText(Integer.toString(mPriceMinimum.intValue()));
                updateMinimumPriceButtons(mMinusIcon);
            }
        });
    }

    private void updateMinimumPriceButtons(ImageView mMinusIcon) {
        if (mPriceMinimum > 0) {
            mMinusIcon.setVisibility(View.VISIBLE);
        } else {
            mMinusIcon.setVisibility(View.GONE);
        }
    }

    private void setPricePerPassengerLayout() {
        final TextView mNumber = (TextView) findViewById(R.id.text_number_price_per_passenger);
        mNumber.setText(Integer.toString(mPricePerPassenger.intValue()));

        final RelativeLayout mMinusButton = (RelativeLayout) findViewById(R.id.button_minus_price_per_passenger);
        final ImageView mMinusIcon = (ImageView) findViewById(R.id.icon_minus_price_per_passenger);
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPricePerPassenger > 1) {
                    mPricePerPassenger--;
                    mNumber.setText(Integer.toString(mPricePerPassenger.intValue()));
                }
                updatePricePerPassengerButtons(mMinusIcon);
            }
        });

        final RelativeLayout mPlusButton = (RelativeLayout) findViewById(R.id.button_plus_price_per_passenger);
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPricePerPassenger++;
                mNumber.setText(Integer.toString(mPricePerPassenger.intValue()));
                updatePricePerPassengerButtons(mMinusIcon);
            }
        });
    }

    private void updatePricePerPassengerButtons(ImageView mMinusIcon) {
        if (mPricePerPassenger > 1) {
            mMinusIcon.setVisibility(View.VISIBLE);
        } else {
            mMinusIcon.setVisibility(View.GONE);
        }
    }
}
