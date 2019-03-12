package Util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;

import com.squidswap.squidchat.squidweather.R;

public class IconUtil {
    //Used to get the drawable image for the given icon.
    public static Drawable getWeatherIcon(String type, Context con) {
        Drawable draw;

        switch(type) {
            case "snow":
                    draw = con.getResources().getDrawable(R.drawable.ic_snowflake);
                break;
            case "partly-cloudy-night":
                    draw = con.getResources().getDrawable(R.drawable.ic_wi_cloudy);
                break;
            case "rain":
                    draw = con.getResources().getDrawable(R.drawable.ic_cloud_rain);
                break;
            case "partly-cloudy-day":
                    draw = con.getResources().getDrawable(R.drawable.ic_cloud_sun);
                break;
            default:
                    draw = con.getResources().getDrawable(R.drawable.ic_hourglass_empty_black_24dp);
                break;
        }
        return draw;
    }
}
