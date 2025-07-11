package eu.isygoit.multitenancy.mapper;

import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.multitenancy.dto.UserLoginEventDto;
import eu.isygoit.multitenancy.model.UserLoginEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface UserLoginEventMapper extends EntityMapper<UserLoginEntity, UserLoginEventDto> {
}
