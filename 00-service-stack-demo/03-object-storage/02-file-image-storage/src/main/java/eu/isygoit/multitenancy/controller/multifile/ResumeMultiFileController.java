package eu.isygoit.multitenancy.controller.multifile;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.api.IMappedMultiFileApi;
import eu.isygoit.com.rest.api.IMappedMultiFileDownloadApi;
import eu.isygoit.com.rest.api.IMappedMultiFileUploadApi;
import eu.isygoit.com.rest.controller.impl.MappedMultiFileController;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedMultiFileTenatController;
import eu.isygoit.mapper.EntityMapper;
import eu.isygoit.multitenancy.dto.ResumeDto;
import eu.isygoit.multitenancy.dto.ResumeLinkedFileDto;
import eu.isygoit.multitenancy.mapper.ResumeLinkedFileMapper;
import eu.isygoit.multitenancy.mapper.ResumeMapper;
import eu.isygoit.multitenancy.model.imagefile.ResumeEntity;
import eu.isygoit.multitenancy.service.ResumeMultiFileService;
import eu.isygoit.multitenancy.service.ResumeService;
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
