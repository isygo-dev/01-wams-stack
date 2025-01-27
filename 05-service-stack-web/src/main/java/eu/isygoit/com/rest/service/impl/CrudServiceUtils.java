package eu.isygoit.com.rest.service.impl;

import eu.isygoit.annotation.SrvRepo;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;

import java.util.Objects;

/**
 * The type Crud service utils.
 *
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CrudServiceUtils<T extends IIdEntity, R extends Repository>
        implements ICrudServiceUtils<T> {

    @Autowired
    private ApplicationContextService applicationContextServie;

    private R repository;

    @Override
    public final R repository() throws JpaRepositoryNotDefinedException {
        if (Objects.isNull(this.repository)) {
            SrvRepo controllerDefinition = this.getClass().getAnnotation(SrvRepo.class);
            if (Objects.nonNull(controllerDefinition)) {
                this.repository = (R) applicationContextServie.getBean(controllerDefinition.value());
                if (Objects.isNull(this.repository)) {
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
