package com.test.davidemelianov.kartz;

import android.support.annotation.Keep;

/**
 * Created by davidemelianov on 6/25/17.
 */

@Keep
public class Settings {
    public Integer passengerLimit;
    public Double priceMinimum;
    public Double pricePerPassenger;

    public float latitude;
    public float longitude;

    public float range;

    public boolean closed;

    public Settings() {}
}