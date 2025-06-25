package eu.isygoit.com.rest.service;

import eu.isygoit.constants.TenantConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.*;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IImageEntity;
import eu.isygoit.model.jakarta.CancelableEntity;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import eu.isygoit.repository.JpaPagingAndSortingTenantAssignableRepository;
import jakarta.transaction.NotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];

    @Override
    @Transactional(readOnly = true)
    public Long count() {
        return repository().count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long count(String tenant) throws NotSupportedException {
        if (!(repository() instanceof JpaPagingAndSortingTenantAssignableRepository jpaRepo)) {
            throw new NotSupportedException("Entity not tenant assignable: " + persistentClass.getSimpleName());
        }

        return jpaRepo.countByTenantIgnoreCase(tenant);
    }

    @Override
    public boolean existsById(I id) {
        return repository().existsById(id);
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
    @Transactional
    public T create(T object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (object.getId() != null) {
            throw new ObjectAlreadyExistsException(object.getClass().getSimpleName() + ": with id " + object.getId());
        }

        assignCodeIfEmpty(object);
        object = this.beforeCreate(object);
        return this.afterCreate((T) repository().save(object));
    }

    @Override
    @Transactional
    public T createAndFlush(T object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (object.getId() != null) {
            throw new ObjectAlreadyExistsException(object.getClass().getSimpleName() + ": with id " + object.getId());
        }

        assignCodeIfEmpty(object);
        object = this.beforeCreate(object);
        return this.afterCreate((T) repository().saveAndFlush(object));
    }

    @Override
    public List<T> create(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        return objects.stream()
                .map(this::create)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public T update(T object) {
        if (object == null) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (object.getId() == null) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_ID_PROVIDED);
        }

        keepOriginalAttributes(object);

        assignCodeIfEmpty(object);

        object = beforeUpdate(object);

        return afterUpdate((T) repository().save(object));
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
    public T updateAndFlush(T object) {
        if (object == null) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (object.getId() == null) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_ID_PROVIDED);
        }

        keepOriginalAttributes(object);

        assignCodeIfEmpty(object);

        object = beforeUpdate(object);

        return afterUpdate((T) repository().saveAndFlush(object));
    }

    @Override
    @Transactional
    public List<T> update(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        return objects.stream()
                .map(this::update)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(String senderTenant, List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        this.beforeDelete(objects);

        objects.parallelStream()
                .map(T::getId)
                .forEach(id -> delete(senderTenant, id));

        this.afterDelete(objects);
    }


    @Override
    @Transactional
    public void delete(String senderTenant, I id) {
        // Validate the input id argument using Objects.requireNonNull
        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_ID_PROVIDED);
        }

        this.findById(id).ifPresentOrElse(object -> {
            if (object instanceof ITenantAssignable entity &&
                    !TenantConstants.SUPER_TENANT_NAME.equals(senderTenant) &&
                    !senderTenant.equals(entity.getTenant())) {
                throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " with id: " + id);
            }

            beforeDelete(id);

            if (object instanceof CancelableEntity cancelable) {
                if (!cancelable.getCheckCancel()) { // check if already canceled
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
        if (ITenantAssignable.class.isAssignableFrom(persistentClass)) {
            throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " " + SHOULD_USE_SAAS_SPECIFIC_METHOD);
        }

        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        this.beforeDelete(objects);

        objects.parallelStream()
                .map(T::getId)
                .forEach(id -> delete(id));

        this.afterDelete(objects);
    }

    @Override
    @Transactional
    public void delete(I id) {
        if (ITenantAssignable.class.isAssignableFrom(persistentClass) || repository() instanceof JpaPagingAndSortingTenantAssignableRepository) {
            throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " " + SHOULD_USE_SAAS_SPECIFIC_METHOD);
        }

        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        this.findById(id).ifPresentOrElse(object -> {
            beforeDelete(id);

            if (object instanceof CancelableEntity cancelable) {
                if (!cancelable.getCheckCancel()) { // check if already canceled
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
    @Transactional(readOnly = true)
    public List<T> findAll() {
        if (ITenantAssignable.class.isAssignableFrom(persistentClass) ||
                repository() instanceof JpaPagingAndSortingTenantAssignableRepository) {
            throw new OperationNotAllowedException("FindAll for " + persistentClass.getSimpleName() + " " + SHOULD_USE_SAAS_SPECIFIC_METHOD);
        }

        List<T> list = repository().findAll();
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : this.afterFindAll(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(Pageable pageable) {
        if (ITenantAssignable.class.isAssignableFrom(persistentClass) ||
                repository() instanceof JpaPagingAndSortingTenantAssignableRepository) {
            throw new OperationNotAllowedException("FindAll for " + persistentClass.getSimpleName() + " " + SHOULD_USE_SAAS_SPECIFIC_METHOD);
        }

        List<T> content = repository().findAll(pageable).getContent();
        return content.isEmpty() ? Collections.emptyList() : this.afterFindAll(content);
    }

    @Override
    public List<T> findAll(String tenant) throws NotSupportedException {
        if (!ITenantAssignable.class.isAssignableFrom(persistentClass) ||
                !(repository() instanceof JpaPagingAndSortingTenantAssignableRepository jpaRepo)) {
            throw new NotSupportedException("Entity not tenant assignable: " + persistentClass.getSimpleName());
        }

        List<T> list = jpaRepo.findByTenantIgnoreCase(tenant);
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : this.afterFindAll(list);
    }

    public List<T> findAll(String tenant, Pageable pageable) throws NotSupportedException {
        if (!ITenantAssignable.class.isAssignableFrom(persistentClass) ||
                !(repository() instanceof JpaPagingAndSortingTenantAssignableRepository jpaRepo)) {
            throw new NotSupportedException("Entity not tenant assignable: " + persistentClass.getSimpleName());
        }

        Page<T> page = jpaRepo.findByTenantIgnoreCase(tenant, pageable);
        return page.isEmpty() ? Collections.emptyList() : this.afterFindAll(page.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(I id) throws ObjectNotFoundException {
        if (ITenantAssignable.class.isAssignableFrom(persistentClass) || repository() instanceof JpaPagingAndSortingTenantAssignableRepository) {
            throw new OperationNotAllowedException("findById " + persistentClass.getSimpleName() + " " + SHOULD_USE_SAAS_SPECIFIC_METHOD);
        }

        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        // Retrieve the entity and apply afterFindById if present
        return repository().findById(id)
                .map(entity -> afterFindById((T) entity));  // Using lambda instead of method reference
    }

    @Override
    @Transactional
    public T saveOrUpdate(T object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (Objects.isNull(object.getId())) { // to create
            return this.create(object);
        } else { //to update
            return this.update(object);
        }
    }

    @Override
    @Transactional
    public List<T> saveOrUpdate(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        return objects.stream()
                .map(this::saveOrUpdate)
                .collect(Collectors.toList());
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

    @Override
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria) {
        if (!StringUtils.hasText(tenant) && ITenantAssignable.class.isAssignableFrom(persistentClass) || repository() instanceof JpaPagingAndSortingTenantAssignableRepository) {
            throw new OperationNotAllowedException("findAllByCriteriaFilter " + persistentClass.getSimpleName() + " " + SHOULD_USE_SAAS_SPECIFIC_METHOD);
        }

        if (CollectionUtils.isEmpty(criteria)) {
            log.error("Criteria filter map is null");
            throw new EmptyCriteriaFilterException("Criteria filter map is null");
        }

        //get criteria data to validate filter
        Specification<T> specification = CriteriaHelper.buildSpecification(tenant, criteria, persistentClass);
        return repository().findAll(specification);
    }

    @Override
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria, PageRequest pageRequest) {
        if (!StringUtils.hasText(tenant) && ITenantAssignable.class.isAssignableFrom(persistentClass) || repository() instanceof JpaPagingAndSortingTenantAssignableRepository) {
            throw new OperationNotAllowedException("findAllByCriteriaFilter " + persistentClass.getSimpleName() + " " + SHOULD_USE_SAAS_SPECIFIC_METHOD);
        }

        if (CollectionUtils.isEmpty(criteria)) {
            log.error("Criteria filter map is null");
            throw new EmptyCriteriaFilterException("Criteria filter map is null");
        }

        //get criteria data to validate filter
        Specification<T> specification = CriteriaHelper.buildSpecification(tenant, criteria, persistentClass);
        return repository().findAll(specification, pageRequest).getContent();
    }
}
