package eu.isygoit.mapper;

import eu.isygoit.dto.TutorialDto;
import eu.isygoit.model.Tutorial;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface TutorialMapper extends EntityMapper<Tutorial, TutorialDto> {
}
