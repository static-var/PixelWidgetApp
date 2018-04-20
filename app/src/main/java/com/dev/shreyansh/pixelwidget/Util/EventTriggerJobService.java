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

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

/* Do not schedule this Job if the Device's API is below 24 | Nougat */
public class EventTriggerJobService extends JobService {

    /*
    * For this JobService to trigger, it's mandatory to have a widget.
    * This JobService class is triggered when any changes in Calendar Provider occurs.
    * Schedule a AsyncTask to POSSIBLY update the widget with new event.
    */
    private static final String TAG = EventTriggerJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {

        /*
        * JobService runs on UI thread, and there's no point to update the widget on UI thread.
        * Start Executing the AsyncTask, to do all the work on worker thread.
        */

        new DoWork(this).execute();

        /*
         * We don't need to reschedule the job as it is trigger based neither do we require UI thread
         * Returning 'false' here helps to release wakelock held by the app early.
        */
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        /*
        * This function is most likely not going to be called as we returned 'false' in 'onStartJob' function
        * to tell that the Job is done, and passing 'false' here also means that the job is done.
        */
        return false;
    }

    private static class DoWork extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> weakReferenceContext;

        private DoWork(Context context) {
            weakReferenceContext = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            /* Start a AsyncTask to update the widget */
            return null;
        }
    }
}
