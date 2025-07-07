package eu.isygoit.multitenancy.mapper;

import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.multitenancy.dto.OrderPlacedEventDto;
import eu.isygoit.multitenancy.model.OrderPlacedEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface OrderPlacedEventMapper extends EntityMapper<OrderPlacedEntity, OrderPlacedEventDto> {
}
