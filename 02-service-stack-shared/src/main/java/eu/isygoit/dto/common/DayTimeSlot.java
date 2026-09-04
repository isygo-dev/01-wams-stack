package eu.isygoit.dto.common;

import java.time.DayOfWeek;
import java.time.LocalTime;

public interface DayTimeSlot<D> {

    DayOfWeek getDayOfWeek();

    D getStartTime();

    D getEndTime();

    String getDescription();
}