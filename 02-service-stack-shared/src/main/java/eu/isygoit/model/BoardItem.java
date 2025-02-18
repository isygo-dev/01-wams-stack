package eu.isygoit.model;


import java.util.Date;
import java.util.List;

/**
 * The interface Board item.
 *
 * @param <S> the type parameter
 */
public interface BoardItem<S> {


    /**
     * Gets id.
     *
     * @return the id
     */
    Long getId();

    /**
     * Gets code.
     *
     * @return the code
     */
    String getCode();

    /**
     * Gets state.
     *
     * @return the state
     */
    S getState();

    /**
     * Sets state.
     *
     * @param state the state
     */
    void setState(S state);

    /**
     * Gets item name.
     *
     * @return the item name
     */
    String getItemName();

    /**
     * Gets item image.
     *
     * @return the item image
     */
    String getItemImage();

    /**
     * Gets create date.
     *
     * @return the create date
     */
    Date getCreateDate();

    /**
     * Gets update date.
     *
     * @return the update date
     */
    Date getUpdateDate();

    /**
     * Gets image path.
     *
     * @return the image path
     */
    String getImagePath();


    /**
     * Gets events.
     *
     * @return the events
     */
    List<BoardEvent> getEvents();
}

