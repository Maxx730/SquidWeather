package com.squidswap.squidchat.squidweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;
import java.util.Locale;

public class LocationService {
    private LocationManager manager;
    private String NETWORK_PROVIDER = LocationManager.GPS_PROVIDER;
    private Location loc;
    private Geocoder geo;
    private Address LocationInfo;
    private RequestQueue que;

    public LocationService(Context context){
        Log.d("LOGIC FLOW","Initializing Location Service");
        this.manager = (LocationManager)  context.getSystemService(Context.LOCATION_SERVICE);
        this.geo = new Geocoder(context, Locale.getDefault());
        this.que = Volley.newRequestQueue(context);

        //Make sure we have access to the location of the device before we start listening.
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.d("LOGIC FLOW","Getting Location");
            this.loc = this.manager.getLastKnownLocation(this.NETWORK_PROVIDER);
            this.RefreshInfo();
        }
    }

    //Takes in a search query and returns the data back from the google geolocation services api
    public void FindLocation(String search,final ServiceInterface serv){
        StringRequest req = new StringRequest(Request.Method.GET, "https://maps.googleapis.com/maps/api/geocode/json?address="+search+"&key=AIzaSyBwsxKA_8mhPi6fKEvnhizWQPQI-bPZ2QU", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                serv.onWeatherRecieved(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                serv.onWeatherError(error.toString());
            }
        });

        this.que.add(req);
    }

    //Uses the geo manager to pull info about the currently chosen location.
    public void RefreshInfo(){
        try{
            List<Address> addresses = this.geo.getFromLocation(this.loc.getLatitude(),this.loc.getLongitude(),1);
            this.LocationInfo = addresses.get(0);
            Log.d("LOGIC FLOW","ADDRESS: "+this.LocationInfo.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //Returns our location.
    public Location getLocation(){
        return this.loc;
    }
    public Address getAddress(){ return this.LocationInfo; }
    public void setLocation(Location loc){ this.loc = loc;this.RefreshInfo();}
}
