package eu.isygoit.multitenancy.controller;

import eu.isygoit.annotation.CtrlMapper;
import eu.isygoit.annotation.CtrlService;
import eu.isygoit.com.rest.controller.impl.MappedCrudController;
import eu.isygoit.multitenancy.dto.TutorialDto;
import eu.isygoit.multitenancy.mapper.TutorialMapper;
import eu.isygoit.multitenancy.model.Tutorial;
import eu.isygoit.multitenancy.service.TutorialService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8081")
@CtrlMapper(mapper = TutorialMapper.class, minMapper = TutorialMapper.class)
@CtrlService(TutorialService.class)
@RestController
@RequestMapping("/api/tutorials")
public class TutorialController extends MappedCrudController<Long, Tutorial,
        TutorialDto, TutorialDto, TutorialService> {
}
