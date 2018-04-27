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

package com.dev.shreyansh.pixelwidget.Util;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.CalendarContract;
import android.util.Log;

/**
 * Created by shreyansh on 3/8/18.
 */

public class Util {
    public static final int JOB_WIDGET_UPDATE = 1001;
    private static final String TAG = Util.class.getSimpleName();
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

            if (checkNetwork(context) && pingGoogle())
                builder.setOverrideDeadline(REFRESH_INTERVAL);
        } else {
            builder.setPeriodic(REFRESH_INTERVAL);
        }

        /* We will check in JobService if we have internet access or not, as we have some offline work to do as well */
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setRequiresCharging(false);
        builder.setRequiresDeviceIdle(false);
        builder.setPersisted(true); /* Should start at boot */
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    public static void cancelJob(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JOB_WIDGET_UPDATE);
    }

    public static boolean checkNetwork(Context context) {
        /* Check the network status */
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            Log.e(TAG, "Error");
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
