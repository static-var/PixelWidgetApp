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

import android.os.AsyncTask;

import org.json.JSONObject;

/**
 * Created by shreyansh on 2/15/18.
 */

public class FetchAsync extends AsyncTask<Double, JSONObject, JSONObject> {
    @Override
    protected JSONObject doInBackground(Double... d) {
        FetchWeatherJSON weatherClass = new FetchWeatherJSON(d[0], d[1]);
        return weatherClass.fetchData();
    }
}
