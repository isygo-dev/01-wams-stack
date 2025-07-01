package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.Tutorial;
import eu.isygoit.repository.JpaPagingAndSortingTenantAssignableRepository;

import java.util.List;

public interface TutorialRepository extends JpaPagingAndSortingTenantAssignableRepository<Tutorial, Long> {

    List<Tutorial> findByPublished(boolean published);

    List<Tutorial> findByTitleContainingIgnoreCase(String title);
}
