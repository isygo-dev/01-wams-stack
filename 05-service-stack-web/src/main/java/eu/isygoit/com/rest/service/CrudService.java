package eu.isygoit.com.rest.service;

import eu.isygoit.constants.LogConstants;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.exception.*;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IImageEntity;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.CancelableEntity;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import eu.isygoit.repository.JpaPagingAndSortingTenantAssignableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * The type Crud service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CrudService<I extends Serializable,
        T extends IIdAssignable<I>,
        R extends JpaPagingAndSortingRepository<T, I>>
        extends CrudServiceUtils<I, T, R>
        implements ICrudServiceMethod<I, T> {

    /**
     * The constant SHOULD_USE_SAAS_SPECIFIC_METHOD.
     */
    public static final String SHOULD_USE_SAAS_SPECIFIC_METHOD = "should use SAAS-specific method";
    //Attention !!! should get the class type of th persist entity
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    private static <I extends Serializable, T extends IIdAssignable<I>> void isObjectIdNotEmptyElseThrow(T object) {
        if (object.getId() == null) {
            throw new NullIdentifierException(object.getClass().getSimpleName() + ": with id " + object.getId());
        }
    }

    private static <I extends Serializable, T extends IIdAssignable<I>> void isObjectNotEmptyElseThrow(T object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
    }

    private static <I extends Serializable, T extends IIdAssignable<I>> void isListNotEmptyElseThrow(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long count() {
        isNotTenantAssignableElseThrow("count ");
        return repository().count();
    }

    private JpaPagingAndSortingTenantAssignableRepository isTenantAssignableElseThrow() {
        if (!ITenantAssignable.class.isAssignableFrom(persistentClass) ||
                !(repository() instanceof JpaPagingAndSortingTenantAssignableRepository jpaRepo)) {
            throw new OperationNotSupportedException("Entity is not tenant assignable: " + persistentClass.getSimpleName());
        }
        return jpaRepo;
    }

    private void isNotTenantAssignableElseThrow(String x) {
        if (!ITenantAssignable.class.isAssignableFrom(persistentClass) || !(repository() instanceof JpaPagingAndSortingTenantAssignableRepository)) {
            throw new OperationNotAllowedException(x + persistentClass.getSimpleName() + " " + SHOULD_USE_SAAS_SPECIFIC_METHOD);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long count(String tenant) {
        final JpaPagingAndSortingTenantAssignableRepository jpaRepo = isTenantAssignableElseThrow();

        return jpaRepo.countByTenantIgnoreCase(tenant);
    }

    @Override
    public boolean existsById(I id) {
        isNotTenantAssignableElseThrow("existsById ");

        return repository().existsById(id);
    }

    @Override
    public boolean existsById(String tenant, I id) {
        isTenantAssignableElseThrow();

        return repository().existsById(id);
    }

    @Override
    @Transactional
    public T create(T object) {
        isNotTenantAssignableElseThrow("create ");
        isObjectNotEmptyElseThrow(object);
        assignCodeIfEmpty(object);
        //perform create
        object = this.beforeCreate(object);
        return this.afterCreate(repository().save(object));
    }

    @Override
    @Transactional
    public T create(String tenant, T object) {
        isTenantAssignableElseThrow();
        isObjectNotEmptyElseThrow(object);
        assignCodeIfEmpty(object);
        ((ITenantAssignable) object).setTenant(tenant);
        //perform create
        object = this.beforeCreate(object);
        return this.afterCreate(repository().save(object));
    }

    @Override
    public List<T> create(List<T> objects) {
        isNotTenantAssignableElseThrow("create ");
        isListNotEmptyElseThrow(objects);
        return objects.stream()
                .map(this::create)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<T> create(String tenant, List<T> objects) {
        isTenantAssignableElseThrow();

        isListNotEmptyElseThrow(objects);
        return objects.stream()
                .map(object -> create(tenant, object))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public T createAndFlush(T object) {
        isNotTenantAssignableElseThrow("createAndFlush ");
        isObjectNotEmptyElseThrow(object);
        assignCodeIfEmpty(object);
        //perform create
        object = this.beforeCreate(object);
        return this.afterCreate(repository().saveAndFlush(object));
    }

    @Override
    @Transactional
    public T createAndFlush(String tenant, T object) {
        isTenantAssignableElseThrow();
        isObjectNotEmptyElseThrow(object);
        assignCodeIfEmpty(object);
        ((ITenantAssignable) object).setTenant(tenant);
        //perform create
        object = this.beforeCreate(object);
        return this.afterCreate(repository().saveAndFlush(object));
    }

    @Override
    @Transactional
    public T update(T object) {
        isNotTenantAssignableElseThrow("update ");
        isObjectNotEmptyElseThrow(object);
        isObjectIdNotEmptyElseThrow(object);
        keepOriginalAttributes(object);
        assignCodeIfEmpty(object);
        //perform update
        object = beforeUpdate(object);
        return afterUpdate(repository().save(object));
    }

    @Override
    @Transactional
    public T update(String tenant, T object) {
        final JpaPagingAndSortingTenantAssignableRepository jpaRepo = isTenantAssignableElseThrow();
        isObjectNotEmptyElseThrow(object);
        isObjectIdNotEmptyElseThrow(object);
        isAllowedForTenantElseThrow(tenant, object.getId());
        keepOriginalAttributes(object);
        assignCodeIfEmpty(object);
        //perform update
        object = beforeUpdate(object);
        return afterUpdate(repository().save(object));
    }

    private void isAllowedForTenantElseThrow(String tenant, I id) {
        if (ITenantAssignable.class.isAssignableFrom(persistentClass) &&
                (repository() instanceof JpaPagingAndSortingTenantAssignableRepository jpaRepo)) {
            if (!tenant.equals(((ITenantAssignable) jpaRepo.findById(id).get()).getTenant())) {
                throw new TenantNotAllowedException("Tenant has no acces to the object");
            }
        }
    }

    @Override
    @Transactional
    public List<T> update(List<T> objects) {
        isNotTenantAssignableElseThrow("update ");
        isListNotEmptyElseThrow(objects);
        return objects.stream()
                .map(this::update)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<T> update(String tenant, List<T> objects) {
        isTenantAssignableElseThrow();
        isListNotEmptyElseThrow(objects);

        return objects.stream()
                .map(t -> update(tenant, t))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public T updateAndFlush(T object) {
        isNotTenantAssignableElseThrow("updateAndFlush ");
        isObjectNotEmptyElseThrow(object);
        isObjectIdNotEmptyElseThrow(object);
        keepOriginalAttributes(object);
        assignCodeIfEmpty(object);
        object = beforeUpdate(object);
        return afterUpdate(repository().saveAndFlush(object));
    }

    @Override
    @Transactional
    public T updateAndFlush(String tenant, T object) {
        isTenantAssignableElseThrow();

        isObjectNotEmptyElseThrow(object);
        if (Objects.isNull(object.getId())) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_ID_PROVIDED);
        }

        if (!findById(tenant, object.getId()).isPresent()) {
            throw new ObjectNotFoundException(" with id: " + object.getId() + " for tenant: " + tenant);
        }

        return updateAndFlush(object);
    }

    private void keepOriginalAttributes(T object) {
        Optional<T> optional = repository().findById(object.getId());
        if (optional.isPresent()) {
            T existing = optional.get();
            applyIfInstance(object, existing, IFileEntity.class, (t, s) -> {
                t.setType(s.getType());
                t.setFileName(s.getFileName());
                t.setOriginalFileName(s.getOriginalFileName());
                t.setPath(s.getPath());
                t.setExtension(s.getExtension());
            });
            applyIfInstance(object, existing, IImageEntity.class, (t, s) -> {
                t.setImagePath(s.getImagePath());
            });
        }
    }

    private <I> void applyIfInstance(T target, T source, Class<I> type, BiConsumer<I, I> action) {
        if (type.isInstance(target) && type.isInstance(source)) {
            action.accept(type.cast(target), type.cast(source));
        }
    }

    @Override
    @Transactional
    public void delete(String tenant, List<T> objects) {
        isTenantAssignableElseThrow();

        isListNotEmptyElseThrow(objects);
        this.beforeDelete(objects);
        objects.parallelStream()
                .map(T::getId)
                .forEach(id -> {
                    try {
                        delete(tenant, id);
                    } catch (OperationNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                });
        this.afterDelete(objects);
    }

    @Override
    @Transactional
    public void delete(String tenant, I id) {
        isTenantAssignableElseThrow();

        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_ID_PROVIDED);
        }
        this.findById(id).ifPresentOrElse(object -> {
            if (object instanceof ITenantAssignable entity &&
                    !TenantConstants.SUPER_TENANT_NAME.equals(tenant) &&
                    !tenant.equals(entity.getTenant())) {
                throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " with id: " + id);
            }
            beforeDelete(id);
            if (object instanceof CancelableEntity cancelable) {
                if (!cancelable.getCheckCancel()) {
                    cancelable.setCheckCancel(true);
                    cancelable.setCancelDate(new Date());
                    update(object);
                }
            } else {
                repository().deleteById(id);
            }
            afterDelete(id);
        }, () -> {
            throw new ObjectNotFoundException(" with id: " + id);
        });
    }

    @Override
    @Transactional
    public void delete(List<T> objects) {
        isNotTenantAssignableElseThrow("delete ");
        isListNotEmptyElseThrow(objects);
        this.beforeDelete(objects);
        objects.parallelStream()
                .map(T::getId)
                .forEach(id -> delete(id));
        this.afterDelete(objects);
    }

    @Override
    @Transactional
    public void delete(I id) {
        isNotTenantAssignableElseThrow("delete ");
        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
        this.findById(id).ifPresentOrElse(object -> {
            beforeDelete(id);
            if (object instanceof CancelableEntity cancelable) {
                if (!cancelable.getCheckCancel()) {
                    cancelable.setCheckCancel(true);
                    cancelable.setCancelDate(new Date());
                    update(object);
                }
            } else {
                repository().deleteById(id);
            }
            afterDelete(id);
        }, () -> {
            throw new ObjectNotFoundException(" with id: " + id);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        isNotTenantAssignableElseThrow("findAll ");
        List<T> list = repository().findAll();
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : this.afterFindAll(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(Pageable pageable) {
        isNotTenantAssignableElseThrow("findAll ");
        List<T> content = repository().findAll(pageable).getContent();
        return content.isEmpty() ? Collections.emptyList() : this.afterFindAll(content);
    }

    @Override
    public List<T> findAll(String tenant) {
        final JpaPagingAndSortingTenantAssignableRepository jpaRepo = isTenantAssignableElseThrow();

        List<T> list = jpaRepo.findByTenantIgnoreCase(tenant);
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : this.afterFindAll(list);
    }

    @Override
    public List<T> findAll(String tenant, Pageable pageable) {
        final JpaPagingAndSortingTenantAssignableRepository jpaRepo = isTenantAssignableElseThrow();

        Page<T> page = jpaRepo.findByTenantIgnoreCase(tenant, pageable);
        return page.isEmpty() ? Collections.emptyList() : this.afterFindAll(page.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(I id) throws ObjectNotFoundException {
        isNotTenantAssignableElseThrow("findById ");
        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
        return repository().findById(id)
                .map(entity -> afterFindById(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(String tenant, I id) throws ObjectNotFoundException, OperationNotSupportedException {
        isTenantAssignableElseThrow();

        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
        return repository().findById(id)
                .filter(t -> ((ITenantAssignable) t).getTenant().equals(tenant))
                .map(entity -> afterFindById(entity));
    }

    @Override
    @Transactional
    public T saveOrUpdate(T object) {
        isNotTenantAssignableElseThrow("saveOrUpdate ");
        isObjectNotEmptyElseThrow(object);
        if (Objects.isNull(object.getId())) {
            return this.create(object);
        } else {
            return this.update(object);
        }
    }

    @Override
    @Transactional
    public List<T> saveOrUpdate(List<T> objects) {
        isNotTenantAssignableElseThrow("saveOrUpdate ");
        isListNotEmptyElseThrow(objects);
        return objects.stream()
                .map(this::saveOrUpdate)
                .collect(Collectors.toList());
    }

    @Override
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria) {
        isTenantAssignableElseThrow();

        if (CollectionUtils.isEmpty(criteria)) {
            log.error("Criteria filter map is null");
            throw new EmptyCriteriaFilterException("Criteria filter map is null");
        }
        Specification<T> specification = CriteriaHelper.buildSpecification(tenant, criteria, persistentClass);
        return repository().findAll(specification);
    }

    @Override
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria, PageRequest pageRequest) {
        isTenantAssignableElseThrow();
        if (CollectionUtils.isEmpty(criteria)) {
            log.error("Criteria filter map is null");
            throw new EmptyCriteriaFilterException("Criteria filter map is null");
        }
        Specification<T> specification = CriteriaHelper.buildSpecification(tenant, criteria, persistentClass);
        return repository().findAll(specification, pageRequest).getContent();
    }

    @Override
    @Transactional
    public T saveOrUpdate(String tenant, T object) {
        isTenantAssignableElseThrow();

        isObjectNotEmptyElseThrow(object);

        ((ITenantAssignable) object).setTenant(tenant);
        return saveOrUpdate(object);
    }

    @Override
    @Transactional
    public List<T> saveOrUpdate(String tenant, List<T> objects) {
        isTenantAssignableElseThrow();

        isListNotEmptyElseThrow(objects);
        return objects.stream()
                .map(object -> {
                    try {
                        return saveOrUpdate(tenant, object);
                    } catch (OperationNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria) {
        isNotTenantAssignableElseThrow("findAllByCriteriaFilter ");
        if (CollectionUtils.isEmpty(criteria)) {
            log.error("Criteria filter map is null");
            throw new EmptyCriteriaFilterException("Criteria filter map is null");
        }
        Specification<T> specification = CriteriaHelper.buildSpecification(null, criteria, persistentClass);
        return repository().findAll(specification);
    }

    @Override
    public List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria, PageRequest pageRequest) {
        isNotTenantAssignableElseThrow("findAllByCriteriaFilter ");
        if (CollectionUtils.isEmpty(criteria)) {
            log.error("Criteria filter map is null");
            throw new EmptyCriteriaFilterException("Criteria filter map is null");
        }
        Specification<T> specification = CriteriaHelper.buildSpecification(null, criteria, persistentClass);
        return repository().findAll(specification, pageRequest).getContent();
    }

    @Override
    public T beforeCreate(T object) {
        return object;
    }

    @Override
    public T afterCreate(T object) {
        return object;
    }

    @Override
    public void beforeDelete(I id) {
    }

    @Override
    public void afterDelete(I id) {
    }

    @Override
    public void beforeDelete(List<T> objects) {
    }

    @Override
    public void afterDelete(List<T> objects) {
    }

    @Override
    public T beforeUpdate(T object) {
        return object;
    }

    @Override
    public T afterUpdate(T object) {
        return object;
    }

    @Override
    public List<T> afterFindAll(List<T> list) {
        return list;
    }

    @Override
    public T afterFindById(T object) {
        return object;
    }
}