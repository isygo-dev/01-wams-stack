package eu.isygoit.helper.bo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

import java.math.BigDecimal;

/**
 * The type Price.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Price {
    @XmlValue
    private BigDecimal value;

    @XmlAttribute(required = true)
    private String currency;

    /**
     * Gets value.
     *
     * @return the value
     */
// Getters and setters
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Gets currency.
     *
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets currency.
     *
     * @param currency the currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
