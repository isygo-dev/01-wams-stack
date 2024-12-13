package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.controller.ICrudControllerSubMethods;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ISAASEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

/**
 * The type Crud controller sub methods.
 *
 * @param <I>     the type parameter
 * @param <T>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 * @param <S>     the type parameter
 */
@Slf4j
public abstract class CrudControllerSubMethods<I, T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND, S
        extends ICrudServiceMethod<I, T>>
        extends CrudControllerUtils<T, MIND, FULLD, S>
        implements ICrudControllerSubMethods<I, T, MIND, FULLD, S> {

    //Attention !!! should get the class type of th persist entity
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    public ResponseEntity<FULLD> subCreate(FULLD object) {
        log.info("Create {} request received", persistentClass.getSimpleName());
        if (object == null) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            object = this.beforeCreate(object);
            return ResponseFactory.ResponseOk(mapper().entityToDto(this.afterCreate(this.crudService().create(mapper().dtoToEntity(object)))));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<FULLD>> subCreate(List<FULLD> objects) {
        log.info("Create {} request received", persistentClass.getSimpleName());
        if (CollectionUtils.isEmpty(objects)) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            objects.forEach(FULLD -> this.beforeCreate(FULLD));
            List<T> entities = this.crudService().create(mapper().listDtoToEntity(objects));
            entities.forEach(t -> this.afterCreate(t));
            return ResponseFactory.ResponseOk(mapper().listEntityToDto(entities));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<?> subDelete(RequestContextDto requestContext, I id) {
        log.info("Delete {} request received", persistentClass.getSimpleName());
        if (id == null) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            if (this.beforeDelete(id)) {
                this.crudService().delete(requestContext.getSenderDomain(), id);
                this.afterDelete(id);
            }
            return ResponseFactory.ResponseOk(exceptionHandler().handleMessage("object.deleted.successfully"));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<?> subDelete(RequestContextDto requestContext, List<FULLD> objects) {
        log.info("Delete {} request received", persistentClass.getSimpleName());
        if (CollectionUtils.isEmpty(objects)) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            if (this.beforeDelete(objects)) {
                this.crudService().delete(requestContext.getSenderDomain(), mapper().listDtoToEntity(objects));
                this.afterDelete(objects);
            }
            return ResponseFactory.ResponseOk(exceptionHandler().handleMessage("object.deleted.successfully"));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<MIND>> subFindAll(RequestContextDto requestContext) {
        log.info("Find all {}s request received", persistentClass.getSimpleName());
        try {
            List<MIND> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = (List<MIND>) this.minDtoMapper().listEntityToDto(this.crudService().findAll(requestContext.getSenderDomain()));
            } else {
                list = (List<MIND>) this.minDtoMapper().listEntityToDto(this.crudService().findAll());
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
    public ResponseEntity<List<MIND>> subFindAllDefault(RequestContextDto requestContext) {
        log.info("Find all {}s request received", persistentClass.getSimpleName());
        try {
            List<MIND> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = (List<MIND>) this.minDtoMapper().listEntityToDto(this.crudService().findAll(DomainConstants.DEFAULT_DOMAIN_NAME));
            } else {
                list = (List<MIND>) this.minDtoMapper().listEntityToDto(this.crudService().findAll());
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
    public ResponseEntity<List<MIND>> subFindAll(RequestContextDto requestContext, Integer page, Integer size) {
        log.info("Find all {}s by page/size request received {}/{}", persistentClass.getSimpleName(), page, size);
        try {
            List<MIND> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = (List<MIND>) this.minDtoMapper().listEntityToDto(this.crudService().findAll(requestContext.getSenderDomain(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            } else {
                list = (List<MIND>) this.minDtoMapper().listEntityToDto(this.crudService().findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
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
    public ResponseEntity<List<FULLD>> subFindAllFull(RequestContextDto requestContext) {
        log.info("Find all {}s request received", persistentClass.getSimpleName());
        try {
            List<FULLD> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.mapper().listEntityToDto(this.crudService().findAll(requestContext.getSenderDomain()));
            } else {
                list = this.mapper().listEntityToDto(this.crudService().findAll());
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
    public ResponseEntity<List<FULLD>> subFindAllFull(RequestContextDto requestContext, Integer page, Integer size) {
        log.info("Find all {}s by page/size request received {}/{}", persistentClass.getSimpleName(), page, size);
        if (page == null || size == null) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            List<FULLD> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.mapper().listEntityToDto(this.crudService().findAll(requestContext.getSenderDomain(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            } else {
                list = this.mapper().listEntityToDto(this.crudService().findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
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
    public ResponseEntity<FULLD> subFindById(RequestContextDto requestContext, I id) {
        log.info("Find {} by id request received", persistentClass.getSimpleName());
        try {
            final FULLD object = this.mapper().entityToDto(this.crudService().findById(id));
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
        log.info("Get count {} request received", persistentClass.getSimpleName());
        try {
            List<FULLD> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                return ResponseFactory.ResponseOk(this.crudService().count(requestContext.getSenderDomain()));
            } else {
                return ResponseFactory.ResponseOk(this.crudService().count());
            }
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<FULLD> subUpdate(I id, FULLD object) {
        log.info("Update {} request received", persistentClass.getSimpleName());
        if (object == null || id == null) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            object.setId(id);
            object = this.beforeUpdate(id, object);
            return ResponseFactory.ResponseOk(this.mapper().entityToDto(this.afterUpdate(this.crudService().update(mapper().dtoToEntity(object)))));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<FULLD>> subUpdate(List<FULLD> objects) {
        log.info("Update {} request received", persistentClass.getSimpleName());
        if (CollectionUtils.isEmpty(objects)) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            return ResponseFactory.ResponseOk(mapper().listEntityToDto(this.crudService().update(mapper().listDtoToEntity(objects))));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<FULLD>> subFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria) {
        try {
            List<FULLD> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.mapper().listEntityToDto(this.crudService().findAllByCriteriaFilter(requestContext.getSenderDomain(), CriteriaHelper.convertStringToCriteria(criteria, ",")));
            } else {
                list = this.mapper().listEntityToDto(this.crudService().findAllByCriteriaFilter(null, CriteriaHelper.convertStringToCriteria(criteria, ",")));
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
    public ResponseEntity<List<FULLD>> subFindAllFilteredByCriteria(RequestContextDto requestContext, String criteria,
                                                                    Integer page, Integer size) {
        try {
            List<FULLD> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.mapper().listEntityToDto(this.crudService().findAllByCriteriaFilter(requestContext.getSenderDomain(), CriteriaHelper.convertStringToCriteria(criteria, ",")
                        , PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            } else {
                list = this.mapper().listEntityToDto(this.crudService().findAllByCriteriaFilter(null, CriteriaHelper.convertStringToCriteria(criteria, ",")
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
            Map<String, String> criteriaMap = CriteriaHelper.getCriteriaData(persistentClass);
            if (CollectionUtils.isEmpty(criteriaMap)) {
                return ResponseFactory.ResponseNoContent();
            }
            return ResponseFactory.ResponseOk(criteriaMap);
        } catch (Exception ex) {
            return getBackExceptionResponse(ex);
        }
    }

    @Override
    public FULLD beforeCreate(FULLD object) {
        return object;
    }

    @Override
    public T afterCreate(T object) {
        return object;
    }

    @Override
    public FULLD beforeUpdate(I id, FULLD object) {
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
    public boolean beforeDelete(List<FULLD> objects) {
        return true;
    }

    @Override
    public boolean afterDelete(List<FULLD> objects) {
        return true;
    }

    @Override
    public FULLD afterFindById(FULLD object) {
        return object;
    }

    @Override
    public List<FULLD> afterFindAllFull(RequestContextDto requestContext, List<FULLD> list) {
        return list;
    }

    @Override
    public List<MIND> afterFindAll(RequestContextDto requestContext, List<MIND> list) {
        return list;
    }
}
