package com.squidswap.squidchat.squidweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.Calendar;

//Android class that will keep track of when the last query was run etc to prevent
//sending too many queries to the API resulting in too many calls.
public class QueryService {
    private Calendar current;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;

    public QueryService(Context con,SharedPreferences pref){
        this.prefs = pref;
        this.edit = pref.edit();

        if(pref.contains("last_query")){
            Log.d("LOGIC FLOW","Pulling last query data from prefs.");
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(prefs.getLong("last_query",0));
            this.current = c;
        }
    }

    //Checks if there has been enough time between the last pull request from the DarkSky API
    //for another pull to prevent too many calls going to the API.
    public boolean AllowPull(){
        if(this.current != null){
            Calendar now = Calendar.getInstance();

            long difference = (now.getTimeInMillis() - this.current.getTimeInMillis());

            Log.d("LOGIC FLOW",String.valueOf(difference));
            return false;
        }else{
            Log.d("LOGIC FLOW","No previous pull date.");
            return true;
        }
    }

    public void RecordCurrent(){
        this.current = Calendar.getInstance();
        this.edit.putLong("last_query",this.current.getTimeInMillis());
        this.edit.apply();
    }

    public Calendar getCurrent(){
        return this.current;
    }
}
