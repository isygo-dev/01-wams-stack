package eu.isygoit.com.rest.service.impl;

import eu.isygoit.annotation.SrvRepo;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;

import java.io.Serializable;

/**
 * The type Crud service utils.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CrudServiceUtils<I extends Serializable, E extends IIdEntity, R extends Repository>
        implements ICrudServiceUtils<I, E> {

    @Autowired
    private ApplicationContextService applicationContextServie;

    private R repository;

    @Override
    public final R repository() throws JpaRepositoryNotDefinedException {
        if (this.repository == null) {
            SrvRepo controllerDefinition = this.getClass().getAnnotation(SrvRepo.class);
            if (controllerDefinition != null) {
                this.repository = (R) applicationContextServie.getBean(controllerDefinition.value());
                if (this.repository == null) {
                    log.error("<Error>: bean {} not found", controllerDefinition.value().getSimpleName());
                    throw new JpaRepositoryNotDefinedException("JpaRepository " + controllerDefinition.value().getSimpleName() + " not found");
                }
            } else {
                log.error("<Error>: Repository bean not defined for {}", this.getClass().getSimpleName());
                throw new JpaRepositoryNotDefinedException("JpaRepository");
            }
        }

        return this.repository;
    }
}
