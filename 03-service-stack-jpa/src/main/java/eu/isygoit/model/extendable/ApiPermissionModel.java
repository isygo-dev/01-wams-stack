package eu.isygoit.model.extendable;

import eu.isygoit.enums.IEnumBinaryStatus;
import eu.isygoit.enums.IEnumRequest;
import eu.isygoit.model.jakarta.AuditableEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaConstantSize;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;

/**
 * The type Api permission model.
 *
 * @param <T> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class ApiPermissionModel<T extends Serializable> extends AuditableEntity<T> {

    @Column(name = ComSchemaColumnConstantName.C_SERVICE, length = ComSchemaConstantSize.S_NAME, nullable = false)
    private String serviceName;

    @Column(name = ComSchemaColumnConstantName.C_OBJECT, length = ComSchemaConstantSize.S_NAME, nullable = false)
    private String object;

    @Column(name = ComSchemaColumnConstantName.C_METHOD, length = ComSchemaConstantSize.S_NAME, nullable = false)
    private String method;

    @Enumerated(EnumType.STRING)
    @Column(name = ComSchemaColumnConstantName.C_RQ_TYPE, length = IEnumRequest.STR_ENUM_SIZE, nullable = false)
    private IEnumRequest.Types rqType;

    @Column(name = ComSchemaColumnConstantName.C_PATH, length = ComSchemaConstantSize.L_DESCRIPTION, nullable = false)
    private String path;

    @Column(name = ComSchemaColumnConstantName.C_DESCRIPTION, length = ComSchemaConstantSize.DESCRIPTION)
    private String description;

    @Builder.Default
    @ColumnDefault("'ENABLED'")
    @Enumerated(EnumType.STRING)
    @Column(name = ComSchemaColumnConstantName.C_STATUS, length = IEnumBinaryStatus.STR_ENUM_SIZE, nullable = false)
    private IEnumBinaryStatus.Types status = IEnumBinaryStatus.Types.ENABLED;

    /**
     * Gets role.
     *
     * @return the role
     */
    public String getRole() {
        return new StringBuilder()
                .append(this.getServiceName()).append(".")
                .append(this.getRqType().action()).append(".")
                .append(this.getObject()).append(".")
                //.append(this.getMethod())
                .toString().toLowerCase();
    }
}
