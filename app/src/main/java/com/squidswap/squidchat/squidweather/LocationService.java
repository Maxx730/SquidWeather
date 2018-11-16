package com.squidswap.squidchat.squidweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

public class LocationService {
    private LocationManager manager;
    private String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;
    private Location loc;

    public LocationService(Context context){
        this.manager = (LocationManager)  context.getSystemService(Context.LOCATION_SERVICE);

        //Make sure we have access to the location of the device before we start listening.
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            this.manager.requestLocationUpdates(this.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });

            this.loc = this.manager.getLastKnownLocation(this.NETWORK_PROVIDER);
        }
    }

    //Returns our location.
    public Location getLocation(){
        return this.loc;
    }
}
