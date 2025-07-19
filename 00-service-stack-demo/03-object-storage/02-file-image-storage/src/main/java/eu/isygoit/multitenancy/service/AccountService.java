package eu.isygoit.multitenancy.service;

import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.com.rest.service.tenancy.CrudTenantService;
import eu.isygoit.multitenancy.model.AccountEntity;
import eu.isygoit.multitenancy.repository.AccountTenantAssignableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@InjectRepository(value = AccountTenantAssignableRepository.class)
public class AccountService extends CrudTenantService<Long, AccountEntity, AccountTenantAssignableRepository> {

}