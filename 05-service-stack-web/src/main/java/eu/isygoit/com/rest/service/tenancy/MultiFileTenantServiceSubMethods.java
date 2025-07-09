package eu.isygoit.com.rest.service.tenancy;

import eu.isygoit.annotation.InjectDmsLinkedFileService;
import eu.isygoit.annotation.InjectLinkedFileRepository;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.api.ILinkedFileApi;
import eu.isygoit.com.rest.service.FileServiceDmsStaticMethods;
import eu.isygoit.com.rest.service.FileServiceLocalStaticMethods;
import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.exception.LinkedFileServiceNotDefinedException;
import eu.isygoit.model.*;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import eu.isygoit.repository.tenancy.JpaPagingAndSortingTenantAndCodeAssignableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * The type Multi file service sub methods.
 *
 * @param <I>  the type parameter
 * @param <T>  the type parameter
 * @param <L>  the type parameter
 * @param <R>  the type parameter
 * @param <RL> the type parameter
 */
@Slf4j
public abstract class MultiFileTenantServiceSubMethods<I extends Serializable,
        T extends IMultiFileEntity<L> & IIdAssignable<I> & ICodeAssignable & ITenantAssignable,
        L extends ILinkedFile & ICodeAssignable & IIdAssignable<I>,
        R extends JpaPagingAndSortingTenantAndCodeAssignableRepository<T, I>,
        RL extends JpaPagingAndSortingRepository<L, I>>
        extends CodeAssignableTenantService<I, T, R> {

    @Autowired
    private ApplicationContextService applicationContextService;

    private ILinkedFileApi linkedFileApi;

    private RL linkFileRepository;

    /**
     * Link file repository rl.
     *
     * @return the rl
     * @throws JpaRepositoryNotDefinedException the jpa repository not defined exception
     */
    public final RL linkFileRepository() throws JpaRepositoryNotDefinedException {
        if (this.linkFileRepository == null) {
            InjectLinkedFileRepository controllerDefinition = this.getClass().getAnnotation(InjectLinkedFileRepository.class);
            if (controllerDefinition != null) {
                this.linkFileRepository = (RL) applicationContextService.getBean(controllerDefinition.value())
                        .orElseThrow(() -> new JpaRepositoryNotDefinedException("JpaRepository " + controllerDefinition.value().getSimpleName() + " not found"));
            } else {
                log.error("<Error>: Link file Repository bean not defined for {}", this.getClass().getSimpleName());
                throw new JpaRepositoryNotDefinedException("Link file Repository");
            }
        }

        return this.linkFileRepository;
    }

    private ILinkedFileApi linkedFileService() throws LinkedFileServiceNotDefinedException {
        if (this.linkedFileApi == null) {
            InjectDmsLinkedFileService annotation = this.getClass().getAnnotation(InjectDmsLinkedFileService.class);
            if (annotation != null) {
                this.linkedFileApi = applicationContextService.getBean((Class<ILinkedFileApi>) annotation.value())
                        .orElseThrow(() -> new LinkedFileServiceNotDefinedException("Bean not found " + annotation.value().getSimpleName() + " not found"));
            } else {
                log.error("<Error>: Linked file service not defined for {}, local storage will be used!", this.getClass().getSimpleName());
            }
        }

        return this.linkedFileApi;
    }

    /**
     * Sub upload file l.
     *
     * @param file   the file
     * @param entity the entity
     * @return the l
     */
    final L subUploadFile(MultipartFile file, L entity) {
        return executeSafely(() -> {
            ILinkedFileApi service = linkedFileService();
            if (service != null) {
                entity.setCode(FileServiceDmsStaticMethods.upload(file, entity, service).getCode());
            } else {
                entity.setCode(FileServiceLocalStaticMethods.upload(file, entity));
            }
            return entity;
        }, entity);
    }

    private <T> T executeSafely(CheckedSupplier<T> operation, T defaultValue) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("Remote feign call failed:", e);
            return defaultValue;
        }
    }

    /**
     * Sub download file resource.
     *
     * @param entity  the entity
     * @param version the version
     * @return the resource
     */
    final Resource subDownloadFile(L entity, Long version) {
        return executeSafely(() -> {
            ILinkedFileApi service = linkedFileService();
            if (service != null) {
                return FileServiceDmsStaticMethods.download(entity, version, service);
            } else {
                return FileServiceLocalStaticMethods.download(entity, version);
            }
        }, null);
    }

    /**
     * Sub delete file boolean.
     *
     * @param entity the entity
     * @return the boolean
     */
    final boolean subDeleteFile(L entity) {
        return executeSafely(() -> {
            linkFileRepository().delete(entity);
            ILinkedFileApi service = linkedFileService();
            if (service != null) {
                return FileServiceDmsStaticMethods.delete(entity, service);
            } else {
                return FileServiceLocalStaticMethods.delete(entity);
            }
        }, false);
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        /**
         * Get t.
         *
         * @return the t
         * @throws Exception the exception
         */
        T get() throws Exception;
    }
}