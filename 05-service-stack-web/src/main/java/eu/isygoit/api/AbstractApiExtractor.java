package eu.isygoit.api;

import eu.isygoit.enums.IEnumRequest;
import eu.isygoit.model.extendable.ApiPermissionModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Abstract api extractor.
 *
 * @param <E> the type parameter
 */
@Slf4j
public abstract class AbstractApiExtractor<E extends ApiPermissionModel> implements IApiExtractor<E> {

    @Transactional
    public List<E> extractApis(Class<?> controller) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<E> list = new ArrayList<>();
        String ctrlName = ClassUtils.getUserClass(controller).getSimpleName().replace("Controller", "");
        //Get controller url
        RequestMapping requestMapping = AnnotationUtils.findAnnotation(controller, RequestMapping.class);
        if (requestMapping != null && !ArrayUtils.isEmpty(requestMapping.path())) {
            String url = requestMapping.path()[0];
            for (Method method : controller.getMethods()) {
                E api = newInstance();
                //Set api object
                api.setObject(ctrlName);
                //Set api method
                api.setMethod(method.getName());
                //Set method type and pathextractApiMapping(url, method, api);
                isAPermission(url, method, api);
                if (api.getRqType() != null) {
                    //Set api description
                    api.setDescription("[" + api.getRqType().action() + "]" +
                            api.getServiceName() +
                            "_" + api.getObject() +
                            " (" + api.getMethod() + ")");
                    list.add(this.saveApi(api));
                }
            }
        }

        return list;
    }

    private boolean isGetApi(String url, Method method, E api) {
        GetMapping mapping = AnnotationUtils.findAnnotation(method, GetMapping.class);
        if (mapping != null && !ArrayUtils.isEmpty(mapping.path())) {
            api.setRqType(IEnumRequest.Types.GET);
            api.setPath(url + mapping.path()[0]);
            return true;
        }
        return false;
    }

    private boolean isDeleteApi(String url, Method method, E api) {
        DeleteMapping mapping = AnnotationUtils.findAnnotation(method, DeleteMapping.class);
        if (mapping != null && !ArrayUtils.isEmpty(mapping.path())) {
            api.setRqType(IEnumRequest.Types.DELETE);
            api.setPath(url + mapping.path()[0]);
            return true;
        }
        return false;
    }

    private boolean isPostApi(String url, Method method, E api) {
        PostMapping mapping = AnnotationUtils.findAnnotation(method, PostMapping.class);
        if (mapping != null && !ArrayUtils.isEmpty(mapping.path())) {
            api.setRqType(IEnumRequest.Types.POST);
            api.setPath(url + mapping.path()[0]);
            return true;
        }
        return false;
    }

    private boolean isPutApi(String url, Method method, E api) {
        PutMapping mapping = AnnotationUtils.findAnnotation(method, PutMapping.class);
        if (mapping != null && !ArrayUtils.isEmpty(mapping.path())) {
            api.setRqType(IEnumRequest.Types.PUT);
            api.setPath(url + mapping.path()[0]);
            return true;
        }
        return false;
    }

    private boolean isPatchApi(String url, Method method, E api) {
        PatchMapping mapping = AnnotationUtils.findAnnotation(method, PatchMapping.class);
        if (mapping != null && !ArrayUtils.isEmpty(mapping.path())) {
            api.setRqType(IEnumRequest.Types.PATCH);
            api.setPath(url + mapping.path()[0]);
            return true;
        }
        return false;
    }

    private void isAPermission(String url, Method method, E api) {
        if (!isGetApi(url, method, api)
                && !isPostApi(url, method, api)
                && !isPutApi(url, method, api)
                && !isPatchApi(url, method, api)
                && !isDeleteApi(url, method, api)) {
            log.info("Method {} is not recognize as a rest api", method);
        }
    }

    /**
     * Save api e.
     *
     * @param api the api
     * @return the e
     */
    public abstract E saveApi(E api);
}
