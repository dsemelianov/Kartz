package com.test.davidemelianov.kartz;

import android.support.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by davidemelianov on 6/25/17.
 */

@Keep
@IgnoreExtraProperties
public class PaymentSource {
    public String last4;
    public String error;

    public PaymentSource() {}
}