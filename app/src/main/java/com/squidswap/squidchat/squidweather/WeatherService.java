package com.squidswap.squidchat.squidweather;

import android.content.Context;
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

//Class that handles making API calls between the application and the weather API endpoint.
//Uses the service interface for callbacks after recieving data.
public class WeatherService{
    private Context context;
    private RequestQueue req;
    private static String API_KEY = "d179a1429d00cc8733023cc8f3264cc6";

    public WeatherService(Context context){
        this.context = context;
        this.req = Volley.newRequestQueue(context);
    }

    public void GetCurrent(String location,final ServiceInterface serv){
        StringRequest test = new StringRequest(Request.Method.GET, "http://api.openweathermap.org/data/2.5/weather?zip="+location+",us&appid="+API_KEY, new Response.Listener<String>() {
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

        this.req.add(test);
    }

    //Sends a qurie to the DarkSky api to pull forcast data.
    public void GetForcast(Location loc, final ServiceInterface serv){
        StringRequest forcast = new StringRequest(Request.Method.GET, "https://api.darksky.net/forecast/84fc2ffca1d3a91019d6047b66fba90e/"+loc.getLatitude()+","+loc.getLongitude(), new Response.Listener<String>() {
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

        this.req.add(forcast);
    }
}
