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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.dev.shreyansh.pixelwidget.R;
import com.dev.shreyansh.pixelwidget.UI.PixelLikeWidget;

import java.text.SimpleDateFormat;

public class EventInfoReceiver extends BroadcastReceiver {

    private static final String TAG = EventInfoReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.e(TAG, "ENTERED");
        Bundle bundle = intent.getExtras();

        Events event = new Events();
        event.setTitle(bundle.getString(StaticStrings.B_TITLE));
        event.setLocation(bundle.getString(StaticStrings.B_LOCATION));
        event.setColor(bundle.getInt(StaticStrings.B_COLOR));
        event.setStartTime(bundle.getLong(StaticStrings.B_START_TIME));
        event.setEndTime(bundle.getLong(StaticStrings.B_END_TIME));
        event.setAllDay(bundle.getBoolean(StaticStrings.B_ALLDAY));

        setEvent(context, event);

        Toast.makeText(context, bundle.getString(StaticStrings.B_TITLE), Toast.LENGTH_SHORT).show();
        Log.e(TAG, "--------------- DONE -------------------");
    }

    private void setEvent(Context context, Events event) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pixel_like_widget);
        ComponentName thisWidget = new ComponentName(context, PixelLikeWidget.class);
        SimpleDateFormat date = new SimpleDateFormat("h:mm a");
    }
}
