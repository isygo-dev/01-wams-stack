package eu.isygoit.multitenancy.controller;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedCrudTenantController;
import eu.isygoit.multitenancy.dto.UserLoginEventDto;
import eu.isygoit.multitenancy.mapper.UserLoginEventMapper;
import eu.isygoit.multitenancy.model.UserLoginEntity;
import eu.isygoit.multitenancy.service.UserLoginEventTenantService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = UserLoginEventMapper.class, minMapper = UserLoginEventMapper.class)
@InjectService(UserLoginEventTenantService.class)
@RestController
@RequestMapping("/api/userlogin")
public class UserLoginEventController extends MappedCrudTenantController<UUID, UserLoginEntity,
        UserLoginEventDto, UserLoginEventDto, UserLoginEventTenantService> {
}
