package com.dev.shreyansh.pixelwidget;

import android.Manifest;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PixelLocationWidget";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;

    private final int PERMISSION_ACCESS_FINE_LOCATION = 0;

    private String currentDate;
    private SimpleDateFormat sdf;

    private JSONObject weatherData;
    private Weather weather;

    /* UI Elements */
    TextView currentDateTV;
    TextView currentBigTemp;
    TextView currentCity;
    TextView networkStatus;

    /* Network state */
    boolean isWiFi;
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Bind necessary UI Elements with our code */
        bindUI();

        /* Check the network status */
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        Log.i(TAG, String.valueOf(isConnected));

        isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        Log.i(TAG, String.valueOf(isWiFi));

        if(! (isConnected || isWiFi))
            networkStatus.setVisibility(View.VISIBLE);
        else
            networkStatus.setVisibility(View.GONE);


        /* Workaround to change the font of ActionBar */
        try {
            TextView tv = new TextView(getApplicationContext());
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(lp);
            tv.setText("Pixel Weather");
            tv.setTextSize(20);
            tv.setTextColor(Color.parseColor("#FFFFFF"));

            /* */
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Product Sans Regular.ttf");
            tv.setTypeface(tf);
            this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            this.getSupportActionBar().setCustomView(tv);

            /* Remove shadow between Action bar and other layouts */
            this.getSupportActionBar().setElevation(0);
        } catch (NullPointerException e) {
            /* If there's a null pointer exception generated,
             * then just use setTitle and
             * screw Typeface in that case */
            Log.e(TAG,e.toString());
            this.getSupportActionBar().setTitle("Pixel Weather");
        }

        /* Request for location access if we don't have access already */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            /* if we have access to location then add a listener and get location */
            if (locationManager == null || locationListener == null) {
                initialiseManagerListener();
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }

        /* Find Current day of the week
         * Add HTML style to it */
        sdf = new SimpleDateFormat("EEEE");
        currentDate = "<b>"+sdf.format(System.currentTimeMillis())+"</b>";
        sdf = new SimpleDateFormat("dd");
        currentDate = currentDate+", "+sdf.format(System.currentTimeMillis());
        currentDateTV.setText(Html.fromHtml(currentDate));



        /* Get Location to begin task */


        /* Request for GPS location if GPS is enabled
         * tempLocation can still be null
         * If the provider is not available
         * or if device is far from the last known location */
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && location == null){
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if(location == null) {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }


        if(location != null) {
            try {
                /* Get weather data  */
                ProgressDialog pd = new ProgressDialog(this);
                pd.setTitle("Fetching");
                pd.setMessage("Fetching weather detail.");
                pd.setIndeterminate(true);
                pd.setCancelable(false);
                pd.show();
                weatherData = new FetchAsync().execute(location.getLatitude(), location.getLongitude()).get();
                weather = new Weather(weatherData);
                pd.dismiss();
                /* Set Data in UI */
                currentBigTemp.setText(String.valueOf(weather.getCurrentTemprature())+(char) 0x00B0);
                currentCity.setText(Html.fromHtml(weather.getCityName()+", <b>"+weather.getCountryCode()+"</b>"));
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.i(TAG,String.valueOf(location));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /* Set listeners if location is granted */
                    initialiseManagerListener();
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    /* Initialize listener and manager */
    public void initialiseManagerListener() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    weatherData = new FetchAsync().execute(location.getLatitude(), location.getLongitude()).get();
                    weather = new Weather(weatherData);
                } catch (Exception e) {
                    //
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    public void bindUI() {
        currentDateTV = findViewById(R.id.todays_date);
        currentBigTemp = findViewById(R.id.current_big_temp);
        currentCity = findViewById(R.id.city_name);
        networkStatus = findViewById(R.id.network_status);
    }
}
