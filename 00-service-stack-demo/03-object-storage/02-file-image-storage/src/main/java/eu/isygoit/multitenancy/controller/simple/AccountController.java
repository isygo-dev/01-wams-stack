package eu.isygoit.multitenancy.controller.simple;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedCrudTenantController;
import eu.isygoit.multitenancy.dto.AccountDto;
import eu.isygoit.multitenancy.mapper.AccountMapper;
import eu.isygoit.multitenancy.model.simple.AccountEntity;
import eu.isygoit.multitenancy.service.AccountService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = AccountMapper.class, minMapper = AccountMapper.class)
@InjectService(AccountService.class)
@RestController
@RequestMapping("/api/v1/account")
public class AccountController extends MappedCrudTenantController<Long, AccountEntity,
        AccountDto, AccountDto, AccountService> {
}
