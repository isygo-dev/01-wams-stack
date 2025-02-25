package eu.isygoit.helper.bo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Inventory.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Inventory {
    @XmlElement(name = "stock")
    private List<Stock> stockList = new ArrayList<>();

    /**
     * Gets stock list.
     *
     * @return the stock list
     */
// Getters and setters
    public List<Stock> getStockList() {
        return stockList;
    }

    /**
     * Sets stock list.
     *
     * @param stockList the stock list
     */
    public void setStockList(List<Stock> stockList) {
        this.stockList = stockList;
    }
}
