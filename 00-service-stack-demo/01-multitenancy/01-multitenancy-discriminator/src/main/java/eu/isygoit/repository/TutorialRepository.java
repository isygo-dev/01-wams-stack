package eu.isygoit.repository;

import eu.isygoit.model.Tutorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {

    List<Tutorial> findByPublished(boolean published);

    List<Tutorial> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT t FROM Tutorial t WHERE t.id = :id")
    Optional<Tutorial> findOneById(@Param("id") Long id);
}
