package com.test.davidemelianov.kartz.admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.RideStatusManager;
import com.test.davidemelianov.kartz.Role;
import com.test.davidemelianov.kartz.User;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ManageDriversActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Query mDriversQuery;
    private ChildEventListener mListener;

    private ManageDriversAdapter listAdapter;
    private ListView mList;

    private EditText mPhoneNumberField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_manage_drivers);
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
                    RideStatusManager.getInstance().checkRideState(ManageDriversActivity.this, false);
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(ManageDriversActivity.this, IntroActivity.class);
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

        mDriversQuery = FirebaseDatabase.getInstance().getReference().child("roles").orderByChild("driver").equalTo(true);
        mListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if (dataSnapshot != null) {
                    //updateState(dataSnapshot);
                    Role role = dataSnapshot.getValue(Role.class);
                    role.key = dataSnapshot.getKey();
                    listAdapter.addItem(role);
                    listAdapter.notifyDataSetChanged();
                    try {
                        mPhoneNumberField.setText("");
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mDriversQuery.addChildEventListener(mListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
            mDriversQuery.removeEventListener(mListener);
        }
    }

    public void initializeActivity() {
        /*TextView mWelcomeText = (TextView) findViewById(R.id.text_welcome);
        mWelcomeText.setText("Welcome back, " + mAuth.getCurrentUser().getDisplayName());

        Button mManageDrivers = (Button) findViewById(R.id.button_manage_drivers);
        mManageDrivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });*/

        mList = (ListView) findViewById(R.id.list);
        listAdapter = new ManageDriversAdapter(new ArrayList<Role>(), ManageDriversActivity.this);
        mList.setAdapter(listAdapter);

        mPhoneNumberField = (EditText) findViewById(R.id.phone_number_field);

        ImageView mAddDriver = (ImageView) findViewById(R.id.button_add);
        mAddDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mPhoneNumber = mPhoneNumberField.getText().toString();
                mPhoneNumber = mPhoneNumber.trim();
                mPhoneNumber = mPhoneNumber.replaceAll( "[^\\d]", "" );

                if (mPhoneNumber.isEmpty()) {
                    // phone number is required
                } else if ((mPhoneNumber.length() < 10) || (mPhoneNumber.length() > 11)
                        || ((mPhoneNumber.length() == 11) && (mPhoneNumber.charAt(0) != '1')) ) {
                    Toast.makeText(ManageDriversActivity.this, "Invalid phone number!", Toast.LENGTH_SHORT).show();
                    mPhoneNumberField.setText("");
                } else {
                    mPhoneNumber = formatPhoneNumber(mPhoneNumber);
                    findUserByPhone(mPhoneNumber);
                }
            }
        });

        Button mSaveButton = (Button) findViewById(R.id.button_save);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(ManageDriversActivity.this, AdminActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mIntent);
            }
        });
    }

    private void findUserByPhone(String mPhoneNumber) {
        Query mUserQuery = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("phone").equalTo(mPhoneNumber);
        mUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // if such user exists
                if ((dataSnapshot != null) && dataSnapshot.hasChildren()) {
                    DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();
                    User user = userSnapshot.getValue(User.class);

                    //Create or update the roles object
                    HashMap<String, Object> roleUpdate = new HashMap<>();
                    roleUpdate.put("driver", true);
                    roleUpdate.put("name", user.name);
                    roleUpdate.put("phone", user.phone);
                    FirebaseDatabase.getInstance().getReference().child("roles").child(userSnapshot.getKey()).updateChildren(roleUpdate);

                    Toast.makeText(ManageDriversActivity.this, "Added " + user.name + " as a driver!", Toast.LENGTH_SHORT).show();

                    // if they do not, complain
                } else {
                    Toast.makeText(ManageDriversActivity.this, "No user with that phone number found", Toast.LENGTH_SHORT).show();
                    mPhoneNumberField.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String formatPhoneNumber(String phone) {
        if ((phone.length() == 11) && (phone.charAt(0) == '1')) {
            phone = phone.substring(1);
        }
        return phone;
    }
}
