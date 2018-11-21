package com.squidswap.squidchat.squidweather;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class LocationAdapter extends ArrayAdapter<Address> {

    private List<Address> locs;
    private Context con;
    private LayoutInflater inflate;
    private int layout_resource;

    public LocationAdapter(@NonNull Context context, int resource, List<Address> locations ) {
        super(context, resource, locations);

        this.locs = locations;
        this.con = context;
        this.layout_resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(this.con).inflate(this.layout_resource,parent,false);
        TextView LocationTitle = (TextView) convertView.findViewById(R.id.LocationName);
        TextView LocationState = (TextView) convertView.findViewById(R.id.LocationState);
        TextView LocationCountry = (TextView) convertView.findViewById(R.id.LocationCountry);

        LocationTitle.setText(this.locs.get(position).getLocality());
        LocationState.setText(this.locs.get(position).getAdminArea());
        LocationCountry.setText(this.locs.get(position).getCountryName());

        return convertView;
    }
}