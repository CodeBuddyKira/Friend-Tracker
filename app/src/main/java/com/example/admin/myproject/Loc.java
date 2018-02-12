package com.example.admin.myproject;

/**
 * Created by Admin on 27-06-2017.
 */
public class Loc {
    double latitude;
    double longitude;

    public Loc(){}

    public Loc(double latitude, double longitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }
    public void setLocation(double longitude, double latitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
