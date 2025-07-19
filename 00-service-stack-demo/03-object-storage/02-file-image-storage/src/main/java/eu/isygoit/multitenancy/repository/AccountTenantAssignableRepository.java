package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.AccountEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;

public interface AccountTenantAssignableRepository extends JpaPagingAndSortingTenantAssignableRepository<AccountEntity, Long> {

}