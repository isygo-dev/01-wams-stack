package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.controller.ICrudControllerSubMethods;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.model.AssignableDomain;
import eu.isygoit.model.AssignableId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The type Crud controller sub methods.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <M> the type parameter
 * @param <F> the type parameter
 * @param <S> the type parameter
 */
@Slf4j
public abstract class CrudControllerSubMethods<I extends Serializable, E extends AssignableId, M extends IIdentifiableDto, F extends M, S extends ICrudServiceMethod<I, E>>
        extends CrudControllerUtils<I, E, M, F, S>
        implements ICrudControllerSubMethods<I, E, M, F, S> {

    @Override
    public ResponseEntity<F> subCreate(F object) {
        log.info("Create {} request received", getPersistentClass().getSimpleName());
        if (object == null) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            object = this.beforeCreate(object);
            return ResponseFactory.ResponseOk(getMapper().entityToDto(this.afterCreate(this.getCrudService().create(getMapper().dtoToEntity(object)))));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<F>> subCreate(List<F> objects) {
        log.info("Create {} request received", getPersistentClass().getSimpleName());
        if (CollectionUtils.isEmpty(objects)) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            objects.forEach(F -> this.beforeCreate(F));
            List<E> entities = this.getCrudService().create(getMapper().listDtoToEntity(objects));
            entities.forEach(t -> this.afterCreate(t));
            return ResponseFactory.ResponseOk(getMapper().listEntityToDto(entities));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<?> subDelete(RequestContextDto requestContext, I id) {
        log.info("Delete {} request received", getPersistentClass().getSimpleName());
        if (id == null) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            if (this.beforeDelete(id)) {
                this.getCrudService().delete(requestContext.getSenderDomain(), id);
                this.afterDelete(id);
            }
            return ResponseFactory.ResponseOk(getExceptionHandler().handleMessage("object.deleted.successfully"));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<?> subDelete(RequestContextDto requestContext, List<F> objects) {
        log.info("Delete {} request received", getPersistentClass().getSimpleName());
        if (CollectionUtils.isEmpty(objects)) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            if (this.beforeDelete(objects)) {
                this.getCrudService().delete(requestContext.getSenderDomain(), getMapper().listDtoToEntity(objects));
                this.afterDelete(objects);
            }
            return ResponseFactory.ResponseOk(getExceptionHandler().handleMessage("object.deleted.successfully"));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<M>> subFindAll(RequestContextDto requestContext) {
        log.info("Find all {}s request received", getPersistentClass().getSimpleName());
        try {
            List<M> list = null;
            if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.getMinDtoMapper().listEntityToDto(this.getCrudService().findAll(requestContext.getSenderDomain()));
            } else {
                list = this.getMinDtoMapper().listEntityToDto(this.getCrudService().findAll());
            }

            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }

            this.afterFindAll(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<M>> subFindAllDefault(RequestContextDto requestContext) {
        log.info("Find all {}s request received", getPersistentClass().getSimpleName());
        try {
            List<M> list = null;
            if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.getMinDtoMapper().listEntityToDto(this.getCrudService().findAll(DomainConstants.DEFAULT_DOMAIN_NAME));
            } else {
                list = this.getMinDtoMapper().listEntityToDto(this.getCrudService().findAll());
            }

            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }

            this.afterFindAll(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<M>> subFindAll(RequestContextDto requestContext, Integer page, Integer size) {
        log.info("Find all {}s by page/size request received {}/{}", getPersistentClass().getSimpleName(), page, size);
        try {
            List<M> list = null;
            if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.getMinDtoMapper().listEntityToDto(this.getCrudService().findAll(requestContext.getSenderDomain(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            } else {
                list = this.getMinDtoMapper().listEntityToDto(this.getCrudService().findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            }

            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }

            this.afterFindAll(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<F>> subFindAllFull(RequestContextDto requestContext) {
        log.info("Find all {}s request received", getPersistentClass().getSimpleName());
        try {
            List<F> list = null;
            if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.getMapper().listEntityToDto(this.getCrudService().findAll(requestContext.getSenderDomain()));
            } else {
                list = this.getMapper().listEntityToDto(this.getCrudService().findAll());
            }

            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }

            this.afterFindAllFull(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<F>> subFindAllFull(RequestContextDto requestContext, Integer page, Integer size) {
        log.info("Find all {}s by page/size request received {}/{}", getPersistentClass().getSimpleName(), page, size);
        if (page == null || size == null) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            List<F> list = null;
            if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.getMapper().listEntityToDto(this.getCrudService().findAll(requestContext.getSenderDomain(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            } else {
                list = this.getMapper().listEntityToDto(this.getCrudService().findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            }

            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }

            this.afterFindAllFull(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }


    @Override
    public ResponseEntity<F> subFindById(RequestContextDto requestContext, I id) {
        log.info("Find {} by id request received", getPersistentClass().getSimpleName());
        try {
            final F object = this.getMapper().entityToDto(this.getCrudService().findById(id).get());
            if (object == null) {
                return ResponseFactory.ResponseNoContent();
            }

            return ResponseFactory.ResponseOk(this.afterFindById(object));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<Long> subGetCount(RequestContextDto requestContext) {
        log.info("Get count {} request received", getPersistentClass().getSimpleName());
        try {
            List<F> list = null;
            if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                return ResponseFactory.ResponseOk(this.getCrudService().count(requestContext.getSenderDomain()));
            } else {
                return ResponseFactory.ResponseOk(this.getCrudService().count());
            }
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<F> subUpdate(I id, F object) {
        log.info("Update {} request received", getPersistentClass().getSimpleName());
        if (object == null || id == null) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            object.setId(id);
            object = this.beforeUpdate(id, object);
            return ResponseFactory.ResponseOk(this.getMapper().entityToDto(this.afterUpdate(this.getCrudService().update(getMapper().dtoToEntity(object)))));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<F>> subUpdate(List<F> objects) {
        log.info("Update {} request received", getPersistentClass().getSimpleName());
        if (CollectionUtils.isEmpty(objects)) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            return ResponseFactory.ResponseOk(getMapper().listEntityToDto(this.getCrudService().update(getMapper().listDtoToEntity(objects))));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria) {
        try {
            List<F> list = null;
            if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.getMapper().listEntityToDto(this.getCrudService().findAllByCriteriaFilter(requestContext.getSenderDomain(), CriteriaHelper.convertStringToCriteria(criteria, ",")));
            } else {
                list = this.getMapper().listEntityToDto(this.getCrudService().findAllByCriteriaFilter(null, CriteriaHelper.convertStringToCriteria(criteria, ",")));
            }

            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }
            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }
            return ResponseFactory.ResponseOk(list);
        } catch (Exception ex) {
            return getBackExceptionResponse(ex);
        }
    }

    @Override
    public ResponseEntity<List<F>> subFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria,
                                                                Integer page, Integer size) {
        try {
            List<F> list = null;
            if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.getMapper().listEntityToDto(this.getCrudService().findAllByCriteriaFilter(requestContext.getSenderDomain(), CriteriaHelper.convertStringToCriteria(criteria, ",")
                        , PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            } else {
                list = this.getMapper().listEntityToDto(this.getCrudService().findAllByCriteriaFilter(null, CriteriaHelper.convertStringToCriteria(criteria, ",")
                        , PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            }
            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }
            return ResponseFactory.ResponseOk(list);
        } catch (Exception ex) {
            return getBackExceptionResponse(ex);
        }
    }

    @Override
    public ResponseEntity<Map<String, String>> subFindAllFilterCriteria() {
        try {
            Map<String, String> criteriaMap = CriteriaHelper.getCriteriaData(getPersistentClass());
            if (CollectionUtils.isEmpty(criteriaMap)) {
                return ResponseFactory.ResponseNoContent();
            }
            return ResponseFactory.ResponseOk(criteriaMap);
        } catch (Exception ex) {
            return getBackExceptionResponse(ex);
        }
    }

    @Override
    public F beforeCreate(F object) {
        return object;
    }

    @Override
    public E afterCreate(E object) {
        return object;
    }

    @Override
    public F beforeUpdate(I id, F object) {
        return object;
    }

    @Override
    public E afterUpdate(E object) {
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
