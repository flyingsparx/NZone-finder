package net.flyingsparx.spotpassandroid.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.flyingsparx.spotpassandroid.R;
import net.flyingsparx.spotpassandroid.model.Venue;
import net.flyingsparx.spotpassandroid.util.SpotPassUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class VenueActivity extends ActionBarActivity {

    Venue venue;
    Double my_latitude, my_longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue);

        Intent intent = getIntent();
        String id = intent.getStringExtra("VENUE_ID");

        try{
            double latitude = intent.getDoubleExtra("MY_LATITUDE",0);
            double longitude = intent.getDoubleExtra("MY_LONGITUDE",0);
            if(latitude != 0 || longitude != 0){ // assume not at (0,0)
                my_latitude = latitude;
                my_longitude = longitude;
            }
        }
        catch(Exception e){}

        if(id == null){
            show_error();
            return;
        }
        new SpotPassUtil.VenueGetter(this).execute(id);
    }

    public void update_venue(Venue v){
        if(v == null) {
            show_error();
            return;
        }
        venue = v;
        update_ui();
    }

    private void show_error(){
        new AlertDialog.Builder(this)
                .setTitle("Error finding venue")
                .setMessage("Sorry, we couldn't find any information for this venue.")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(R.drawable.ic_error_outline_grey600_48dp)
                .show();
    }

    private void update_ui(){
        if(venue != null){
            ((RelativeLayout)findViewById(R.id.loading_venue)).setVisibility(View.GONE);
            ((RelativeLayout)findViewById(R.id.venue_info)).setVisibility(View.VISIBLE);

            ((TextView)findViewById(R.id.venue_name)).setText(venue.get_name());
            ((TextView)findViewById(R.id.venue_type)).setText(venue.get_type());
            ((TextView)findViewById(R.id.address)).setText(venue.get_street1()+"\n"+venue.get_city()+"\n"+venue.get_postcode()+"\n"+venue.get_country());
            ((TextView)findViewById(R.id.ssid)).setText(venue.get_ssid());
            ((TextView)findViewById(R.id.services)).setText(android.text.TextUtils.join("\n", venue.get_services()));

            if(my_latitude != null && my_longitude != null) {
                int dist = (int) distance(venue.get_latitude(), my_latitude, venue.get_longitude(), my_longitude);
                String dist_string = "";
                if (dist > 1000 && dist < 5000) {
                    DecimalFormat decimalFormat = new DecimalFormat("#.#");
                    String t = decimalFormat.format(dist / 1000);
                    dist_string = t + " km";
                } else if (dist >= 5000) {
                    DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
                    String t = decimalFormat.format(dist / 1000);
                    dist_string = t + " km";
                } else {
                    dist_string = dist + " m";
                }
                ((TextView) findViewById(R.id.distance)).setText("About " + dist_string + " away.");
            }
            else{
                ((TextView) findViewById(R.id.distance)).setVisibility(View.GONE);
                ((ImageView) findViewById(R.id.distance_icon)).setVisibility(View.GONE);
            }
        }
    }

    private double distance(double lat1, double lat2, double lon1, double lon2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = deg2rad(lat2 - lat1);
        Double lonDistance = deg2rad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);
        return Math.sqrt(distance);
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
}
