package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.ICodifiableService;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.*;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.model.ICodifiable;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ISAASEntity;
import eu.isygoit.model.jakarta.CancelableEntity;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import eu.isygoit.repository.JpaPagingAndSortingSAASRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.NotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * The type Crud service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CrudService<I, T extends IIdEntity, R extends JpaPagingAndSortingRepository> extends CrudServiceUtils<T, R>
        implements ICrudServiceMethod<I, T> {

    //Attention !!! should get the class type of th persist entity
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    @Transactional(readOnly = true)
    public Long count() {
        return repository().count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long count(String domain) {
        if (JpaPagingAndSortingSAASRepository.class.isAssignableFrom(repository().getClass())) {
            return ((JpaPagingAndSortingSAASRepository) repository()).countByDomainIgnoreCase(domain);
        } else {
            throw new UnsupportedOperationException("this is not a SAS entity/repository: " + repository().getClass().getSimpleName());
        }
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

        if (object.getId() == null) {
            object = this.beforeCreate(object);
            if (ICodifiableService.class.isAssignableFrom(this.getClass()) &&
                    ICodifiable.class.isAssignableFrom(object.getClass()) &&
                    !StringUtils.hasText(((ICodifiable) object).getCode())) {
                ((ICodifiable) object).setCode(((ICodifiableService) this).getNextCode());
            }
            return this.afterCreate((T) repository().save(object));
        } else {
            throw new EntityExistsException();
        }
    }

    @Override
    @Transactional
    public T createAndFlush(T object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (object.getId() == null) {
            object = this.beforeCreate(object);
            if (ICodifiableService.class.isAssignableFrom(this.getClass()) &&
                    ICodifiable.class.isAssignableFrom(object.getClass()) &&
                    !StringUtils.hasText(((ICodifiable) object).getCode())) {
                ((ICodifiable) object).setCode(((ICodifiableService) this).getNextCode());
            }
            return this.afterCreate((T) repository().saveAndFlush(object));
        } else {
            throw new EntityExistsException();
        }
    }

    @Override
    public List<T> create(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        List<T> createdObjects = new ArrayList<>();
        objects.forEach(object -> {
            createdObjects.add(this.create(object));
        });

        return createdObjects;
    }

    @Override
    @Transactional
    public T update(T object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (object.getId() != null) {
            object = this.beforeUpdate(object);
            if (ICodifiableService.class.isAssignableFrom(this.getClass()) &&
                    ICodifiable.class.isAssignableFrom(object.getClass()) &&
                    !StringUtils.hasText(((ICodifiable) object).getCode())) {
                ((ICodifiable) object).setCode(((ICodifiableService) this).getNextCode());
            }
            return this.afterUpdate((T) repository().save(object));
        } else {
            throw new EntityNotFoundException();
        }
    }

    @Override
    @Transactional
    public T updateAndFlush(T object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (object.getId() != null) {
            object = this.beforeUpdate(object);
            if (ICodifiableService.class.isAssignableFrom(this.getClass()) &&
                    ICodifiable.class.isAssignableFrom(object.getClass()) &&
                    !StringUtils.hasText(((ICodifiable) object).getCode())) {
                ((ICodifiable) object).setCode(((ICodifiableService) this).getNextCode());
            }
            return this.afterUpdate((T) repository().saveAndFlush(object));
        } else {
            throw new EntityNotFoundException();
        }
    }

    @Override
    @Transactional
    public List<T> update(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }
        List<T> updatedObjects = new ArrayList<>();
        objects.forEach(object -> {
            updatedObjects.add(this.update(object));
        });

        return updatedObjects;
    }

    @Override
    @Transactional
    public void delete(String senderDomain, List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        if (ISAASEntity.class.isAssignableFrom(persistentClass) && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            objects.forEach(object -> {
                if (!senderDomain.equals(((ISAASEntity) object).getDomain())) {
                    throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " with id: " + object.getId());
                }
            });
        }

        this.beforeDelete(objects);
        repository().deleteAll(objects);
        this.afterDelete(objects);
    }

    @Override
    @Transactional
    public void delete(String senderDomain, I id) {
        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        T object = this.findById(id);
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            if (!senderDomain.equals(((ISAASEntity) object).getDomain())) {
                throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " with id: " + id);
            }
        }

        this.beforeDelete(id);
        if (CancelableEntity.class.isAssignableFrom(persistentClass) || CancelableEntity.class.isAssignableFrom(persistentClass)) {
            ((CancelableEntity) object).setCheckCancel(true);
            ((CancelableEntity) object).setCancelDate(new Date());
            this.update(object);
        } else {
            repository().deleteById(id);
        }
        this.afterDelete(id);
    }

    @Override
    @Transactional
    public void delete(List<T> objects) {
        if (ISAASEntity.class.isAssignableFrom(persistentClass)) {
            throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " should use SAAS delete");
        }

        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }
        this.beforeDelete(objects);
        repository().deleteAll(objects);
        this.afterDelete(objects);
    }

    @Override
    @Transactional
    public void delete(I id) {
        if (ISAASEntity.class.isAssignableFrom(persistentClass)) {
            throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " should use SAAS delete");
        }

        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        T object = this.findById(id);
        this.beforeDelete(id);
        if (CancelableEntity.class.isAssignableFrom(persistentClass) || CancelableEntity.class.isAssignableFrom(persistentClass)) {
            ((CancelableEntity) object).setCheckCancel(true);
            ((CancelableEntity) object).setCancelDate(new Date());
            this.update(object);
        } else {
            repository().deleteById(id);
        }
        this.afterDelete(id);
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
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && JpaPagingAndSortingSAASRepository.class.isAssignableFrom(repository().getClass())) {
            log.warn("Find all give vulnerability to SAS entity...");
        }
        List<T> list = repository().findAll();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;

        }

        return this.afterFindAll(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(Pageable pageable) {
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && JpaPagingAndSortingSAASRepository.class.isAssignableFrom(repository().getClass())) {
            log.warn("Find all give vulnerability to SAS entity...");
        }

        Page<T> page = repository().findAll(pageable);
        if (page.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        return this.afterFindAll(page.getContent());
    }

    @Override
    public List<T> findAll(String domain) throws NotSupportedException {
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && JpaPagingAndSortingSAASRepository.class.isAssignableFrom(repository().getClass())) {
            List<T> list = ((JpaPagingAndSortingSAASRepository) repository()).findByDomainIgnoreCase(domain);
            if (CollectionUtils.isEmpty(list)) {
                return Collections.EMPTY_LIST;
            }
            return this.afterFindAll(list);
        } else {
            throw new NotSupportedException("find all by domain for :" + persistentClass.getSimpleName());
        }
    }

    @Override
    public List<T> findAll(String domain, Pageable pageable) throws NotSupportedException {
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && JpaPagingAndSortingSAASRepository.class.isAssignableFrom(repository().getClass())) {
            Page<T> page = ((JpaPagingAndSortingSAASRepository) repository()).findByDomainIgnoreCase(domain, pageable);
            if (page.isEmpty()) {
                return Collections.EMPTY_LIST;
            }

            return this.afterFindAll(page.getContent());
        } else {
            throw new NotSupportedException("find all by domain for :" + persistentClass.getSimpleName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public T findById(I id) throws ObjectNotFoundException {
        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
        Optional<T> optional = repository().findById(id);
        if (optional.isPresent()) {
            return this.afterFindById(optional.get());
        }
        return null;
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

        List<T> updatedObjects = new ArrayList<>();
        objects.forEach(object -> {
            updatedObjects.add(this.saveOrUpdate(object));
        });

        return updatedObjects;
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
    public List<T> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria) {
        if (!CollectionUtils.isEmpty(criteria)) {
            //get criteria data to validate filter
            Specification<T> specification = CriteriaHelper.buildSpecification(domain, criteria, persistentClass);
            return repository().findAll(specification);
        } else {
            log.error("Criteria filter map is null");
            throw new EmptyCriteriaFilterException("Criteria filter map is null");
        }
    }

    @Override
    public List<T> findAllByCriteriaFilter(String domain, List<QueryCriteria> criteria, PageRequest pageRequest) {
        if (!CollectionUtils.isEmpty(criteria)) {
            //get criteria data to validate filter
            Specification<T> specification = CriteriaHelper.buildSpecification(domain, criteria, persistentClass);
            return repository().findAll(specification, pageRequest).getContent();
        } else {
            log.error("Criteria filter map is null");
            throw new EmptyCriteriaFilterException("Criteria filter map is null");
        }
    }
}
