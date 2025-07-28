package eu.isygoit.controller.imagefile;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedFileTenantController;
import eu.isygoit.dto.ResumeDto;
import eu.isygoit.mapper.ResumeMapper;
import eu.isygoit.model.imagefile.ResumeEntity;
import eu.isygoit.service.ResumeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = ResumeMapper.class, minMapper = ResumeMapper.class)
@InjectService(ResumeService.class)
@RestController
@RequestMapping("/api/v1/resume")
public class ResumeFileController extends MappedFileTenantController<Long, ResumeEntity,
        ResumeDto, ResumeDto, ResumeService> {
}
