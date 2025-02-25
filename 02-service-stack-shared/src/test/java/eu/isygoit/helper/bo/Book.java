package eu.isygoit.helper.bo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * The type Book.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Book {
    @XmlAttribute(required = true)
    private String id;

    @XmlAttribute(required = true)
    private String category;

    @XmlElement(required = true)
    private String title;

    @XmlElement(required = true)
    private Author author;

    @XmlElement(required = true)
    private int publicationYear;

    @XmlElement(required = true)
    private Price price;

    @XmlElement(required = true)
    private String description;

    @XmlElement(required = true)
    private Reviews reviews;

    /**
     * Gets id.
     *
     * @return the id
     */
// Getters and setters
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets category.
     *
     * @param category the category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets author.
     *
     * @return the author
     */
    public Author getAuthor() {
        return author;
    }

    /**
     * Sets author.
     *
     * @param author the author
     */
    public void setAuthor(Author author) {
        this.author = author;
    }

    /**
     * Gets publication year.
     *
     * @return the publication year
     */
    public int getPublicationYear() {
        return publicationYear;
    }

    /**
     * Sets publication year.
     *
     * @param publicationYear the publication year
     */
    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    /**
     * Gets price.
     *
     * @return the price
     */
    public Price getPrice() {
        return price;
    }

    /**
     * Sets price.
     *
     * @param price the price
     */
    public void setPrice(Price price) {
        this.price = price;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets reviews.
     *
     * @return the reviews
     */
    public Reviews getReviews() {
        return reviews;
    }

    /**
     * Sets reviews.
     *
     * @param reviews the reviews
     */
    public void setReviews(Reviews reviews) {
        this.reviews = reviews;
    }
}
