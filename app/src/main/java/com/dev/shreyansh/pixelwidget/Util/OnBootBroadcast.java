package com.dev.shreyansh.pixelwidget.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.calendar.CalendarScopes;

/**
 * Created by shreyansh on 3/8/18.
 */

public class OnBootBroadcast extends BroadcastReceiver {

    public static final String TAG = OnBootBroadcast.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Broadcast receiver for Pixel Weather App.");
        /* Schedule Job if the user has logged in and if has granted us permission to access calendar */
        GoogleSignInAccount account = null;
        if (GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(context), new Scope(CalendarScopes.CALENDAR)))
            account = GoogleSignIn.getLastSignedInAccount(context);
        if(account != null) {
            Log.i(TAG, "Lol");
            Util.widgetData(context);
        }
    }
}
