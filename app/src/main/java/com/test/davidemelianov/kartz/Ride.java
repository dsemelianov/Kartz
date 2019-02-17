package com.test.davidemelianov.kartz;

import android.support.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by davidemelianov on 6/25/17.
 */
@Keep
@IgnoreExtraProperties
public class Ride {
    public String key;

    public Long timestamp;

    public String passengerId;
    public String passengerName;
    public String passengerPhone;

    public Double latitude;
    public Double longitude;

    public Integer distance;
    public String distanceText;

    public Integer numberOfPassengers;
    public Double pricePerPassenger;
    public Double totalPrice;

    public String driverId;
    public String driverName;
    public String driverPhone;

    public Double driverLatitude;
    public Double driverLongitude;

    public Integer rideStatus;
    public boolean cancellationSeen;

    public Ride() {}

    public void createRide(Long mTimestamp,
                           String mPassengerId, String mPassengerName, String mPassengerPhone,
                           Double mLatitude, Double mLongitude,
                           Integer mNumberOfPassengers, Double mPricePerPassenger, Double mTotalPrice) {

        this.timestamp = mTimestamp;
        this.rideStatus = RideStatus.AVAILABLE.value();

        this.passengerId = mPassengerId;
        this.passengerName = mPassengerName;
        this.passengerPhone = mPassengerPhone;

        this.latitude = mLatitude;
        this.longitude = mLongitude;

        this.numberOfPassengers = mNumberOfPassengers;
        this.pricePerPassenger = mPricePerPassenger;
        this.totalPrice = mTotalPrice;
    }

}