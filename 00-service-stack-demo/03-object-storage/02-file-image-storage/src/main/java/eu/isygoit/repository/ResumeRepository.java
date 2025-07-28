package eu.isygoit.repository;

import eu.isygoit.model.imagefile.ResumeEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;

public interface ResumeRepository extends JpaPagingAndSortingTenantAndCodeAssignableRepository<ResumeEntity, Long> {

}