package eu.isygoit.helper.bo;

import jakarta.xml.bind.annotation.*;

import java.util.Date;

/**
 * The type Review.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Review {
    @XmlAttribute
    private int rating;

    @XmlElement(required = true)
    private String reviewer;

    @XmlElement(required = true)
    private String comment;

    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    private Date date;

    /**
     * Gets rating.
     *
     * @return the rating
     */
// Getters and setters
    public int getRating() {
        return rating;
    }

    /**
     * Sets rating.
     *
     * @param rating the rating
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Gets reviewer.
     *
     * @return the reviewer
     */
    public String getReviewer() {
        return reviewer;
    }

    /**
     * Sets reviewer.
     *
     * @param reviewer the reviewer
     */
    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    /**
     * Gets comment.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets comment.
     *
     * @param comment the comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets date.
     *
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets date.
     *
     * @param date the date
     */
    public void setDate(Date date) {
        this.date = date;
    }
}
