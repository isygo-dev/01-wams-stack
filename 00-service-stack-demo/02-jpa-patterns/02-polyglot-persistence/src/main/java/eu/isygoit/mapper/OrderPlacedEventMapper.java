package eu.isygoit.mapper;

import eu.isygoit.dto.OrderPlacedEventDto;
import eu.isygoit.model.OrderPlacedEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface OrderPlacedEventMapper extends EntityMapper<OrderPlacedEntity, OrderPlacedEventDto> {
}
