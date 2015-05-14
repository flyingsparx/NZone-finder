package net.flyingsparx.spotpassandroid.model;

import android.location.Location;

/**
 * Created by will on 5/12/15.
 */
public class Spot {

    private String name;
    private Double latitude, longitude;
    private Boolean is_group;
    private int size;

    public Spot(Double l, Double l2, String n){
        this.latitude = l;
        this.longitude = l2;
        this.name = n;
        is_group = false;
    }

    public Spot(Double l, Double l2, int s){
        this.latitude = l;
        this.longitude = l2;
        this.size = s;
        is_group = true;
    }

    public Double get_latitude(){
        return latitude;
    }

    public Double get_longitude(){
        return longitude;
    }

    public String get_name(){
        return name;
    }

    public Boolean is_group(){
        return is_group;
    }

    public int get_size(){
        return size;
    }
}
