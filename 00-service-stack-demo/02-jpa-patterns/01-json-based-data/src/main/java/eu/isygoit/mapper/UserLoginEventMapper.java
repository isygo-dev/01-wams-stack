package eu.isygoit.mapper;

import eu.isygoit.dto.UserLoginEventDto;
import eu.isygoit.model.UserLoginEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface UserLoginEventMapper extends EntityMapper<UserLoginEntity, UserLoginEventDto> {
}
