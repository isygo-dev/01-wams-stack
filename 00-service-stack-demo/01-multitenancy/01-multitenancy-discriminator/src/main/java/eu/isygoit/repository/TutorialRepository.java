package eu.isygoit.repository;

import eu.isygoit.model.Tutorial;

import java.util.List;

public interface TutorialRepository extends JpaPagingAndSortingRepository<Tutorial, Long> {

    List<Tutorial> findByPublished(boolean published);

    List<Tutorial> findByTitleContainingIgnoreCase(String title);
}
