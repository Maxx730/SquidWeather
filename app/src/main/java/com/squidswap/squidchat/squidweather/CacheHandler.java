package com.squidswap.squidchat.squidweather;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

//Class that handles saving and loading weather data pulled from the web into and out
//of a cache, this helps cause les API calls as well as will work for the most recent data
//if the user does not currently have internet access.
public class CacheHandler {
    private Context con;

    public CacheHandler(Context con){
        this.con = con;
    }

    public void SaveCache(String value){
        Log.d("CACHE HANDLER","Saving data: "+value);

        try{
            File cache = new File(this.con.getCacheDir().getPath() + "/cache.sqc");
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(cache));
            out.write(value);
            out.close();
            Log.d("CACHE HANDLER","Cache saved successfully!");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String LoadCache(){
        Log.d("CACHE HANDLER","Loading cache...");
        try{
            File cache = new File(this.con.getCacheDir().getPath() + "/cache.sqc");
            FileInputStream fil = new FileInputStream(cache);
            BufferedReader read = new BufferedReader(new InputStreamReader(fil, StandardCharsets.ISO_8859_1));
            String line = null;
            StringBuilder build = new StringBuilder();

            //Loop through each line in the bufferend stream reader and append.
            while((line = read.readLine()) != null){
                build.append(line);
            }

            read.close();

            Log.d("CACHE HANDLER","Found Contents:" + build.toString());
            return(build.toString());
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
