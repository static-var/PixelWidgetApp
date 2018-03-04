package com.dev.shreyansh.pixelwidget;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by shreyansh on 3/4/18.
 */

public class FetchAsyncForecast extends AsyncTask<Double, JSONObject, JSONObject> {
    @Override
    protected JSONObject doInBackground(Double... d){
        FetchAndProcessForecastWeather forecastClass = new FetchAndProcessForecastWeather(d[0],d[1]);
        return forecastClass.fetchData();
    }
}