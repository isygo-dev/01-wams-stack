package eu.isygoit.repository;

import eu.isygoit.model.EventEntity;
import eu.isygoit.repository.json.JsonBasedTenantAssignableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventTenantAssignableRepository extends JsonBasedTenantAssignableRepository<EventEntity, Long> {

}