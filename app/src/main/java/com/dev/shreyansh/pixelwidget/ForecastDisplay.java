package com.dev.shreyansh.pixelwidget;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public class ForecastDisplay extends AppCompatActivity {
    private static final String TAG = "ForecastDisplayClass";
    private static final String degree = (char) 0x00B0+" C";
    private Bundle bundle;

    private TextView bigCity;
    private TextView mainWeather;
    private TextView descWeather;
    private TextView minTemp;
    private TextView maxTemp;
    private TextView dayTemp;
    private TextView nightTemp;
    private TextView eveningTemp;
    private TextView morningTemp;
    private TextView cloudiness;
    private TextView humidity;
    private TextView wind;

    private ImageView weatherImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_display);

        /* Get Data from previous activity */
        bundle = getIntent().getExtras();

        /* Bind UI elements to this code */
        bind();

        /* Necessary changes for ActionBar */
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setTitle(Html.fromHtml(bundle.getString("dateText")));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        setContents();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        setResult(1);
    }

    private void bind() {
        bigCity = findViewById(R.id.big_city);
        mainWeather = findViewById(R.id.main_weather);
        descWeather = findViewById(R.id.desc_weather);
        minTemp = findViewById(R.id.min_temp_value);
        maxTemp = findViewById(R.id.max_temp_value);
        dayTemp = findViewById(R.id.forecast_day_value);
        nightTemp = findViewById(R.id.forecast_night_value);
        morningTemp = findViewById(R.id.forecast_morning_value);
        eveningTemp = findViewById(R.id.forecast_evening_value);
        cloudiness = findViewById(R.id.forecast_cloudiness_value);
        humidity = findViewById(R.id.forecast_humidity_value);
        wind = findViewById(R.id.forecast_wind_value);
        weatherImage = findViewById(R.id.forecast_weather_image);
    }

    private void setContents() {
        bigCity.setText(bundle.getString("cityName"));
        mainWeather.setText(bundle.getString("mainWeather"));
        weatherImage.setImageResource(returnImageRes(bundle.getString("descWeather")));
        descWeather.setText(WordUtils.capitalize(bundle.getString("descWeather")));
        minTemp.setText(String.valueOf(bundle.getDouble("minTemp")) + degree);
        maxTemp.setText(String.valueOf(bundle.getDouble("maxTemp")) + degree);
        dayTemp.setText(String.valueOf(bundle.getDouble("dayTemp")) + degree);
        nightTemp.setText(String.valueOf(bundle.getDouble("nightTemp")) + degree);
        morningTemp.setText(String.valueOf(bundle.getDouble("morningTemp")) + degree);
        eveningTemp.setText(String.valueOf(bundle.getDouble("eveningTemp")) + degree);
        cloudiness.setText(String.valueOf(bundle.getDouble("cloudiness")) + " %");
        humidity.setText(String.valueOf(bundle.getDouble("humidity")) + " %");
        wind.setText(String.valueOf(bundle.getDouble("wind")) + " m/s");
    }

    private int returnImageRes(String weather) {
        switch (weather.toLowerCase().trim()) {
            case "clear sky":
            case "sky is clear": return R.drawable.danieledesantis_weather_icons_sunny;
            case "few clouds": return R.drawable.danieledesantis_weather_icons_cloudy;
            case "scattered clouds": return R.drawable.danieledesantis_weather_icons_cloudy_two;
            case "broken clouds" : return R.drawable.danieledesantis_weather_icons_cloudy_three;
            case "shower rain":
            case "moderate rain": return R.drawable.danieledesantis_weather_icons_rainy_two;
            case "rain":
            case "light rain": return R.drawable.danieledesantis_weather_icons_rainy;
            case "thunderstorm":
            case "heavy intensity rain": return R.drawable.danieledesantis_weather_icons_stormy;
            case "snow": return R.drawable.danieledesantis_weather_icons_snowy;
            default: return R.drawable.danieledesantis_weather_icons_cloudy;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.enter1, R.anim.exit1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
