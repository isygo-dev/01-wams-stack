package eu.isygoit.repository;

import eu.isygoit.model.Tutorial;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;

import java.util.List;

/**
 * The interface Tutorial repository.
 */
public interface TutorialRepository extends JpaPagingAndSortingTenantAssignableRepository<Tutorial, Long> {

    /**
     * Find by published list.
     *
     * @param published the published
     * @return the list
     */
    List<Tutorial> findByPublished(boolean published);

    /**
     * Find by title containing ignore case list.
     *
     * @param title the title
     * @return the list
     */
    List<Tutorial> findByTitleContainingIgnoreCase(String title);
}
