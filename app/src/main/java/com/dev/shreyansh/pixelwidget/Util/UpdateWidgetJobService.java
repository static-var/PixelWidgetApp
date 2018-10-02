package com.dev.shreyansh.pixelwidget.Util;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.dev.shreyansh.pixelwidget.UI.PixelLikeWidget;
import com.dev.shreyansh.pixelwidget.R;
import com.dev.shreyansh.pixelwidget.WeatherAndForecast.FetchAsync;
import com.dev.shreyansh.pixelwidget.WeatherAndForecast.Weather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
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

    @Override
    public boolean onStartJob(JobParameters params) {

        /* To give more flexibility to reschedule job, Refer Util#widgetData for more info */
        Util.widgetData(getApplicationContext());

        Log.i(TAG,  "JobService - onStartJob()");


        /* Run AsyncTask only if we have access to internet, otherwise they will stack up in the background */
        if(Util.pingGoogle() && Util.checkNetwork(getApplicationContext()))
            /*
             * As JobService runs on Main Thread
             * So if something goes wrong the app will generate ANR.
             * Avoid ANR totally by using AsyncTask, as this will shift the work to background thread(s).
             * Also this will make sure that the wakelock held by the Job is released as soon as possible.
             * Irrespective of when the AsyncTask is completed.
             */
            new DoWork(this).execute();

        /* AsyncTask will happen in background thread(s), we can pass false in that case */
        return false;
        /* Now the wakelock will be released */
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG,"JobService - onStopJob()");

        /* We don't need to reschedule the Job, pass false */
        return false;
    }


    /* AsyncTask to update Widget's data */
    private static class DoWork extends AsyncTask<Void, Void, Void> {
        private static final int UPDATE_DURATION = 10000;
        private static final int DISPLACEMENT = 10;
        private static final int FASTEST_UPDATE = 1000;

        private Weather weather;
        private JSONObject weatherData;

        private GoogleApiClient googleApiClient;
        private Location location;
        private LocationManager locationManager;
        private LocationListener locationListener;
        private List<Event> eventList;
        private LocationRequest locationRequest;

        private WeakReference<Context> contextWeakReference;

        private boolean eventScheduled = false;
        private boolean overrideDate = false;
        private static int COUNT = 0;

        private DoWork(Context context) {
            contextWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i(TAG, "COUNT : "+COUNT);

            /*
            * There's a check before we are starting this background task but as this is background thread,
            * there might be delay in execution from that point in time
            * So again check for internet again before proceeding.
            */

            if(Util.checkNetwork(contextWeakReference.get()) && Util.pingGoogle()) {
                if (COUNT == 1)
                    doAllLocationStuff(contextWeakReference.get());

                if (COUNT == 9)
                    COUNT = 0;
                else
                    COUNT++;

                googleCalendarWork(contextWeakReference.get());
            }
            return null;
        }

        private void doAllLocationStuff(Context context) {
            if(Util.checkNetwork(context)) {
                if(Util.pingGoogle()) {
                    prepareGoogleApiClient();
                }
            }
        }

        @SuppressWarnings("deprecation")
        private GoogleApiClient.ConnectionCallbacks connectionCallbacks() {
            return new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    if (ContextCompat.checkSelfPermission(contextWeakReference.get(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if(locationListener == null || googleApiClient == null || locationRequest == null)
                            prepareGoogleApiClient();
                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
                        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                        if (location == null) {
                            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
                        } else {
                            Log.i("---", String.valueOf(location.getLatitude())+","+location.getLongitude());
                            fetchData(contextWeakReference.get(), location);
                        }
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            };
        }

        private void fetchData(final Context context, final Location location) {
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

        private int returnImageRes(String weather, boolean isStillDay) {
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
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
            googleApiClient.disconnect();
            Log.i(TAG,"Widget Updated");
        }

        private synchronized void googleCalendarWork(final Context context) {
            new Thread() {
                public void run() {
                    try {
                        eventList = new GoogleCalendarAsync(context).execute().get();
                    } catch (Exception e) {
                        Log.e(TAG,"Google Account Sign In Issue",e);
                    }
                    if (eventList == null || eventList.size() == 0) {
                        Log.i(TAG, "No Events coming up.");
                    } else {
                        Log.i(TAG, "Signed In");
                        processEventList(eventList, context);
                    }
                }
            }.start();
        }

        private void processEventList(List<Event> eventList,Context context) {
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
        }

        private void prepareGoogleApiClient() {
            googleApiClient = new GoogleApiClient.Builder(contextWeakReference.get(), connectionCallbacks(), connectionFailedListener())
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();

            try {
                locationManager = (LocationManager) contextWeakReference.get().getSystemService(Context.LOCATION_SERVICE);

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(UPDATE_DURATION)
                            .setFastestInterval(FASTEST_UPDATE)
                            .setSmallestDisplacement(DISPLACEMENT);
                } else {
                    locationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                            .setInterval(UPDATE_DURATION)
                            .setFastestInterval(FASTEST_UPDATE)
                            .setSmallestDisplacement(DISPLACEMENT);
                }
            } catch (Exception e) {
                Log.i(TAG, "Airplane Mode");
                /* Phone is in airplane mode when location manager returns null */
            }

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location newLocation) {
                    try {
                        location = newLocation;
                        Log.i("onLocationChanged", String.valueOf(location.getLatitude())+","+location.getLongitude());
                    } catch (Exception e) {
                        //
                    }
                }
            };
        }

        /* This will let us know if connection fails and we can request to connect again */
        private GoogleApiClient.OnConnectionFailedListener connectionFailedListener() {
            return new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    Log.i(TAG, "Connection Failed");
                    if (googleApiClient.isConnected() || googleApiClient.isConnecting())
                        googleApiClient.disconnect();
                    googleApiClient.connect();
                }
            };
        }
    }


}

