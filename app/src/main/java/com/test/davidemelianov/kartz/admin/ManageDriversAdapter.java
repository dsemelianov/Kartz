package com.test.davidemelianov.kartz.admin;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.test.davidemelianov.kartz.R;
import com.test.davidemelianov.kartz.Role;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ManageDriversAdapter extends BaseAdapter {

    ArrayList<Role> mList;
    Activity mActivity;

    public ManageDriversAdapter(ArrayList<Role> list, Activity context) {
        this.mList = list;
        this.mActivity = context;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.list_item_manage_driver, parent, false);
        }

        final Role driver = (Role) getItem(position);

        TextView mNameLayout = (TextView) convertView.findViewById(R.id.name);
        mNameLayout.setText(driver.name);
        mNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(mActivity, RideHistoryActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("User", driver.key);
                mActivity.startActivity(mIntent);;
            }
        });

        ((TextView) convertView.findViewById(R.id.phone_number)).setText(driver.phone);

        RelativeLayout mPhoneLayout = (RelativeLayout) convertView.findViewById(R.id.phone_layout);
        mPhoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + driver.phone));
                    mActivity.startActivity(intent);
                } else {
                    ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.CALL_PHONE}, 69);
                }
            }
        });

        ImageView mDelete = (ImageView) convertView.findViewById(R.id.delete);
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Role mCurrentRole = mList.get(position);
                FirebaseDatabase.getInstance().getReference().child("roles").child(mCurrentRole.key).child("driver").setValue(false);
                mList.remove(position);
                notifyDataSetChanged();
            }
        });


        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) { return mList.get(position); }

    public Object addItem(Role role) { return mList.add(role); }

    @Override
    public int getCount() { return mList.size(); }

}