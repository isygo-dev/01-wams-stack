package eu.isygoit.dto.common;

import java.time.DayOfWeek;

public interface DayTimeSlot<D> {

    DayOfWeek getDayOfWeek();

    D getStartTime();

    D getEndTime();

    String getDescription();

    String getLocation();

    String getOwner();
}