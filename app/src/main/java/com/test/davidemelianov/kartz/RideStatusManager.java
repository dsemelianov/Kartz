package com.test.davidemelianov.kartz;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.test.davidemelianov.kartz.driver.BrowseRidesActivity;
import com.test.davidemelianov.kartz.driver.ConfirmRideActivity;
import com.test.davidemelianov.kartz.driver.GiveRideActivity;
import com.test.davidemelianov.kartz.driver.RideCancelledActivity;
import com.test.davidemelianov.kartz.driver.RideCompletedActivity;
import com.test.davidemelianov.kartz.passenger.DifferentCardActivity;
import com.test.davidemelianov.kartz.passenger.PaymentFailedActivity;
import com.test.davidemelianov.kartz.passenger.RequestSentActivity;
import com.test.davidemelianov.kartz.passenger.RideActivity;
import com.test.davidemelianov.kartz.passenger.RideExpiredActivity;
import com.test.davidemelianov.kartz.passenger.RideNeedsPaymentActivity;
import com.test.davidemelianov.kartz.passenger.RideNeedsReviewActivity;
import com.test.davidemelianov.kartz.passenger.SelectPassengersActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;

public class RideStatusManager {

    private static RideStatusManager mRideStatusManager;

    private FirebaseUser mCurrentAuth;

    private boolean passengerApp;

    private Activity mCurrentActivity;

    private Ride mCurrentRide;

    private FirebaseAnalytics mFirebaseAnalytics;

    private RideStatusManager() {
    }

    public static RideStatusManager getInstance() {

        if (mRideStatusManager == null) {
            mRideStatusManager = new RideStatusManager();
        }
        return mRideStatusManager;
    }

    public void checkRideState(Activity currentActivity, boolean passenger) {
        mCurrentActivity = currentActivity;
        passengerApp = passenger;
        mCurrentAuth = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference mRideReference = FirebaseDatabase.getInstance().getReference().child("rides");
        if (passengerApp) {
            mRideReference.orderByChild("passengerId").equalTo(mCurrentAuth.getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    if (dataSnapshot != null) {
                        updateState(dataSnapshot);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                    if (dataSnapshot != null) {
                        updateState(dataSnapshot);
                    }
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
            });
        } else {
            mRideReference.orderByChild("driverId").equalTo(mCurrentAuth.getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    if (dataSnapshot != null) {
                        updateState(dataSnapshot);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                    if (dataSnapshot != null) {
                        updateState(dataSnapshot);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        if (mCurrentActivity instanceof GiveRideActivity) {
                            ((GiveRideActivity) mCurrentActivity).rideTaken();
                        }
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void updateState(DataSnapshot snapshot) {
        Ride ride = snapshot.getValue(Ride.class);
        ride.key = snapshot.getKey();

        // if this is a driver giving a ride,
        // make sure the ride did not get scooped
        if ((mCurrentActivity instanceof GiveRideActivity) && (!ride.driverId.equals(mCurrentAuth.getUid()))) {
            ((GiveRideActivity) mCurrentActivity).rideTaken();
        }
        //check if there are rides that need attention
        else if ((ride.rideStatus == RideStatus.AVAILABLE.value()) && passengerApp) {
            setCurrentRide(ride);
            // go to the request sent page
            if (!(mCurrentActivity instanceof RequestSentActivity)) {
                Intent mIntent = new Intent(mCurrentActivity, RequestSentActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                try {
                    mCurrentActivity.startActivity(mIntent);
                } catch (Exception e) {
                }
            }
        } else if (ride.rideStatus == RideStatus.CLAIMED.value()) {
            setCurrentRide(ride);
            if (passengerApp) {
                // go to ride claimed screen
                if (!(mCurrentActivity instanceof RideActivity)) {
                    Intent mIntent = new Intent(mCurrentActivity, RideActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    try {
                        mCurrentActivity.startActivity(mIntent);
                    } catch (Exception e) {
                    }
                }
            } else {
                // go to driving page
                if (!(mCurrentActivity instanceof GiveRideActivity)) {
                    Intent mIntent = new Intent(mCurrentActivity, GiveRideActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    try {
                        mCurrentActivity.startActivity(mIntent);
                    } catch (Exception e) {
                    }
                } else {
                    ((GiveRideActivity) mCurrentActivity).setClaimedMode();
                }
            }
        } else if (ride.rideStatus == RideStatus.IN_TRANSIT.value()) {
            setCurrentRide(ride);
            if (passengerApp) {
                // go to ride claimed screen
                if (!(mCurrentActivity instanceof RideActivity)) {
                    Intent mIntent = new Intent(mCurrentActivity, RideActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    try {
                        mCurrentActivity.startActivity(mIntent);
                    } catch (Exception e) {
                    }
                } else {
                    ((RideActivity) mCurrentActivity).setTransitModeUI();
                }
            } else {
                // go to driving page
                if ((!(mCurrentActivity instanceof GiveRideActivity)) && (!(mCurrentActivity instanceof ConfirmRideActivity))) {
                    Intent mIntent = new Intent(mCurrentActivity, GiveRideActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    try {
                        mCurrentActivity.startActivity(mIntent);
                    } catch (Exception e) {
                    }
                } else if (mCurrentActivity instanceof GiveRideActivity) {
                    ((GiveRideActivity) mCurrentActivity).setTransitModeUI();
                }
            }
        } else if (ride.rideStatus == RideStatus.EXPIRED.value() && passengerApp && !ride.cancellationSeen) {
            setCurrentRide(ride);
            // show expired screen
            if (!(mCurrentActivity instanceof RideExpiredActivity)) {
                Intent mIntent = new Intent(mCurrentActivity, RideExpiredActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                try {
                    mCurrentActivity.startActivity(mIntent);
                } catch (Exception e) {
                }
            }
        } else if (ride.rideStatus == RideStatus.CANCELLED_BY_PASSENGER.value() && !passengerApp && !ride.cancellationSeen) {
            setCurrentRide(ride);
            //show cancelled by passenger screen
            if (!(mCurrentActivity instanceof RideCancelledActivity)) {
                Intent mIntent = new Intent(mCurrentActivity, RideCancelledActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                try {
                    mCurrentActivity.startActivity(mIntent);
                } catch (Exception e) {
                }
            }
        } else if (ride.rideStatus == RideStatus.CANCELLED_BY_DRIVER.value() && passengerApp && !ride.cancellationSeen) {
            setCurrentRide(ride);
            //show cancelled by driver screen
            if (!(mCurrentActivity instanceof com.test.davidemelianov.kartz.passenger.RideCancelledActivity)) {
                Intent mIntent = new Intent(mCurrentActivity, com.test.davidemelianov.kartz.passenger.RideCancelledActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                try {
                    mCurrentActivity.startActivity(mIntent);
                } catch (Exception e) {
                }
            }
        } else if (ride.rideStatus == RideStatus.NEEDS_PAYMENT.value() && passengerApp) {
            setCurrentRide(ride);
            //go to payment page
            if (!(mCurrentActivity instanceof RideNeedsPaymentActivity)) {
                Intent mIntent = new Intent(mCurrentActivity, RideNeedsPaymentActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                try {
                    mCurrentActivity.startActivity(mIntent);
                } catch (Exception e) {
                    int x =0;
                    x++;
                }
            }
        } else if (ride.rideStatus == RideStatus.PAYMENT_FAILED.value() && passengerApp) {
            setCurrentRide(ride);
            //go to failed payment page
            if ((!(mCurrentActivity instanceof PaymentFailedActivity)) && (!(mCurrentActivity instanceof DifferentCardActivity))) {
                Intent mIntent = new Intent(mCurrentActivity, PaymentFailedActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                try {
                    mCurrentActivity.startActivity(mIntent);
                } catch (Exception e) {
                }
            }
        } else if (ride.rideStatus == RideStatus.NEEDS_REVIEW.value() && passengerApp) {
            setCurrentRide(ride);
            //go to review page
            if (!(mCurrentActivity instanceof RideNeedsReviewActivity)) {
                Intent mIntent = new Intent(mCurrentActivity, RideNeedsReviewActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                try {
                    mCurrentActivity.startActivity(mIntent);
                } catch (Exception e) {
                }
            }
        }
    }

    public void setCurrentRide(Ride ride) {
        mCurrentRide = ride;
    }

    public String getDriverNumber() {
        if (mCurrentRide != null) {
            return mCurrentRide.driverPhone;
        }
        return null;
    }

    public String getPassengerNumber() {
        if (mCurrentRide != null) {
            return mCurrentRide.passengerPhone;
        }
        return null;
    }

    public String getPassengerName() {
        if (mCurrentRide != null) {
            return mCurrentRide.passengerName;
        }
        return null;
    }

    public String getTotalPrice() {
        if (mCurrentRide != null) {
            return Integer.toString(mCurrentRide.totalPrice.intValue());
        }
        return Integer.toString(0);
    }

    public String getCurrentRideID() {
        if (mCurrentRide != null) {
            return mCurrentRide.key;
        }
        return null;
    }

    public boolean isTransitMode() {
        if (mCurrentRide != null) {
            return mCurrentRide.rideStatus == RideStatus.IN_TRANSIT.value();
        }
        return false;
    }

    public void updateDriverLocation(Location location) {
        //Create or update the ride object
        HashMap<String, Object> rideUpdate = new HashMap<>();
        rideUpdate.put("driverLatitude", location.getLatitude());
        rideUpdate.put("driverLongitude", location.getLongitude());
        FirebaseDatabase.getInstance().getReference().child("rides").child(mCurrentRide.key).updateChildren(rideUpdate);
    }

    public Location getCurrentRideLocation() {
        Location destination = new Location("");
        destination.setLongitude(mCurrentRide.longitude);
        destination.setLatitude(mCurrentRide.latitude);
        return destination;
    }

    public Location getCurrentRideDriverLocation() {
        Location destination = new Location("");
        destination.setLongitude(mCurrentRide.driverLongitude);
        destination.setLatitude(mCurrentRide.driverLatitude);
        return destination;
    }

    public boolean isPassengerApp() {
        return passengerApp;
    }

    //////

    public void setRideToClaimed(String key, final Location driverLocation) {
        DatabaseReference mRideReference = FirebaseDatabase.getInstance().getReference().child("rides").child(key);
        mRideReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Ride rideToClaim = mutableData.getValue(Ride.class);
                if (rideToClaim == null) {
                    return Transaction.success(mutableData);
                }

                // if ride is still available, let's claim it
                if (rideToClaim.rideStatus == RideStatus.AVAILABLE.value()) {
                    rideToClaim.driverId = mCurrentAuth.getUid();
                    rideToClaim.driverName = mCurrentAuth.getDisplayName();
                    rideToClaim.driverPhone = mCurrentAuth.getPhoneNumber();
                    rideToClaim.driverLatitude = driverLocation.getLatitude();
                    rideToClaim.driverLongitude = driverLocation.getLongitude();
                    rideToClaim.rideStatus = RideStatus.CLAIMED.value();
                    mutableData.setValue(rideToClaim);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                boolean claimSuccessful = false;
                if (databaseError == null) {
                    Ride ride = dataSnapshot.getValue(Ride.class);
                    if (ride.driverId.equals(mCurrentAuth.getUid())) {
                        claimSuccessful = true;
                        setCurrentRide(ride);
                        mCurrentRide.key = dataSnapshot.getKey();
                    }
                }
                if (mCurrentActivity instanceof BrowseRidesActivity) {
                    ((BrowseRidesActivity) mCurrentActivity).claimRideCallback(claimSuccessful);
                }
            }
        });
    }

    public void setRideToInTransit() {
        DatabaseReference mRideReference = FirebaseDatabase.getInstance().getReference().child("rides").child(mCurrentRide.key);
        mRideReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Ride rideToClaim = mutableData.getValue(Ride.class);
                if (rideToClaim == null) {
                    return Transaction.success(mutableData);
                }

                if (rideToClaim.rideStatus == RideStatus.CLAIMED.value()) {
                    rideToClaim.rideStatus = RideStatus.IN_TRANSIT.value();
                    mutableData.setValue(rideToClaim);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                if (databaseError == null) {
                }
            }
        });
    }

    public void setRideToCancelledByPassenger(final String activity_value) {
        DatabaseReference mRideReference = FirebaseDatabase.getInstance().getReference().child("rides").child(mCurrentRide.key);
        mRideReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Ride rideToClaim = mutableData.getValue(Ride.class);
                if (rideToClaim == null) {
                    return Transaction.success(mutableData);
                }

                Bundle bundle = new Bundle();
                bundle.putString(Analytics.Parameters.ACTIVITY_NAME.value(), activity_value);
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(mCurrentActivity);
                mFirebaseAnalytics.logEvent(Analytics.Events.CANCELLED_BY_PASSENGER.value(), bundle);

                rideToClaim.rideStatus = RideStatus.CANCELLED_BY_PASSENGER.value();
                mutableData.setValue(rideToClaim);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                if (databaseError == null) {
                    Intent mIntent = new Intent(mCurrentActivity, CancelledYouActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mCurrentActivity.startActivity(mIntent);
                }
            }
        });
    }

    public void setRideToCancelledByDriver(final String activity_value) {
        DatabaseReference mRideReference = FirebaseDatabase.getInstance().getReference().child("rides").child(mCurrentRide.key);
        mRideReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Ride rideToClaim = mutableData.getValue(Ride.class);
                if (rideToClaim == null) {
                    return Transaction.success(mutableData);
                }

                Bundle bundle = new Bundle();
                bundle.putString(Analytics.Parameters.ACTIVITY_NAME.value(), activity_value);
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(mCurrentActivity);
                mFirebaseAnalytics.logEvent(Analytics.Events.CANCELLED_BY_DRIVER.value(), bundle);

                rideToClaim.rideStatus = RideStatus.CANCELLED_BY_DRIVER.value();
                mutableData.setValue(rideToClaim);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                if (databaseError == null) {
                    Intent mIntent = new Intent(mCurrentActivity, CancelledYouActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mCurrentActivity.startActivity(mIntent);
                }
            }
        });
    }

    public void setRideToNeedsPayment() {
        DatabaseReference mRideReference = FirebaseDatabase.getInstance().getReference().child("rides").child(mCurrentRide.key);
        mRideReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Ride rideToClaim = mutableData.getValue(Ride.class);
                if (rideToClaim == null) {
                    return Transaction.success(mutableData);
                }

                // if ride is still in transit, put it in payment mode
                if (rideToClaim.rideStatus == RideStatus.IN_TRANSIT.value()) {
                    rideToClaim.rideStatus = RideStatus.NEEDS_PAYMENT.value();
                    mutableData.setValue(rideToClaim);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                if (databaseError == null) {
                    Intent mIntent = new Intent(mCurrentActivity, RideCompletedActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    try {
                        mCurrentActivity.startActivity(mIntent);
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

    public void setRideToRetryPayment() {
        DatabaseReference mRideReference = FirebaseDatabase.getInstance().getReference().child("rides").child(mCurrentRide.key);
        mRideReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Ride rideToClaim = mutableData.getValue(Ride.class);
                if (rideToClaim == null) {
                    return Transaction.success(mutableData);
                }

                rideToClaim.rideStatus = RideStatus.NEEDS_PAYMENT.value();
                mutableData.setValue(rideToClaim);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                if (databaseError == null) {
                }
            }
        });
    }

    public void setRideToCompleted() {
        DatabaseReference mRideReference = FirebaseDatabase.getInstance().getReference().child("rides").child(mCurrentRide.key);
        mRideReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Ride rideToClaim = mutableData.getValue(Ride.class);
                if (rideToClaim == null) {
                    return Transaction.success(mutableData);
                }

                // if ride is still in needs review, put it in completed mode
                if (rideToClaim.rideStatus == RideStatus.NEEDS_REVIEW.value()) {
                    rideToClaim.rideStatus = RideStatus.COMPLETED.value();
                    mutableData.setValue(rideToClaim);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                if (databaseError == null) {
                    Intent mIntent = new Intent(mCurrentActivity, SelectPassengersActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    try {
                        mCurrentActivity.startActivity(mIntent);
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

    public void setCancellationSeen() {
        mCurrentRide.cancellationSeen = true;
        FirebaseDatabase.getInstance().getReference().child("rides").child(mCurrentRide.key).child("cancellationSeen").setValue(true);
    }
}