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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* Pass this class a list of all the events and call appropriate method to get the event which has to be put up on the widget. */
public class EventFilter {

    private static Comparator<Events> TimeBased = new Comparator<Events>() {
        @Override
        public int compare(Events o1, Events o2) {
            return (int) (o1.getStartTime() - o2.getStartTime());
        }
    };

    private List<Events> allEvents;
    private boolean reminderOverEvent;
    private List<Events> timeFiltered;
    private List<Events> allDayEvents;

    public EventFilter(List<Events> allEvents, boolean reminderOverEvent) {
        this.allEvents = allEvents;
        this.reminderOverEvent = reminderOverEvent;
        allDayEvents = new ArrayList<Events>();
    }

    public Events returnEvent() {
        if (allEvents.size() == 0) {
            return null;
        } else {
            if (allEvents.size() == 1)
                return allEvents.get(0);
            else {
                Collections.sort(allEvents, TimeBased);
                int index = 0;

                /* Separate All day events from this list */
                for (Events single : allEvents) {
                    index++;
                    if(single.isAllDay()) {
                        allDayEvents.add(single);
                        allEvents.remove(index);
                        /*
                         * After deleting item from ArrayList the ArrayList rearranges itself
                         * so when keeping track of indexes, make sure that when you remove an item from ArrayList
                         * Also decrease the value of index by 1.
                         */
                        index--;
                    }
                }

                /*
                 * Now that we have 2 different lists,
                 * we can prioritise all day events less over limited duration events.
                 */

                /* Now just return the top most event of which ever list needed. */
                if(allEvents.size() == 0) {
                    return allDayEvents.get(0);
                } else {
                    /* Sort the list again, Just in case. */
                    Collections.sort(allEvents, TimeBased);
                    return allEvents.get(0);
                }
            }
        }
    }
}
