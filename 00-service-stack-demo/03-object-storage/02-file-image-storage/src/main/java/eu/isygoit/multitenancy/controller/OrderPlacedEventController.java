package eu.isygoit.multitenancy.controller;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedCrudTenantController;
import eu.isygoit.multitenancy.dto.UserLoginEventDto;
import eu.isygoit.multitenancy.mapper.UserLoginEventMapper;
import eu.isygoit.multitenancy.model.OrderPlacedEntity;
import eu.isygoit.multitenancy.service.OrderPlacedEventTenantService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = UserLoginEventMapper.class, minMapper = UserLoginEventMapper.class)
@InjectService(OrderPlacedEventTenantService.class)
@RestController
@RequestMapping("/api/orderplaced")
public class OrderPlacedEventController extends MappedCrudTenantController<UUID, OrderPlacedEntity,
        UserLoginEventDto, UserLoginEventDto, OrderPlacedEventTenantService> {
}
