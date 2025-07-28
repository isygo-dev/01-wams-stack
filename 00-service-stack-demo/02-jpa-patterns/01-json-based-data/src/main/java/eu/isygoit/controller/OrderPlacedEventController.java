package eu.isygoit.controller;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedCrudTenantController;
import eu.isygoit.dto.UserLoginEventDto;
import eu.isygoit.mapper.UserLoginEventMapper;
import eu.isygoit.model.OrderPlacedEntity;
import eu.isygoit.service.OrderPlacedEventTenantService;
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
