package eu.isygoit.model;


import eu.isygoit.enums.IEnum;

import java.util.List;

/**
 * The interface Board event.
 *
 * @param <I> the type parameter
 */
public interface IBoardEvent<I extends IEnum> {

    /**
     * Gets id.
     *
     * @return the id
     */
    Long getId();

    /**
     * Gets title.
     *
     * @return the title
     */
    String getTitle();

    /**
     * Gets type.
     *
     * @return the type
     */
    I getType();

    /**
     * Gets calendar.
     *
     * @return the calendar
     */
    String getCalendar();

    /**
     * Gets event code.
     *
     * @return the event code
     */
    String getEventCode();

    /**
     * Gets participants.
     *
     * @return the participants
     */
    List<String> getParticipants();
}

