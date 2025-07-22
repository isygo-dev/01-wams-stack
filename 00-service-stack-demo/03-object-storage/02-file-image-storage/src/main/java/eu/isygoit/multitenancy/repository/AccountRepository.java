package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.AccountEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;

public interface AccountRepository extends JpaPagingAndSortingTenantAssignableRepository<AccountEntity, Long> {

}