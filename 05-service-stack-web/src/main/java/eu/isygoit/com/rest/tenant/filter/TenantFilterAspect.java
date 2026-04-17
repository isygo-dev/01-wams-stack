package eu.isygoit.com.rest.tenant.filter;

import eu.isygoit.audit.TenantContext;
import eu.isygoit.com.rest.service.CrudService;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.model.ITenantAssignable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
@ConditionalOnProperty(
        name = "app.tenancy.mode",
        havingValue = "DISCRIMINATOR",
        matchIfMissing = false
)
@Slf4j
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("@annotation(tenantFilterable)")
    public void enableTenantFilter(JoinPoint joinPoint, TenantFilterable tenantFilterable) {

        // Get the actual controller class
        Object target = joinPoint.getTarget();
        Class<?> controllerClass = target.getClass();

        // Resolve generic entity class T from CrudService
        Class<?> entityClass = resolveEntityClass(controllerClass);

        if (entityClass == null) {
            log.warn("Could not resolve entity class (T) from controller: {}", controllerClass.getName());
            return;
        }

        if (!ITenantAssignable.class.isAssignableFrom(entityClass)) {
            log.debug("Skipping tenant filter — {} does not implement ITenantAssignable",
                    entityClass.getSimpleName());
            return;
        }

        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.equals(TenantConstants.SUPER_TENANT_NAME)) {
            log.debug("Skipping tenant filter — tenantId is null or super tenant");
            return;
        }

        try {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);

            log.debug("Tenant filter enabled for tenantId='{}' on entity '{}' via {}.{}",
                    tenantId,
                    entityClass.getSimpleName(),
                    controllerClass.getSimpleName(),
                    joinPoint.getSignature().getName());
        } catch (Exception e) {
            log.warn("Could not enable 'tenantFilter' for entity {}: {}",
                    entityClass.getSimpleName(), e.getMessage());
        }
    }

    /**
     * Resolves the entity class (T) from CrudService<I, T, M, F, S>
     */
    private Class<?> resolveEntityClass(Class<?> controllerClass) {
        Type type = controllerClass.getGenericSuperclass();

        // Walk up the class hierarchy until we find the parameterized CrudService
        while (type != null && !(type instanceof ParameterizedType pt
                && isCrudService(pt))) {

            if (type instanceof Class) {
                type = ((Class<?>) type).getGenericSuperclass();
            } else {
                break;
            }
        }

        if (type instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            // T is the second type parameter → index 1
            if (typeArguments.length > 1) {
                Type tType = typeArguments[1];

                if (tType instanceof Class) {
                    return (Class<?>) tType;
                } else if (tType instanceof ParameterizedType) {
                    return (Class<?>) ((ParameterizedType) tType).getRawType();
                }
            }
        }

        log.debug("Failed to resolve generic type T from {}", controllerClass.getName());
        return null;
    }

    /**
     * Checks if the parameterized type is (or extends) CrudService
     */
    private boolean isCrudService(ParameterizedType pt) {
        Type rawType = pt.getRawType();
        if (rawType instanceof Class) {
            Class<?> rawClass = (Class<?>) rawType;
            return CrudService.class.equals(rawClass);
        }
        return false;
    }
}