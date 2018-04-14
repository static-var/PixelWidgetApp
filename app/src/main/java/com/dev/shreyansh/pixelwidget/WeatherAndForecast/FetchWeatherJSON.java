package com.dev.shreyansh.pixelwidget.WeatherAndForecast;

import android.content.Context;
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
import org.json.JSONObject;

/**
 * Created by shreyansh on 2/14/18.
 */

public class FetchWeatherJSON {

    /* Necessary attributes */
    public static final String TAG = "FetchWeatherJSON";
    private String weatherURL = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&appid=%s";
    private double latitude;
    private double longitude;
    private boolean fahrenheit;
    private JSONObject data;
    private Context appContext;

    private HttpClient client;
    private HttpGet httpGet;
    private HttpResponse httpResponse;
    private static int failedAttempts;

    /* This constructor should be called once at-least,
     * more importantly it should be called first
     */
    public FetchWeatherJSON(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        fahrenheit = false;
        weatherURL = String.format(weatherURL,latitude,longitude, OpenWeatherKey.KEY);
        Log.i(TAG,weatherURL);
        failedAttempts = 0;
    }


    public JSONObject fetchData() {
        httpGet = new HttpGet(weatherURL);
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);
        client = new DefaultHttpClient(httpParams);

        try {
            httpResponse = client.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            data = new JSONObject(EntityUtils.toString(entity));
            return data;
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            failedAttempts++;
            if(failedAttempts == 4)
                return null;
            data = fetchData();
            return data;
        }
    }

}
