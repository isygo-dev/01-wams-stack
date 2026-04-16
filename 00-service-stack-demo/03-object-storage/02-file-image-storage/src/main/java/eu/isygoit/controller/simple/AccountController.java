package eu.isygoit.controller.simple;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedCrudTenantController;
import eu.isygoit.dto.AccountDto;
import eu.isygoit.mapper.AccountMapper;
import eu.isygoit.model.simple.AccountEntity;
import eu.isygoit.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = AccountMapper.class, minMapper = AccountMapper.class)
@InjectService(AccountService.class)
@RestController
@RequestMapping("/api/v1/account")
@Tag(name = "Account Management", description = "Endpoints for managing user accounts")
public class AccountController extends MappedCrudTenantController<Long, AccountEntity,
        AccountDto, AccountDto, AccountService> {
}
