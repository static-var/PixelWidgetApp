package com.dev.shreyansh.pixelwidget;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by shreyansh on 3/4/18.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.MyViewHolder> {
    private List<ForecastSingleDayWeather> forecastSingleDayWeathers;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView forecastImage;
        public TextView forecastDay;
        public TextView forecastTemp;

        public MyViewHolder(View view) {
            super(view);
            forecastImage = view.findViewById(R.id.forecast_weather_image);
            forecastDay = view.findViewById(R.id.forecast_day_text);
            forecastTemp = view.findViewById(R.id.forecast_day_temp);
        }
    }

    public ForecastAdapter(List<ForecastSingleDayWeather> allWeather){
        forecastSingleDayWeathers = allWeather;
    }

    @Override
    public int getItemCount() {
        return forecastSingleDayWeathers.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_row_view, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ForecastSingleDayWeather forecastSingleDayWeather = forecastSingleDayWeathers.get(position);
        holder.forecastImage.setImageResource(returnImageRes(forecastSingleDayWeather.getDescWeather()));
        holder.forecastDay.setText(Html.fromHtml(forecastSingleDayWeather.getDateText()));
        holder.forecastTemp.setText(String.valueOf(forecastSingleDayWeather.getDayTemperature()));
    }

    private int returnImageRes(String weather) {
        switch (weather.toLowerCase().trim()) {
            case "clear sky": return R.drawable.danieledesantis_weather_icons_sunny;
            case "few clouds":return R.drawable.danieledesantis_weather_icons_cloudy;
            case "scattered clouds": return R.drawable.danieledesantis_weather_icons_cloudy_two;
            case "broken clouds" : return R.drawable.danieledesantis_weather_icons_cloudy_three;
            case "shower rain": return R.drawable.danieledesantis_weather_icons_rainy_two;
            case "rain": return R.drawable.danieledesantis_weather_icons_rainy;
            case "thunderstorm": return R.drawable.danieledesantis_weather_icons_stormy;
            case "snow": return R.drawable.danieledesantis_weather_icons_snowy;
            default: return R.drawable.danieledesantis_weather_icons_cloudy;
        }
    }
}
