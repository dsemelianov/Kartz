package com.test.davidemelianov.kartz;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.test.davidemelianov.kartz.driver.ConfirmRideActivity;
import com.test.davidemelianov.kartz.driver.GiveRideActivity;
import com.test.davidemelianov.kartz.passenger.RequestSentActivity;
import com.test.davidemelianov.kartz.passenger.RideActivity;

/**
 * Created by davidemelianov on 10/17/15.
 */
public class ConfirmCancelDialog {

    private Activity mActivity;

    private Dialog mDialog;

    private Button mConfirmButton;

    public ConfirmCancelDialog(Activity activity) {

        mActivity = activity;

        // Create custom dialog object
        mDialog = new Dialog(mActivity);
        // Include dialog.xml file
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_confirm_cancel);

        mConfirmButton = (Button) mDialog.findViewById(R.id.button_confirm);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mDialog.hide();
            if (mActivity instanceof RequestSentActivity) {
                ((RequestSentActivity) mActivity).cancel();
            } else if (mActivity instanceof RideActivity) {
                ((RideActivity) mActivity).cancel();
            } else if (mActivity instanceof GiveRideActivity) {
                ((GiveRideActivity) mActivity).cancel();
            } else if (mActivity instanceof ConfirmRideActivity) {
                ((ConfirmRideActivity) mActivity).cancel();
            }

            }
        });
    }

    public void show() {
        mDialog.show();
    }
}