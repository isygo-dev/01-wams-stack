package eu.isygoit.repository;

import eu.isygoit.model.image.UserEntity;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;

public interface UserRepository extends JpaPagingAndSortingTenantAndCodeAssignableRepository<UserEntity, Long> {

}