package eu.isygoit.multitenancy.controller;

import eu.isygoit.annotation.CtrlMapper;
import eu.isygoit.annotation.CtrlService;
import eu.isygoit.com.rest.controller.impl.MappedCrudController;
import eu.isygoit.multitenancy.common.UserLoginEntity;
import eu.isygoit.multitenancy.dto.UserLoginEventDto;
import eu.isygoit.multitenancy.mapper.UserLoginEventMapper;
import eu.isygoit.multitenancy.service.UserLoginEventService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@CrossOrigin(origins = "http://localhost:8081")
@CtrlMapper(mapper = UserLoginEventMapper.class, minMapper = UserLoginEventMapper.class)
@CtrlService(UserLoginEventService.class)
@RestController
@RequestMapping("/api/userlogin")
public class UserLoginEventController extends MappedCrudController<UUID, UserLoginEntity,
        UserLoginEventDto, UserLoginEventDto, UserLoginEventService> {
}
