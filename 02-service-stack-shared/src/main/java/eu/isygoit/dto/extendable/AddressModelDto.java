package eu.isygoit.dto.extendable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * The type Address model dto.
 *
 * @param <T> the type parameter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class AddressModelDto<T extends Serializable> extends AuditableDto<T> {

    private String country;
    private String state;
    private String city;
    private String street;
    private String zipCode;
    private String additionalInfo;
    private Double latitude;
    private Double longitude;
    private List<String> compAddress;

    /**
     * Format string.
     *
     * @return the string
     */
    public String format() {
        return new StringBuilder(additionalInfo).append("-").append(street).append("\n")
                .append(city).append("-").append(state).append("_n")
                .append(zipCode).append("-").append(country)
                .toString();
    }
}
