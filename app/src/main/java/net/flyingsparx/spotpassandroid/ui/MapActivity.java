/*
Copyright 2015 Will Webberley.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

The full text of the License is available in the root of this
project repository.
*/

package net.flyingsparx.spotpassandroid.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.flyingsparx.spotpassandroid.R;
import net.flyingsparx.spotpassandroid.util.SpotPassUtil;
import net.flyingsparx.spotpassandroid.model.Spot;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap map;
    MapFragment map_fragment;
    GoogleApiClient api_client;
    Location my_location;
    LatLng current_location;
    float current_zoom;


    ArrayList<Spot> spots;
    ArrayList<Marker> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        init_map();
        get_current_location();

    }

    private void get_current_location(){
        api_client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        api_client.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}
    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(api_client);
        if (location != null) {
            my_location = location;
            if(map != null){
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15);
                map.animateCamera(update);
            }
        }
        api_client.disconnect();
    }
    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        init_map();
    }

    private void init_map() {
        if(map_fragment == null || map == null) {
            map_fragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            map = map_fragment.getMap();
            final MapActivity m = this;
            GoogleMap.OnCameraChangeListener listener = new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    LatLng new_location = cameraPosition.target;
                    float new_zoom = cameraPosition.zoom;

                    if(current_location != null) {
                        if (new_location.latitude == current_location.latitude && new_location.longitude == current_location.longitude && new_zoom == current_zoom) {
                            return;
                        }
                    }

                    current_location = new_location;
                    current_zoom = new_zoom;

                    LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                    LatLng sw = bounds.southwest;
                    LatLng ne = bounds.northeast;
                    double zoom = cameraPosition.zoom;
                    SpotPassUtil.SpotGetter getter = new SpotPassUtil.SpotGetter(m);
                    getter.execute(sw.latitude, sw.longitude, ne.latitude, ne.longitude, zoom);
                }
            };
            GoogleMap.OnMarkerClickListener listener2 = new GoogleMap.OnMarkerClickListener(){
                @Override
                public boolean onMarkerClick(Marker marker) {
                    marker.showInfoWindow();
                    return true;
                }
            };
            GoogleMap.OnInfoWindowClickListener listener3 = new GoogleMap.OnInfoWindowClickListener(){
                @Override
                public void onInfoWindowClick(Marker marker) {

                    for(int i = 0; i < markers.size(); i++){
                        if(markers.get(i).getId().equals(marker.getId())){
                            Intent intent = new Intent(m, VenueActivity.class);
                            String message = spots.get(i).get_id();
                            intent.putExtra("VENUE_ID", message);
                            if(my_location != null) {
                                intent.putExtra("MY_LATITUDE", my_location.getLatitude());
                                intent.putExtra("MY_LONGITUDE", my_location.getLongitude());
                            }
                            startActivity(intent);
                            break;
                        }
                    }
                }
            };
            map.setOnCameraChangeListener(listener);
            map.setOnMarkerClickListener(listener2);
            map.setOnInfoWindowClickListener(listener3);
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
        }
        System.out.println(map);
    }

    public void update_spots(ArrayList<Spot> s){
        spots = s;
        markers = new ArrayList<Marker>();
        map.clear();
        for(int i = 0; i < spots.size(); i++){
            Spot spot = spots.get(i);
            if(!spot.is_group()) {
                Marker m = map.addMarker(new MarkerOptions()
                                .position(new LatLng(spot.get_latitude(), spot.get_longitude()))
                                .title(spot.get_name())
                                .snippet("(tap for more info)")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.spot_small))
                );
                markers.add(m);
            }
            else{
                BitmapDescriptor b = null;
                if(spot.get_size() == 1){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_1);}
                if(spot.get_size() == 2){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_2);}
                if(spot.get_size() == 3){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_3);}
                if(spot.get_size() == 4){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_4);}
                if(spot.get_size() == 5){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_5);}
                if(spot.get_size() > 5){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_6);}
                Marker m = map.addMarker(new MarkerOptions()
                                .position(new LatLng(spot.get_latitude(), spot.get_longitude()))
                                .icon(b)
                );
                markers.add(m);
            }
        }
    }

    public void show_place_result(List<Address> a){
        final List<Address> addresses = a;
        if(addresses == null){
            new AlertDialog.Builder(this)
                    .setTitle("Lookup failed")
                    .setMessage("We could not find a location matching your request.")
                    .setNeutralButton("Try again", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            show_address_dialog(null);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {}
                    })
                    .setIcon(R.drawable.ic_error_outline_grey600_48dp)
                    .show();
        }
        else if(addresses.size() == 1){
            move_to_place(addresses.get(0));
        }
        else if(addresses.size() > 1){
            ArrayList<String> names = new ArrayList<String>();
            for(int i = 0; i < addresses.size(); i++){
                names.add(addresses.get(i).getAddressLine(0)+", "+addresses.get(i).getAddressLine(1));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Did you mean...")
                    .setItems(names.toArray(new CharSequence[names.size()]), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            move_to_place(addresses.get(which));
                        }
                    });
            builder.create();
            builder.show();
        }
    }

    public void move_to_place(Address a){
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(a.getLatitude(), a.getLongitude()), 15);
            map.animateCamera(update);
    }

    public void show_address_dialog(MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final MapActivity m = this;
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_location, null);
        builder.setView(view).setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText box = (EditText)view.findViewById(R.id.custom_location);
                new SpotPassUtil.LocationGetter(m).execute(box.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void show_about_activity(MenuItem item){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void go_to_me(MenuItem item){
        get_current_location();
    }
}
