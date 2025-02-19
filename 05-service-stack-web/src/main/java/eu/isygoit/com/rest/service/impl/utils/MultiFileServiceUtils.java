package eu.isygoit.com.rest.service.impl.utils;

import eu.isygoit.annotation.SrvLinkedFileRepo;
import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableId;
import eu.isygoit.model.AssignableMultiFile;
import eu.isygoit.model.LinkedFile;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Optional;

/**
 * The type File service utils.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class MultiFileServiceUtils<E extends AssignableMultiFile & AssignableId & AssignableCode,
        I extends Serializable,
        L extends LinkedFile & AssignableCode & AssignableId,
        R extends JpaPagingAndSortingRepository<E, I>,
        RL extends JpaPagingAndSortingRepository<L, I>>
        extends FileServiceUtils<E, I, R> {

    private RL linkedFileRepository;

    /**
     * Linked file repository rl.
     *
     * @return the rl
     * @throws JpaRepositoryNotDefinedException the jpa repository not defined exception
     */
    public final RL linkedFileRepository() throws JpaRepositoryNotDefinedException {
        if (this.linkedFileRepository == null) {
            var controllerDefinition = this.getClass().getAnnotation(SrvLinkedFileRepo.class);

            Optional.ofNullable(controllerDefinition)
                    .map(SrvLinkedFileRepo::value)
                    .map(bean -> getContextService().getBean(bean))
                    .ifPresentOrElse(bean -> this.linkedFileRepository = (RL) bean,
                            () -> handleRepositoryNotFound(controllerDefinition));
        }

        return this.linkedFileRepository;
    }


    private void handleRepositoryNotFound(SrvLinkedFileRepo controllerDefinition) {
        var errorMsg = controllerDefinition == null
                ? "Repository bean not defined for " + this.getClass().getSimpleName()
                : "JpaRepository " + controllerDefinition.value().getSimpleName() + " not found";

        log.error("<Error>: {}", errorMsg);
        throw new JpaRepositoryNotDefinedException(errorMsg);
    }
}
