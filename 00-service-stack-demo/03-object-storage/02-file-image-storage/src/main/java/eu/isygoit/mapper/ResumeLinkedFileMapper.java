package eu.isygoit.mapper;

import eu.isygoit.dto.ResumeLinkedFileDto;
import eu.isygoit.model.multifile.ResumeLinkedFile;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

/**
 * The interface Resume linked file mapper.
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface ResumeLinkedFileMapper extends EntityMapper<ResumeLinkedFile, ResumeLinkedFileDto> {
}
