package eu.isygoit.controller;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantFilterable {
    /**
     * The entity class to check against ITenantAssignable.
     * Required so the aspect knows which entity this endpoint manages.
     */
    Class<?> entity();
}