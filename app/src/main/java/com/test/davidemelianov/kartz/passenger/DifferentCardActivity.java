package com.test.davidemelianov.kartz.passenger;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.test.davidemelianov.kartz.IntroActivity;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.RideStatusManager;
import com.test.davidemelianov.kartz.UserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

public class DifferentCardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private RelativeLayout mLoading;
    public boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passenger_activity_different_card);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (Exception e) {

        }
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    RideStatusManager.getInstance().checkRideState(DifferentCardActivity.this, true);
                    initializeActivity();
                } else {
                    // User is signed out
                    Intent mIntent = new Intent(DifferentCardActivity.this, IntroActivity.class);
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
        mLoading = (RelativeLayout) findViewById(R.id.loading_screen);

        final CardInputWidget mCardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);

        final Button mNextButton = (Button) findViewById(R.id.button_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Card cardToSave = mCardInputWidget.getCard();
                if (cardToSave == null) {
                    mNextButton.setText("Invalid card info!");
                    mNextButton.setBackgroundColor(Color.RED);
                } else if (!isLoading){
                    setLoading();

                    Stripe stripe = new Stripe(DifferentCardActivity.this, getResources().getString(R.string.stripe_key));
                    stripe.createToken(
                            cardToSave,
                            new TokenCallback() {
                                public void onSuccess(Token token) {
                                    // Send token to your server
                                    UserManager.getInstance().addCard(DifferentCardActivity.this, token);
                                }
                                public void onError(Exception error) {
                                    // Show localized error message
                                    replaceCardFailed(error.getLocalizedMessage());
                                }
                            }
                    );
                }

            }
        });
    }

    public void replaceCardFailed(String error) {
        Toast.makeText(DifferentCardActivity.this, error, Toast.LENGTH_LONG).show();
        isLoading = false;
        mLoading.setVisibility(View.GONE);
    }

    public void setLoading() {
        isLoading = true;
        mLoading.setVisibility(View.VISIBLE);
    }
}
