package com.dev.shreyansh.pixelwidget;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by shreyansh on 3/8/18.
 */

public class UpdateWidgetJobService extends JobService {

    /*
    * The aim of this JobService is to fetch data of weather periodically
    * and update the widget with the latest weather data
    * this job is NOT supposed to be called too many times
    */

    private static final String TAG = UpdateWidgetJobService.class.getSimpleName();
    private static final int UPDATE_DURATION = 10000;
    private static final int DISPLACEMENT = 10;
    private static final int FASTEST_UPDATE = 1000;

    public static int COUNT = 0;

    private boolean eventScheduled = false;
    private boolean overrideDate = false;
    private int count;

    private Weather weather;
    private JSONObject weatherData;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Location location;

    public List<Event> eventList;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, TAG + " Job Service started, ");
        if(COUNT==9)
            COUNT = 0;
        else
            COUNT++;
        startWorkOnNewThread(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        Log.i(TAG,"Job Stopped.");
        jobFinished(params, true);
        return true;
    }

    private void startWorkOnNewThread(final JobParameters jobParameters) {
        /*
        * Run the code in a different thread
        * this prevents framework generating ANR dialogs for the app
        */

        /*
        * Below is a workaround which will make sure that weather gets updated only 1 time
        * even when the calender API related work will happen all 4 times
        */
        Log.i(TAG,"Counter : "+String.valueOf(COUNT));
        new Thread(new Runnable() {
            public void run() {
                googleCalendarWork(getApplicationContext(), jobParameters);
                if(COUNT == 1) {
                    doWork();
                }
            }
        }).start();
    }

    private synchronized void doWork() {
        new Thread() {
            public void run() {
                doAllLocationStuff(getApplicationContext());
            }
        }.start();

    }

    private synchronized void doAllLocationStuff(Context context) {
        if(checkNetwork(context)) {
            if(pingGoogle()) {
                if (checkPlayServices(context)) {
                    initialiseManagerListener(context);
                    buildGoogleApiClient(context);
                    googleApiClient.connect();
                }
            }
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
            Log.e(TAG, "Error", e);
            return false;
        }
    }

    /* Check if required version of play services is available in device or not */
    @SuppressWarnings("deprecation")
    private boolean checkPlayServices(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if(resultCode != ConnectionResult.SUCCESS){
            return false;
        }
        return true;
    }

    private boolean pingGoogle() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            Log.i(TAG, "Error", e);
            return false;
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
                    fetchData(context, location);

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

    public void fetchData(final Context context, final Location location) {
        if(location != null) {
            new Thread() {
                public void run() {
                    try {
                        weatherData = new FetchAsync().execute(location.getLatitude(), location.getLongitude()).get();
                        weather = new Weather(weatherData);
                        displayDataOnWidget(context);
                    } catch (Exception e) {
                        Log.e(TAG, "Error on fetching weather data.", e);
                    }
                }
            }.start();
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

    private void displayDataOnWidget(Context context){
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pixel_like_widget);
        SimpleDateFormat date = new SimpleDateFormat("EEEE");
        String day = date.format(System.currentTimeMillis()) + ", ";
        date = new SimpleDateFormat("MMM");
        day = day + date.format(System.currentTimeMillis()) + " ";
        date = new SimpleDateFormat("dd");
        day = day + date.format(System.currentTimeMillis());
        if(!eventScheduled)
            views.setTextViewText(R.id.event_display_widget, weather.getCityName());
        if(!overrideDate)
            views.setTextViewText(R.id.date_or_event_duration, day + "  |  ");
        views.setTextViewText(R.id.weather_temp, String.valueOf(Math.round(weather.getCurrentTemperature())) + (char) 0x00B0 + " C");
        views.setImageViewResource(R.id.weather_icon, returnImageRes(weather.getDescription(), weather.getIsDayTime()));
        ComponentName thisWidget = new ComponentName(context, PixelLikeWidget.class);
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
        googleApiClient.disconnect();
        Log.i(TAG,"Widget Updated");
    }

    private synchronized void googleCalendarWork(final Context context, final JobParameters parameters) {
        new Thread() {
            public void run() {
                try {
                    eventList = new GoogleCalendarAsync(context).execute().get();
                } catch (Exception e) {
                    Log.e(TAG,"Google Account Sign In Issue",e);
                }
                if (eventList == null || eventList.size() == 0) {
                    Log.i(TAG, "User has not yet Signed in.");
                } else {
                    Log.i(TAG, "Signed In");
                    processEventList(eventList, context, parameters);
                }
            }
        }.start();
    }

    private void processEventList(List<Event> eventList,Context context, JobParameters parameters) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pixel_like_widget);
        ComponentName thisWidget = new ComponentName(context, PixelLikeWidget.class);
        SimpleDateFormat date = new SimpleDateFormat("h:mm a");
        String startAndEnd;

        /* Process the Events which we have */
        int counter = eventList.size();
        if(counter == 0) {
            eventScheduled = false;
            overrideDate = false;
            return;
        }
        else if(counter == 1){
            /*
            * If whole day then set now
            * otherwise check if the event is ongoing or not if yes then set accordingly
            * if not then set only 30 minutes before the event
            */
            DateTime start = eventList.get(0).getStart().getDateTime();
            if (start != null) {
                /* Timed event will not generate null on .getStart().getDateTime() */
                DateTime end = eventList.get(0).getEnd().getDateTime();
                Long expire = end.getValue();
                Long timeInMS = start.getValue();
                Long now = System.currentTimeMillis();
                if(now < expire && now > (timeInMS-(30*60*1000))) {
                    startAndEnd = date.format(timeInMS) + " - ";
                    startAndEnd = startAndEnd + date.format(expire);
                    views.setTextViewText(R.id.date_or_event_duration, startAndEnd + "   | ");
                    eventScheduled = true;
                    overrideDate = true;
                    views.setTextViewText(R.id.event_display_widget, "Event : "+eventList.get(0).getSummary());
                } else {
                    eventScheduled = false;
                    overrideDate = false;
                }
            } else {
                /* All-day events don't have start times, so just set the event name */
                eventScheduled = true;
                overrideDate = false;
                views.setTextViewText(R.id.event_display_widget, eventList.get(0).getSummary());
            }
        } else {
            int index = 0;
            for(Event event : eventList) {
                DateTime start = event.getStart().getDateTime();
                if(start != null)
                    break;
                index++;
            }
            DateTime start = eventList.get(index).getStart().getDateTime();
            DateTime end = eventList.get(index).getEnd().getDateTime();
            Long expire = end.getValue();
            Long timeInMS = start.getValue();
            Long now = System.currentTimeMillis();
            startAndEnd = date.format(timeInMS) + " - ";
            startAndEnd = startAndEnd + date.format(expire);
            if(now < expire && now > (timeInMS-(30*60*1000))) {
                views.setTextViewText(R.id.date_or_event_duration, startAndEnd + "   | ");
                eventScheduled = true;
                overrideDate = true;
                views.setTextViewText(R.id.event_display_widget, "Event : "+eventList.get(index).getSummary());
            } else {
                eventScheduled = false;
                overrideDate = false;
            }
        }
        /* Call widget for update */
        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, views);
        Log.i(TAG,"Job Service Ends.");
        jobFinished(parameters, true);
    }

}
