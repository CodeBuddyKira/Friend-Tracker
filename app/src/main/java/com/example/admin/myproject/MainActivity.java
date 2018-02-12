package com.example.admin.myproject;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private mAdapter myAdapter;
    private User currentUser;
    private List<User> users = new ArrayList<>();
    private List<User> friendList = new ArrayList<>();
    private Loc myLocation;
    private RecyclerView recyclerView;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private GoogleApiClient googleApiClient;
    private TextToSpeech t1;
    private FloatingSearchView mSearchView;
    private MainActivity mainActivity;
    private Intent intent;
    private Map<String, Integer> sent = new HashMap<String, Integer>();
    private Map<String, Integer> received = new HashMap<String, Integer>();
    private Map<String, Integer> friends = new HashMap<String, Integer>();
    SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSearchView = (FloatingSearchView) findViewById(R.id.search);
        context= this;
        userList.context = context;
        myAdapter = new mAdapter(context, users);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(myAdapter);
        userList.setMyAdapter(myAdapter);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        getUsersFromFirebase();

        if (!isMyServiceRunning(MyService.class)) {
            intent = new Intent(this, MyService.class);
            startService(intent);
        }
        setupFloatingSearch();
    }

    public void signOut() {

        auth.signOut();
    }

    @Override
    protected void onResume() {
        userList.setInterval(30000);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        super.onResume();
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        userList.setInterval(300000);
        googleApiClient.disconnect();
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }

        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
        super.onStop();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void setupFloatingSearch() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {

                newQuery = newQuery.toLowerCase();
                final List<User> filteredList = new ArrayList<>();
                for (int i = 0; i < users.size(); i++) {
                    final String text = users.get(i).getName().toLowerCase();
                    if (text.contains(newQuery)) {
                        filteredList.add(users.get(i));
                    }
                }
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                myAdapter = new mAdapter(context, filteredList);
                recyclerView.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
                Log.d("main", "onSearchTextChanged()");
            }
        });
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {

                if (item.getItemId() == R.id.action_map) {
                    friendList.clear();
                    for(User user:users){
                        if(userList.getFriends().get(user.getUid())!=null && userList.getFriends().get(user.getUid())==1)
                            friendList.add(user);
                    }
                    userList.setUsers(friendList);
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(intent);
                }
                else if (item.getItemId() == R.id.action_nearest) {
                    String toSpeak = "Nearest Friend To You is " + getNearest();
                    Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                }
                else if (item.getItemId() == R.id.action_signOut) {
                    signOut();
                }
                else if   (item.getItemId() == R.id.action_profile){
                    userList.setUser(currentUser);
                    startActivity(new Intent(MainActivity.this,profileActivity.class));
                }
            }
        });

        mSearchView.setOnSuggestionsListHeightChanged(new FloatingSearchView.OnSuggestionsListHeightChanged() {
            @Override
            public void onSuggestionsListHeightChanged(float newHeight) {
                recyclerView.setTranslationY(newHeight);
            }
        });
    }

    private String getNearest() {
        String Nearest = "";
        Location startPoint = new Location("A");
        startPoint.setLatitude(myLocation.getLatitude());
        startPoint.setLongitude(myLocation.getLongitude());
        Location endPoint = new Location("B");
        double dis = 1000000000.00;
        for (User user : users) {
            endPoint.setLatitude(user.getLocation().getLatitude());
            endPoint.setLongitude(user.getLocation().getLongitude());
            if (dis > (startPoint.distanceTo(endPoint))) {
                dis = (startPoint.distanceTo(endPoint));
                Nearest = user.getName();
            }
        }
        return Nearest;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void getUsersFromFirebase() {

        FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren()
                                .iterator();
                        users.clear();
                        while (dataSnapshots.hasNext()) {
                            DataSnapshot dataSnapshotChild = dataSnapshots.next();
                            User user = dataSnapshotChild.getValue(User.class);
                            if (!TextUtils.equals(user.uid,
                                    FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                users.add(user);
                            }
                            else{
                                currentUser = user;
                                userList.setCurrentUser(currentUser);
                            }
                        }
                        myAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Unable to retrieve the users.
                    }
                });

        FirebaseDatabase.getInstance()
                .getReference()
                .child("FriendRequestReceiver")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren()
                                .iterator();
                        received.clear();
                        while (dataSnapshots.hasNext()) {
                            DataSnapshot dataSnapshotChild = dataSnapshots.next();
                            Integer a = dataSnapshotChild.getValue(Integer.class);
                            Log.d(String.valueOf(a)+" Received",dataSnapshotChild.getKey());
                            if (a != 0)
                                received.put(dataSnapshotChild.getKey(), a);
                        }
                        userList.setReceived(received);
                        myAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Unable to retrieve the users.
                    }
                });

        FirebaseDatabase.getInstance()
                .getReference()
                .child("FriendRequestSender")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren()
                                .iterator();
                        sent.clear();
                        while (dataSnapshots.hasNext()) {
                            DataSnapshot dataSnapshotChild = dataSnapshots.next();
                            Integer a = dataSnapshotChild.getValue(Integer.class);
                            if (a != 0)
                                sent.put(dataSnapshotChild.getKey(), a);
                        }
                        userList.setSent(sent);
                        myAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Unable to retrieve the users.
                    }
                });

        FirebaseDatabase.getInstance()
                .getReference()
                .child("friends")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren()
                                .iterator();
                        friends.clear();
                        while (dataSnapshots.hasNext()) {
                            DataSnapshot dataSnapshotChild = dataSnapshots.next();
                            Integer a = dataSnapshotChild.getValue(Integer.class);
                            if (a != 0) {
                                friends.put(dataSnapshotChild.getKey(), a);
                            }
                        }
                        userList.setFriends(friends);

                        myAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Unable to retrieve the users.
                    }
                });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "PERMISSON NOT GRANTED", Toast.LENGTH_SHORT).show();
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location required")
                                .setMessage("Please Click OK Permision to continue")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else{
            android.location.Location location = null;
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (location != null) {
                myLocation = new Loc(location.getLatitude(), location.getLongitude());
                userList.setLoc(myLocation);
                FirebaseDatabase.getInstance().getReference().child("users")
                        .child(auth.getCurrentUser().getUid())
                        .child("location")
                        .setValue(myLocation).addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                } else {
                                    // failed to add user
                                }
                            }
                        }
                );
                FirebaseDatabase.getInstance().getReference().child("users")
                        .child(auth.getCurrentUser().getUid())
                        .child("timestamp")
                        .setValue(ServerValue.TIMESTAMP).addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                } else {
                                    // failed to add user
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(MainActivity.this, "NULL", Toast.LENGTH_SHORT).show();
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:

                        android.location.Location location = null;
                        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                        if (location != null) {
                            myLocation = new Loc(location.getLatitude(), location.getLongitude());
                            userList.setLoc(myLocation);
                            FirebaseDatabase.getInstance().getReference().child("users")
                                    .child(auth.getCurrentUser().getUid())
                                    .child("location")
                                    .setValue(myLocation).addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                            } else {
                                                // failed to add user
                                            }
                                        }
                                    }
                            );
                            FirebaseDatabase.getInstance().getReference().child("users")
                                    .child(auth.getCurrentUser().getUid())
                                    .child("timestamp")
                                    .setValue(ServerValue.TIMESTAMP).addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                            } else {
                                                // failed to add user
                                            }
                                        }
                                    }
                            );

                        } else {
                            Toast.makeText(MainActivity.this, "NULL", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
                else {
                    return;
                }
                return;
            }

        }
    }
}