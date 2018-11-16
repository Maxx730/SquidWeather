package com.squidswap.squidchat.squidweather;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class SquidReports extends AppCompatActivity {
    private LayoutInflater inflate;
    private WeatherService service;
    private TemperatureConvert conv;
    private LocationService loc;
    private SensorService sense;
    private SharedPreferences prefs;

    private String APP_LOCATION = "05401";
    private int LOCATION_ACCESS = 45,INTERNET_ACCESS = 46;
    private Location pos;

    //Grab all of the layouts for the application.
    private static RelativeLayout MainContainer,WeatherLayout,SettingsShade;
    private static LinearLayout FlickView;
    private TextView TemperatureText,LocationText,MainText,LongitudeText,LatitudeText;
    private ImageButton RefreshButton,SettingsButton;
    private Button CloseSettings;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            MainContainer.removeAllViews();

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    MainContainer.addView(WeatherLayout);
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.settings:
                Intent i = new Intent(getApplicationContext(),Preferences.class);
                startActivity(i);
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_squid_reports);

        //Different screen layouts
        MainContainer = (RelativeLayout) findViewById(R.id.MainContainer);
        WeatherLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.weather_layout,null);
        SettingsShade = (RelativeLayout) WeatherLayout.findViewById(R.id.SettingsShade);

        //Textviews for weather information to occupy.
        TemperatureText = (TextView) WeatherLayout.findViewById(R.id.TempText);
        LocationText = (TextView) WeatherLayout.findViewById(R.id.LocationText);
        MainText = (TextView) WeatherLayout.findViewById(R.id.MainText);
        RefreshButton = (ImageButton) WeatherLayout.findViewById(R.id.RefreshButton);
        SettingsButton = (ImageButton) WeatherLayout.findViewById(R.id.SettingsButton);
        CloseSettings = (Button) WeatherLayout.findViewById(R.id.CloseSettings);

        final Animation rot = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate);



        //Check if the user has given the application access to use the internet.
        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            System.out.println("NO INTERNET ACCESS PROVIDED");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},INTERNET_ACCESS);
        }else if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            System.out.println("NO LOCATION ACCESS PROVIDED");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_ACCESS);
        }

        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //Utilitie initialization
            service = new WeatherService(getApplicationContext());
            conv = new TemperatureConvert();
            loc = new LocationService(getApplicationContext());
            //sense = new SensorService(getApplicationContext());
            prefs = PreferenceManager.getDefaultSharedPreferences(this);

            pos = loc.getLocation();

            APP_LOCATION = prefs.getString("api_zipcode","05401");

            service.GetCurrent(APP_LOCATION, new ServiceInterface() {
                @Override
                public void onWeatherRecieved(String reponse) {
                    try{
                        JSONObject repObj = new JSONObject(reponse);

                        //Set the retrieved data into the text views.
                        TemperatureText.setText(Double.toString(conv.KelvinToFarenheit(repObj.getJSONObject("main").getDouble("temp"))) + (char) 0x00B0);
                        LocationText.setText(repObj.getString("name") + ", " + repObj.getJSONObject("sys").getString("country"));
                        MainText.setText(repObj.getJSONArray("weather").getJSONObject(0).getString("main"));

                        switch(repObj.getJSONArray("weather").getJSONObject(0).getString("main")){

                        }
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onWeatherError(String error) {
                    Toast.makeText(getApplicationContext(),error,Toast.LENGTH_LONG).show();
                }
            });

            //Click event for the refresh animation and to repull data.
            RefreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RefreshButton.startAnimation(rot);
                }
            });

            SettingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation ani = new AlphaAnimation(0.0f,1.0f);
                    ani.setDuration(250);
                    SettingsShade.startAnimation(ani);
                    SettingsShade.setVisibility(View.VISIBLE);
                }
            });

            CloseSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation ani = new AlphaAnimation(1.0f,0.0f);
                    ani.setDuration(250);
                    SettingsShade.startAnimation(ani);
                    SettingsShade.setVisibility(View.GONE);
                }
            });

            MainContainer.addView(WeatherLayout);
        }
    }
}
