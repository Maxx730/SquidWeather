package com.squidswap.squidchat.squidweather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SquidReports extends AppCompatActivity {
    private LayoutInflater inflate;
    private WeatherService service;
    private TemperatureConvert conv;
    private LocationService loc;
    private SensorService sense;
    private SharedPreferences prefs;

    private String APP_LOCATION = "05401";
    private int LOCATION_ACCESS = 45,INTERNET_ACCESS = 46,BACKGROUND_TEMP=R.drawable.cold_gradient;
    private Location pos;

    //Grab all of the layouts for the application.
    private static RelativeLayout MainContainer,WeatherLayout,SettingsShade,SettingsView,WeatherBackground;
    private static LinearLayout FlickView;
    private TextView TemperatureText,LocationText,MainText,LongitudeText,LatitudeText;
    private ImageButton RefreshButton,SettingsButton;
    private ImageView TempIcon,IndicationIcon;
    private Button CloseSettings;
    private LineChart ForecastGraph;

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
        SettingsView = (RelativeLayout) WeatherLayout.findViewById(R.id.SettingsFragment);
        WeatherBackground = (RelativeLayout) WeatherLayout.findViewById(R.id.WeatherBackground);

        //Textviews for weather information to occupy.
        TemperatureText = (TextView) WeatherLayout.findViewById(R.id.TempText);
        LocationText = (TextView) WeatherLayout.findViewById(R.id.LocationText);
        MainText = (TextView) WeatherLayout.findViewById(R.id.MainText);
        RefreshButton = (ImageButton) WeatherLayout.findViewById(R.id.RefreshButton);
        SettingsButton = (ImageButton) WeatherLayout.findViewById(R.id.SettingsButton);
        CloseSettings = (Button) WeatherLayout.findViewById(R.id.CloseSettings);
        LongitudeText = (TextView) WeatherLayout.findViewById(R.id.LongitudeText);
        LatitudeText = (TextView) WeatherLayout.findViewById(R.id.LatitudeText);

        TempIcon = (ImageView) WeatherLayout.findViewById(R.id.TempIcon);
        IndicationIcon = (ImageView) WeatherLayout.findViewById(R.id.IndicationIcon);

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

            ForecastGraph = (LineChart) WeatherLayout.findViewById(R.id.ForecastChart);

            service.GetForcast(pos, new ServiceInterface() {
                @Override
                public void onWeatherRecieved(String reponse) {
                    try{
                        JSONArray hourly_data = new JSONObject(reponse).getJSONObject("daily").getJSONArray("data");
                        List<Entry> entries = new ArrayList<>();

                        for(int i = 0;i < hourly_data.length();i++){
                            entries.add(new Entry(i,(int) hourly_data.getJSONObject(i).getDouble("temperatureHigh")));
                        }

                        LineDataSet forecasts = new LineDataSet(entries,"Day");
                        forecasts.setColor(getResources().getColor(android.R.color.background_light));
                        forecasts.setLineWidth(3);
                        forecasts.setDrawCircles(false);
                        forecasts.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                        forecasts.setCircleRadius(7);
                        forecasts.setCircleColor(getResources().getColor(android.R.color.background_light));
                        forecasts.setDrawFilled(true);
                        forecasts.setFillColor(getResources().getColor(android.R.color.background_light));

                        XAxis x = ForecastGraph.getXAxis();
                        x.setDrawAxisLine(false);
                        x.setDrawGridLines(false);
                        x.setDrawLabels(false);
                        x.setDrawGridLinesBehindData(true);


                        YAxis yl = ForecastGraph.getAxisLeft();
                        YAxis yr = ForecastGraph.getAxisRight();

                        yl.setDrawAxisLine(false);
                        yr.setDrawAxisLine(false);
                        yr.setDrawLabels(false);
                        yl.setDrawLabels(false);
                        yl.setDrawGridLines(true);
                        yl.setDrawZeroLine(true);
                        yl.setGridColor(Color.parseColor("#FFFFFF"));
                        yl.setDrawTopYLabelEntry(false);
                        yr.setDrawGridLines(false);
                        yr.setDrawLabels(false);

                        LineData finalFor = new LineData(forecasts);
                        finalFor.setDrawValues(false);
                        finalFor.setValueTextColor(getResources().getColor(android.R.color.background_light));
                        finalFor.setValueTextSize(14);

                        //Add our line styling here.
                        ForecastGraph.setAutoScaleMinMaxEnabled(false);
                        ForecastGraph.getLegend().setEnabled(false);
                        ForecastGraph.getDescription().setEnabled(false);
                        ForecastGraph.setContentDescription("");
                        ForecastGraph.animateX(100);
                        ForecastGraph.setPadding(40,0,40,0);
                        ForecastGraph.setTouchEnabled(false);
                        ForecastGraph.setData(finalFor);
                        ForecastGraph.invalidate();
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onWeatherError(String error) {

                }
            });

            LongitudeText.setText(String.valueOf(Math.floor((double) pos.getLongitude())) + ", ");
            LatitudeText.setText(String.valueOf(Math.floor((double) pos.getLatitude())));

            APP_LOCATION = prefs.getString("api_zipcode","33573");

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
                            case "Clouds":
                                IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_iconfinder_cloud_2995000));
                                break;
                            case "Rain":
                                IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_iconfinder_rain_2995004));
                                break;
                            case "Snow":
                                IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_iconfinder_snow_2995006));
                                break;
                            case "Clear":
                                IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_iconfinder_rainbow_2995008));
                                break;
                            default:
                                IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_iconfinder_cloud_2995000));
                                break;
                        }

                        if(conv.KelvinToFarenheit(repObj.getJSONObject("main").getDouble("temp")) > 90){
                            TempIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_thermometer_100));
                            TempIcon.setImageTintList(getColorStateList(R.color.temp_100));
                            TemperatureText.setTextColor(getResources().getColor(R.color.temp_100));
                            WeatherBackground.setBackground(getResources().getDrawable(R.drawable.warm_gradient));
                            BACKGROUND_TEMP = R.drawable.warm_gradient;
                        }else if(conv.KelvinToFarenheit(repObj.getJSONObject("main").getDouble("temp")) > 70){
                            TempIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_thermometer_75));
                            TempIcon.setImageTintList(getColorStateList(R.color.temp_75));
                            TemperatureText.setTextColor(getResources().getColor(R.color.temp_75));
                            WeatherBackground.setBackground(getResources().getDrawable(R.drawable.warm_gradient));
                            BACKGROUND_TEMP = R.drawable.warm_gradient;
                        }else if(conv.KelvinToFarenheit(repObj.getJSONObject("main").getDouble("temp")) > 40){
                            TempIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_thermometer_50));
                            TempIcon.setImageTintList(getColorStateList(R.color.medium_end));
                            TemperatureText.setTextColor(getResources().getColor(R.color.medium_end));
                            WeatherBackground.setBackground(getResources().getDrawable(R.drawable.medium_gradient));
                            BACKGROUND_TEMP = R.drawable.medium_gradient;
                        }else if(conv.KelvinToFarenheit(repObj.getJSONObject("main").getDouble("temp")) > 0){
                            TempIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_thermometer_25));
                            TempIcon.setImageTintList(getColorStateList(R.color.cold_text));
                            TemperatureText.setTextColor(getResources().getColor(R.color.cold_text));
                            WeatherBackground.setBackground(getResources().getDrawable(R.drawable.cold_gradient));
                            BACKGROUND_TEMP = R.drawable.cold_gradient;
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
                    Intent i = new Intent(getApplicationContext(),Preferences.class);
                    i.putExtra("TempColor",BACKGROUND_TEMP);
                    startActivity(i);
                }
            });

            MainContainer.addView(WeatherLayout);
        }
    }
}
