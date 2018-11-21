package com.squidswap.squidchat.squidweather;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SquidReports extends AppCompatActivity {
    private LayoutInflater inflate;
    private WeatherService service;
    private TemperatureConvert conv;
    private LocationService loc;
    private SensorService sense;
    private CacheHandler cache;
    private SharedPreferences prefs;
    private Geocoder geo;
    private SharedPreferences.Editor edit;
    private QueryService quer; private String APP_LOCATION = "05401";
    private int LOCATION_ACCESS = 45,INTERNET_ACCESS = 46,BACKGROUND_TEMP=R.drawable.cold_gradient;
    private Location pos;

    private AlertDialog alert;

    //Grab all of the layouts for the application.
    private static RelativeLayout MainContainer,WeatherLayout,SettingsShade,SettingsView,WeatherBackground,TopData,MainData,ExtraDetails,ResultsShade;
    private static LinearLayout FlickView;
    private TextView TemperatureText,LocationText,MainText,LongitudeText,LatitudeText;
    private ImageButton RefreshButton,SettingsButton,CloseResults,DefaultLocationButton;
    private ImageView TempIcon,IndicationIcon;
    private Button CloseSettings;
    private LineChart ForecastGraph;
    private EditText SearchField,LocationSearchInput;
    private ListView LocationList;

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

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Different screen layouts
        MainContainer = (RelativeLayout) findViewById(R.id.MainContainer);
        WeatherLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.weather_layout,null);
        SettingsShade = (RelativeLayout) WeatherLayout.findViewById(R.id.SettingsShade);
        SettingsView = (RelativeLayout) WeatherLayout.findViewById(R.id.SettingsFragment);
        WeatherBackground = (RelativeLayout) WeatherLayout.findViewById(R.id.WeatherBackground);
        MainData = (RelativeLayout) WeatherLayout.findViewById(R.id.MainData);
        ExtraDetails = (RelativeLayout) WeatherLayout.findViewById(R.id.ExtraDetails);
        TopData = (RelativeLayout) WeatherLayout.findViewById(R.id.TopData);
        ResultsShade = (RelativeLayout) WeatherLayout.findViewById(R.id.ResultsShade);

        //Textviews for weather information to occupy.
        TemperatureText = (TextView) WeatherLayout.findViewById(R.id.TempText);
        LocationText = (TextView) WeatherLayout.findViewById(R.id.LocationText);
        MainText = (TextView) WeatherLayout.findViewById(R.id.MainText);
        RefreshButton = (ImageButton) WeatherLayout.findViewById(R.id.RefreshButton);
        SettingsButton = (ImageButton) WeatherLayout.findViewById(R.id.SettingsButton);
        CloseSettings = (Button) WeatherLayout.findViewById(R.id.CloseSettings);
        LongitudeText = (TextView) WeatherLayout.findViewById(R.id.LongitudeText);
        LatitudeText = (TextView) WeatherLayout.findViewById(R.id.LatitudeText);
        SearchField = (EditText) WeatherLayout.findViewById(R.id.LocationSearchText);
        TempIcon = (ImageView) WeatherLayout.findViewById(R.id.TempIcon);
        IndicationIcon = (ImageView) WeatherLayout.findViewById(R.id.IndicationIcon);
        CloseResults = (ImageButton) WeatherLayout.findViewById(R.id.CloseSearchResults);
        LocationSearchInput = (EditText) WeatherLayout.findViewById(R.id.LocationSearchInput);
        LocationList = (ListView) WeatherLayout.findViewById(R.id.LocationsList);
        DefaultLocationButton = (ImageButton) WeatherLayout.findViewById(R.id.SetDefaultButton);

        //Build our Alert dialog for asking about default location.
        alert = new AlertDialog.Builder(SquidReports.this).create();
        alert.setTitle("Set Default Location");
        alert.setMessage("Would you like to set this location as your default weather location?");
        alert.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                edit = prefs.edit();
                edit.putFloat("default_lat",(float) loc.getLocation().getLatitude());
                edit.putFloat("default_lon",(float) loc.getLocation().getLongitude());
                edit.apply();
            }
        });
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        this.geo = new Geocoder(getApplicationContext(), Locale.getDefault());

        final Animation rot = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate);

        //Check if the user has given the application access to use the internet.
        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            System.out.println("NO INTERNET ACCESS PROVIDED");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},INTERNET_ACCESS);
        }else if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            System.out.println("NO LOCATION ACCESS PROVIDED");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_ACCESS);
        }

        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.d("LOGIC FLOW","Permissions Passed");
            //Utilitie initialization
            service = new WeatherService(getApplicationContext());
            conv = new TemperatureConvert();
            loc = new LocationService(getApplicationContext());
            quer = new QueryService(getApplicationContext(),prefs);
            cache = new CacheHandler(getApplicationContext());


            //Click event for the refresh animation and to repull data.
            RefreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RefreshButton.startAnimation(rot);
                    PullInfo(false,false);
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

            SearchField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToggleResults(true);
                    SearchField.clearFocus();
                }
            });

            CloseResults.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToggleResults(false);
                }
            });

            DefaultLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.show();
                }
            });

            LocationSearchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //Make sure we have a value before sending the volley request to the API
                    if(!s.equals("")){
                        loc.FindLocation(s.toString(), new ServiceInterface() {
                            @Override
                            public void onWeatherRecieved(String reponse) {
                                try{
                                    final List<Address> locations = new ArrayList<>();
                                    JSONArray results = new JSONObject(reponse).getJSONArray("results");

                                    for(int i = 0;i < results.length();i++){
                                        List<Address> locs = geo.getFromLocation(results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat"),results.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng"),1);
                                        locations.add(locs.get(0));
                                    }

                                    //Here is where we want to apply the data to the array adapter.
                                    LocationAdapter adapt = new LocationAdapter(getApplicationContext(),R.layout.single_location,locations);
                                    LocationList.setAdapter(adapt);

                                    LocationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            ToggleResults(false);
                                            Location loca = new Location("");

                                            loca.setLatitude(locations.get(position).getLatitude());
                                            loca.setLongitude(locations.get(position).getLongitude());

                                            loc.setLocation(loca);
                                            LocationSearchInput.setText("");
                                            PullInfo(false,false);
                                        }
                                    });
                                }catch(JSONException e){
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onWeatherError(String error) {

                            }
                        });
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            MainContainer.addView(WeatherLayout);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //First we need ot determine if we are going to be pulling from the API or be
        //using our cache to load the data.
        if(quer.AllowPull()){
            PullInfo(false,false);
        }else{
            PullInfo(false,true);
        }
    }

    //Runs the WEB requests and outputs the information into  the UI
    private void PullInfo(boolean useDefault, final boolean fromCache){
        quer.RecordCurrent();
        //sense = new SensorService(getApplicationContext());

        if(prefs.contains("default_lat") && prefs.contains("default_lon") && useDefault){
            Location loca = new Location("");

            loca.setLongitude(prefs.getFloat("default_lon",0));
            loca.setLatitude(prefs.getFloat("default_lat",0));
            loc.setLocation(loca);

            pos = loc.getLocation();
        }else{
            pos = loc.getLocation();
        }

        ForecastGraph = (LineChart) WeatherLayout.findViewById(R.id.ForecastChart);

        if(fromCache){
            Log.d("LOGIC FLOW","Pulling data from cache.");
            ApplyUI(cache.LoadCache());
        }else{
            Log.d("LOGIC FLOW","Pulling data from Web.");
            service.GetForcast(pos, new ServiceInterface() {
                @Override
                public void onWeatherRecieved(String reponse) {
                    cache.SaveCache(reponse);
                    ApplyUI(reponse);
                }

                @Override
                public void onWeatherError(String error) {

                }
            });
        }

        LongitudeText.setText(String.valueOf(Math.floor((double) pos.getLongitude())) + ", ");
        LatitudeText.setText(String.valueOf(Math.floor((double) pos.getLatitude())));

        APP_LOCATION = prefs.getString("api_zipcode","33573");
    }

    private void ToggleResults(boolean val){
        if(val){
            TopData.setVisibility(View.GONE);
            MainData.setVisibility(View.GONE);
            ExtraDetails.setVisibility(View.GONE);
            ResultsShade.setVisibility(View.VISIBLE);
            LocationSearchInput.requestFocus();
        }else{
            TopData.setVisibility(View.VISIBLE);
            MainData.setVisibility(View.VISIBLE);
            ExtraDetails.setVisibility(View.VISIBLE);
            ResultsShade.setVisibility(View.GONE);
        }
    }

    private void ApplyUI(String reponse){
        try{
            JSONArray hourly_data = new JSONObject(reponse).getJSONObject("daily").getJSONArray("data");
            List<Entry> entries = new ArrayList<>();

            for(int i = 0;i < hourly_data.length();i++){
                entries.add(new Entry(i,(int) hourly_data.getJSONObject(i).getDouble("temperatureHigh")));
            }

            LineDataSet forecasts = new LineDataSet(entries,"Day");
            forecasts.setColor(getResources().getColor(android.R.color.background_light));
            forecasts.setLineWidth(3);
            forecasts.setDrawCircles(prefs.getBoolean("graph_dots",false));

            if(prefs.getBoolean("curve_graph",true)){
                forecasts.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                forecasts.setCubicIntensity(.05f);
            }else{
                forecasts.setMode(LineDataSet.Mode.LINEAR);
            }

            forecasts.setCircleRadius(5);
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
            yl.setDrawLabels(true);
            yl.setDrawGridLines(prefs.getBoolean("graph_gridlines",false));
            yl.setDrawZeroLine(true);
            yl.setGridColor(Color.parseColor("#FFFFFF"));
            yl.setDrawTopYLabelEntry(true);
            yl.setTextColor(getResources().getColor(android.R.color.background_light));
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
            ForecastGraph.animateX(300);
            ForecastGraph.setPadding(40,0,40,0);
            ForecastGraph.setTouchEnabled(false);
            ForecastGraph.setData(finalFor);
            ForecastGraph.invalidate();

            //Logic for the Top Data Card
            JSONObject repObj = new JSONObject(reponse);
            //Set the retrieved data into the text views.
            TemperatureText.setText(Integer.toString((int) Math.floor(repObj.getJSONObject("currently").getDouble("temperature"))) + (char) 0x00B0);
            LocationText.setText(loc.getAddress().getLocality() + ", " + loc.getAddress().getAdminArea());
            MainText.setText(repObj.getJSONObject("currently").getString("summary"));

            Intent sendData = new Intent(SquidWidget.INFO_UPDATE);
            getApplicationContext().sendBroadcast(sendData);

            switch(repObj.getJSONObject("currently").getString("summary")){
                case "Partly Cloudy":
                    IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_wi_cloudy));
                    break;
                case "Clouds":
                    IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_wi_cloudy));
                    break;
                case "Light Rain":
                    IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_wi_sprinkle));
                    break;
                case "Light Snow":
                    IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_wi_snow));
                    break;
                case "Drizzle":
                    IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_wi_raindrops));
                case "Breezy and Mostly Cloudy":
                    IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_wi_cloudy_windy));
                    break;
                case "Rain":
                    IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_wi_day_rain));
                    break;
                case "Snow":
                    IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_wi_day_snow));
                    break;
                case "Clear":
                    IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_wi_day_sunny));
                    break;
                default:
                    IndicationIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_wi_cloudy));
                    break;
            }

            if(repObj.getJSONObject("currently").getDouble("temperature") > 90){
                TempIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_thermometer_100));
                TempIcon.setImageTintList(getColorStateList(R.color.temp_100));
                TemperatureText.setTextColor(getResources().getColor(R.color.temp_100));
                WeatherBackground.setBackground(getResources().getDrawable(R.drawable.warm_gradient));
                IndicationIcon.setColorFilter(getResources().getColor(R.color.temp_100));
                BACKGROUND_TEMP = R.drawable.warm_gradient;
            }else if(repObj.getJSONObject("currently").getDouble("temperature") > 70){
                TempIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_thermometer_75));
                TempIcon.setImageTintList(getColorStateList(R.color.temp_75));
                TemperatureText.setTextColor(getResources().getColor(R.color.temp_75));
                WeatherBackground.setBackground(getResources().getDrawable(R.drawable.warm_gradient));
                IndicationIcon.setColorFilter(getResources().getColor(R.color.temp_75));
                BACKGROUND_TEMP = R.drawable.warm_gradient;
            }else if(repObj.getJSONObject("currently").getDouble("temperature") > 40){
                TempIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_thermometer_50));
                TempIcon.setImageTintList(getColorStateList(R.color.medium_end));
                TemperatureText.setTextColor(getResources().getColor(R.color.medium_end));
                WeatherBackground.setBackground(getResources().getDrawable(R.drawable.medium_gradient));
                IndicationIcon.setColorFilter(getResources().getColor(R.color.medium_end));
                BACKGROUND_TEMP = R.drawable.medium_gradient;
            }else if(repObj.getJSONObject("currently").getDouble("temperature") > 0){
                TempIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_thermometer_25));
                TempIcon.setImageTintList(getColorStateList(R.color.cold_text));
                TemperatureText.setTextColor(getResources().getColor(R.color.cold_text));
                WeatherBackground.setBackground(getResources().getDrawable(R.drawable.cold_gradient));
                IndicationIcon.setColorFilter(getResources().getColor(R.color.cold_text));
                BACKGROUND_TEMP = R.drawable.cold_gradient;
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
}
