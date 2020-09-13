// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
        return Arrays.asList();
    }
    ArrayList<TimeRange> occupiedSlotsMandatory =  new ArrayList<TimeRange>();
    ArrayList<TimeRange> occupiedSlotsAll =  new ArrayList<TimeRange>();
    boolean foundAnyAttendee;
    for (Event event: events) {
        foundAnyAttendee = false;
        for (String attendee: event.getAttendees()) {
            if (request.getAttendees().contains(attendee)) {
                occupiedSlotsMandatory.add(event.getWhen());
                occupiedSlotsAll.add(event.getWhen());
                break;
            }
            if (foundAnyAttendee == false && request.getOptionalAttendees().contains(attendee)) {
                occupiedSlotsAll.add(event.getWhen());
                foundAnyAttendee = true;
            }
        }
    }
    ArrayList<TimeRange> SlotsForAllAttendees = getOptionalSlots(occupiedSlotsAll, request);
    if (SlotsForAllAttendees.isEmpty()) {
        return getOptionalSlots(occupiedSlotsMandatory, request);
    }  
    return SlotsForAllAttendees;
  }

    public ArrayList<TimeRange> getOptionalSlots(ArrayList<TimeRange> occupiedSlots, MeetingRequest request) {
        ArrayList<TimeRange> meetingSlots =  new ArrayList<>(); 
        if (occupiedSlots.isEmpty()) {
            meetingSlots.add(TimeRange.WHOLE_DAY);
        } else {
            Collections.sort(occupiedSlots, TimeRange.ORDER_BY_START);
            int start = TimeRange.START_OF_DAY;
            int end =  occupiedSlots.get(0).start();
            int slotDuration = end - start;
            for (int i = 0 ; i < occupiedSlots.size(); i++) {
                if (legalSlot(slotDuration, request)) {
                    meetingSlots.add(TimeRange.fromStartEnd(start, end, false));
                }
                if (occupiedSlots.get(i).end() > start){
                    start = occupiedSlots.get(i).end();
                }
                //if this is the last occupied slot, end of meeting slot will be the end of the day
                if (i == occupiedSlots.size() - 1) {
                    end =  TimeRange.END_OF_DAY;
                } else {
                    end = occupiedSlots.get(i+1).start();
                }
                slotDuration = end - start;
            }
            if (legalSlot(slotDuration, request)) {
                meetingSlots.add(TimeRange.fromStartEnd(start, end, true));
            } 
        }
        return meetingSlots;
    }

    public boolean legalSlot(int slotDuration, MeetingRequest request) {
        return (0 < slotDuration &&  slotDuration >= request.getDuration());
    }


}


