package com.squidswap.squidchat.squidweather;

public class TemperatureConvert {
    public double KelvinToFarenheit(double kel) {
        return Math.floor(((kel - 273.15)*1.8) + 32);
    }
}