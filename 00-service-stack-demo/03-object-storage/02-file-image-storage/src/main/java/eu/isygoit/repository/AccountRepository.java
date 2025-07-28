package eu.isygoit.repository;

import eu.isygoit.model.simple.AccountEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;

public interface AccountRepository extends JpaPagingAndSortingTenantAssignableRepository<AccountEntity, Long> {

}