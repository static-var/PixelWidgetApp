package com.dev.shreyansh.pixelwidget.Util;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.util.Collections;
import java.util.List;

/**
 * Created by shreyansh on 3/9/18.
 */

public class GoogleCalendarAsync extends AsyncTask<Void,Void,List<Event>> {
    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";
    private static final String TAG = GoogleCalendarAsync.class.getSimpleName();

    private Account account;
    private Context context;

    GoogleAccountCredential credential;
    private com.google.api.services.calendar.Calendar service = null;


    public GoogleCalendarAsync( Context context) {
        this.context = context;
    }

    @Override
    public List<Event> doInBackground(Void... params) {
        try {
            account = GoogleSignIn.getLastSignedInAccount(context).getAccount();
            credential = GoogleAccountCredential.usingOAuth2(context,
                            Collections.singleton(CALENDAR_SCOPE));
            credential.setSelectedAccount(account);

            service = new com.google.api.services.calendar.Calendar.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("Pixel Weather")
                    .build();

            Log.i(TAG, GoogleSignIn.getLastSignedInAccount(context).getDisplayName());

            return getEventsFromCal(service);


        } catch (Exception e) {
            Log.i(TAG, "Error", e);
            return null;
        }
    }

    private List<Event> getEventsFromCal(com.google.api.services.calendar.Calendar mService){
        DateTime now = new DateTime(System.currentTimeMillis());
        DateTime max = new DateTime(System.currentTimeMillis() + (24*60*60*1000) );

        try {
            /* Events from now to 24 hours from now */
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setTimeMax(max)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            for(Event e : events.getItems()){
                Log.i(TAG, e.getSummary());
            }

            /*
            * An event which is full day event
            * will return null on .getDateTime()
            * and where as
            * An event which is not a full day event
            * will return null on .getDate()
            */

            return events.getItems();
        } catch (Exception e) {
            Log.i(TAG,"Error While Fetching Data", e);
            return null;
        }
    }


}
