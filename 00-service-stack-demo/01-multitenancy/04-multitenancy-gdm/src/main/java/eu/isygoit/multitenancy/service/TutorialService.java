package eu.isygoit.multitenancy.service;

import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.com.rest.service.tenancy.CrudTenantService;
import eu.isygoit.multitenancy.model.Tutorial;
import eu.isygoit.multitenancy.repository.TutorialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@InjectRepository(value = TutorialRepository.class)
public class TutorialService extends CrudTenantService<Long, Tutorial, TutorialRepository> {
}
