package eu.isygoit.mapper;

import eu.isygoit.dto.ContractDto;
import eu.isygoit.model.file.ContractEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface ContractMapper extends EntityMapper<ContractEntity, ContractDto> {
}
