package com.test.davidemelianov.kartz.driver;

import android.content.Context;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.davidemelianov.kartz.Analytics;
import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.Ride;
import com.test.davidemelianov.kartz.RideStatusManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BrowseRidesAdapter extends BaseAdapter {

    ArrayList<Ride> mList;
    BrowseRidesActivity mActivity;

    public BrowseRidesAdapter(ArrayList<Ride> list, BrowseRidesActivity context) {
        this.mList = list;
        this.mActivity = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_list_browse_ride, parent, false);
        }

        final Ride ride = (Ride) getItem(position);

        ((TextView) convertView.findViewById(R.id.name)).setText(ride.passengerName);

        if (ride.distanceText == null) {
            ((TextView) convertView.findViewById(R.id.distance)).setText("Loading...");
        } else {
            ((TextView) convertView.findViewById(R.id.distance)).setText(ride.distanceText);
        }

        if (ride.numberOfPassengers == 1) {
            ((TextView) convertView.findViewById(R.id.passenger)).setText("1 passenger");
        } else {
            ((TextView) convertView.findViewById(R.id.passenger)).setText(ride.numberOfPassengers + " passengers");
        }

        final ImageView mAccept = (ImageView) convertView.findViewById(R.id.approve);
        mAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mActivity.isLoading) {
                    Bundle bundle = new Bundle();
                    mActivity.mFirebaseAnalytics.logEvent(Analytics.Events.CLAIM_RIDE.value(), bundle);
                    mActivity.setLoading();
                    RideStatusManager.getInstance().setRideToClaimed(getItem(position).key, mActivity.mLocationHandler.getCurrentLocation());
                }
            }
        });

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Ride getItem(int position) { return mList.get(position); }

    public void addItem(Ride ride) {
        setDistance(ride);
        mList.add(ride);
        mActivity.updateEmptyView(mList.isEmpty());
        sort();

        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 400 milliseconds
        v.vibrate(400);

        final MediaPlayer mp = MediaPlayer.create(mActivity, R.raw.ride_notification);
        mp.start();
    }

    public void removeItem(String id) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).key.equals(id)) {
                mList.remove(i);
                notifyDataSetChanged();
                break;
            }
        }
        mActivity.updateEmptyView(mList.isEmpty());
    }

    @Override
    public int getCount() { return mList.size(); }

    public void sort() {
        Collections.sort(mList, new Comparator<Ride>() {
            @Override
            public int compare(Ride r1, Ride r2) {
                if (r1.distance == null) {
                    return 1;
                } else if (r2.distance == null) {
                    return -1;
                }
                Integer compare = r1.distance - r2.distance;
                return compare.intValue();
            }
        });
        notifyDataSetChanged();
    }

    public void updateLocation(Location location) {
        for (Ride ride : mList) {
            // recalculate distance
            if (mActivity.mLocationHandler.getCurrentLocation() != null) {
                Location rideLocation = new Location("ride location");
                rideLocation.setLatitude(ride.latitude);
                rideLocation.setLongitude(ride.longitude);

                Integer mDistanceValueInt = Math.round(rideLocation.distanceTo(location));
                String mStringDistance = (mDistanceValueInt > 1000) ? "1000+" : mDistanceValueInt.toString();

                ride.distance = mDistanceValueInt;
                ride.distanceText = mStringDistance + " m";
            }
        }
        sort();
    }

    private void setDistance(Ride ride) {
        if (mActivity.mLocationHandler != null && mActivity.mLocationHandler.getCurrentLocation() != null) {
            Location rideLocation = new Location("ride location");
            rideLocation.setLatitude(ride.latitude);
            rideLocation.setLongitude(ride.longitude);

            Integer mDistanceValueInt = Math.round(rideLocation.distanceTo(mActivity.mLocationHandler.getCurrentLocation()));
            String mStringDistance = (mDistanceValueInt > 1000) ? "1000+" : mDistanceValueInt.toString();

            ride.distance = mDistanceValueInt;
            ride.distanceText = mStringDistance + " m";
        }
    }

}