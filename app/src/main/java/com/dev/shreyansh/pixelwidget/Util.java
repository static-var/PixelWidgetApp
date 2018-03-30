package com.dev.shreyansh.pixelwidget;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;

/**
 * Created by shreyansh on 3/8/18.
 */

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    private static final int JOB_WIDGET_UPDATE = 1001;
    private static final int REFRESH_INTERVAL = 5 * 1000;

    public static void widgetData(Context context) {

        /* Weather data will be refreshed every 1 hour */
        ComponentName serviceComponent = new ComponentName(context, UpdateWidgetJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_WIDGET_UPDATE, serviceComponent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setMinimumLatency(REFRESH_INTERVAL);

            /*
            * When UpdateWidgetJobService#onStartService will call this function
            * Check if we have access to internet or not
            * If we have internet, then only set OverrideDeadline
            * As this property will run the JobService even when there's no network
            * or when phone is in doze more
            * This check will save some juice.
            */
            if(checkNetwork(context) && pingGoogle())
                builder.setOverrideDeadline(REFRESH_INTERVAL);
        } else {
            builder.setPeriodic(REFRESH_INTERVAL);
        }
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setRequiresCharging(false); // we don't care if the device is charging or not

        JobScheduler jobScheduler = (JobScheduler)  context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    public static boolean checkNetwork(Context context) {
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

    public static boolean pingGoogle() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            Log.i(TAG, "Error", e);
            return false;
        }
    }

}
