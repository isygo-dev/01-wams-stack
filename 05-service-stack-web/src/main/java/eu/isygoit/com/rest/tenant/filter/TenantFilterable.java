package eu.isygoit.com.rest.tenant.filter;

import java.lang.annotation.*;
import java.lang.annotation.ElementType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantFilterable {

}