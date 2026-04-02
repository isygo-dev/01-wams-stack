package eu.isygoit.repository;

import eu.isygoit.model.EventEntity;
import eu.isygoit.repository.json.JsonBasedRepository;

import java.util.List;

public interface EventRepository extends JsonBasedRepository<EventEntity, Long> {

}