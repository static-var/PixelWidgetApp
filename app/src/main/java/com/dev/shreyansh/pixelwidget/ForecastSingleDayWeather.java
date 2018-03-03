package com.dev.shreyansh.pixelwidget;

/**
 * Created by shreyansh on 3/4/18.
 */

public class ForecastSingleDayWeather {
    private final static String TAG = "ForecastSingleDayWeatherClass";

    /* Add attributes of class */
    private double temperature;
    private double minTemperature;
    private double maxTemperature;
    private String mainWeather;
    private String descWeather;
    private double humidity;
    private String dateText;

    /* Default Empty Constructor */
    public ForecastSingleDayWeather() {

    }

    /* Default Parametrised constructor */
    public ForecastSingleDayWeather(double temperature, double maxTemperature, double minTemperature) {
        this.temperature = temperature;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
    }

    /* All Setter Functions */
    public void setTemperature(double temperature){
        this.temperature = temperature;
    }

    public void setMinTemperature(double minTemperature){
        this.minTemperature = minTemperature;
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

    /* All Getter Functions */
    public double getTemperature(){
        return temperature;
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
        return dateText;
    }

    public double getHumidity(){
        return humidity;
    }
}