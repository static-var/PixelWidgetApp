package com.dev.shreyansh.pixelwidget;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shreyansh on 3/4/18.
 */

public class ForecastSingleDayWeather {
    private final static String TAG = "SingleDayWeatherClass";

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

    /* Convert Epoch time to human readable time */
    SimpleDateFormat simpleDateFormat;

    /* Default Empty Constructor */
    public ForecastSingleDayWeather() {

    }

    /* Default Parametrised constructor */
    public ForecastSingleDayWeather(double temperature, double maxTemperature, double minTemperature) {
        this.dayTemperature = temperature;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
    }

    /* All Setter Functions */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setDayTemperature(double temperature){
        this.dayTemperature = temperature;
    }

    public void setMinTemperature(double minTemperature){
        this.minTemperature = minTemperature;
    }

    public void setMorningTemperature(double morningTemperature) {
        this.morningTemperature = morningTemperature;
    }

    public void setNightTemperature(double nightTemperature) {
        this.nightTemperature = nightTemperature;
    }

    public void setEveningTemperature(double eveningTemperature) {
        this.eveningTemperature = eveningTemperature;
    }

    public void setMaxTemperature(double maxTemperature){
        this.maxTemperature = maxTemperature;
    }

    public void setMainWeather(String mainWeather){
        this.mainWeather = mainWeather;
    }

    public void setDescWeather(String descWeather){
        this.descWeather = descWeather;
    }

    public void setDateText(String dateText){
        this.dateText = dateText;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public void setWindspeed(double windspeed) {
        this.windspeed = windspeed;
    }

    public void setCloudiness(double cloudiness){
        this.cloudiness = cloudiness;
    }

    /* All Getter Functions */
    public String getCityName() {
        return cityName;
    }

    public double getDayTemperature(){
        return dayTemperature;
    }

    public double getNightTemperature() {
        return nightTemperature;
    }

    public double getEveningTemperature() {
        return eveningTemperature;
    }

    public double getMorningTemperature() {
        return morningTemperature;
    }

    public double getMinTemperature(){
        return minTemperature;
    }

    public double getMaxTemperature(){
        return maxTemperature;
    }

    public String getMainWeather(){
        return mainWeather;
    }

    public String getDescWeather(){
        return descWeather;
    }

    public String getDateText(){
        /* Convert String time to long as it is in epoch format */
        Long epochTime = Long.parseLong(dateText)*1000;

        /* Format to get Day */
        simpleDateFormat = new SimpleDateFormat("EEE");
        String day = simpleDateFormat.format(new Date(epochTime));

        /* Format to get Date alone */
        simpleDateFormat = new SimpleDateFormat("MMMM dd");
        String date = simpleDateFormat.format(new Date(epochTime));

        return "<b>"+day+"</b>, "+date;
    }

    public double getHumidity(){
        return humidity;
    }

    public double getWindspeed() {
        return windspeed;
    }

    public double getCloudiness() {
        return cloudiness;
    }
}