package eu.isygoit.repository;

import eu.isygoit.model.EventEntity;
import eu.isygoit.repository.json.JsonBasedRepository;

public interface EventRepository extends JsonBasedRepository<EventEntity, Long> {

}