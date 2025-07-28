package eu.isygoit.repository;

import eu.isygoit.model.file.ContractEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;

public interface ContractRepository extends JpaPagingAndSortingTenantAndCodeAssignableRepository<ContractEntity, Long> {

}