package eu.isygoit.com.rest.service.impl;

import eu.isygoit.annotation.SrvRepo;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;

/**
 * The type Crud service utils.
 *
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CrudServiceUtils<T extends IIdEntity, R extends Repository> implements ICrudServiceUtils<T> {

    @Autowired
    private ApplicationContextService applicationContextService;

    private R repository;

    @Override
    public final R repository() throws JpaRepositoryNotDefinedException {
        if (repository == null) {
            synchronized (this) {
                if (repository == null) {
                    SrvRepo controllerDefinition = this.getClass().getAnnotation(SrvRepo.class);

                    if (controllerDefinition != null) {
                        this.repository = (R) applicationContextService.getBean(controllerDefinition.value());

                        if (this.repository == null) {
                            String errorMessage = String.format("<Error>: Bean %s not found", controllerDefinition.value().getSimpleName());
                            log.error(errorMessage);
                            throw new JpaRepositoryNotDefinedException("JpaRepository " + controllerDefinition.value().getSimpleName() + " not found");
                        }
                    } else {
                        String errorMessage = String.format("<Error>: Repository bean not defined for %s", this.getClass().getSimpleName());
                        log.error(errorMessage);
                        throw new JpaRepositoryNotDefinedException("JpaRepository not defined");
                    }
                }
            }
        }

        return repository;
    }
}
