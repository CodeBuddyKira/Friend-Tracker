package com.example.admin.myproject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity
        implements GoogleMap.OnMarkerClickListener,OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    List<User> users = new ArrayList<>();
    Map<LatLng, Marker> map = new HashMap<>();
    Map<Marker, String> titlemap = new HashMap<>();
    Map<Marker,List<User>> usersHere = new HashMap<Marker,List<User>>();
    FloatingSearchView mSearchView;
    RecyclerView recyclerView;
    userAdapter myAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {

            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {

                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                } else {
                    mSearchView.showProgress();
                    final List<User> filteredList = new ArrayList<>();

                    for (int i = 0; i < users.size(); i++) {
                        final String text = users.get(i).getName().toLowerCase();
                        if (text.contains(newQuery.toLowerCase())) {
                            filteredList.add(users.get(i));
                        }
                    }
                    mSearchView.swapSuggestions(filteredList);
                    mSearchView.hideProgress();
                }
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {
                User user = (User) searchSuggestion;
                Toast.makeText(getApplicationContext(), user.getName(), Toast.LENGTH_SHORT).show();
                LatLng latLng = new LatLng(user.getLocation().getLatitude(), user.getLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo((float) 15));
                mSearchView.setSearchBarTitle(user.getName());
                mSearchView.clearSuggestions();
            }

            @Override
            public void onSearchAction(String query) {
                Toast.makeText(getApplicationContext(), "Select from suggestions", Toast.LENGTH_SHORT).show();
            }
        });
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {

                if (item.getItemId() == R.id.action_location) {
                    LatLng latLng = new LatLng(userList.getLoc().getLatitude(), userList.getLoc().getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo((float) 15));
                } else {
                    Toast.makeText(getApplicationContext().getApplicationContext(), item.getTitle(),
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        LatLng latLng = new LatLng(0,0);
        users = userList.getUsers();
        int i=0;
        for (User user : users) {
            latLng = new LatLng(user.getLocation().getLatitude(), user.getLocation().getLongitude());
            Marker marker = map.get(latLng);
            if (marker == null) {
                List<User>user1 = new ArrayList<User>();
                user1.add(user);
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title(user.getName()));
                usersHere.put(marker,user1);
                map.put(latLng, marker);
                titlemap.put(marker, user.getName());
            } else {
                List<User>CurrentUsers = new ArrayList<>();
                CurrentUsers = usersHere.get(marker);
                CurrentUsers.add(user);
                String s = titlemap.get(marker);
                marker.remove();
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title(user.getName() + ", " + s));
                usersHere.put(marker,CurrentUsers);
                map.put(latLng, marker);
                titlemap.put(marker, user.getName() + "," + s);
            }
        }
        latLng = new LatLng(userList.getLoc().getLatitude(), userList.getLoc().getLongitude());
        Marker marker = map.get(latLng);
        if (marker == null) {
            List<User>user1 = new ArrayList<User>();
            user1.add(userList.getCurrentUser());
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title("YOU"));
            usersHere.put(marker,user1);
        } else {
            List<User>CurrentUsers = new ArrayList<>();
            CurrentUsers = usersHere.get(marker);
            CurrentUsers.add(userList.getCurrentUser());
            String s = titlemap.get(marker);
            marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title("YOU, " + s));
            usersHere.put(marker,CurrentUsers);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo((float) 15));
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Log.d("HELLO",usersHere.get(marker).toString());
        myAdapter = new userAdapter(this, usersHere.get(marker));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
       // Toast.makeText(this,"Hello "+usersHere.get(marker).get(0).getName(),Toast.LENGTH_SHORT).show();
        return false;
    }
}