package eu.isygoit.jsonbased.mapper;

import eu.isygoit.jsonbased.dto.EventDto;
import eu.isygoit.jsonbased.model.EventEntity;
import eu.isygoit.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface EventMapper extends EntityMapper<EventEntity, EventDto> {
}
