package com.test.davidemelianov.kartz;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.test.davidemelianov.kartz.driver.BrowseRidesActivity;
import com.test.davidemelianov.kartz.passenger.AddPaymentActivity;
import com.test.davidemelianov.kartz.passenger.ConfirmLocationActivity;
import com.test.davidemelianov.kartz.passenger.DifferentCardActivity;
import com.test.davidemelianov.kartz.passenger.SelectPassengersActivity;
import com.test.davidemelianov.kartz.signin.NameActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.stripe.android.model.Token;

public class UserManager {

    private static UserManager mStateManager;

    public FirebaseUser mCurrentAuth;

    private Activity mCurrentActivity;

    private boolean hasSpecialRoles;

    private UserManager() {
        if (mCurrentAuth == null) {
            mCurrentAuth = FirebaseAuth.getInstance().getCurrentUser();
        }
    }

    public static UserManager getInstance() {
        if (mStateManager == null) {
            mStateManager = new UserManager();
        }
        return mStateManager;
    }

    public void loadUserInfo(Activity activity) {
        mCurrentActivity = activity;

        // if auth object is missing info, redirect to sign in flow to set it
        if ((mCurrentAuth.getEmail() == null) || (mCurrentAuth.getDisplayName() == null)) {
            Intent mIntent = new Intent(mCurrentActivity, NameActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            try {
                mCurrentActivity.startActivity(mIntent);
            } catch (Exception e) {
            }
        } else if (mCurrentActivity instanceof MainActivity){
            loadUserRoles(mCurrentActivity);
        } else if (mCurrentActivity instanceof BrowseRidesActivity) {
            loadUserActiveState(mCurrentActivity);
        }
    }

    // load any special roles associated with this user
    // invokes UI initialization callback if this is the main activity
    public void loadUserRoles(Activity activity) {
        mCurrentActivity = activity;

        DatabaseReference mRolesReference = FirebaseDatabase.getInstance().getReference().child("roles");

        mRolesReference.child(mCurrentAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if they do, initialize the UI
                if (dataSnapshot != null) {
                    boolean isDriver = false;
                    try {
                        isDriver = (boolean) dataSnapshot.child("driver").getValue();
                    } catch (Exception e) {

                    }
                    boolean isAdmin = false;
                    try {
                        isAdmin = (boolean) dataSnapshot.child("admin").getValue();
                    } catch (Exception e) {

                    }
                    // if the user has special roles, initialize the UI
                    if (isAdmin || isDriver) {
                        hasSpecialRoles = true;
                        if (mCurrentActivity instanceof MainActivity) {
                            ((MainActivity) mCurrentActivity).initializeActivity(isDriver, isAdmin);
                        }
                        //if they do not, skip to the request ride activity
                    } else {
                        hasSpecialRoles = false;
                        Intent mIntent = new Intent(mCurrentActivity, SelectPassengersActivity.class);
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mCurrentActivity.startActivity(mIntent);
                    }
                } else {
                    Intent mIntent = new Intent(mCurrentActivity, SelectPassengersActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mCurrentActivity.startActivity(mIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // load the state for this driver
    // invokes UI initialization callback if this is the give ride activity
    public void loadUserActiveState(Activity activity) {
        mCurrentActivity = activity;

        final Query mUserQuery = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentAuth.getUid());
        mUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot != null) && mCurrentActivity instanceof BrowseRidesActivity) {
                    User currentUser = dataSnapshot.getValue(User.class);
                    ((BrowseRidesActivity) mCurrentActivity).driverStatusCallback(currentUser.active);
                } else {
                    Intent mIntent = new Intent(mCurrentActivity, MainActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mCurrentActivity.startActivity(mIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mCurrentActivity, "Uh oh! Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference mRolesReference = FirebaseDatabase.getInstance().getReference().child("roles");

        mRolesReference.child(mCurrentAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if they do, initialize the UI
                if (dataSnapshot != null) {
                    boolean isDriver = false;
                    try {
                        isDriver = (boolean) dataSnapshot.child("driver").getValue();
                    } catch (Exception e) {

                    }
                    boolean isAdmin = false;
                    try {
                        isAdmin = (boolean) dataSnapshot.child("admin").getValue();
                    } catch (Exception e) {

                    }
                    // if the user has special roles, initialize the UI
                    if (isAdmin || isDriver) {
                        hasSpecialRoles = true;
                        if (mCurrentActivity instanceof MainActivity) {
                            ((MainActivity) mCurrentActivity).initializeActivity(isDriver, isAdmin);
                        }
                        //if they do not, skip to the request ride activity
                    } else {
                        hasSpecialRoles = false;
                        Intent mIntent = new Intent(mCurrentActivity, SelectPassengersActivity.class);
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mCurrentActivity.startActivity(mIntent);
                    }
                } else {
                    Intent mIntent = new Intent(mCurrentActivity, SelectPassengersActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mCurrentActivity.startActivity(mIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean userHasSpecialRoles() {
        return hasSpecialRoles;
    }

    public void checkForPaymentSources(Activity activity) {
        mCurrentActivity = activity;
        checkCardStatus();
    }

    public void addCard(Activity activity, final Token token) {
        mCurrentActivity = activity;
        FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentAuth.getUid()).child("paymentSources").push().child("token").setValue(token.getId());
        checkCardStatus();
    }

    private void checkCardStatus() {
        final Query mCardQuery = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentAuth.getUid()).child("paymentSources").orderByKey().limitToLast(1);
        mCardQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot != null) && dataSnapshot.hasChildren()) {
                    DataSnapshot cardSnapshot = dataSnapshot.getChildren().iterator().next();

                    PaymentSource card = cardSnapshot.getValue(PaymentSource.class);
                    if (card.last4 != null && !card.last4.isEmpty()) {
                        if (mCurrentActivity instanceof AddPaymentActivity) {
                            mCurrentActivity.finish();
                        } else if (mCurrentActivity instanceof ConfirmLocationActivity) {
                            ((ConfirmLocationActivity) mCurrentActivity).requestRide();
                        }
                    } else if (card.error != null && !card.error.isEmpty()) {
                        if (mCurrentActivity instanceof AddPaymentActivity) {
                            ((AddPaymentActivity) mCurrentActivity).replaceCardFailed(card.error);
                        } else if (mCurrentActivity instanceof DifferentCardActivity) {
                            ((DifferentCardActivity) mCurrentActivity).replaceCardFailed(card.error);
                        } else if (mCurrentActivity instanceof ConfirmLocationActivity) {
                            ((ConfirmLocationActivity) mCurrentActivity).requestPayment();
                        }
                    } else {
                        checkCardStatus();
                    }
                } else {
                    if (mCurrentActivity instanceof ConfirmLocationActivity) {
                        ((ConfirmLocationActivity) mCurrentActivity).requestPayment();
                    } else {
                        checkCardStatus();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (mCurrentActivity instanceof ConfirmLocationActivity) {
                    ((ConfirmLocationActivity) mCurrentActivity).checkForPaymentSourcesFailed(databaseError.getMessage());
                } else if (mCurrentActivity instanceof AddPaymentActivity) {
                    ((AddPaymentActivity) mCurrentActivity).replaceCardFailed(databaseError.getMessage());
                } else if (mCurrentActivity instanceof DifferentCardActivity) {
                    ((DifferentCardActivity) mCurrentActivity).replaceCardFailed(databaseError.getMessage());
                }
            }
        });
    }

    public void setActiveState(boolean active) {
        FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentAuth.getUid()).child("active").setValue(active);
    }

}