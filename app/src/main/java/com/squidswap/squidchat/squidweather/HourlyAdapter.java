package com.squidswap.squidchat.squidweather;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.List;

public class HourlyAdapter extends ArrayAdapter {
    public HourlyAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
    }
}
