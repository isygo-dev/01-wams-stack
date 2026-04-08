package eu.isygoit.model.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.helper.JsonBasedEntityHelper;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.jakarta.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
@Getter
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

    /*
    First initialization: elementType is permitted
     */
    public final void init(String elementType, JsonNode attributes) {
        baseInit(elementType, attributes);
        doInit(elementType, attributes);
    }

    protected void baseInit(String elementType, JsonNode attributes) {
        if (elementType == null || attributes == null) {
            throw new IllegalArgumentException("elementType and attributes must not be null");
        }
        if (this.elementType != null || this.attributes != null) {
            throw new IllegalStateException("Entity already initialized");
        }
        this.elementType = elementType;
        this.attributes = attributes;
    }

    protected void doInit(String elementType, JsonNode attributes) {
    }

    /*
    update: elementType not permitted
     */
    public final void update(JsonNode attributes) {
        baseUpdate(attributes);
        doUpdate(attributes);
    }

    protected void baseUpdate(JsonNode attributes) {
        if (this.elementType == null) {
            throw new IllegalStateException("Entity not initialized");
        }
        this.attributes = attributes;
    }

    protected void doUpdate(JsonNode attributes) {
    }

    /**
     * To json entity json based entity.
     *
     * @param <T>          the type parameter
     * @param element      the element
     * @param elementType  the element type
     * @param objectMapper the object mapper
     * @return the json based entity
     */
    public final <T extends IIdAssignable<UUID> & JsonElement<UUID>, E extends JsonBasedEntity<?>> E toJsonEntity(
            T element, String elementType, ObjectMapper objectMapper
    ) {
        E entity = (E) JsonBasedEntityHelper.toJsonEntity(element, elementType, this.getClass(), objectMapper);
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
    public final <T extends IIdAssignable<UUID> & JsonElement<UUID>> T toJsonElement(
            Class<T> jsonElementClass, ObjectMapper objectMapper
    ) {
        T element = JsonBasedEntityHelper.toJsonElement(this, jsonElementClass, objectMapper);
        return afterToJsonElement(element);
    }

    /**
     * After to json entity json based entity.
     *
     * @param entity the entity
     * @return the json based entity
     */
    public <T extends JsonBasedEntity<?>> T afterToJsonEntity(T entity) {
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