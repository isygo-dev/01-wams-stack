package eu.isygoit.mapper;

import eu.isygoit.dto.TutorialDto;
import eu.isygoit.model.Tutorial;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

/**
 * The interface Tutorial mapper.
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface TutorialMapper extends EntityMapper<Tutorial, TutorialDto> {
}
