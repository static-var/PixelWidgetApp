package com.dev.shreyansh.pixelwidget;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;

/**
 * Created by shreyansh on 3/8/18.
 */

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    private static final int JOB_WIDGET_UPDATE = 1001;

    public static void widgetData(Context context, int count) {

        /* Weather data will be refreshed every 1 hour */
        ComponentName serviceComponent = new ComponentName(context, UpdateWidgetJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_WIDGET_UPDATE, serviceComponent);
        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt("count",count);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            builder.setPeriodic(60 * 60 * 1000,  JobInfo.getMinFlexMillis());
//        } else {
//            builder.setMinimumLatency(50 * 10 * 1000); // wait at least
//            builder.setOverrideDeadline(60 * 60 * 1000); // maximum delay
//        }
        builder.setMinimumLatency(5 * 1000); // wait at least 10 Minutes
        builder.setOverrideDeadline(10 * 1000);
        builder.setExtras(bundle);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler)  context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

}
