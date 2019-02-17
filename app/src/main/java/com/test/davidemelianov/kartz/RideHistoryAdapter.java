package com.test.davidemelianov.kartz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.test.davidemelianov.kartz.admin.RideHistoryActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class RideHistoryAdapter extends BaseAdapter {

    ArrayList<Ride> mList;
    Activity mActivity;

    public RideHistoryAdapter(ArrayList<Ride> list, Activity context) {
        this.mList = list;
        this.mActivity = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_list_past_ride, parent, false);
        }

        final Ride ride = (Ride) getItem(position);

        RelativeLayout mRow = convertView.findViewById(R.id.body);
        mRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity instanceof RideHistoryActivity) {
                    Bundle bundle = new Bundle();
                    bundle.putString("user", ride.driverId);
                    Intent mIntent = new Intent(mActivity, com.test.davidemelianov.kartz.driver.RideHistoryActivity.class);
                    mIntent.putExtras(bundle);
                    mActivity.startActivity(mIntent);
                }
            }
        });

        ((TextView) convertView.findViewById(R.id.name)).setText(ride.driverName);

        setTimeOfDay(convertView, ride);

        setPassengerText(convertView, ride);

        setRideStatus(convertView, ride);

        return convertView;
    }

    private void setTimeOfDay(View convertView, Ride ride) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ride.timestamp);

        String month = new SimpleDateFormat("MMMM").format(calendar.getTime());
        String date = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
        String hour = Integer.toString(calendar.get(Calendar.HOUR));
        if (hour.equals("0")) {
            hour = "12";
        }
        int min = calendar.get(Calendar.MINUTE);
        String minute;
        if (min < 10) {
            minute = "0" + min;
        } else {
            minute = Integer.toString(min);
        }
        String timeOfDay;
        if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
            timeOfDay = "AM";
        } else {
            timeOfDay = "PM";
        }

        ((TextView) convertView.findViewById(R.id.date)).setText(month + " " + date + " at " + hour + ":" + minute + " " + timeOfDay);
    }

    private void setPassengerText(View convertView, Ride ride) {
        String totalPrice = "$" + Double.toString(ride.totalPrice) + "0";
        ((TextView) convertView.findViewById(R.id.price)).setText(totalPrice);
    }

    private void setRideStatus(View convertView, Ride ride) {
        if (ride.rideStatus == RideStatus.CLAIMED.value()) {
            ((TextView) convertView.findViewById(R.id.payment)).setText("Ride in progress");
        } else if (ride.rideStatus == RideStatus.IN_TRANSIT.value()) {
            ((TextView) convertView.findViewById(R.id.payment)).setText("Ride in progress");
        } else if (ride.rideStatus == RideStatus.EXPIRED.value()) {
            ((TextView) convertView.findViewById(R.id.payment)).setText("Ride expired");
        } else if (ride.rideStatus == RideStatus.CANCELLED_BY_PASSENGER.value()) {
            ((TextView) convertView.findViewById(R.id.payment)).setText("Cancelled by passenger");
        } else if (ride.rideStatus == RideStatus.CANCELLED_BY_DRIVER.value()) {
            ((TextView) convertView.findViewById(R.id.payment)).setText("Cancelled by driver");
        } else if (ride.rideStatus == RideStatus.NEEDS_PAYMENT.value()) {
            ((TextView) convertView.findViewById(R.id.payment)).setText("Needs payment");
        } else if (ride.rideStatus == RideStatus.PAYMENT_FAILED.value()) {
            ((TextView) convertView.findViewById(R.id.payment)).setText("Payment failed");
        } else if (ride.rideStatus == RideStatus.NEEDS_REVIEW.value()) {
            ((TextView) convertView.findViewById(R.id.payment)).setText("Ride completed");
        } else if (ride.rideStatus == RideStatus.COMPLETED.value()) {
            ((TextView) convertView.findViewById(R.id.payment)).setText("Ride completed");
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Ride getItem(int position) { return mList.get(position); }

    public void addItem(Ride ride) {
        if (mList.isEmpty()) {
            mList.add(ride);
        } else {
            int i = 0;
            while (mList.get(i).timestamp > ride.timestamp) {
                i++;
            }
            mList.add(i, ride);
        }
        notifyDataSetChanged();
        if (mActivity instanceof com.test.davidemelianov.kartz.driver.RideHistoryActivity) {
            ((com.test.davidemelianov.kartz.driver.RideHistoryActivity) mActivity).updateEmptyView(mList.isEmpty());
        } else if (mActivity instanceof com.test.davidemelianov.kartz.passenger.RideHistoryActivity) {
            ((com.test.davidemelianov.kartz.passenger.RideHistoryActivity) mActivity).updateEmptyView(mList.isEmpty());
        } else if (mActivity instanceof RideHistoryActivity) {
            ((RideHistoryActivity) mActivity).updateEmptyView(mList.isEmpty());
        }
    }

    @Override
    public int getCount() { return mList.size(); }
}