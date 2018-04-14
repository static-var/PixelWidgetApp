package com.dev.shreyansh.pixelwidget.WeatherAndForecast;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shreyansh on 2/15/18.
 */

public class Weather {
    public static final String TAG = "WeatherJSONParserClass";
    private double currentTemperature;
    private double maxTemperature;
    private double minTemperature;
    private double windSpeed;
    private double humidity;
    private String cityName;
    private String countryCode;
    private String main;
    private String description;
    private String sunrise;
    private String sunset;
    private boolean isDayTime;
    private JSONObject data;

    public Weather(JSONObject data) {
        this.data = data;
        Log.i(TAG, this.data.toString());
        if(processData(this.data)){
            Log.i(TAG,"Data fetched and processed");
        } else {
            Log.e(TAG,"Unable to parse JSON");
        }
    }

    private boolean processData(JSONObject data) {
        try {
            /* Set Time Format */
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

            /* Fetch Status of result */
            if(data.getInt("cod") == 200) {
                /* Fetch textual weather info */
                JSONArray json = data.getJSONArray("weather");
                JSONObject subDetail = json.getJSONObject(0);
                main = subDetail.getString("main");
                description = subDetail.getString("description");

                /* Fetch City Name */
                cityName = data.getString("name");

                /* Fetch Sunrise, Sunset and CountryCode */
                subDetail = data.getJSONObject("sys");
                countryCode = subDetail.getString("country");
                sunrise = simpleDateFormat.format(new Date(subDetail.getLong("sunrise")*1000));
                sunset = simpleDateFormat.format(new Date(subDetail.getLong("sunset")*1000));

                /* Determine if it's DayTime or NightTime */
                isDayTime = (System.currentTimeMillis()/1000) < subDetail.getLong("sunset");

                /* Get temperature data */
                subDetail = data.getJSONObject("main");
                currentTemperature = subDetail.getDouble("temp");
                minTemperature = subDetail.getDouble("temp_min");
                maxTemperature = subDetail.getDouble("temp_max");
                humidity = subDetail.getDouble("humidity");

                /* Get Wind speed */
                subDetail = data.getJSONObject("wind");
                windSpeed = subDetail.getDouble("speed");
                return true;
            } else {
                return false;
            }

        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e.getCause()));
            return false;
        }
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getHumidity() {
        return humidity;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getMain() {
        return main;
    }

    public String getDescription() {
        return description;
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public boolean getIsDayTime() {
        return isDayTime;
    }

}
