package Util;

import android.util.Log;

public class DateUtils {
    public static String getDayName(int day) {
        if( day > 6){
            day = day - 7;
        }

        String days[] = {"SUN","MON","TUE","WED","THU","FRI","SAT"};
        return days[day];
    }
}
