package com.dev.shreyansh.pixelwidget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ForecastDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_display);

        /* TODO : Extract Bundle using Bundle <name> = getIntent().getExtras(<StringKeyValue>) */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*
         * TODO : Change Title to Date of the forecast use getSupportActionBar().setTitle(<Date here>)
         * TODO : Set elevation of action bar to 0 using getSupportActionBar().setElevation(0)
         * TODO : Set Data in TextViews and display all the data from ForecastSingleDayWeather class
         */
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        setResult(1);
    }
}
