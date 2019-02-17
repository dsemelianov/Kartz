package com.test.davidemelianov.kartz;

import android.support.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by davidemelianov on 6/25/17.
 */
@Keep
@IgnoreExtraProperties
public class Role {
    public String key;

    public boolean driver;
    public boolean admin;

    public String name;
    public String phone;

    public Role() {}

    public void setKey(String key) {
        this.key = key;
    }

}