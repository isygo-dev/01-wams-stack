package eu.isygoit.multitenancy.repository;

import eu.isygoit.multitenancy.model.EventEntity;
import eu.isygoit.repository.json.JsonBasedRepository;

public interface EventRepository extends JsonBasedRepository<EventEntity, Long> {

}