package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.simple.AccountEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;

public interface AccountRepository extends JpaPagingAndSortingTenantAssignableRepository<AccountEntity, Long> {

}