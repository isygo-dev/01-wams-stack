package eu.isygoit.model.json;

import com.fasterxml.jackson.databind.JsonNode;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.jakarta.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class JsonBasedEntity<I extends Serializable> extends AuditableEntity<I> implements IIdAssignable<I> {

    @Column(name = "ELEMENT_TYPE", nullable = false)
    private String elementType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode attributes;
}