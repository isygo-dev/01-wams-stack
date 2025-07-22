package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.ContractEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;

public interface ContractRepository extends JpaPagingAndSortingTenantAndCodeAssignableRepository<ContractEntity, Long> {

}