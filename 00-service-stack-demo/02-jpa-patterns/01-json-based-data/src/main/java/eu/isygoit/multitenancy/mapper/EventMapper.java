package eu.isygoit.multitenancy.mapper;

import eu.isygoit.multitenancy.dto.EventDto;
import eu.isygoit.multitenancy.model.EventEntity;
import eu.isygoit.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface EventMapper extends EntityMapper<EventEntity, EventDto> {
}
