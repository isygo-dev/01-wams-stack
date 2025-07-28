package eu.isygoit.mapper;

import eu.isygoit.dto.ResumeDto;
import eu.isygoit.model.imagefile.ResumeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface ResumeMapper extends EntityMapper<ResumeEntity, ResumeDto> {
}
