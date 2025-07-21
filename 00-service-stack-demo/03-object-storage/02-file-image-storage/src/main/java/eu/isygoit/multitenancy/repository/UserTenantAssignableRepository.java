package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.UserEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAssignableRepository;

public interface UserTenantAssignableRepository extends JpaPagingAndSortingTenantAssignableRepository<UserEntity, Long>, JpaPagingAndSortingTenantAndCodeAssignableRepository<UserEntity, Long> {

}