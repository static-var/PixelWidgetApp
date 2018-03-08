package com.dev.shreyansh.pixelwidget;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import junit.runner.Version;

import java.text.SimpleDateFormat;

/**
 * Implementation of App Widget functionality.
 */
public class PixelLikeWidget extends AppWidgetProvider{

    public static final String TAG = "WidgetPixelWidget";
    private static final int UPDATE_DURATION = 10000;
    private static final int DISPLACEMENT = 10;
    private static final int FASTEST_UPDATE = 1000;

    TextView eventDisplay;
    TextView eventDurationOrDate;

    Weather weather;
    JSONObject weatherData;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationListener locationListener;
    private LocationManager locationManager;
    public Location location;
    private FusedLocationProviderClient fusedLocationProviderClient;

    WallpaperManager wallpaperManager;


    void updateAppWidget(final Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pixel_like_widget);

        try {
            if(location != null) {
                weatherData = new FetchAsync().execute(location.getLatitude(), location.getLongitude()).get();
                weather = new Weather(weatherData);
                Log.i(TAG, weather.getCityName());
            } else {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            setLocation(location,context);
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onUpdate", e);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        if(checkNetwork(context)) {
            if (checkPlayServices(context)) {
                initialiseManagerListener(context);
                buildGoogleApiClient(context);
                googleApiClient.connect();
            }
        }
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.pixel_like_widget);
        Intent configIntent = new Intent(context, MainActivity.class);

        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.event_display_widget, configPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(final Context context) {
        // Enter relevant functionality for when the first widget is created
        Log.i(TAG,"onEnable");
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context,intent);
        if(intent.getAction()!=null) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), PixelLikeWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            onUpdate(context, appWidgetManager, appWidgetIds);
            Log.i(TAG, "Aaya bc");
        }
    }

    public void initialiseManagerListener(final Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_DURATION)
                    .setSmallestDisplacement(DISPLACEMENT)
                    .setFastestInterval(FASTEST_UPDATE);
        else
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setInterval(UPDATE_DURATION)
                    .setSmallestDisplacement(DISPLACEMENT)
                    .setFastestInterval(FASTEST_UPDATE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    setLocation(location, context);
                } catch (Exception e) {
                    //
                }
            }
        };
        boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!(gps_enabled || network_enabled)) {
            // What to do when we dont have location enabled?

        } else {
            // But if we do then what?
        }

    }

    /* Update the changed location and UI with it */
    public void setLocation(Location newLocation, Context context) {
        if(checkNetwork(context) && location != null && newLocation != null) {
            if (newLocation.getLongitude() != location.getLongitude() && newLocation.getLatitude() != location.getLatitude()) {
                location = newLocation;
                // Write data to UI
            }
        } else {
            googleApiClient.connect();
        }
    }

    public boolean checkNetwork(Context context) {
        /* Check the network status */
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public int returnImageRes(String weather, boolean isStillDay) {

        if(isStillDay) {
            switch (weather.toLowerCase().trim()) {
                case "clear sky":
                case "sky is clear": return R.drawable.danieledesantis_weather_icons_sunny;
                case "few clouds":return R.drawable.danieledesantis_weather_icons_cloudy;
                case "scattered clouds": return R.drawable.danieledesantis_weather_icons_cloudy_two;
                case "broken clouds" : return R.drawable.danieledesantis_weather_icons_cloudy_three;
                case "shower rain":
                case "moderate rain": return R.drawable.danieledesantis_weather_icons_rainy_two;
                case "rain":
                case "light rain": return R.drawable.danieledesantis_weather_icons_rainy;
                case "thunderstorm":
                case "heavy intensity rain":return R.drawable.danieledesantis_weather_icons_stormy;
                case "snow": return R.drawable.danieledesantis_weather_icons_snowy;
                default: return R.drawable.danieledesantis_weather_icons_cloudy;
            }
        } else {
            switch (weather.toLowerCase().trim()) {
                case "clear sky":
                case "sky is clear": return R.drawable.danieledesantis_weather_icons_night_clear;
                case "few clouds":return R.drawable.danieledesantis_weather_icons_night_cloudy;
                case "scattered clouds": return R.drawable.danieledesantis_weather_icons_night_cloudy_two;
                case "broken clouds" : return R.drawable.danieledesantis_weather_icons_night_cloudy_three;
                case "shower rain":
                case "moderate rain": return R.drawable.danieledesantis_weather_icons_night_rainy_two;
                case "rain":
                case "light rain": return R.drawable.danieledesantis_weather_icons_night_rainy;
                case "thunderstorm":
                case "heavy intensity rain": return R.drawable.danieledesantis_weather_icons_night_stormy;
                case "snow": return R.drawable.danieledesantis_weather_icons_night_snowy;
                default: return R.drawable.danieledesantis_weather_icons_night_cloudy;
            }
        }
    }

    /* Check if required version of play services is available in device or not */
    @SuppressWarnings("deprecation")
    private boolean checkPlayServices(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if(resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                // No play services.
            } else {
                // Nothing can be done.
            }
            return false;
        }
        return true;
    }

    private synchronized void buildGoogleApiClient(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(ret(context))
                .addOnConnectionFailedListener(retFail())
                .addApi(LocationServices.API).build();
    }

    /* ConnectionCallback function */
    @SuppressWarnings("deprecation")
    private GoogleApiClient.ConnectionCallbacks ret(final Context context){
        return new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && locationManager != null) {
                    locationManager =  (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    if(!googleApiClient.isConnected()){
                        googleApiClient.connect();
                        return;
                    }
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
                    location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    setData(context, location);

                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                googleApiClient.connect();
            }
        };
    }

    /* OnConnectionFailedListener function */
    private GoogleApiClient.OnConnectionFailedListener retFail() {
        return new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.i(TAG,"Not able to get Location through Fuse API");
            }
        };
    }

    public void setData(Context context, Location location) {
        if(location != null) {
            try {
                Log.i(TAG, "Setting Data on widget");
                weatherData = new FetchAsync().execute(location.getLatitude(), location.getLongitude()).get();
                weather = new Weather(weatherData);
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pixel_like_widget);
                SimpleDateFormat date = new SimpleDateFormat("EEEE");
                String day = date.format(System.currentTimeMillis()) + ", ";
                date = new SimpleDateFormat("MMM");
                day = day + date.format(System.currentTimeMillis()) + " ";
                date = new SimpleDateFormat("dd");
                day = day + date.format(System.currentTimeMillis());
                views.setTextViewText(R.id.date_or_event_duration, day + "  |");
                views.setTextViewText(R.id.event_display_widget, weather.getCityName());
                views.setTextViewText(R.id.weather_temp, String.valueOf(Math.round(weather.getCurrentTemperature())) + (char) 0x00B0 + " C");
                views.setImageViewResource(R.id.weather_icon, returnImageRes(weather.getDescription(), weather.getIsDayTime()));

                ComponentName thisWidget = new ComponentName(context, PixelLikeWidget.class);
                AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
                googleApiClient.disconnect();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }

}

// TODO : Use intent filter to check when the phone restart and when network connects
