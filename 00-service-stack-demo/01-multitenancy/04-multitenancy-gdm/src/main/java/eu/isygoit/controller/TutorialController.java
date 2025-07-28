package eu.isygoit.controller;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedCrudTenantController;
import eu.isygoit.dto.TutorialDto;
import eu.isygoit.mapper.TutorialMapper;
import eu.isygoit.model.Tutorial;
import eu.isygoit.service.TutorialService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = TutorialMapper.class, minMapper = TutorialMapper.class)
@InjectService(TutorialService.class)
@RestController
@RequestMapping("/api/tutorials")
public class TutorialController extends MappedCrudTenantController<Long, Tutorial,
        TutorialDto, TutorialDto, TutorialService> {
}
