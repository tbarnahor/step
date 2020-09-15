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
 
   /**
   * Given a meeting request and the other events that occur on that day, returns all the time slots when the meeting could happen, based on:
   * If one or more time slots exists so that both mandatory and optional attendees can attend, it returns those time slots.
   * Otherwise, returns the time slots that fit just the mandatory attendees.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getDuration() >= TimeRange.WHOLE_DAY.duration()) {
        return Arrays.asList();
    }
    List<TimeRange> occupiedSlotsMandatory = new ArrayList<>(); 
    List<TimeRange> occupiedSlotsOptinal = new ArrayList<>(); 
    List<TimeRange> occupiedSlotsAll =  new ArrayList<>();
    boolean foundOptinalAttendee, foundMandatoryAttendee;
    for (Event event: events) {
        foundOptinalAttendee = foundMandatoryAttendee = false;
        for (String attendee: event.getAttendees()) {
            if (foundMandatoryAttendee == false && request.getAttendees().contains(attendee)) {
                occupiedSlotsMandatory.add(event.getWhen());
                foundMandatoryAttendee = true;
            } else if (foundOptinalAttendee == false && request.getOptionalAttendees().contains(attendee)) {
                occupiedSlotsOptinal.add(event.getWhen());
                foundOptinalAttendee = true;
            } else {
                break;
            }
        }
    }
    Collections.sort(occupiedSlotsMandatory, TimeRange.ORDER_BY_START);
    Collections.sort(occupiedSlotsOptinal, TimeRange.ORDER_BY_START);
    mergeSortedLists(occupiedSlotsMandatory, occupiedSlotsOptinal, occupiedSlotsAll);
    List<TimeRange> slotsForAllAttendees = getOptionalSlots(occupiedSlotsAll, request);
    if (slotsForAllAttendees.isEmpty()) {
        return getOptionalSlots(occupiedSlotsMandatory, request);
    }  
    return slotsForAllAttendees;
  } 
 
    /* Recieves a list of occupied slots sorted by start time and returns all possible meeting slots */
    public List<TimeRange> getOptionalSlots(List<TimeRange> occupiedSlots, MeetingRequest request) {
        List<TimeRange> meetingSlots =  new ArrayList<>(); 
        if (occupiedSlots.isEmpty()) {
            meetingSlots.add(TimeRange.WHOLE_DAY);
        } else {
            Collections.sort(occupiedSlots, TimeRange.ORDER_BY_START);
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
        }
        return meetingSlots;
    }
 
    public boolean legalSlot(int slotDuration, long duration) {
        return (slotDuration > 0 && slotDuration >= duration);
    }
 
    public void mergeSortedLists(List<TimeRange> lst1, List<TimeRange> lst2, List<TimeRange> result) { 
        int i = 0, j = 0, k = 0; 
        while (i < lst1.size() && j < lst2.size()) { 
            if (lst1.get(i).start() < lst2.get(j).start() ) {
                result.add(k++, lst1.get(i++)); 
            } else
                result.add(k++, lst2.get(j++)); 
        } 
        // Store remaining elements of first list 
        while (i < lst1.size()) 
            result.add(k++, lst1.get(i++)); 
        // Store remaining elements of second list 
        while (j < lst2.size()) 
            result.add(k++, lst2.get(j++)); 
    } 
}
