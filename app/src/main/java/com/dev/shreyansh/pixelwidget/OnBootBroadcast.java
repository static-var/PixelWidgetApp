package com.dev.shreyansh.pixelwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by shreyansh on 3/8/18.
 */

public class OnBootBroadcast extends BroadcastReceiver {

    public static final String TAG = OnBootBroadcast.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Broadcast receiver for Pixel Weather App.");
        /* Schedule Jobs here */
        Util.widgetData(context,0);
    }
}
