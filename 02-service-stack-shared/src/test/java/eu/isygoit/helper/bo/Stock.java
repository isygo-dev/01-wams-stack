package eu.isygoit.helper.bo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * The type Stock.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Stock {
    @XmlElement(required = true)
    private String bookId;

    @XmlElement(required = true)
    private int quantity;

    @XmlElement(required = true)
    private String location;

    /**
     * Gets book id.
     *
     * @return the book id
     */
// Getters and setters
    public String getBookId() {
        return bookId;
    }

    /**
     * Sets book id.
     *
     * @param bookId the book id
     */
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    /**
     * Gets quantity.
     *
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets quantity.
     *
     * @param quantity the quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets location.
     *
     * @param location the location
     */
    public void setLocation(String location) {
        this.location = location;
    }
}
