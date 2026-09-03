package eu.isygoit.dto.common;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface TimeSlot {

    public DayOfWeek getDayOfWeek();
    public LocalDateTime getStartTime();
    public LocalDateTime getEndTime() ;

    public String getDescription();
}