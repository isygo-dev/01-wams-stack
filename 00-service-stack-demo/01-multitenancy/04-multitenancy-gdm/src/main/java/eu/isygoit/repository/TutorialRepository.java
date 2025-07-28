package eu.isygoit.repository;

import eu.isygoit.model.Tutorial;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;

import java.util.List;

public interface TutorialRepository extends JpaPagingAndSortingTenantAssignableRepository<Tutorial, Long> {

    List<Tutorial> findByPublished(boolean published);

    List<Tutorial> findByTitleContainingIgnoreCase(String title);
}
