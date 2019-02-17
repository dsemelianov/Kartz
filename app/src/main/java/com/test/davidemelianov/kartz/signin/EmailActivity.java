package com.test.davidemelianov.kartz.signin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.test.davidemelianov.kartz.Analytics;
import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.MainActivity;
import com.test.davidemelianov.kartz.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EmailActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_email);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch(Exception e) {

        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // User is signed out
                    mAuth.signOut();
                    Intent mIntent = new Intent(EmailActivity.this, IntroActivity.class);
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
        final EditText mEmailField = (EditText) findViewById(R.id.email_field);

        ImageView mContinueButton = (ImageView) findViewById(R.id.continue_button);
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mEmail = mEmailField.getText().toString().trim();

                if (!mEmail.isEmpty()) {
                    Bundle bundle = new Bundle();
                    mFirebaseAnalytics.logEvent(Analytics.Events.ENTER_EMAIL.value(), bundle);

                    final FirebaseUser user = mAuth.getCurrentUser();
                    //update auth object
                    user.updateEmail(mEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
                                        Map<String, Object> userUpdate = new HashMap<String, Object>();
                                        userUpdate.put("email", mEmail);
                                        mUserReference.updateChildren(userUpdate);

                                        Intent mIntent = new Intent(EmailActivity.this, MainActivity.class);
                                        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mIntent);
                                    } else {
                                        Toast.makeText(EmailActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                }

            }
        });
    }
}
