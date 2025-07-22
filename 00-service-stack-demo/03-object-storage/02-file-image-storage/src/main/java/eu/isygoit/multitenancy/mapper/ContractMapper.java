package eu.isygoit.multitenancy.mapper;

import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.multitenancy.dto.ContractDto;
import eu.isygoit.multitenancy.model.file.ContractEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface ContractMapper extends EntityMapper<ContractEntity, ContractDto> {
}
