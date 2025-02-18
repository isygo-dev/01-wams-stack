package eu.isygoit.model.extendable;

import eu.isygoit.model.jakarta.Auditable;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.model.schema.ComSchemaFkConstantName;
import eu.isygoit.model.schema.ComSchemaTableConstantName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * The type Address model.
 *
 * @param <I> the type parameter
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AddressModel<I extends Serializable> extends Auditable<I> {


    @ElementCollection
    @CollectionTable(name = ComSchemaTableConstantName.T_COMP_ADDRESS,
            joinColumns = @JoinColumn(name = ComSchemaColumnConstantName.C_ADDRESS,
                    referencedColumnName = ComSchemaColumnConstantName.C_ID,
                    foreignKey = @ForeignKey(name = ComSchemaFkConstantName.FK_COMP_ADR_REF_ADR)))
    @Column(name = ComSchemaColumnConstantName.C_COMP_ADDRESS_DESC)
    private List<String> compAddress;
    @Column(name = ComSchemaColumnConstantName.C_COUNTRY_NAME)
    private String country;
    @Column(name = ComSchemaColumnConstantName.C_STATE)
    private String state;
    @Column(name = ComSchemaColumnConstantName.C_CITY_NAME)
    private String city;
    @Column(name = ComSchemaColumnConstantName.C_STREET)
    private String street;
    @Column(name = ComSchemaColumnConstantName.C_ADDITIONAL_INFO)
    private String additionalInfo;
    @Column(name = ComSchemaColumnConstantName.C_ZIP_CODE)
    private String zipCode;
    @Column(name = ComSchemaColumnConstantName.C_LATITUDE)
    private Double latitude;
    @Column(name = ComSchemaColumnConstantName.C_LONGITUDE)
    private Double longitude;
}
