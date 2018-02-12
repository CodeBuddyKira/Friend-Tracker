package com.example.admin.myproject;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 28-06-2017.
 */
public class userList {
    static private User user;
    static private User currentUser;
    static private List<User> users = new ArrayList<User>();
    static private Loc loc = new Loc(0,0);
    static private Map<String,Integer> sent = new HashMap<String, Integer>();
    static private Map<String,Integer> received = new HashMap<String, Integer>();
    static private Map<String,Integer> friends = new HashMap<String, Integer>();
    static private int interval = 30000;
    static public Context context;
    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        userList.currentUser = currentUser;
    }

    public static Map<String, Integer> getSent() {
        return sent;

    }
    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        userList.user = user;
    }
    public static int getInterval() {
        return interval;
    }

    public static void setInterval(int interval) {
        userList.interval = interval;
    }

    public static void setSent(Map<String, Integer> sent) {
        userList.sent = sent;
    }

    public static Map<String, Integer> getReceived() {
        return received;
    }

    public static void setReceived(Map<String, Integer> received) {
        userList.received = received;
    }

    public static Map<String, Integer> getFriends() {
        return friends;
    }

    public static void setFriends(Map<String, Integer> friends) {
        userList.friends = friends;
    }

    public static mAdapter getMyAdapter() {
        return myAdapter;

    }

    public static void setMyAdapter(mAdapter myAdapter) {
        userList.myAdapter = myAdapter;
    }

    static private mAdapter myAdapter;
     public userList(){
     }

    static public List<User> getUsers() {
        return users;
    }

    public static Loc getLoc() {
        return loc;
    }

    public static void setLoc(Loc loc) {
        userList.loc = loc;
    }

    static public void setUsers(List<User> user) {
        users = user;
    }
}
