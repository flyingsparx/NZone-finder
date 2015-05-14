package net.flyingsparx.spotpassandroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap map;
    MapFragment map_fragment;
    GoogleApiClient api_client;
    LatLng current_location;
    float current_zoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        init_map();

        api_client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        api_client.connect();
    }

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
                    SpotPassUtil.DataGetter getter = new SpotPassUtil.DataGetter(m);
                    getter.execute(sw.latitude, sw.longitude, ne.latitude, ne.longitude, zoom);
                }
            };
            map.setOnCameraChangeListener(listener);
            map.setMyLocationEnabled(true);
        }
        System.out.println(map);
    }

    public void update_spots(ArrayList<Spot> spots){
        map.clear();
        for(int i = 0; i < spots.size(); i++){
            Spot spot = spots.get(i);
            if(!spot.is_group()) {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(spot.get_latitude(), spot.get_longitude()))
                        .title(spot.get_name())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.spot))
                );
            }
            else{
                BitmapDescriptor b = null;
                if(spot.get_size() == 1){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_1);}
                if(spot.get_size() == 2){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_2);}
                if(spot.get_size() == 3){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_3);}
                if(spot.get_size() == 4){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_4);}
                if(spot.get_size() == 5){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_5);}
                if(spot.get_size() > 5){b = BitmapDescriptorFactory.fromResource(R.drawable.icon_6);}
                map.addMarker(new MarkerOptions()
                                .position(new LatLng(spot.get_latitude(), spot.get_longitude()))
                                .icon(b)
                );
            }
        }
    }

    public void move_to_place(Address a){
        if(a == null){
            new AlertDialog.Builder(this)
                    .setTitle("Lookup failed")
                    .setMessage("We could not find a location matching your request.")
                    .setNeutralButton("Try again", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            showAddressDialog(null);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {}
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else {
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(a.getLatitude(), a.getLongitude()), 15);
            map.moveCamera(update);
        }
    }

    public void showAddressDialog(MenuItem item){
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

    public void showAboutActivity(MenuItem item){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}
    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(api_client);
        if (location != null) {
            System.out.println(location);
            if(map != null){
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15);
                map.moveCamera(update);
            }
        }
        api_client.disconnect();
    }
    @Override
    public void onConnectionSuspended(int i) {}
}
