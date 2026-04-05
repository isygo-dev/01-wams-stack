package eu.isygoit.repository;

import eu.isygoit.model.EventEntity;
import eu.isygoit.repository.json.JsonBasedTenantAssignableRepository;

public interface EventTenantAssignableRepository extends JsonBasedTenantAssignableRepository<EventEntity, Long> {

}