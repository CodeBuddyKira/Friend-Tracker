package com.example.admin.myproject;

import android.os.Parcel;

import java.io.ObjectInput;
import java.io.Serializable;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
/**
 * Created by Admin on 27-06-2017.
 */
public class User  implements SearchSuggestion{
    public String uid;
    public String email;
    public String name;
    public Loc location;
    public String url;
    public Object timestamp;

    public User(){
    }
    public String getTimestamp() {
        return String.valueOf(timestamp);
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public User(String uid, String email, String name, Loc location, String url, Object timestamp) {
        this.uid = uid;
        this.email = email;
        this.timestamp = timestamp;
        this.name = name;
        this.location = location;
        this.url=url;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLocation(Loc location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Loc getLocation() {
        return location;
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

    public String getlatlng(){
        return (String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude()));
    }

    @Override
    public String getBody() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
    }
}
