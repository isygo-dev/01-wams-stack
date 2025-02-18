package eu.isygoit.com.rest.service.impl.crud;

import eu.isygoit.annotation.CodeGenKms;
import eu.isygoit.annotation.CodeGenLocal;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.service.IAssignableCodeService;
import eu.isygoit.dto.common.NextCodeDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.BadResponseException;
import eu.isygoit.exception.NextCodeServiceNotDefinedException;
import eu.isygoit.exception.RemoteNextCodeServiceNotDefinedException;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableId;
import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.repository.JpaPagingAndSortingAssignableCodeRepository;
import eu.isygoit.repository.JpaPagingAndSortingAssignableDomainAndCodeRepository;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import eu.isygoit.service.IKmsCodeService;
import eu.isygoit.service.nextCode.ILocalCodeService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The type Assignable code service.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class AssignableCodeService<I extends Serializable,
        E extends AssignableId & AssignableCode,
        R extends JpaPagingAndSortingRepository<I, E>>
        extends CrudService<I, E, R>
        implements IAssignableCodeService<I, E> {

    /**
     * The constant inMemoNextCode.
     */
    public static final ConcurrentMap<String, NextCodeModel> inMemoNextCode = new ConcurrentHashMap<>();

    @Getter
    @Autowired
    private ApplicationContextService contextService;

    private ILocalCodeService localCodeService;

    private IKmsCodeService kmsCodeService;

    /**
     * Register incremental next code.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerIncrementalNextCode() {
        this.createCodeGenerator()
                .filter(nextCode -> StringUtils.hasText(nextCode.getEntity()))
                .ifPresent(nextCode -> {
                    var remoteService = getKmsCodeService();
                    var nextCodeDto = mapToDto(nextCode);

                    if (remoteService != null) {
                        try {
                            var response = remoteService.subscribeNextCode(
                                    nextCode.getDomain(), nextCodeDto
                            );

                            if (response.getStatusCode().is2xxSuccessful()) {
                                log.info("Successfully subscribed to NextCode: {}", nextCode);
                            }
                        } catch (Exception e) {
                            log.error("Remote service call failed: {}", e.getMessage(), e);
                        }
                    } else {
                        log.info("Using local code service for: {}", nextCode);
                        localCodeService.save(nextCode);
                    }
                });
    }

    private NextCodeDto mapToDto(NextCodeModel nextCode) {
        return NextCodeDto.builder()
                .domain(nextCode.getDomain())
                .entity(nextCode.getEntity())
                .attribute(nextCode.getAttribute())
                .prefix(nextCode.getPrefix())
                .suffix(nextCode.getSuffix())
                .increment(nextCode.getIncrement())
                .valueLength(nextCode.getValueLength())
                .value(nextCode.getValue())
                .build();
    }

    @Override
    @Transactional
    public Optional<String> getNextCode() {
        return createCodeGenerator()
                .filter(nextCode -> StringUtils.hasText(nextCode.getEntity()))
                .map(nextCode -> {
                    if (getKmsCodeService() != null) {
                        return getKmsNextCode(nextCode);
                    } else if (localCodeService != null) {
                        return getLocalNextCode(nextCode);
                    } else {
                        log.error("No service available for generating next code");
                        return null;
                    }
                })
                .or(() -> {
                    log.error("Error: Code generator could not be initialized");
                    return Optional.empty();
                });
    }

    private String getLocalNextCode(NextCodeModel initNextCode) {
        var key = getNextCodeKey(initNextCode);
        log.info("Fetching NextCode for key: {}", key);

        // Step 1: Attempt to retrieve from cache, database, or create a new entry if necessary
        var nextCode = Optional.ofNullable(inMemoNextCode.get(key))
                .or(() -> fetchFromDatabase(initNextCode))
                .orElseGet(() -> saveAndCache(initNextCode, key));

        // Step 2: Increment value in the database if service is available
        Optional.ofNullable(getLocalCodeService()).ifPresent(service -> {
            log.info("Incrementing NextCode for domain: {}, entity: {}", nextCode.getDomain(), nextCode.getEntity());
            service.increment(nextCode.getDomain(), nextCode.getEntity(), nextCode.getIncrement());
        });

        var nextCodeValue = nextCode.nextCode().getCode();
        log.info("Generated NextCode: {}", nextCodeValue);
        return nextCodeValue;
    }

    private Optional<NextCodeModel> fetchFromDatabase(NextCodeModel initNextCode) {
        log.info("Fetching NextCode from database for domain: {}, entity: {}", initNextCode.getDomain(), initNextCode.getEntity());

        return Optional.ofNullable(getLocalCodeService())
                .flatMap(service -> service.findByDomainAndEntityAndAttribute(
                        initNextCode.getDomain(), initNextCode.getEntity(), initNextCode.getAttribute()))
                .map(nextCode -> {
                    log.info("NextCode found in database, caching it with key: {}", getNextCodeKey(initNextCode));
                    inMemoNextCode.put(getNextCodeKey(initNextCode), nextCode);
                    return nextCode;
                });
    }

    private NextCodeModel saveAndCache(NextCodeModel initNextCode, String key) {
        log.info("NextCode not found, creating new entry for key: {}", key);

        return Optional.ofNullable(getLocalCodeService())
                .map(service -> {
                    var savedCode = service.saveAndFlush(initNextCode);
                    inMemoNextCode.put(key, savedCode);
                    log.info("New NextCode saved and cached: {}", savedCode);
                    return savedCode;
                })
                .orElseThrow(() -> {
                    log.error("Failed to save NextCode: NextCodeService is unavailable");
                    return new IllegalStateException("NextCodeService is unavailable");
                });
    }

    private String getKmsNextCode(NextCodeModel initNextCode) {
        log.info("Generating NextCode from KMS service for domain: {}, entity: {}", initNextCode.getDomain(), initNextCode.getEntity());
        var result = getKmsCodeService()
                .generateNextCode(RequestContextDto.builder().build(),
                        initNextCode.getDomain(),
                        initNextCode.getEntity(),
                        initNextCode.getAttribute()
                );

        return Optional.ofNullable(result)
                .filter(response -> response.getStatusCode().is2xxSuccessful())
                .filter(ResponseEntity::hasBody)
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new BadResponseException("Remote NextCode generation failed"));
    }

    @Override
    public String getNextCodeKey(NextCodeModel initNextCode) {
        return initNextCode.getEntity() + "@" + initNextCode.getDomain();
    }

    @Override
    public Optional<NextCodeModel> createCodeGenerator() {
        log.warn("Code generator is not implemented for type {}", this.getClass().getCanonicalName());
        return Optional.empty();  // Return Optional.empty() to indicate no code generator is provided
    }

    @Override
    public final IKmsCodeService getKmsCodeService() throws RemoteNextCodeServiceNotDefinedException {
        if (this.kmsCodeService == null) {
            var annotation = this.getClass().getAnnotation(CodeGenKms.class);
            Optional.ofNullable(annotation)
                    .map(CodeGenKms::value)
                    .map(value -> getContextService().getBean(value))
                    .ifPresentOrElse(service -> this.kmsCodeService = service,
                            () -> {
                                log.error("KMS code generator not defined for {}", this.getClass().getSimpleName());
                                throw new RemoteNextCodeServiceNotDefinedException(
                                        String.format("Bean not found: %s not defined", annotation != null ? annotation.value().getSimpleName() : "unknown"));
                            });
        }
        return this.kmsCodeService;
    }

    @Override
    public final ILocalCodeService<NextCodeModel> getLocalCodeService() throws NextCodeServiceNotDefinedException {
        if (this.localCodeService == null) {
            var annotation = this.getClass().getAnnotation(CodeGenLocal.class);
            Optional.ofNullable(annotation)
                    .map(CodeGenLocal::value)
                    .map(value -> getContextService().getBean(value))
                    .ifPresentOrElse(service -> this.localCodeService = service,
                            () -> {
                                log.error("Local code generator not defined for {}", this.getClass().getSimpleName());
                                throw new NextCodeServiceNotDefinedException(
                                        String.format("Bean not found: %s not defined", annotation != null ? annotation.value().getSimpleName() : "unknown"));
                            });
        }
        return this.localCodeService;
    }

    public Optional<E> findByCode(String code) {
        return findCodeAssignableRepository(getRepository())
                .map(repo -> repo.findByCodeIgnoreCase(code))
                .orElseThrow(() -> new UnsupportedOperationException("Not a code assignable repository"));
    }

    public List<E> findByCode(List<String> codeList) {
        return findCodeAssignableRepository(getRepository())
                .map(repo -> repo.findByCodeIgnoreCaseIn(codeList))
                .orElseThrow(() -> new UnsupportedOperationException("Not a code assignable repository"));
    }

    private Optional<JpaPagingAndSortingAssignableCodeRepository> findCodeAssignableRepository(Object repository) {
        if (repository instanceof JpaPagingAndSortingAssignableCodeRepository assignableCodeRepository) {
            return Optional.of(assignableCodeRepository);
        } else if (repository instanceof JpaPagingAndSortingAssignableDomainAndCodeRepository assignableDomainCodeRepository) {
            return Optional.of(assignableDomainCodeRepository);
        }
        return Optional.empty();
    }
}
