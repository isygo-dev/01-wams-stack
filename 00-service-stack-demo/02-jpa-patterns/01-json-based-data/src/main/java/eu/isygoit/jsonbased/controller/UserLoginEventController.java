package eu.isygoit.jsonbased.controller;

import eu.isygoit.annotation.CtrlMapper;
import eu.isygoit.annotation.CtrlService;
import eu.isygoit.com.rest.controller.impl.MappedCrudController;
import eu.isygoit.jsonbased.common.UserLoginEntity;
import eu.isygoit.jsonbased.dto.UserLoginEventDto;
import eu.isygoit.jsonbased.mapper.UserLoginEventMapper;
import eu.isygoit.jsonbased.service.UserLoginEventService;
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
