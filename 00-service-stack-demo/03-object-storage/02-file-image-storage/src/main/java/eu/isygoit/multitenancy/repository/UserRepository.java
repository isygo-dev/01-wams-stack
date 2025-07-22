package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.image.UserEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;

public interface UserRepository extends JpaPagingAndSortingTenantAndCodeAssignableRepository<UserEntity, Long> {

}