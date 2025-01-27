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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The type Codifiable service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CodifiableService<I, T extends IIdEntity, R extends JpaPagingAndSortingRepository> extends CrudService<I, T, R>
        implements ICodifiableService<I, T> {

    /**
     * The In memo next code.
     */
    static final Map<String, NextCodeModel> inMemoNextCode = new HashMap<>();

    @Autowired
    private ApplicationContextService applicationContextServie;

    private INextCodeService nextCodeService;

    private IRemoteNextCodeService remoteNextCodeService;

    /**
     * Register incremental next code.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerIncrementalNextCode() {
        Optional<NextCodeModel> optional = this.initCodeGenerator();
        if (optional.isPresent() && StringUtils.hasText(optional.get().getEntity())) {
            NextCodeModel initNextCode = optional.get();
            if (Objects.nonNull(remoteNextCodeService())) {
                try {
                    ResponseEntity<String> result = remoteNextCodeService()
                            .subscribeNextCode(//RequestContextDto.builder().build(),
                                    initNextCode.getDomain(), NextCodeDto.builder()
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
                    log.error("Remote feign call failed : ", e);
                    //throw new RemoteCallFailedException(e);
                }
            } else {

            }
        }
    }

    @Override
    @Transactional
    public Optional<String> getNextCode() {
        Optional<NextCodeModel> optional = this.initCodeGenerator();
        if (optional.isPresent() && StringUtils.hasText(optional.get().getEntity())) {
            if (Objects.nonNull(remoteNextCodeService())) {
                return getRemoteNextCode(optional.get());
            } else {
                //get from memory map
                return getLocalNextCode(optional.get());
            }
        }
        return Optional.empty();
    }

    private Optional<String> getLocalNextCode(NextCodeModel initNextCode) {
        //Get from memory map (optimization)
        NextCodeModel nextCode = inMemoNextCode.get(getNextCodeKey(initNextCode));

        //if not in memory map, get from database and update the memory map
        if (Objects.isNull(nextCode) && Objects.nonNull(nextCodeService())) {
            nextCodeService().findByDomainAndEntityAndAttribute(initNextCode.getDomain(), initNextCode.getEntity(), initNextCode.getAttribute())
                    .ifPresent(nextCodeModel -> {
                        inMemoNextCode.put(getNextCodeKey(initNextCode), nextCodeModel);
                    });
        }

        //Get from memory map
        nextCode = inMemoNextCode.get(getNextCodeKey(initNextCode));

        //if not in memory map, save a fist use and update the memory map
        if (Objects.isNull(nextCode) && Objects.nonNull(nextCodeService())) {
            nextCode = nextCodeService().saveAndFlush(initNextCode);
            inMemoNextCode.put(getNextCodeKey(initNextCode), nextCode);
        }

        //increment and update database
        if (Objects.nonNull(nextCodeService())) {
            nextCodeService().increment(nextCode.getDomain(), nextCode.getEntity(), nextCode.getIncrement());
        }

        return Optional.ofNullable(nextCode.nextCode().getCode());
    }

    private Optional<String> getRemoteNextCode(NextCodeModel initNextCode) {
        if (Objects.nonNull(remoteNextCodeService())) {
            try {
                ResponseEntity<String> result = remoteNextCodeService()
                        .generateNextCode(RequestContextDto.builder().build(),
                                initNextCode.getDomain(),
                                initNextCode.getEntity(),
                                initNextCode.getAttribute()
                        );
                if (result.getStatusCode().is2xxSuccessful() && result.hasBody()) {
                    return Optional.ofNullable(result.getBody());
                } else {
                    throw new BadResponseException("remote next code");
                }
            } catch (Exception e) {
                log.error("Remote feign call failed : ", e);
                //throw new RemoteCallFailedException(e);
            }
        }

        throw new NextCodeServiceNotDefinedException(initNextCode.toString());
    }

    @Override
    public String getNextCodeKey(NextCodeModel initNextCode) {
        return initNextCode.getEntity() +
                "@" +
                initNextCode.getDomain();
    }

    @Override
    public Optional<NextCodeModel> initCodeGenerator() {
        log.warn("Code generator is not implemented for type {}", this.getClass().getCanonicalName());
        return Optional.empty();
    }

    @Override
    public final IRemoteNextCodeService remoteNextCodeService() throws RemoteNextCodeServiceNotDefinedException {
        if (Objects.isNull(this.remoteNextCodeService)) {
            CodeGenKms annotation = this.getClass().getAnnotation(CodeGenKms.class);
            if (Objects.nonNull(annotation)) {
                this.remoteNextCodeService = applicationContextServie.getBean(annotation.value());
                if (Objects.isNull(this.remoteNextCodeService)) {
                    log.error("<Error>: bean {} not found", annotation.value().getSimpleName());
                    throw new RemoteNextCodeServiceNotDefinedException("Bean not found " + annotation.value().getSimpleName() + " not found");
                }
            } else {
                log.error("<Error>: KMS code generator not defined for {}", this.getClass().getSimpleName());
                //throw new RemoteNextCodeServiceNotDefinedException("RemoteNextCodeService");
            }
        }

        return this.remoteNextCodeService;
    }

    @Override
    public final INextCodeService<NextCodeModel> nextCodeService() throws NextCodeServiceNotDefinedException {
        if (Objects.isNull(this.nextCodeService)) {
            CodeGenLocal annotation = this.getClass().getAnnotation(CodeGenLocal.class);
            if (Objects.nonNull(annotation)) {
                this.nextCodeService = applicationContextServie.getBean(annotation.value());
                if (Objects.isNull(this.nextCodeService)) {
                    log.error("<Error>: bean {} not found", annotation.value().getSimpleName());
                    throw new NextCodeServiceNotDefinedException("Bean not found " + annotation.value().getSimpleName() + " not found");
                }
            } else {
                log.error("<Error>: Local code generator not defined for {}", this.getClass().getSimpleName());
                //throw new NextCodeServiceNotDefinedException("NextCodeService");
            }
        }

        return this.nextCodeService;
    }
}

