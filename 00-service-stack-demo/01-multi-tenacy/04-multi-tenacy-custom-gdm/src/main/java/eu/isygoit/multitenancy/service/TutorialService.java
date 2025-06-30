package eu.isygoit.multitenancy.service;

import eu.isygoit.annotation.ServRepo;
import eu.isygoit.com.rest.service.CrudService;
import eu.isygoit.multitenancy.model.Tutorial;
import eu.isygoit.multitenancy.repository.TutorialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@ServRepo(value = TutorialRepository.class)
public class TutorialService extends CrudService<Long, Tutorial, TutorialRepository> {
}
