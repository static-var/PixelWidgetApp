/*
 * Copyright (C) 2017-2018 Shreyansh Lodha <slodha96@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PixelWidget.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dev.shreyansh.pixelwidget.WeatherAndForecast;

import android.util.Log;

import com.dev.shreyansh.pixelwidget.Keys.OpenWeatherKey;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shreyansh on 3/4/18.
 */

public class FetchAndProcessForecastWeather {
    private final static String TAG = "F&P-ForecastWeather";
    private static int failedAttempts;
    private ArrayList<ForecastSingleDayWeather> forecastSingleDayWeathers = new ArrayList<>();
    private String query = "http://api.openweathermap.org/data/2.5/forecast/daily?lat=%s&lon=%s&units=metric&cnt=15&appid=%s";
    private double latitude;
    private double longitude;
    private HttpClient client;
    private HttpGet httpGet;
    private HttpResponse httpResponse;

    /* Default constructor which should receive latitude and longitude */
    public FetchAndProcessForecastWeather(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        query = String.format(query, latitude, longitude, OpenWeatherKey.KEY);
        Log.i(TAG, query);
    }

    public FetchAndProcessForecastWeather() {
    }

    /* Fetch data in AsyncTask */
    public JSONObject fetchData() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);
        client = new DefaultHttpClient(httpParams);
        httpGet = new HttpGet(query);

        try {
            httpResponse = client.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            return new JSONObject(EntityUtils.toString(entity));
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            failedAttempts++;
            if (failedAttempts == 4)
                return null;
            return fetchData();
        }
    }

    /* Return the forecast in a list */
    public List<ForecastSingleDayWeather> processData(JSONObject data) {
        Log.i(TAG, String.valueOf(data));
        try {
            /*
             * If location exists the return will be 200
             * Then only proceed further for fetching data
             */
            if (data.getInt("cod") == 200) {
                /* All forecast are in a JSONArray
                 * of KEY called 'list' */
                JSONArray allForecast = data.getJSONArray("list");
                JSONObject singleData;
                JSONObject subObject;

                /* Fetch city name */
                String cityname;
                data = data.getJSONObject("city");
                cityname = data.getString("name");

                /* Should be 15 */
                int len = allForecast.length();
                /* Take Forecast for next 14 days */
                for (int i = 1; i < len; i++) {
                    /* Process Forecast of one day at a time */
                    singleData = allForecast.getJSONObject(i);

                    /*
                     * Initialize forecast class empty
                     * and later use setter functions to initialize the value
                     */
                    ForecastSingleDayWeather singleDayWeather = new ForecastSingleDayWeather();

                    /*
                     * Process JSONObject and set all the attributes of
                     * Forecast class as per the processed JSONObject
                     */
                    singleDayWeather.setCityName(cityname);
                    singleDayWeather.setDateText(singleData.getString("dt"));
                    subObject = singleData.getJSONObject("temp");
                    singleDayWeather.setDayTemperature(subObject.getDouble("day"));
                    singleDayWeather.setMinTemperature(subObject.getDouble("min"));
                    singleDayWeather.setMaxTemperature(subObject.getDouble("max"));
                    singleDayWeather.setMorningTemperature(subObject.getDouble("morn"));
                    singleDayWeather.setEveningTemperature(subObject.getDouble("eve"));
                    singleDayWeather.setNightTemperature(subObject.getDouble("night"));
                    singleDayWeather.setHumidity(singleData.getDouble("humidity"));
                    singleDayWeather.setWindspeed(singleData.getDouble("speed"));
                    singleDayWeather.setCloudiness(singleData.getDouble("clouds"));
                    JSONArray obj = singleData.getJSONArray("weather");
                    subObject = obj.getJSONObject(0);
                    singleDayWeather.setMainWeather(subObject.getString("main"));
                    singleDayWeather.setDescWeather(subObject.getString("description"));

                    /* add each day's fetched data to List of forecast class */
                    forecastSingleDayWeathers.add(singleDayWeather);
                }
            } else {
                /* No data is there to be processed */
                Log.i(TAG, "Location Data not available");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        }

        /* This will be either null or it will have all the date */
        return forecastSingleDayWeathers;
    }
}
