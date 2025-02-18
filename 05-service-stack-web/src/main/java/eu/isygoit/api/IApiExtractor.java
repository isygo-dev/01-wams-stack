package eu.isygoit.api;

import eu.isygoit.model.extendable.ApiPermissionModel;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * The interface Api extractor.
 *
 * @param <T> the type parameter
 */
public interface IApiExtractor<T extends ApiPermissionModel> {

    /**
     * New instance t.
     *
     * @return the t
     */
    T newInstance();

    /**
     * Extract apis list.
     *
     * @param controller the controller
     * @return the list
     * @throws NoSuchMethodException     the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws InstantiationException    the instantiation exception
     * @throws IllegalAccessException    the illegal access exception
     */
    List<T> extractApis(Class<?> controller) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
