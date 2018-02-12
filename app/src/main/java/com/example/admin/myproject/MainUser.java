package com.example.admin.myproject;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

/**
 * Created by Admin on 27-06-2017.
 */
public class MainUser {
    public String uid;
    public String email;
    public String name;
    public Loc location;
    public String url;
    Object timestamp;
    public MainUser() {}

    public MainUser(String uid, String email, String name, Loc location, String url) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.location = location;
        this.url = url;
        this.timestamp = "";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Loc getLocation() {
        return location;
    }

    public void setLocation(Loc location) {
        this.location = location;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
