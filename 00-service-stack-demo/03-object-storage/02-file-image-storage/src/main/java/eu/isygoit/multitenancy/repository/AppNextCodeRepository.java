package eu.isygoit.multitenancy.repository;

import eu.isygoit.repository.NextCodeRepository;
import org.springframework.stereotype.Repository;


/**
 * The interface App next code repository.
 */
@Repository
public interface AppNextCodeRepository extends NextCodeRepository<eu.isygoit.multitenancy.model.AppNextCode, Long> {

}
