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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shreyansh on 3/4/18.
 */

public class ForecastSingleDayWeather {
    private final static String TAG = "SingleDayWeatherClass";
    /* Convert Epoch time to human readable time */
    SimpleDateFormat simpleDateFormat;
    /* Add attributes of class */
    private String cityName;
    private double dayTemperature;
    private double nightTemperature;
    private double eveningTemperature;
    private double morningTemperature;
    private double minTemperature;
    private double maxTemperature;
    private String mainWeather;
    private String descWeather;
    private double humidity;
    private String dateText;
    private double windspeed;
    private double cloudiness;

    /* Default Empty Constructor */
    public ForecastSingleDayWeather() {

    }

    /* Default Parametrised constructor */
    public ForecastSingleDayWeather(double temperature, double maxTemperature, double minTemperature) {
        this.dayTemperature = temperature;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
    }

    /* All Getter Functions */
    public String getCityName() {
        return cityName;
    }

    /* All Setter Functions */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public double getDayTemperature() {
        return dayTemperature;
    }

    public void setDayTemperature(double temperature) {
        this.dayTemperature = temperature;
    }

    public double getNightTemperature() {
        return nightTemperature;
    }

    public void setNightTemperature(double nightTemperature) {
        this.nightTemperature = nightTemperature;
    }

    public double getEveningTemperature() {
        return eveningTemperature;
    }

    public void setEveningTemperature(double eveningTemperature) {
        this.eveningTemperature = eveningTemperature;
    }

    public double getMorningTemperature() {
        return morningTemperature;
    }

    public void setMorningTemperature(double morningTemperature) {
        this.morningTemperature = morningTemperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public String getMainWeather() {
        return mainWeather;
    }

    public void setMainWeather(String mainWeather) {
        this.mainWeather = mainWeather;
    }

    public String getDescWeather() {
        return descWeather;
    }

    public void setDescWeather(String descWeather) {
        this.descWeather = descWeather;
    }

    public String getDateText() {
        /* Convert String time to long as it is in epoch format */
        Long epochTime = Long.parseLong(dateText) * 1000;

        /* Format to get Day */
        simpleDateFormat = new SimpleDateFormat("EEE");
        String day = simpleDateFormat.format(new Date(epochTime));

        /* Format to get Date alone */
        simpleDateFormat = new SimpleDateFormat("MMMM dd");
        String date = simpleDateFormat.format(new Date(epochTime));

        return "<b>" + day + "</b>, " + date;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getWindspeed() {
        return windspeed;
    }

    public void setWindspeed(double windspeed) {
        this.windspeed = windspeed;
    }

    public double getCloudiness() {
        return cloudiness;
    }

    public void setCloudiness(double cloudiness) {
        this.cloudiness = cloudiness;
    }
}