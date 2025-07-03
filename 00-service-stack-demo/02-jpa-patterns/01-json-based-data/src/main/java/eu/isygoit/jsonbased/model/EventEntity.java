package eu.isygoit.jsonbased.model;

import com.fasterxml.jackson.databind.JsonNode;
import eu.isygoit.jsonbased.common.JsonBasedEntity;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "EVENTS")
public class EventEntity implements ITenantAssignable, IIdAssignable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "events_seq_generator")
    @SequenceGenerator(name = "events_seq_generator", sequenceName = "events_seq", allocationSize = 1)
    private Long id;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private String tenant;

    @Column(name = "ELEMENT_TYPE", nullable = false)
    private String elementType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode attributes;
}