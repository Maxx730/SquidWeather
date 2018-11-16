package com.squidswap.squidchat.squidweather;

//Interface that will be used to interact with the weather service.
public interface ServiceInterface {
    public void onWeatherRecieved(String reponse);
    public void onWeatherError(String error);
}
