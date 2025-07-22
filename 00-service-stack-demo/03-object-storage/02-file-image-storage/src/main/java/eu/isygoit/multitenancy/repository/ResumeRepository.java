package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.imagefile.ResumeEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;

public interface ResumeRepository extends JpaPagingAndSortingTenantAndCodeAssignableRepository<ResumeEntity, Long> {

}