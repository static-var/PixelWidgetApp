package com.dev.shreyansh.pixelwidget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

/**
 * Created by shreyansh on 3/18/18.
 */

public class AwarenessLocation implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = AwarenessLocation.class.getSimpleName();

    private GoogleApiClient client;
    private Context context;

    private double latitude;
    private double longitude;

    public AwarenessLocation(Context context){
        this.context = context;
        client = new GoogleApiClient.Builder(context)
                .addApi(Awareness.API)
                .build();
        client.connect();
        if(getLocation()) {
            Log.i(TAG, "Location fetched successfully");
        }
    }

    private boolean getLocation() {
        /*
        * Check for permission first and then move forward
        */
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            /* If we have permission, then request for location */
            Awareness.SnapshotApi.getLocation(client)
                    .setResultCallback(new ResultCallback<LocationResult>() {
                        @Override
                        public void onResult(@NonNull LocationResult locationResult) {
                            if (!locationResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Could not get location.");
                                return;
                            }
                            Location location = locationResult.getLocation();

                            /* Set latitude and longitude values */
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            Log.i(TAG," "+latitude+", "+longitude);
                        }
                    });
            return true;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,"Unable to get Location from Awareness API");
    }

    private double getLatitude() {
        return latitude;
    }

    private double getLongitude() {
        return longitude;
    }


}
