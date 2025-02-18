package eu.isygoit.com.rest.service.impl.utils;

import eu.isygoit.annotation.DmsLinkFileService;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.api.IDmsLinkedFileService;
import eu.isygoit.com.rest.service.impl.crud.AssignableCodeService;
import eu.isygoit.exception.LinkedFileServiceNotDefinedException;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableId;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
public abstract class FileServiceUtils<I extends Serializable,
        E extends AssignableId & AssignableCode,
        R extends JpaPagingAndSortingRepository<I, E>>
        extends AssignableCodeService<I, E, R> {

    @Getter
    @Autowired
    private ApplicationContextService contextService;

    private IDmsLinkedFileService dmsLinkedFileService;

    /**
     * Gets dms linked file service.
     *
     * @return the dms linked file service
     * @throws LinkedFileServiceNotDefinedException the linked file service not defined exception
     */
    public final IDmsLinkedFileService getDmsLinkedFileService() throws LinkedFileServiceNotDefinedException {
        if (this.dmsLinkedFileService == null) {
            var annotation = this.getClass().getAnnotation(DmsLinkFileService.class);

            Optional.ofNullable(annotation)
                    .map(DmsLinkFileService::value)
                    .map(bean -> getContextService().getBean(bean))
                    .ifPresentOrElse(
                            bean -> this.dmsLinkedFileService = bean,
                            () -> handleLinkedFileServiceNotFound(annotation)
                    );
        }

        return this.dmsLinkedFileService;
    }

    private void handleLinkedFileServiceNotFound(DmsLinkFileService annotation) {
        var errorMsg = annotation == null
                ? "Linked file service not defined for " + this.getClass().getSimpleName() + ", local storage will be used!"
                : "Bean not found " + annotation.value().getSimpleName() + " not found";

        log.error("<Error>: {}", errorMsg);
        if (annotation != null) {
            throw new LinkedFileServiceNotDefinedException(errorMsg);
        }
    }
}
