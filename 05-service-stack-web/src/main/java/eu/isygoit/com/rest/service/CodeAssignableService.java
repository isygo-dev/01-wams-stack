package eu.isygoit.com.rest.service;

import eu.isygoit.annotation.CodeGenKms;
import eu.isygoit.annotation.CodeGenLocal;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.dto.common.NextCodeDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.BadResponseException;
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

/**
 * The type Codifiable service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CodeAssignableService<I extends Serializable, T extends IIdAssignable<I>, R extends JpaPagingAndSortingRepository<T, I>>
        extends CrudService<I, T, R>
        implements ICodeAssignableService<I, T> {

    // In-memory store for next codes
    private static final Map<String, NextCodeModel> inMemoNextCode = new HashMap<>();

    @Autowired
    private ApplicationContextService applicationContextService;

    private ICodeGeneratorService nextCodeService;
    private IRemoteNextCodeService remoteNextCodeService;

    /**
     * Register incremental next code.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerIncrementalNextCode() {
        NextCodeModel initNextCode = this.initCodeGenerator();
        if (initNextCode != null && StringUtils.hasText(initNextCode.getEntity())) {
            if (remoteNextCodeService() != null) {
                try {
                    ResponseEntity<String> result = remoteNextCodeService()
                            .subscribeNextCode(initNextCode.getDomain(), NextCodeDto.builder()
                                    .domain(initNextCode.getDomain())
                                    .entity(initNextCode.getEntity())
                                    .attribute(initNextCode.getAttribute())
                                    .prefix(initNextCode.getPrefix())
                                    .suffix(initNextCode.getSuffix())
                                    .increment(initNextCode.getIncrement())
                                    .valueLength(initNextCode.getValueLength())
                                    .value(initNextCode.getValue())
                                    .build());
                    if (result.getStatusCode().is2xxSuccessful()) {
                        log.info("Subscribe NextCode executed successfully {}", initNextCode);
                    }
                } catch (Exception e) {
                    log.error("Remote feign call failed: ", e);
                }
            }
        }
    }

    @Override
    @Transactional
    public String getNextCode() {
        NextCodeModel initNextCode = this.initCodeGenerator();
        if (initNextCode != null && StringUtils.hasText(initNextCode.getEntity())) {
            if (remoteNextCodeService() != null) {
                return getRemoteNextCode(initNextCode);
            } else {
                return getLocalNextCode(initNextCode);
            }
        }
        return null;
    }

    private String getLocalNextCode(NextCodeModel initNextCode) {
        var nextCode = inMemoNextCode.get(getNextCodeKey(initNextCode));

        if (nextCode == null && nextCodeService() != null) {
            nextCode = nextCodeService()
                    .findByDomainAndEntityAndAttribute(initNextCode.getDomain(),
                            initNextCode.getEntity(),
                            initNextCode.getAttribute())
                    .orElse(null);
            if (nextCode != null) {
                inMemoNextCode.put(getNextCodeKey(initNextCode), nextCode);
            }
        }

        if (nextCode == null && nextCodeService() != null) {
            nextCode = nextCodeService().saveAndFlush(initNextCode);
            inMemoNextCode.put(getNextCodeKey(initNextCode), nextCode);
        }

        if (nextCodeService() != null) {
            nextCodeService().increment(nextCode.getDomain(), nextCode.getEntity(), nextCode.getIncrement());
        }

        return nextCode != null ? nextCode.nextCode().getCode() : null;
    }

    private String getRemoteNextCode(NextCodeModel initNextCode) {
        try {
            ResponseEntity<String> result = remoteNextCodeService()
                    .generateNextCode(RequestContextDto.builder().build(),
                            initNextCode.getDomain(),
                            initNextCode.getEntity(),
                            initNextCode.getAttribute());
            if (result.getStatusCode().is2xxSuccessful() && result.hasBody()) {
                return result.getBody();
            } else {
                throw new BadResponseException("remote next code");
            }
        } catch (Exception e) {
            log.error("Remote feign call failed: ", e);
        }

        throw new NextCodeServiceNotDefinedException(initNextCode.toString());
    }

    @Override
    public String getNextCodeKey(NextCodeModel initNextCode) {
        return initNextCode.getEntity() + "@" + initNextCode.getDomain();
    }

    @Override
    public NextCodeModel initCodeGenerator() {
        log.warn("Code generator is not implemented for type {}", this.getClass().getCanonicalName());
        return null;
    }

    @Override
    public final IRemoteNextCodeService remoteNextCodeService() throws RemoteNextCodeServiceNotDefinedException {
        if (this.remoteNextCodeService == null) {
            CodeGenKms annotation = this.getClass().getAnnotation(CodeGenKms.class);
            if (annotation != null) {
                this.remoteNextCodeService = applicationContextService.getBean(annotation.value())
                        .orElseThrow(() -> new RemoteNextCodeServiceNotDefinedException("Bean not found " + annotation.value().getSimpleName() + " not found"));
            } else {
                log.error("<Error>: KMS code generator not defined for {}", this.getClass().getSimpleName());
            }
        }

        return this.remoteNextCodeService;
    }

    @Override
    public final ICodeGeneratorService<NextCodeModel> nextCodeService() throws NextCodeServiceNotDefinedException {
        if (this.nextCodeService == null) {
            CodeGenLocal annotation = this.getClass().getAnnotation(CodeGenLocal.class);
            if (annotation != null) {
                this.nextCodeService = applicationContextService.getBean(annotation.value())
                        .orElseThrow(() -> new NextCodeServiceNotDefinedException("Bean not found " + annotation.value().getSimpleName() + " not found"));
            } else {
                log.error("<Error>: Local code generator not defined for {}", this.getClass().getSimpleName());
            }
        }

        return this.nextCodeService;
    }
}