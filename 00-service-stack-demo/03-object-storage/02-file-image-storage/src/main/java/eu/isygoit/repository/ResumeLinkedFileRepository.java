package eu.isygoit.repository;

import eu.isygoit.model.multifile.ResumeLinkedFile;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Resume linked file repository.
 */
@Repository
public interface ResumeLinkedFileRepository extends JpaPagingAndSortingTenantAssignableRepository<ResumeLinkedFile, Long> {
}
