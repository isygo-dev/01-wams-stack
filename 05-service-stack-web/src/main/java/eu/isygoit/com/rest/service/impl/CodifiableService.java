package eu.isygoit.com.rest.service.impl;

import eu.isygoit.annotation.CodeGenKms;
import eu.isygoit.annotation.CodeGenLocal;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.service.ICodifiableService;
import eu.isygoit.dto.common.NextCodeDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.BadResponseException;
import eu.isygoit.exception.NextCodeServiceNotDefinedException;
import eu.isygoit.exception.RemoteNextCodeServiceNotDefinedException;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import eu.isygoit.service.IRemoteNextCodeService;
import eu.isygoit.service.nextCode.INextCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The type Codifiable service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CodifiableService<I, T extends IIdEntity, R extends JpaPagingAndSortingRepository>
        extends CrudService<I, T, R> implements ICodifiableService<I, T> {

    /**
     * The In memo next code.
     */
    static final Map<String, NextCodeModel> inMemoNextCode = new HashMap<>();
    private INextCodeService<NextCodeModel> nextCodeService;
    private IRemoteNextCodeService remoteNextCodeService;

    protected abstract ApplicationContextService getApplicationContextServiceInstance();

    // Returns an Optional containing the ApplicationContextService, if present
    protected Optional<ApplicationContextService> getApplicationContextService() {
        return Optional.ofNullable(getApplicationContextServiceInstance());
    }

    /**
     * Register incremental next code.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerIncrementalNextCode() {
        initCodeGenerator()
                .filter(initNextCode -> StringUtils.hasText(initNextCode.getEntity()))
                .ifPresent(initNextCode -> {
                    if (remoteNextCodeService != null) {
                        try {
                            ResponseEntity<String> result = remoteNextCodeService()
                                    .subscribeNextCode(initNextCode.getDomain(), buildNextCodeDto(initNextCode));

                            if (result.getStatusCode().is2xxSuccessful()) {
                                log.info("Subscribe NextCode executed successfully {}", initNextCode);
                            }
                        } catch (Exception e) {
                            log.error("Remote feign call failed: ", e);
                        }
                    }
                });
    }

    private NextCodeDto buildNextCodeDto(NextCodeModel initNextCode) {
        return NextCodeDto.builder()
                .domain(initNextCode.getDomain())
                .entity(initNextCode.getEntity())
                .attribute(initNextCode.getAttribute())
                .prefix(initNextCode.getPrefix())
                .suffix(initNextCode.getSuffix())
                .increment(initNextCode.getIncrement())
                .valueLength(initNextCode.getValueLength())
                .value(initNextCode.getValue())
                .build();
    }

    @Override
    @Transactional
    public Optional<String> getNextCode() {
        return initCodeGenerator()
                .filter(initNextCode -> StringUtils.hasText(initNextCode.getEntity()))
                .map(initNextCode -> remoteNextCodeService != null
                        ? getRemoteNextCode(initNextCode)
                        : getLocalNextCode(initNextCode))
                .orElse(Optional.empty());
    }

    private Optional<String> getLocalNextCode(NextCodeModel initNextCode) {
        String key = getNextCodeKey(initNextCode);
        NextCodeModel nextCode = inMemoNextCode.computeIfAbsent(key, k -> loadNextCodeFromDb(initNextCode));

        if (nextCode != null) {
            nextCodeService.incrementNextCode(nextCode.getDomain(), nextCode.getEntity(), nextCode.getIncrement());
            return Optional.ofNullable(nextCode.nextCode().getCode());
        }

        return Optional.empty();
    }

    private NextCodeModel loadNextCodeFromDb(NextCodeModel initNextCode) {
        return nextCodeService.getByDomainEntityAndAttribute(initNextCode.getDomain(), initNextCode.getEntity(), initNextCode.getAttribute())
                .orElseGet(() -> nextCodeService.saveAndFlush(initNextCode));
    }

    private Optional<String> getRemoteNextCode(NextCodeModel initNextCode) {
        if (remoteNextCodeService != null) {
            try {
                ResponseEntity<String> result = remoteNextCodeService().generateNextCode(
                        RequestContextDto.builder().build(),
                        initNextCode.getDomain(),
                        initNextCode.getEntity(),
                        initNextCode.getAttribute()
                );

                if (result.getStatusCode().is2xxSuccessful() && result.hasBody()) {
                    return Optional.ofNullable(result.getBody());
                }
                throw new BadResponseException("Remote next code generation failed");
            } catch (Exception e) {
                log.error("Remote feign call failed: ", e);
            }
        }
        throw new NextCodeServiceNotDefinedException(initNextCode.toString());
    }

    @Override
    public String getNextCodeKey(NextCodeModel initNextCode) {
        return initNextCode.getEntity() + "@" + initNextCode.getDomain();
    }

    @Override
    public Optional<NextCodeModel> initCodeGenerator() {
        log.warn("Code generator is not implemented for type {}", this.getClass().getCanonicalName());
        return Optional.empty();
    }

    @Override
    public final IRemoteNextCodeService remoteNextCodeService() {


        if (remoteNextCodeService == null) {
            var applicationContextService = getApplicationContextService()
                    .orElseThrow(() -> new ApplicationContextException("ApplicationContextService not found"));
            
            var annotation = this.getClass().getAnnotation(CodeGenKms.class);
            
            if (annotation != null) {
                remoteNextCodeService = applicationContextService.getBean(annotation.value());
                if (remoteNextCodeService == null) {
                    log.error("Bean {} not found", annotation.value().getSimpleName());
                    throw new RemoteNextCodeServiceNotDefinedException("Bean not found " + annotation.value().getSimpleName());
                }
            }
        }
        return remoteNextCodeService;
    }

    @Override
    public final INextCodeService<NextCodeModel> nextCodeService() {


        if (nextCodeService == null) {
            var applicationContextService = getApplicationContextService()
                    .orElseThrow(() -> new ApplicationContextException("ApplicationContextService not found"));
            
            var annotation = this.getClass().getAnnotation(CodeGenLocal.class);
            
            if (annotation != null) {
                nextCodeService = applicationContextService.getBean(annotation.value());
                if (nextCodeService == null) {
                    log.error("Bean {} not found", annotation.value().getSimpleName());
                    throw new NextCodeServiceNotDefinedException("Bean not found " + annotation.value().getSimpleName());
                }
            }
        }
        return nextCodeService;
    }
}