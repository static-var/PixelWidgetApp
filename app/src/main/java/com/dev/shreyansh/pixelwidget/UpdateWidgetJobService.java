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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
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
        Util.widgetData(getApplicationContext());
        Log.i(TAG,  "JobService - onStartJob()");
        startWorkOnNewThread(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG,"JobService - onStopJob()");
        jobFinished(params, false);
        return true;
    }

    private void startWorkOnNewThread(JobParameters param) {
        /*
        *  Avoid ANR errors as JobService runs on Main Thread
        * So if something goes wrong the app will generate ANR
        * avoid ANR totally by using AsyncTask, as this will shit the work to background thread(s).
        */
        new DoWork().execute(getApplicationContext());
        jobFinished(param, false);
    }



    private static class DoWork extends AsyncTask<Context, Void, Void> {

        private Weather weather;
        private JSONObject weatherData;

        private GoogleApiClient googleApiClient;
        private Location location;
        public List<Event> eventList;

        private WeakReference<Context> contextWeakReference;

        private boolean eventScheduled = false;
        private boolean overrideDate = false;
        public static int COUNT = 0;

        @Override
        protected Void doInBackground(Context... params) {
            Log.i(TAG, "COUNT : "+COUNT);
            contextWeakReference = new WeakReference<>(params[0]);

            if (COUNT == 1)
                doWork();

            if (COUNT == 9)
                COUNT = 0;
            else
                COUNT++;

            googleCalendarWork(contextWeakReference.get());
            return null;
        }

        private void doWork() {
            doAllLocationStuff(contextWeakReference.get());
        }

        private void doAllLocationStuff(Context context) {
            if(checkNetwork(context)) {
                if(pingGoogle()) {
                    if (checkPlayServices(context)) {
                        googleApiClient = new GoogleApiClient.Builder(context)
                                .addApi(Awareness.API)
                                .addConnectionCallbacks(connectionCallbacks())
                                .build();
                        googleApiClient.connect();
                    }
                }
            }
        }

        @SuppressWarnings("deprecation")
        private GoogleApiClient.ConnectionCallbacks connectionCallbacks() {
            return new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    if (ContextCompat.checkSelfPermission(contextWeakReference.get(), Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                    } else {
                        Awareness.SnapshotApi.getLocation(googleApiClient).setResultCallback(new ResultCallback<LocationResult>() {
                            @Override
                            public void onResult(@NonNull LocationResult locationResult) {
                                if (locationResult.getStatus().isSuccess()) {
                                    location = locationResult.getLocation();
                                    Log.i(TAG, location.getLatitude() + "");
                                    fetchData(contextWeakReference.get(), location);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            };
        }

        private boolean checkNetwork(Context context) {
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

        /* Check if required version of play s is available in device or not */
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
    }

}

