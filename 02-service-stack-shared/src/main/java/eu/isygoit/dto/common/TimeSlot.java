package eu.isygoit.dto.common;

import java.time.DayOfWeek;
import java.time.LocalTime;

public interface TimeSlot {

    public DayOfWeek getDayOfWeek();
    public LocalTime getStartTime();
    public LocalTime getEndTime() ;

    public String getDescription();
}