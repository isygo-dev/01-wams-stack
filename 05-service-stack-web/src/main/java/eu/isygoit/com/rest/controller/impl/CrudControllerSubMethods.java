package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.controller.ICrudControllerSubMethods;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.model.IDomainAssignable;
import eu.isygoit.model.IIdAssignable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The type Crud controller sub methods.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class CrudControllerSubMethods<I extends Serializable, T extends IIdAssignable,
        M extends IIdentifiableDto,
        F extends M,
        S extends ICrudServiceMethod<I, T>>
        extends CrudControllerUtils<T, M, F, S>
        implements ICrudControllerSubMethods<I, T, M, F, S> {

    //Attention !!! should get the class type of th persist entity
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    public ResponseEntity<F> subCreate(F object) {
        log.info("Create {} request received", persistentClass.getSimpleName());

        try {
            // Utilisation de Optional pour améliorer la sécurité et la lisibilité
            var createdObject = Optional.of(object)
                    .map(o -> beforeCreate(o))
                    .map(o -> mapper().dtoToEntity(o))
                    .map(crudService()::create)
                    .map(o -> afterCreate(o))
                    .map(mapper()::entityToDto)
                    .orElseThrow(() -> new BadArgumentException("Object creation failed"));

            return ResponseFactory.ResponseOk(createdObject);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<F>> subUpdate(List<F> objects) {
        log.info("Update {} request received", persistentClass.getSimpleName());

        return Optional.ofNullable(objects)
                .filter(list -> !list.isEmpty())
                .map(list -> {
                    try {
                        var processedDtos = list.parallelStream()
                                .map(f -> beforeUpdate((I) f.getId(), f))
                                .map(f -> mapper().dtoToEntity(f))
                                .map(t -> crudService().update(t))
                                .map(t -> afterUpdate(t))
                                .toList();

                        return ResponseFactory.ResponseOk(mapper().listEntityToDto(processedDtos));
                    } catch (Throwable e) {
                        log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
                        return getBackExceptionResponse(e);
                    }
                })
                .orElseGet(ResponseFactory::ResponseBadRequest);
    }

    @Override
    public ResponseEntity<List<F>> subCreate(List<F> objects) {
        log.info("Create {} request received", persistentClass.getSimpleName());

        return Optional.ofNullable(objects)
                .filter(list -> !list.isEmpty())
                .map(list -> {
                    try {
                        var processedDtos = list.parallelStream()
                                .map(f -> beforeCreate(f))
                                .map(f -> mapper().dtoToEntity(f))
                                .map(t -> crudService().create(t))
                                .map(t -> afterCreate(t))
                                .toList();

                        return ResponseFactory.ResponseOk(mapper().listEntityToDto(processedDtos));
                    } catch (Throwable e) {
                        log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
                        return getBackExceptionResponse(e);
                    }
                })
                .orElseGet(ResponseFactory::ResponseBadRequest);
    }


    @Override
    public ResponseEntity<?> subDelete(RequestContextDto requestContext, I id) {
        log.info("Delete {} request received", persistentClass.getSimpleName());

        return Optional.ofNullable(id)
                .map(validId -> {
                    try {
                        if (beforeDelete(validId)) {
                            crudService().delete(requestContext.getSenderDomain(), validId);
                            afterDelete(validId);
                        }
                        return ResponseFactory.ResponseOk(exceptionHandler().handleMessage("object.deleted.successfully"));
                    } catch (Throwable e) {
                        log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
                        return getBackExceptionResponse(e);
                    }
                })
                .orElseGet(ResponseFactory::ResponseBadRequest);
    }


    @Override
    public ResponseEntity<?> subDelete(RequestContextDto requestContext, List<F> objects) {
        log.info("Delete {} request received", persistentClass.getSimpleName());

        return Optional.ofNullable(objects)
                .filter(list -> !list.isEmpty())
                .map(validObjects -> {
                    try {
                        if (beforeDelete(validObjects)) {
                            crudService().delete(requestContext.getSenderDomain(), mapper().listDtoToEntity(validObjects));
                            afterDelete(validObjects);
                        }
                        return ResponseFactory.ResponseOk(exceptionHandler().handleMessage("object.deleted.successfully"));
                    } catch (Throwable e) {
                        log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
                        return getBackExceptionResponse(e);
                    }
                })
                .orElseGet(ResponseFactory::ResponseBadRequest);
    }


    @Override
    public ResponseEntity<List<M>> subFindAll(RequestContextDto requestContext) {
        log.info("Find all {}s request received", persistentClass.getSimpleName());

        try {
            var list = Optional.ofNullable(
                            IDomainAssignable.class.isAssignableFrom(persistentClass) &&
                                    !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())
                                    ? crudService().findAll(requestContext.getSenderDomain())
                                    : crudService().findAll()
                    ).map(minDtoMapper()::listEntityToDto)
                    .orElseGet(List::of);

            if (list.isEmpty()) {
                return ResponseFactory.ResponseNoContent();
            }

            afterFindAll(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }


    @Override
    public ResponseEntity<List<M>> subFindAllDefault(RequestContextDto requestContext) {
        log.info("Find all {}s request received", persistentClass.getSimpleName());

        try {
            var entities = IDomainAssignable.class.isAssignableFrom(persistentClass) &&
                    !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())
                    ? crudService().findAll(DomainConstants.DEFAULT_DOMAIN_NAME)
                    : crudService().findAll();

            var list = Optional.ofNullable(entities)
                    .map(minDtoMapper()::listEntityToDto)
                    .orElseGet(List::of);

            if (list.isEmpty()) {
                return ResponseFactory.ResponseNoContent();
            }

            afterFindAll(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }


    @Override
    public ResponseEntity<List<M>> subFindAll(RequestContextDto requestContext, Integer page, Integer size) {
        log.info("Find all {}s by page/size request received {}/{}", persistentClass.getSimpleName(), page, size);

        try {
            var pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));

            var entities = IDomainAssignable.class.isAssignableFrom(persistentClass) &&
                    !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())
                    ? crudService().findAll(requestContext.getSenderDomain(), pageRequest)
                    : crudService().findAll(pageRequest);

            var list = Optional.ofNullable(entities)
                    .map(minDtoMapper()::listEntityToDto)
                    .orElseGet(List::of);

            if (list.isEmpty()) {
                return ResponseFactory.ResponseNoContent();
            }

            afterFindAll(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }


    @Override
    public ResponseEntity<List<F>> subFindAllFull(RequestContextDto requestContext) {
        log.info("Find all {}s request received", persistentClass.getSimpleName());

        try {
            var entities = IDomainAssignable.class.isAssignableFrom(persistentClass) &&
                    !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())
                    ? crudService().findAll(requestContext.getSenderDomain())
                    : crudService().findAll();

            var list = Optional.ofNullable(entities)
                    .map(mapper()::listEntityToDto)
                    .orElseGet(List::of);

            if (list.isEmpty()) {
                return ResponseFactory.ResponseNoContent();
            }

            afterFindAllFull(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<F>> subFindAllFull(RequestContextDto requestContext, Integer page, Integer size) {
        log.info("Find all {}s by page/size request received {}/{}", persistentClass.getSimpleName(), page, size);

        if (page == null || size == null) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            var pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));

            var entities = IDomainAssignable.class.isAssignableFrom(persistentClass) &&
                    !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())
                    ? crudService().findAll(requestContext.getSenderDomain(), pageRequest)
                    : crudService().findAll(pageRequest);

            var list = Optional.ofNullable(entities)
                    .map(mapper()::listEntityToDto)
                    .orElseGet(List::of);

            if (list.isEmpty()) {
                return ResponseFactory.ResponseNoContent();
            }

            afterFindAllFull(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }


    @Override
    public ResponseEntity<F> subFindById(RequestContextDto requestContext, I id) {
        log.info("Find {} by id request received", persistentClass.getSimpleName());

        try {
            var optionalObject = crudService().findById(id);

            // Utilisation de Optional pour éviter les appels potentiellement risqués à .get()
            var object = optionalObject.map(mapper()::entityToDto).orElse(null);

            if (object == null) {
                return ResponseFactory.ResponseNoContent();
            }

            return ResponseFactory.ResponseOk(afterFindById(object));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }


    @Override
    public ResponseEntity<Long> subGetCount(RequestContextDto requestContext) {
        log.info("Get count {} request received", persistentClass.getSimpleName());

        try {
            var count = IDomainAssignable.class.isAssignableFrom(persistentClass) &&
                    !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())
                    ? crudService().count(requestContext.getSenderDomain())
                    : crudService().count();

            return ResponseFactory.ResponseOk(count);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }


    @Override
    public ResponseEntity<F> subUpdate(I id, F object) {
        log.info("Update {} request received", persistentClass.getSimpleName());

        if (object == null || id == null) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            object.setId(id);

            // Utilisation de Optional pour améliorer la sécurité et la lisibilité
            var updatedObject = Optional.of(object)
                    .map(o -> beforeUpdate(id, o))
                    .map(o -> mapper().dtoToEntity(o))
                    .map(crudService()::update)
                    .map(o -> afterUpdate(o))
                    .map(mapper()::entityToDto)
                    .orElseThrow(() -> new BadArgumentException("Object update failed"));

            return ResponseFactory.ResponseOk(updatedObject);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }


    @Override
    public ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria) {
        try {
            // Utilisation de var pour simplifier les déclarations
            var criteriaList = CriteriaHelper.convertStringToCriteria(criteria, ",");

            // Sélection du domaine de recherche
            var senderDomain = IDomainAssignable.class.isAssignableFrom(persistentClass) &&
                    !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())
                    ? requestContext.getSenderDomain()
                    : null;

            // Récupération des objets filtrés par critères
            var list = this.mapper().listEntityToDto(
                    this.crudService().findAllByCriteriaFilter(senderDomain, criteriaList)
            );

            // Vérification de la liste et réponse appropriée
            return CollectionUtils.isEmpty(list)
                    ? ResponseFactory.ResponseNoContent()
                    : ResponseFactory.ResponseOk(list);

        } catch (Exception ex) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, ex); // Ajout de log pour faciliter le debugging
            return getBackExceptionResponse(ex);  // Réponse d'erreur spécifique
        }
    }


    @Override
    public ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria,
                                                                Integer page, Integer size) {
        try {
            // Conversion des critères en liste et préparation de la page
            var criteriaList = CriteriaHelper.convertStringToCriteria(criteria, ",");
            var pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));

            // Détermination conditionnelle du domaine de recherche
            var senderDomain = IDomainAssignable.class.isAssignableFrom(persistentClass) &&
                    !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())
                    ? requestContext.getSenderDomain()
                    : null;

            // Récupération des entités filtrées avec les critères et pagination
            var list = this.mapper().listEntityToDto(
                    this.crudService().findAllByCriteriaFilter(senderDomain, criteriaList, pageRequest)
            );

            // Vérification de la liste et retour approprié
            return CollectionUtils.isEmpty(list)
                    ? ResponseFactory.ResponseNoContent()
                    : ResponseFactory.ResponseOk(list);

        } catch (Exception ex) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, ex); // Ajout d'un log d'erreur pour faciliter le debugging
            return getBackExceptionResponse(ex);  // Retour de la réponse d'erreur spécifique
        }
    }


    @Override
    public ResponseEntity<Map<String, String>> subFindAllFilterCriteria() {
        try {
            // Récupération des critères à partir de CriteriaHelper
            var criteriaMap = CriteriaHelper.getCriteriaData(persistentClass);

            // Vérification si la map est vide et renvoi de la réponse appropriée
            return CollectionUtils.isEmpty(criteriaMap)
                    ? ResponseFactory.ResponseNoContent()
                    : ResponseFactory.ResponseOk(criteriaMap);

        } catch (Exception ex) {
            // Log de l'erreur pour faciliter le débogage
            log.error(CtrlConstants.ERROR_API_EXCEPTION, ex);
            return getBackExceptionResponse(ex);
        }
    }

    @Override
    public T afterCreate(T object) {
        return object;
    }

    @Override
    public F beforeUpdate(I id, F object) {
        return object;
    }

    @Override
    public F beforeCreate(F object) {
        return object;
    }

    @Override
    public T afterUpdate(T object) {
        return object;
    }

    @Override
    public boolean beforeDelete(I id) {
        return true;
    }

    @Override
    public boolean afterDelete(I id) {
        return true;
    }

    @Override
    public boolean beforeDelete(List<F> objects) {
        return true;
    }

    @Override
    public boolean afterDelete(List<F> objects) {
        return true;
    }

    @Override
    public F afterFindById(F object) {
        return object;
    }

    @Override
    public List<F> afterFindAllFull(RequestContextDto requestContext, List<F> list) {
        return list;
    }

    @Override
    public List<M> afterFindAll(RequestContextDto requestContext, List<M> list) {
        return list;
    }
}
