package eu.isygoit.com.rest.service.impl;

import eu.isygoit.com.rest.service.ICodifiableService;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.EmptyCriteriaFilterException;
import eu.isygoit.exception.EmptyListException;
import eu.isygoit.exception.OperationNotAllowedException;
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
        if (repository() instanceof JpaPagingAndSortingSAASRepository repository) {
            return repository.countByDomainIgnoreCase(domain);
        } else {
            throw new UnsupportedOperationException("this is not a SAS entity/repository: " + repository().getClass().getSimpleName());
        }
    }

    @Override
    public boolean existsById(I id) {
        return repository().existsById(id);
    }

    private void handleCodifiableEntity(T object) {
        if (this instanceof ICodifiableService && object instanceof ICodifiable codifiable && !StringUtils.hasText(codifiable.getCode())) {
            ((ICodifiableService) this).getNextCode().ifPresent(code -> codifiable.setCode((String) code));
        }
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

        if (Objects.isNull(object.getId())) {
            object = this.beforeCreate(object);
            handleCodifiableEntity(object);
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

        if (Objects.isNull(object.getId())) {
            object = this.beforeCreate(object);
            handleCodifiableEntity(object);
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

        return objects.stream().map(this::create).toList();
    }

    @Override
    @Transactional
    public T update(T object) {
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }

        if (Objects.nonNull(object.getId())) {
            object = this.beforeUpdate(object);
            handleCodifiableEntity(object);
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

        if (Objects.nonNull(object.getId())) {
            object = this.beforeUpdate(object);
            handleCodifiableEntity(object);
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

        return objects.stream().map(this::update).toList();
    }

    @Override
    @Transactional
    public void delete(String senderDomain, List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }

        //Verify if one of the objects is not eligible for the operation (diff domain)
        if (ISAASEntity.class.isAssignableFrom(persistentClass)
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            Optional<T> optional = objects.stream()
                    .filter(object -> !senderDomain.equals(((ISAASEntity) object).getDomain()) /*Diff domain*/)
                    .findFirst();
            if (optional.isPresent()) {
                throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " with id: " + optional.get().getId());
            }
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

        this.getById(id).ifPresent(object -> {
            //For SaaS Entity, check if delete ope is allowed : super domain or same domain
            if (ISAASEntity.class.isAssignableFrom(persistentClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
                if (!senderDomain.equals(((ISAASEntity) object).getDomain())) {
                    throw new OperationNotAllowedException("Delete " + persistentClass.getSimpleName() + " with id: " + id);
                }
            }

            //before delete ops
            this.beforeDelete(id);

            if (CancelableEntity.class.isAssignableFrom(persistentClass)) {
                ((CancelableEntity) object).setCheckCancel(true);
                ((CancelableEntity) object).setCancelDate(new Date());
                this.update(object);
            } else {
                repository().deleteById(id);
            }

            //after delete ope
            this.afterDelete(id);
        });
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

        this.getById(id).ifPresent(object -> {
            this.beforeDelete(id);

            if (CancelableEntity.class.isAssignableFrom(persistentClass)) {
                ((CancelableEntity) object).setCheckCancel(true);
                ((CancelableEntity) object).setCancelDate(new Date());
                this.update(object);
            } else {
                repository().deleteById(id);
            }

            this.afterDelete(id);
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
    public List<T> getAll() {
        if (ISAASEntity.class.isAssignableFrom(persistentClass)
                && repository() instanceof JpaPagingAndSortingSAASRepository) {
            log.warn("Find all give vulnerability to SAS entity...");
        }
        List<T> list = repository().findAll();
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : this.afterGetAll(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> getAll(Pageable pageable) {
        if (ISAASEntity.class.isAssignableFrom(persistentClass)
                && repository() instanceof JpaPagingAndSortingSAASRepository) {
            log.warn("Find all give vulnerability to SAS entity...");
        }

        Page<T> page = repository().findAll(pageable);
        return page.isEmpty() ? Collections.emptyList() : this.afterGetAll(page.getContent());
    }

    @Override
    public List<T> getAll(String domain) throws NotSupportedException {
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && repository() instanceof JpaPagingAndSortingSAASRepository) {
            List<T> list = ((JpaPagingAndSortingSAASRepository) repository()).findByDomainIgnoreCase(domain);
            return CollectionUtils.isEmpty(list) ? Collections.emptyList() : this.afterGetAll(list);
        } else {
            throw new NotSupportedException("find all by domain for: " + persistentClass.getSimpleName());
        }
    }

    @Override
    public List<T> getAll(String domain, Pageable pageable) throws NotSupportedException {
        if (ISAASEntity.class.isAssignableFrom(persistentClass) && repository() instanceof JpaPagingAndSortingSAASRepository) {
            Page<T> page = ((JpaPagingAndSortingSAASRepository) repository()).findByDomainIgnoreCase(domain, pageable);
            return page.isEmpty() ? Collections.emptyList() : this.afterGetAll(page.getContent());
        } else {
            throw new NotSupportedException("find all by domain for: " + persistentClass.getSimpleName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> getById(I id) {
        return Objects.isNull(id) ? Optional.empty() : repository().findById(id);
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

        return objects.stream().map(object ->
                this.saveOrUpdate(object)
        ).toList();
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
    public List<T> afterGetAll(List<T> list) {
        return list;
    }

    @Override
    public T afterFindById(T object) {
        return object;
    }

    @Override
    public List<T> getAllByCriteriaFilter(String domain, List<QueryCriteria> criteria) {
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
    public List<T> getAllByCriteriaFilter(String domain, List<QueryCriteria> criteria, PageRequest pageRequest) {
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