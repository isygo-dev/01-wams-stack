package eu.isygoit.mapper;

import eu.isygoit.dto.EventDto;
import eu.isygoit.model.EventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface EventMapper extends EntityMapper<EventEntity, EventDto> {
}
