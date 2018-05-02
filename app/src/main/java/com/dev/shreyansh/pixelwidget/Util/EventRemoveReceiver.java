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
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class EventRemoveReceiver extends BroadcastReceiver {

    private static final String TAG = EventRemoveReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: After removing the event, check if there's another event or all day event, otherwise restart the jobService

        Toast.makeText(context, "Remove", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "--------------- DONE -------------------");
    }
}
