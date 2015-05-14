package net.flyingsparx.spotpassandroid.model;

import java.util.ArrayList;

/**
 * Created by will on 5/14/15.
 */
public class Venue {
    private String id, name, street1, street2, postcode, ssid, city, country, type;
    private double latitude, longitude;
    private String[] services;

    public Venue(String id, String name, String street1, String street2, String postcode, String city, String country, String type, String ssid, String services, String lat, String lon){
        this.id = id;
        this.name = name.replaceAll("&amp;", "&");
        this.street1 = street1.replaceAll("&amp;", "&");;
        this.street2 = street2;
        this.postcode = postcode;
        this.city = city.replaceAll("&amp;", "&");;
        this.country = country.replaceAll("&amp;", "&");;
        this.type = type.replaceAll("&amp;", "&");;
        this.ssid = ssid;
        this.services = services.split(",");
        this.latitude = Double.parseDouble(lat);
        this.longitude = Double.parseDouble(lon);
    }

    public String get_name(){
        return name;
    }
    public String get_street1(){
        return street1;
    }
    public String get_street2(){
        return street2;
    }
    public String get_postcode(){
        return postcode;
    }
    public String get_city(){
        return city;
    }
    public String get_country(){
        return country;
    }
    public String get_type(){
        return type;
    }
    public String get_ssid(){
        return ssid;
    }
    public String[] get_services(){
        return services;
    }
    public Double get_latitude(){
        return latitude;
    }
    public Double get_longitude(){
        return longitude;
    }
}
