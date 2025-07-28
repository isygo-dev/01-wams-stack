package eu.isygoit.repository;

import eu.isygoit.model.AppNextCode;
import eu.isygoit.repository.code.NextCodeRepository;
import org.springframework.stereotype.Repository;


/**
 * The interface App next code repository.
 */
@Repository
public interface AppNextCodeRepository extends NextCodeRepository<AppNextCode, Long> {

}
