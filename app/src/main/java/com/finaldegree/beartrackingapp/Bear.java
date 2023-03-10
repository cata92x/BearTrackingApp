package com.finaldegree.beartrackingapp;

import java.util.Date;

public class Bear {
    private int code;
    private final double latitude;
    private final double longitude;
    private Date lastUpdate;

    public Bear(int code, double latitude, double longitude, Date lastUpdate) {
        this.code = code;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdate = lastUpdate;
    }

    public Bear(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getCode() {
        return code;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }
}
