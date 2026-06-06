package eu.isygoit.config;

import eu.isygoit.annotation.ExcludeOnResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;
import java.util.Arrays;

@ControllerAdvice
public class ExcludeFieldsAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Apply to all responses; you can add conditions (e.g., only GET endpoints)
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null) {
            return null;
        }
        // Handle collections (if your endpoint returns List<TokenConfigDto>)
        if (body instanceof Iterable) {
            for (Object item : (Iterable<?>) body) {
                nullifyExcludedFields(item);
            }
        } else {
            nullifyExcludedFields(body);
        }
        return body;
    }

    private void nullifyExcludedFields(Object obj) {
        Class<?> clazz = obj.getClass();
        // Traverse the class hierarchy (including superclasses like AuditableDto)
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            Arrays.stream(fields)
                    .filter(field -> field.isAnnotationPresent(ExcludeOnResponse.class))
                    .forEach(field -> {
                        field.setAccessible(true);
                        try {
                            field.set(obj, null);
                        } catch (IllegalAccessException e) {
                            // Log error but continue
                            e.printStackTrace();
                        }
                    });
            clazz = clazz.getSuperclass();
        }
    }
}