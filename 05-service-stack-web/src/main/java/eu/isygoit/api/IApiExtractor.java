package eu.isygoit.api;

import eu.isygoit.model.extendable.ApiPermissionModel;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * The interface Api extractor.
 *
 * @param <E> the type parameter
 */
public interface IApiExtractor<E extends ApiPermissionModel> {

    /**
     * New instance e.
     *
     * @return the e
     */
    E newInstance();

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
    List<E> extractApis(Class<?> controller) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
