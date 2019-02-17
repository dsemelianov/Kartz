package com.test.davidemelianov.kartz;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.test.davidemelianov.kartz.driver.BrowseRidesActivity;
import com.test.davidemelianov.kartz.driver.GiveRideActivity;
import com.test.davidemelianov.kartz.passenger.ConfirmLocationActivity;

public class CurrentLocationHandler {

    private ConfirmLocationActivity mConfirmLocationActivity;
    private BrowseRidesActivity mBrowseRidesActivity;
    private GiveRideActivity mGiveRideActivity;

    private static CurrentLocationHandler mLocationHandler;

    private LocationManager mLocationManager = null;
    private boolean mInitialized = false;
    private Location mLocation;

    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 1f;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private CurrentLocationHandler(ConfirmLocationActivity c) {
        mLocation = new Location("cached");
        mLocation.setLatitude(0f);
        mLocation.setLongitude(0f);
        initializeLocationManager(c);
    }

    private CurrentLocationHandler(BrowseRidesActivity c) {
        mLocation = new Location("cached");
        mLocation.setLatitude(0f);
        mLocation.setLongitude(0f);
        initializeLocationManager(c);
    }

    private CurrentLocationHandler(GiveRideActivity c) {
        mLocation = new Location("cached");
        mLocation.setLatitude(0f);
        mLocation.setLongitude(0f);
        initializeLocationManager(c);
    }

    public static CurrentLocationHandler getInstance(ConfirmLocationActivity c) {

        if (mLocationHandler == null) {
            mLocationHandler = new CurrentLocationHandler(c);
        } else {
            mLocationHandler.initializeLocationManager(c);
        }
        return mLocationHandler;
    }

    public static CurrentLocationHandler getInstance(BrowseRidesActivity c) {

        if (mLocationHandler == null) {
            mLocationHandler = new CurrentLocationHandler(c);
        } else {
            mLocationHandler.initializeLocationManager(c);
        }
        return mLocationHandler;
    }

    public static CurrentLocationHandler getInstance(GiveRideActivity c) {

        if (mLocationHandler == null) {
            mLocationHandler = new CurrentLocationHandler(c);
        } else {
            mLocationHandler.initializeLocationManager(c);
        }
        return mLocationHandler;
    }

    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider) {
            mLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            // Toast.makeText(getBaseContext(), location.getLatitude() + ", " +location.getLongitude(), Toast.LENGTH_LONG).show();
            if (isBetterLocation(location, mLocation)) {
                if ((location.getLongitude() != 0f) && (location.getLatitude() != 0f)) {
                    mLocation.set(location);
                    if (mConfirmLocationActivity != null) {
                        try {
                            mConfirmLocationActivity.updateLocation(getCurrentLocation());
                        } catch (Exception e) {
                            Toast.makeText(mConfirmLocationActivity, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (mBrowseRidesActivity != null) {
                        try {
                            mBrowseRidesActivity.updateLocation(getCurrentLocation());
                        } catch (Exception e) {
                            Toast.makeText(mBrowseRidesActivity, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (mGiveRideActivity != null) {
                        try {
                            RideStatusManager.getInstance().updateDriverLocation(location);
                        } catch (Exception e) {
                            Toast.makeText(mGiveRideActivity, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    public void unregisterLocationManager() {
        if (mInitialized) {
            if (mLocationManager != null) {
                for (int i = 0; i < mLocationListeners.length; i++) {
                    try {
                        mLocationManager.removeUpdates(mLocationListeners[i]);
                        mInitialized = false;
                    } catch (SecurityException e) {
                        if (mConfirmLocationActivity != null) {
                            Toast.makeText(mConfirmLocationActivity, "You haven't given us location permissions!", Toast.LENGTH_SHORT).show();
                        }
                        if (mBrowseRidesActivity != null) {
                            Toast.makeText(mBrowseRidesActivity, "You haven't given us location permissions!", Toast.LENGTH_SHORT).show();
                        }
                        if (mGiveRideActivity != null) {
                            Toast.makeText(mGiveRideActivity, "You haven't given us location permissions!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        if (mConfirmLocationActivity != null) {
                            Toast.makeText(mConfirmLocationActivity, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        if (mBrowseRidesActivity != null) {
                            Toast.makeText(mBrowseRidesActivity, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        if (mGiveRideActivity != null) {
                            Toast.makeText(mGiveRideActivity, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
        mInitialized = false;
    }

    // TRY USING HIGH ACCURACY CRITERIA!!!!!!!!!
    public void initializeLocationManager(Activity activity) {
        setCurrentActivity(activity);
        if (!mInitialized) {
            mLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[1]);
            } catch (SecurityException e) {
                Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
            }

            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (SecurityException e) {
                Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
            }

            mInitialized = true;
        }
    }

    private void setCurrentActivity(Activity activity) {
        mConfirmLocationActivity = null;
        mBrowseRidesActivity = null;
        mGiveRideActivity = null;

        if (activity instanceof ConfirmLocationActivity) {
            mConfirmLocationActivity = (ConfirmLocationActivity) activity;
        } else if (activity instanceof BrowseRidesActivity) {
            mBrowseRidesActivity = (BrowseRidesActivity) activity;
        } else if (activity instanceof GiveRideActivity) {
            mGiveRideActivity = (GiveRideActivity) activity;
        }
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
       // return true;

        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;

        boolean isMoreAccurate = accuracyDelta < 0;
        if (isMoreAccurate) return true;

        boolean isSignificantlyLessAccurate = accuracyDelta > 5;
        boolean isAccurate = location.getAccuracy() < 8;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        //if it's accurate and not old, use it
        if (isAccurate && !isSignificantlyOlder) return true;

        //if it's a lot newer, use it
        if (isSignificantlyNewer) return true;

        //if new reading isn't older, use it     ????
        //if (!isSignificantlyOlder) return true;

        if (isNewer && isAccurate) return true;

       // If the new location is older and less accurate, it must be worse
        if (isSignificantlyOlder && isSignificantlyLessAccurate) return false;

        // Determine location quality using a combination of timeliness and accuracy
        if (isNewer && !isLessAccurate)  return true;
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) return true;

        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public Location getCurrentLocation() {
        if (mLocation.getLatitude() != 0.f && mLocation.getLongitude() != 0.f) {
            return mLocation;
        }
        return null;
    }

}