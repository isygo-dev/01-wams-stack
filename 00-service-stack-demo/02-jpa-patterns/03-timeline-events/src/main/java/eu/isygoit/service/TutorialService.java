package eu.isygoit.service;

import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.com.rest.service.tenancy.CrudTenantService;
import eu.isygoit.model.Tutorial;
import eu.isygoit.repository.TutorialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Tutorial service.
 */
@Service
@Transactional
@InjectRepository(value = TutorialRepository.class)
public class TutorialService extends CrudTenantService<Long, Tutorial, TutorialRepository> {
}
