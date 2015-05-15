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

package net.flyingsparx.spotpassandroid.util;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.StrictMode;

import net.flyingsparx.spotpassandroid.model.Spot;
import net.flyingsparx.spotpassandroid.model.Venue;
import net.flyingsparx.spotpassandroid.ui.MapActivity;
import net.flyingsparx.spotpassandroid.ui.VenueActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 5/12/15.
 */
public class SpotPassUtil {

    public static class LocationGetter extends AsyncTask<String, Integer, List<Address>>{

        private MapActivity caller;

        public LocationGetter(MapActivity caller){
            this.caller = caller;
        }

        @Override
        protected List<Address> doInBackground(String... params) {
            String address = params[0];

            try{
                Geocoder geocoder = new Geocoder(caller);
                List<Address> places = geocoder.getFromLocationName(address, 5);
                return places;
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Address> places){
            if(places != null && places.size() > 0){
                caller.show_place_result(places);
            }
            else {
                caller.show_place_result(null);
            }
        }
    }


    public static  class SpotGetter extends AsyncTask<Double, Integer, ArrayList<Spot>> {

        MapActivity caller;
        ArrayList<Spot> spots;
        final private static String BASE_URL = "https://microsite.nintendo-europe.com/hotspots/api/hotspots/get?summary_mode=true";

        public SpotGetter(MapActivity caller){
            this.caller = caller;
        }

        @Override
        protected ArrayList<Spot> doInBackground(Double... arg0) {
            double ll_latitude = arg0[0];
            double ll_longitude = arg0[1];
            double tr_latitude = arg0[2];
            double tr_longitude = arg0[3];
            int zoom = arg0[4].intValue();

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            BufferedReader reader = null;
            StringBuffer completeData = new StringBuffer();
            String data = null;
            try {
                URL url = new URL(BASE_URL+"&zoom="+zoom+"&bbox="+ll_latitude+","+ll_longitude+","+tr_latitude+","+tr_longitude);
                System.out.println(url);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                InputStream in = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    completeData.append(line);
                }
                reader.close();

                data = completeData.toString();
                JSONArray array = new JSONArray(data);
                spots = new ArrayList<Spot>();
                for(int i = 0; i < array.length(); i++){
                    JSONObject entry = array.optJSONObject(i);
                    Spot s;
                    if(entry.has("group_size")){
                        if(entry.getInt("group_size") > 1){
                            s = new Spot(entry.getDouble("lat"), entry.getDouble("lon"), entry.getInt("group_size"));
                        }
                        else{
                            s = new Spot(entry.getString("id"), entry.getDouble("lat"), entry.getDouble("lon"), entry.getString("na").replaceAll("&amp;", "&"));
                        }
                    }
                    else{
                        s = new Spot(entry.getString("id"), entry.getDouble("lat"), entry.getDouble("lon"), entry.getString("na"));
                    }

                    spots.add(s);
                }

            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            return spots;
        }

        @Override
        protected void onPostExecute(ArrayList<Spot> result){
            caller.update_spots(spots);
        }
    }


    public static  class VenueGetter extends AsyncTask<String, Integer, Venue> {

        VenueActivity caller;
        final private static String BASE_URL = "https://microsite.nintendo-europe.com/hotspots/api/hotspots/getbyid";

        public VenueGetter(VenueActivity caller){
            this.caller = caller;
        }

        @Override
        protected Venue doInBackground(String... arg0) {
            String id = arg0[0];

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            BufferedReader reader = null;
            StringBuffer completeData = new StringBuffer();
            String data = null;
            try {
                URL url = new URL(BASE_URL+"?id="+id);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                InputStream in = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    completeData.append(line);
                }
                reader.close();

                data = completeData.toString();
                JSONObject o = new JSONObject(data);

                Venue v = new Venue(o.getString("id"), o.getString("address_name"), o.getString("street"), o.getString("street2"), o.getString("postcode"), o.getString("city"), o.getString("country_name"), o.getString("address_type"), o.getString("ssid"), o.getString("services_names"), o.getString("latitude"), o.getString("longitude"));
                return v;

            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Venue v){
            caller.update_venue(v);
        }
    }
}
