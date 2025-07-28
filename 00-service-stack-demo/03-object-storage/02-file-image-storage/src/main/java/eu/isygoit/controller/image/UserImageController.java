package eu.isygoit.controller.image;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedImageTenantController;
import eu.isygoit.dto.UserDto;
import eu.isygoit.mapper.UserMapper;
import eu.isygoit.model.image.UserEntity;
import eu.isygoit.service.UserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = UserMapper.class, minMapper = UserMapper.class)
@InjectService(UserService.class)
@RestController
@RequestMapping("/api/v1/user")
public class UserImageController extends MappedImageTenantController<Long, UserEntity,
        UserDto, UserDto, UserService> {
}
