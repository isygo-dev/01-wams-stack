package eu.isygoit.multitenancy.mapper;

import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.multitenancy.dto.ResumeLinkedFileDto;
import eu.isygoit.multitenancy.model.multifile.ResumeLinkedFile;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

/**
 * The interface Resume linked file mapper.
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface ResumeLinkedFileMapper extends EntityMapper<ResumeLinkedFile, ResumeLinkedFileDto> {
}
