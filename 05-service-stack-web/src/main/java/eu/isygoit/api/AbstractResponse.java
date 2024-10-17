package eu.isygoit.api;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * The type Abstract response.
 */
@Data
@SuperBuilder
public class AbstractResponse {

    private Boolean hasError;
    private Integer errorCode;
    private String errorMessage;
}
