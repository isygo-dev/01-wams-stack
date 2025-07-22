package eu.isygoit.multitenancy.mapper;

import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.multitenancy.dto.ResumeDto;
import eu.isygoit.multitenancy.model.imagefile.ResumeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface ResumeMapper extends EntityMapper<ResumeEntity, ResumeDto> {
}
