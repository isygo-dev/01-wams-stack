package eu.isygoit.helper.bo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Bookstore.
 */
@XmlRootElement(name = "bookstore")
@XmlAccessorType(XmlAccessType.FIELD)
public class Bookstore {
    @XmlElement(name = "book", required = true)
    private List<Book> books = new ArrayList<>();

    @XmlElement(required = true)
    private Inventory inventory;

    /**
     * Gets books.
     *
     * @return the books
     */
// Getters and setters
    public List<Book> getBooks() {
        return books;
    }

    /**
     * Sets books.
     *
     * @param books the books
     */
    public void setBooks(List<Book> books) {
        this.books = books;
    }

    /**
     * Gets inventory.
     *
     * @return the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Sets inventory.
     *
     * @param inventory the inventory
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}


