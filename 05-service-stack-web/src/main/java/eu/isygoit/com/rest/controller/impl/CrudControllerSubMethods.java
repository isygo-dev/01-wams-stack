package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.controller.ICrudControllerSubMethods;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.exception.ObjectNotFoundException;
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
import java.util.Objects;

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
        if (Objects.isNull(object)) {
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
            return ResponseFactory.ResponseOk(mapper().listEntityToDto(objects.stream().map(FULLD -> this.crudService()
                    .create(mapper().dtoToEntity(this.beforeCreate(FULLD)))).toList()));
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<String> subDelete(RequestContextDto requestContext, I id) {
        log.info("Delete {} request received", persistentClass.getSimpleName());
        if (Objects.isNull(id)) {
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
    public ResponseEntity<String> subDelete(RequestContextDto requestContext, List<FULLD> objects) {
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
    public ResponseEntity<List<MIND>> subGetAll(RequestContextDto requestContext) {
        log.info("Find all {}s request received", persistentClass.getSimpleName());
        try {
            List<MIND> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.minDtoMapper().listEntityToDto(this.crudService().getAll(requestContext.getSenderDomain()));
            } else {
                list = this.minDtoMapper().listEntityToDto(this.crudService().getAll());
            }

            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }

            this.afterGetAll(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<MIND>> subGetAllDefault(RequestContextDto requestContext) {
        log.info("Find all {}s request received", persistentClass.getSimpleName());
        try {
            List<MIND> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.minDtoMapper().listEntityToDto(this.crudService().getAll(DomainConstants.DEFAULT_DOMAIN_NAME));
            } else {
                list = this.minDtoMapper().listEntityToDto(this.crudService().getAll());
            }

            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }

            this.afterGetAll(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<MIND>> subGetAllPaged(RequestContextDto requestContext, int page, int size) {
        log.info("Find all {}s by page/size request received {}/{}", persistentClass.getSimpleName(), page, size);
        try {
            List<MIND> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.minDtoMapper().listEntityToDto(this.crudService().getAll(requestContext.getSenderDomain(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            } else {
                list = this.minDtoMapper().listEntityToDto(this.crudService().getAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            }

            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }

            this.afterGetAll(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<FULLD>> subGetAllFull(RequestContextDto requestContext) {
        log.info("Find all {}s request received", persistentClass.getSimpleName());
        try {
            List<FULLD> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.mapper().listEntityToDto(this.crudService().getAll(requestContext.getSenderDomain()));
            } else {
                list = this.mapper().listEntityToDto(this.crudService().getAll());
            }

            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }

            this.afterGetAllFull(requestContext, list);
            return ResponseFactory.ResponseOk(list);
        } catch (Throwable e) {
            log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<FULLD>> subGetAllFullPaged(RequestContextDto requestContext, int page, int size) {
        log.info("Find all {}s by page/size request received {}/{}", persistentClass.getSimpleName(), page, size);
        if (Objects.isNull(page) || Objects.isNull(size)) {
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            List<FULLD> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.mapper().listEntityToDto(this.crudService().getAll(requestContext.getSenderDomain(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            } else {
                list = this.mapper().listEntityToDto(this.crudService().getAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            }

            if (CollectionUtils.isEmpty(list)) {
                return ResponseFactory.ResponseNoContent();
            }

            this.afterGetAllFull(requestContext, list);
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
            return ResponseFactory.ResponseOk(this.afterFindById(this.mapper().entityToDto(this.crudService().getById(id)
                    .orElseThrow(() -> new ObjectNotFoundException(this.persistentClass.getSimpleName() + " with id " + id)))));
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
        if (Objects.isNull(object) || Objects.isNull(id)) {
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
    public ResponseEntity<List<FULLD>> subGetAllFiltered(RequestContextDto requestContext, String criteria) {
        try {
            List<FULLD> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.mapper().listEntityToDto(this.crudService().getAllByCriteriaFilter(requestContext.getSenderDomain(), CriteriaHelper.convertStringToCriteria(criteria, ",")));
            } else {
                list = this.mapper().listEntityToDto(this.crudService().getAllByCriteriaFilter(null, CriteriaHelper.convertStringToCriteria(criteria, ",")));
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
    public ResponseEntity<List<FULLD>> subGetAllFilteredPaged(RequestContextDto requestContext, String criteria,
                                                              int page, int size) {
        try {
            List<FULLD> list = null;
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())) {
                list = this.mapper().listEntityToDto(this.crudService().getAllByCriteriaFilter(requestContext.getSenderDomain(), CriteriaHelper.convertStringToCriteria(criteria, ",")
                        , PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"))));
            } else {
                list = this.mapper().listEntityToDto(this.crudService().getAllByCriteriaFilter(null, CriteriaHelper.convertStringToCriteria(criteria, ",")
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
    public ResponseEntity<Map<String, String>> subGetAllFilterCriteria() {
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
    public List<FULLD> afterGetAllFull(RequestContextDto requestContext, List<FULLD> list) {
        return list;
    }

    @Override
    public List<MIND> afterGetAll(RequestContextDto requestContext, List<MIND> list) {
        return list;
    }
}
