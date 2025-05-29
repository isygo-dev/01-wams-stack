package eu.isygoit.com.rest.service;

import eu.isygoit.annotation.CodeGenKms;
import eu.isygoit.annotation.CodeGenLocal;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.dto.common.NextCodeDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.BadResponseException;
import eu.isygoit.exception.NextCodeGenMethodNotDefinedException;
import eu.isygoit.exception.NextCodeServiceNotDefinedException;
import eu.isygoit.exception.RemoteNextCodeServiceNotDefinedException;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import eu.isygoit.service.IRemoteNextCodeService;
import eu.isygoit.service.nextCode.ICodeGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract base class for services that handle entities with code generation.
 *
 * @param <I> ID type
 * @param <T> Entity type (must implement IIdAssignable)
 * @param <R> Repository type
 */
@Slf4j
public abstract class CodeAssignableService<I extends Serializable, T extends IIdAssignable<I>, R extends JpaPagingAndSortingRepository<T, I>>
        extends CrudService<I, T, R>
        implements ICodeAssignableService<I, T> {

    private static final Map<String, NextCodeModel> inMemoryNextCodes = new HashMap<>();

    @Autowired
    private ApplicationContextService applicationContextService;

    private ICodeGeneratorService<NextCodeModel> nextCodeService;
    private IRemoteNextCodeService remoteNextCodeService;

    /**
     * Registers the next code configuration to the remote code service after application startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerIncrementalNextCode() {
        Optional.ofNullable(initCodeGenerator())
                .filter(code -> StringUtils.hasText(code.getEntity()))
                .ifPresent(code -> {
                    Optional.ofNullable(remoteNextCodeService()).ifPresent(remoteService -> {
                        try {
                            ResponseEntity<String> result = remoteService.subscribeNextCode(code.getDomain(),
                                    NextCodeDto.builder()
                                            .domain(code.getDomain())
                                            .entity(code.getEntity())
                                            .attribute(code.getAttribute())
                                            .prefix(code.getPrefix())
                                            .suffix(code.getSuffix())
                                            .increment(code.getIncrement())
                                            .valueLength(code.getValueLength())
                                            .value(code.getValue())
                                            .build());

                            if (result.getStatusCode().is2xxSuccessful()) {
                                log.info("NextCode subscription successful: {}", code);
                            }
                        } catch (Exception e) {
                            log.error("Remote Feign call failed during subscription", e);
                        }
                    });
                });
    }

    /**
     * Generates the next code using either the local or remote service.
     *
     * @return next code as String
     */
    @Override
    @Transactional
    public String getNextCode() {
        return Optional.ofNullable(initCodeGenerator())
                .filter(code -> StringUtils.hasText(code.getEntity()))
                .map(code -> remoteNextCodeService() != null
                        ? getRemoteNextCode(code)
                        : getLocalNextCode(code))
                .orElse(null);
    }

    /**
     * Retrieves the next code from the local service.
     */
    private String getLocalNextCode(NextCodeModel initNextCode) {
        String key = getNextCodeKey(initNextCode);

        NextCodeModel nextCode = inMemoryNextCodes.computeIfAbsent(key, k -> {
            return nextCodeService().findByDomainAndEntityAndAttribute(
                    initNextCode.getDomain(),
                    initNextCode.getEntity(),
                    initNextCode.getAttribute()
            ).orElseGet(() -> nextCodeService().saveAndFlush(initNextCode));
        });

        if (nextCode != null) {
            nextCodeService().increment(
                    nextCode.getDomain(),
                    nextCode.getEntity(),
                    nextCode.getIncrement()
            );
            return nextCode.nextCode().getCode();
        }

        return null;
    }

    /**
     * Retrieves the next code from the remote service.
     */
    private String getRemoteNextCode(NextCodeModel initNextCode) {
        try {
            ResponseEntity<String> response = remoteNextCodeService().generateNextCode(
                    RequestContextDto.builder().build(),
                    initNextCode.getDomain(),
                    initNextCode.getEntity(),
                    initNextCode.getAttribute()
            );

            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return response.getBody();
            } else {
                throw new BadResponseException("Invalid response from remote next code service.");
            }
        } catch (Exception e) {
            log.error("Remote Feign call failed during code generation", e);
            throw new NextCodeServiceNotDefinedException(initNextCode.toString());
        }
    }

    /**
     * Builds a unique key for next code caching.
     */
    @Override
    public String getNextCodeKey(NextCodeModel initNextCode) {
        return initNextCode.getEntity() + "@" + initNextCode.getDomain();
    }

    /**
     * Should be overridden to initialize the NextCodeModel configuration.
     */
    @Override
    public NextCodeModel initCodeGenerator() {
        log.error("initCodeGenerator not implemented for {}", this.getClass().getCanonicalName());
        throw new NextCodeGenMethodNotDefinedException("Override initCodeGenerator method for: " + this.getClass().getCanonicalName());
    }

    /**
     * Retrieves the remote next code service bean from Spring context.
     */
    @Override
    public final IRemoteNextCodeService remoteNextCodeService() throws RemoteNextCodeServiceNotDefinedException {
        if (this.remoteNextCodeService == null) {
            CodeGenKms annotation = this.getClass().getAnnotation(CodeGenKms.class);
            if (annotation != null) {
                this.remoteNextCodeService = applicationContextService.getBean(annotation.value())
                        .orElseThrow(() ->
                                new RemoteNextCodeServiceNotDefinedException("Bean not found: " + annotation.value().getSimpleName()));
            } else {
                log.error("KMS code generator not defined for {}", this.getClass().getSimpleName());
            }
        }
        return this.remoteNextCodeService;
    }

    /**
     * Retrieves the local next code generator service bean from Spring context.
     */
    @Override
    public final ICodeGeneratorService<NextCodeModel> nextCodeService() throws NextCodeServiceNotDefinedException {
        if (this.nextCodeService == null) {
            CodeGenLocal annotation = this.getClass().getAnnotation(CodeGenLocal.class);
            if (annotation != null) {
                this.nextCodeService = applicationContextService.getBean(annotation.value())
                        .orElseThrow(() ->
                                new NextCodeServiceNotDefinedException("Bean not found: " + annotation.value().getSimpleName()));
            } else {
                log.error("Local code generator not defined for {}", this.getClass().getSimpleName());
            }
        }
        return this.nextCodeService;
    }
}