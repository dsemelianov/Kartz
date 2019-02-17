package com.test.davidemelianov.kartz;

import android.support.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by davidemelianov on 6/25/17.
 */
@Keep
@IgnoreExtraProperties
public class User {

    public String name;
    public String phone;

    public boolean android;
    public boolean active;

    public User() {}

}