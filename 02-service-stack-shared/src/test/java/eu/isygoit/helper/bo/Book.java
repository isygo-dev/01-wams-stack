package eu.isygoit.helper.bo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Book.
 */

@Getter
@Setter
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
}
