package com.dev.shreyansh.pixelwidget.UI;

import android.R.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.dev.shreyansh.pixelwidget.R;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private SharedPreferences.Editor spe;

    private Switch calendarApp;
    private Switch calendarOnline;
    private Switch unit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        sp = getSharedPreferences("PIXEL", MODE_PRIVATE);

        this.getSupportActionBar().setElevation(0);
        this.getSupportActionBar().setTitle("Settings");


        bindUI();

        checkedChangedListener();
    }

    private void bindUI() {

    }

    private void checkedChangedListener() {
    }
}
