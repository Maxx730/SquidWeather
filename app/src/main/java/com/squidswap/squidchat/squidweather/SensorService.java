package com.squidswap.squidchat.squidweather;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorService implements SensorEventListener{
    private SensorManager sense;
    private Sensor accel;
    private float x,y,z = 0;

    public SensorService(Context context){
        this.sense = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accel = this.sense.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.sense.registerListener(this,accel,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        this.x = event.values[0];
        this.y = event.values[1];
        this.z = event.values[2];

        System.out.println("X:"+this.x+" Y:"+this.y+" Z:"+this.z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

