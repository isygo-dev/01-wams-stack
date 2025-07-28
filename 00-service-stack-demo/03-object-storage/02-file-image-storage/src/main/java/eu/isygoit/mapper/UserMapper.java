package eu.isygoit.mapper;

import eu.isygoit.dto.UserDto;
import eu.isygoit.model.image.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface UserMapper extends EntityMapper<UserEntity, UserDto> {
}
