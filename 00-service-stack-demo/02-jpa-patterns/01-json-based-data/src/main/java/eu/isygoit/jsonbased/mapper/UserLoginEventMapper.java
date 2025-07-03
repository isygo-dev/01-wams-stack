package eu.isygoit.jsonbased.mapper;

import eu.isygoit.jsonbased.common.UserLoginEntity;
import eu.isygoit.jsonbased.dto.UserLoginEventDto;
import eu.isygoit.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface UserLoginEventMapper extends EntityMapper<UserLoginEntity, UserLoginEventDto> {
}
