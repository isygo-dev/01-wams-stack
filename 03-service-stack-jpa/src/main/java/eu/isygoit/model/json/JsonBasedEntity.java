package eu.isygoit.model.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.helper.JsonBasedEntityHelper;
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
import java.util.UUID;

/**
 * The type Json based entity.
 *
 * @param <I> the type parameter
 */
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

    /**
     * To json entity json based entity.
     *
     * @param <T>          the type parameter
     * @param element      the element
     * @param elementType  the element type
     * @param objectMapper the object mapper
     * @return the json based entity
     */
    public final <T extends IIdAssignable<UUID> & JsonElement<UUID>>
    JsonBasedEntity toJsonEntity(T element, String elementType, ObjectMapper objectMapper) {
        JsonBasedEntity entity = JsonBasedEntityHelper.toJsonEntity(element, elementType, this.getClass(), objectMapper);
        return afterToJsonEntity(entity);
    }

    /**
     * To json element t.
     *
     * @param <T>              the type parameter
     * @param jsonElementClass the json element class
     * @param objectMapper     the object mapper
     * @return the t
     */
    public final <T extends IIdAssignable<UUID> & JsonElement<UUID>>
    T toJsonElement(Class<T> jsonElementClass, ObjectMapper objectMapper) {
        T element = JsonBasedEntityHelper.toJsonElement(this, jsonElementClass, objectMapper);
        return afterToJsonElement(element);
    }

    /**
     * After to json entity json based entity.
     *
     * @param entity the entity
     * @return the json based entity
     */
    public JsonBasedEntity afterToJsonEntity(JsonBasedEntity entity) {
        return entity;
    }

    /**
     * After to json element t.
     *
     * @param <T>     the type parameter
     * @param element the element
     * @return the t
     */
    public <T extends IIdAssignable<UUID> & JsonElement<UUID>> T afterToJsonElement(T element) {
        return element;
    }
}