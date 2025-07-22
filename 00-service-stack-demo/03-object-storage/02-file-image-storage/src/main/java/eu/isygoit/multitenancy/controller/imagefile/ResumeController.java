package eu.isygoit.multitenancy.controller.imagefile;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedCrudTenantController;
import eu.isygoit.multitenancy.dto.ResumeDto;
import eu.isygoit.multitenancy.mapper.ResumeMapper;
import eu.isygoit.multitenancy.model.imagefile.ResumeEntity;
import eu.isygoit.multitenancy.service.ResumeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = ResumeMapper.class, minMapper = ResumeMapper.class)
@InjectService(ResumeService.class)
@RestController
@RequestMapping("/api/v1/resume")
public class ResumeController extends MappedCrudTenantController<Long, ResumeEntity,
        ResumeDto, ResumeDto, ResumeService> {
}
