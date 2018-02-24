package com.dev.shreyansh.pixelwidget;

import android.os.AsyncTask;

import org.json.JSONObject;

/**
 * Created by shreyansh on 2/15/18.
 */

public class FetchAsync extends AsyncTask<Double, JSONObject, JSONObject> {
    @Override
    protected JSONObject doInBackground(Double... d){
        FetchWeatherJSON weatherClass = new FetchWeatherJSON(d[0],d[1]);
        return weatherClass.fetchData();
    }
}
