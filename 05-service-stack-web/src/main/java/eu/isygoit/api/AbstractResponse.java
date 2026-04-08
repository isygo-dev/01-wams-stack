package eu.isygoit.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The type Abstract response.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AbstractResponse {

    private Boolean hasError;
    private Integer errorCode;
    private String errorMessage;
}
