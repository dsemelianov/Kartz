package com.test.davidemelianov.kartz.signin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.Analytics;
import com.test.davidemelianov.kartz.IntroActivity;

public class PhoneNumberActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_phone_number);
        try {
            getSupportActionBar().hide();
        } catch(Exception e) {

        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    mAuth.signOut();
                    Intent mIntent = new Intent(PhoneNumberActivity.this, IntroActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mIntent);
                } else {
                    initializeActivity();
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
        final EditText mPhoneNumberField = (EditText) findViewById(R.id.phone_number_field);

        ImageView mContinueButton = (ImageView) findViewById(R.id.continue_button);
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mPhoneNumber = mPhoneNumberField.getText().toString();
                mPhoneNumber = mPhoneNumber.trim();
                mPhoneNumber = mPhoneNumber.replaceAll( "[^\\d]", "" );

                if (mPhoneNumber.isEmpty()) {
                    // phone number is required
                } else if ((mPhoneNumber.length() < 10) || (mPhoneNumber.length() > 11)
                        || ((mPhoneNumber.length() == 11) && (mPhoneNumber.charAt(0) != '1')) ) {
                    Toast.makeText(PhoneNumberActivity.this, "Invalid number!", Toast.LENGTH_LONG).show();
                } else {
                    Bundle bundle = new Bundle();
                    mFirebaseAnalytics.logEvent(Analytics.Events.ENTER_NUMBER.value(), bundle);

                    Intent mIntent = new Intent(PhoneNumberActivity.this, PhoneAuthActivity.class);
                    Bundle mBundle = new Bundle();
                    mPhoneNumber = formatPhoneNumber(mPhoneNumber);
                    mBundle.putString("phone", mPhoneNumber);
                    mIntent.putExtras(mBundle);
                    startActivity(mIntent);
                }

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
