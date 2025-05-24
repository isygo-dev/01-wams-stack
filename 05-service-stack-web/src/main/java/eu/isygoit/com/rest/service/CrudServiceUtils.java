package eu.isygoit.com.rest.service;

import eu.isygoit.annotation.ServRepo;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.model.ICodeAssignable;
import eu.isygoit.model.IIdAssignable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * The type Crud service utils.
 *
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CrudServiceUtils<I extends Serializable, T extends IIdAssignable<I>, R extends Repository<T, I>>
        implements ICrudServiceUtils<I, T> {

    @Autowired
    private ApplicationContextService applicationContextService;

    private R repository;

    @Override
    public final R repository() throws JpaRepositoryNotDefinedException {
        if (this.repository == null) {
            ServRepo controllerDefinition = this.getClass().getAnnotation(ServRepo.class);
            if (controllerDefinition != null) {
                this.repository = (R) applicationContextService.getBean(controllerDefinition.value())
                        .orElseThrow(() -> new JpaRepositoryNotDefinedException("JpaRepository " + controllerDefinition.value().getSimpleName() + " not found"));
            } else {
                log.error("<Error>: Repository bean not defined for {}", this.getClass().getSimpleName());
                throw new JpaRepositoryNotDefinedException("JpaRepository");
            }
        }

        return this.repository;
    }

    @SuppressWarnings("unchecked")
    protected void assignCodeIfEmpty(Object object) {
        if (this instanceof ICodeAssignableService codeAssignableService &&
                object instanceof ICodeAssignable codeAssignable &&
                !StringUtils.hasText(codeAssignable.getCode())) {
            codeAssignable.setCode(codeAssignableService.getNextCode());
        }
    }
}
