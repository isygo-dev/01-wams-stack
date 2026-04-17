package eu.isygoit.controller;

import eu.isygoit.audit.TenantContext;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.Tutorial;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

// eu/isygoit/aspect/TenantFilterAspect.java
@Aspect
@Component
@Slf4j
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("@annotation(tenantFilterable)")
    public void enableTenantFilter(JoinPoint joinPoint, TenantFilterable tenantFilterable) {
        Class<?> entityClass = tenantFilterable.entity();

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
            log.debug("Tenant filter enabled for tenantId='{}' on {}.{}",
                    tenantId,
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName());
        } catch (Exception e) {
            log.warn("Could not enable 'tenantFilter' for entity {}: {}",
                    entityClass.getSimpleName(), e.getMessage());
        }
    }
}