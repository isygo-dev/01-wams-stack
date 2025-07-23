package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.multifile.ResumeLinkedFile;
import eu.isygoit.repository.JpaPagingAndSortingCodeAssingnableRepository;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Resume linked file repository.
 */
@Repository
public interface ResumeLinkedFileRepository extends JpaPagingAndSortingTenantAssignableRepository<ResumeLinkedFile, Long> {
}
