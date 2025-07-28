package eu.isygoit.controller.multifile;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.api.IMappedMultiFileApi;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedMultiFileTenatController;
import eu.isygoit.dto.ResumeDto;
import eu.isygoit.dto.ResumeLinkedFileDto;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.mapper.ResumeLinkedFileMapper;
import eu.isygoit.mapper.ResumeMapper;
import eu.isygoit.model.imagefile.ResumeEntity;
import eu.isygoit.service.ResumeMultiFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * The type Resume multi file controller.
 */
@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = ResumeMapper.class, minMapper = ResumeMapper.class)
@InjectService(ResumeMultiFileService.class)
@RestController
@RequestMapping("/api/v1/resume")
public class ResumeMultiFileController extends MappedMultiFileTenatController<Long, ResumeEntity, ResumeLinkedFileDto,
        ResumeDto, ResumeDto, ResumeMultiFileService>
        implements IMappedMultiFileApi<ResumeLinkedFileDto, Long> {

    @Autowired
    private ResumeLinkedFileMapper resumeLinkedFileMapper;

    @Override
    public EntityMapper linkedFileMapper() {
        return resumeLinkedFileMapper;
    }
}
