package eu.isygoit.service;

import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.com.rest.service.tenancy.CrudTenantService;
import eu.isygoit.model.simple.AccountEntity;
import eu.isygoit.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@InjectRepository(value = AccountRepository.class)
public class AccountService extends CrudTenantService<Long, AccountEntity, AccountRepository> {

}