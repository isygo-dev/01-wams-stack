package eu.isygoit.dto.common;

import java.time.DayOfWeek;

public interface DayTimeSlot<D, S> {

    DayOfWeek getDayOfWeek();

    D getStartTime();

    D getEndTime();

    String getTitle();

    String getDescription();

    String getLocation();

    String getOwner();

    S getStatus();
}