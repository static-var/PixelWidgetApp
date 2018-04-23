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

package com.dev.shreyansh.pixelwidget.UI;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.dev.shreyansh.pixelwidget.R;
import com.dev.shreyansh.pixelwidget.Util.CalendarContractHelper;
import com.dev.shreyansh.pixelwidget.Util.StaticStrings;
import com.dev.shreyansh.pixelwidget.Util.Util;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.calendar.CalendarScopes;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final int CAL_REQ = 1001;
    private static final int CAL_WRITE_REQ = 1002;

    private SharedPreferences sp;
    private SharedPreferences.Editor spe;

    private Switch calendarApp;
    private Switch unit;
    private Switch showCityName;

    private TextView integrateGCO;

    private Activity activity;
    private Context context;

    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /* They will be required through out this activity */
        context = this;
        activity = this;

        /* To save User settings */
        sp = getSharedPreferences(StaticStrings.SP, MODE_PRIVATE);

        /* To maintain uniformity through out the app */
        this.getSupportActionBar().setElevation(0);
        this.getSupportActionBar().setTitle("Settings");
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Bind UI elements */
        bindUI();

        GoogleSignInAccount account = null;
        if (GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(this), new Scope(CalendarScopes.CALENDAR)))
            account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            integrateGCO.setText(getText(R.string.disconnect_google_cal));
        }


        /* Put listeners on all elements */
        checkedChangedListener();

        /* Check if Google Calendar app is installed or not */
        if (!checkIfCalendarInstalled(context)) {
            calendarApp.setEnabled(false);
            spe = sp.edit();
            spe.putBoolean(StaticStrings.CAL_APP, false);
            spe.apply();
        } else {
            calendarApp.setEnabled(true);
        }


        if (sp.getBoolean(StaticStrings.CAL_APP, false))
            calendarApp.setChecked(true);

        if (sp.getBoolean(StaticStrings.UNIT_F, false))
            unit.setChecked(true);

        if (sp.getBoolean(StaticStrings.SHOW_CITY, false))
            showCityName.setChecked(true);

        intent = new Intent(this, GoogleAccountsActivity.class);
    }

    private void bindUI() {
        calendarApp = findViewById(R.id.cal_app);
        unit = findViewById(R.id.use_fahrenheit);
        showCityName = findViewById(R.id.show_city_name);
        integrateGCO = findViewById(R.id.calendar_online);
    }

    private void checkedChangedListener() {
        if (checkIfCalendarInstalled(context))
            calendarApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_CALENDAR}, CAL_WRITE_REQ);
                        }
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CALENDAR}, CAL_REQ);
                        }
                        spe = sp.edit();
                        spe.putBoolean(StaticStrings.CAL_APP, true);
                        spe.apply();

                        integrateGCO.setTextColor(Color.GRAY);
                        integrateGCO.setEnabled(false);

                        CalendarContractHelper helper = new CalendarContractHelper(context);
                        helper.getCalendars();
                        helper.getEvents();
                        Util.scheduleCalendarJob(context);
                    } else {
                        spe = sp.edit();
                        spe.putBoolean(StaticStrings.CAL_APP, false);
                        spe.apply();

                        integrateGCO.setTextColor(Color.BLACK);
                        integrateGCO.setEnabled(true);
                        /* Disable job for calendar stuff */
                    }
                }
            });

        unit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    spe = sp.edit();
                    spe.putBoolean(StaticStrings.UNIT_F, true);
                    spe.apply();
                    /* Change request string and C to F */
                } else {
                    spe = sp.edit();
                    spe.putBoolean(StaticStrings.UNIT_F, false);
                    spe.apply();
                    /* Change request string and F to C */
                }
            }
        });

        showCityName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    spe = sp.edit();
                    spe.putBoolean(StaticStrings.SHOW_CITY, true);
                    spe.apply();
                    /* Change request string and C to F */
                } else {
                    spe = sp.edit();
                    spe.putBoolean(StaticStrings.SHOW_CITY, false);
                    spe.apply();
                    /* Change request string and F to C */
                }
            }
        });

        integrateGCO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAL_REQ:
            case CAL_WRITE_REQ:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    /* Show alert with appropriate options */
                    AlertDialog builder = new AlertDialog.Builder(this)
                            .setMessage("We require Calendar permission.")
                            .setTitle("Calendar Permission.")
                            .setCancelable(false)
                            .setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("Close App", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                    builder.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
                    builder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
                }
        }
    }

    private boolean checkIfCalendarInstalled(Context context) {
        try {
            context.getPackageManager().getApplicationInfo(StaticStrings.CALENDAR_APP, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
