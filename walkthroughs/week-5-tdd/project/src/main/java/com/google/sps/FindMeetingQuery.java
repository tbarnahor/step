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
import java.util.Set;
import java.util.HashSet;

 
public final class FindMeetingQuery {
 
   /**
   * Given a meeting request and the other events that occur on that day, returns all the time slots when the meeting could happen, based on:
   * If one or more time slots exists so that both mandatory and optional attendees can attend, it returns those time slots.
   * Otherwise, returns the time slots that fit just the mandatory attendees.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getDuration() >= TimeRange.WHOLE_DAY.duration()) {
        return Arrays.asList();
    }
    List<TimeRange> occupiedSlotsMandatory = getOccupiedSlots(events, request.getAttendees(),  new ArrayList<TimeRange>());
    List<TimeRange> occupiedSlotsAll = getOccupiedSlots(events, request.getOptionalAttendees(), new ArrayList<TimeRange>(occupiedSlotsMandatory));
    List<TimeRange> availableSlotsAllAttendees = getOptionalSlots(occupiedSlotsAll, request);
    if (availableSlotsAllAttendees.isEmpty()) {
        return getOptionalSlots(occupiedSlotsMandatory, request);
    }  
    return availableSlotsAllAttendees;
  } 
 
    
    /* Recieves a collection of events, and a list of requested attendees and a list of occupied slots and adds to that list all the slots occupied by the requested attendies sorted by start time in ascending order*/
    private List<TimeRange> getOccupiedSlots(Collection<Event> events, Collection<String> requestedAttendees, List<TimeRange> occupiedSlots) {
        Set<String> requestedAttendeesSet = new HashSet<>(requestedAttendees);
        for (Event event: events) {
            for (String attendee: event.getAttendees()) {
                if (requestedAttendeesSet.contains(attendee)) {
                    occupiedSlots.add(event.getWhen());
                    break;
                }
            }
        }
        Collections.sort(occupiedSlots, TimeRange.ORDER_BY_START);
        return occupiedSlots;
    }


    /* Recieves a list of occupied slots sorted by start time and returns all possible meeting slots */
    private List<TimeRange> getOptionalSlots(List<TimeRange> occupiedSlots, MeetingRequest request) {
        if (occupiedSlots.isEmpty()) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }
        List<TimeRange> meetingSlots =  new ArrayList <>(); 
        int start = TimeRange.START_OF_DAY;
        int end =  occupiedSlots.get(0).start();
        int slotDuration = end - start;
        for (int i = 0;  i < occupiedSlots.size(); i++) {
            if (legalSlot(slotDuration, request.getDuration())) {
                meetingSlots.add(TimeRange.fromStartEnd(start, end, false));
            }
            if (occupiedSlots.get(i).end() > start){
                start = occupiedSlots.get(i).end();
            }
            // If this is the last occupied slot, end of meeting slot will be the end of the day
            if (i == occupiedSlots.size() - 1) {
                end =  TimeRange.END_OF_DAY;
            } else {
                end = occupiedSlots.get(i + 1).start();
            }
            slotDuration = end - start;
        }
        if (legalSlot(slotDuration, request.getDuration())) {
            meetingSlots.add(TimeRange.fromStartEnd(start, end, true));
        } 
        return meetingSlots;
    }
 
    private boolean legalSlot(int slotDuration, long duration) {
        return (slotDuration > 0 && slotDuration >= duration);
    }
 
}
