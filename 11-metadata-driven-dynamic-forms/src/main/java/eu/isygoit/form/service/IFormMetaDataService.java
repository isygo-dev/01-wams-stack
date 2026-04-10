package eu.isygoit.form.service;

import eu.isygoit.form.annotation.FormView;
import eu.isygoit.form.domain.ViewMetaData;
import eu.isygoit.form.dto.FormSubmitRequest;
import eu.isygoit.form.dto.FormSubmitResponse;

import java.util.List;

/**
 * Contract for form metadata services.
 * <p>
 * Implementations are responsible for:
 * <ul>
 *   <li>Registering {@link FormView}-annotated classes</li>
 *   <li>Resolving and caching {@link ViewMetaData}</li>
 *   <li>Validating and processing form submissions</li>
 * </ul>
 *
 * @see AbstractFormMetaDataService
 */
public interface IFormMetaDataService {

    /**
     * Registers a {@link FormView}-annotated class so it can be resolved by name.
     *
     * @param viewClass the class to register; must carry {@link FormView}
     */
    void registerView(Class<?> viewClass);

    /**
     * Registers all {@link FormView}-annotated classes found in the given base packages.
     * Intended for auto-scan at application startup.
     *
     * @param basePackages one or more packages to scan
     */
    void scanAndRegister(String... basePackages);

    /**
     * Returns the metadata for the latest version of a registered view.
     *
     * @param viewName the logical name declared in {@link FormView#name()}
     * @return the {@link ViewMetaData} describing the form
     */
    ViewMetaData getMetaData(String viewName);

    /**
     * Returns the metadata for a specific version of a registered view.
     *
     * @param viewName the logical name declared in {@link FormView#name()}
     * @param version  the version string (e.g. "1.0", "2.0")
     * @return the {@link ViewMetaData} describing the form
     */
    ViewMetaData getMetaData(String viewName, String version);

    /**
     * Generates metadata directly from a class without requiring prior registration.
     * Useful for one-off or test scenarios.
     *
     * @param viewClass the class to derive metadata from
     * @return the {@link ViewMetaData} for the given class
     */
    ViewMetaData getMetaDataFromClass(Class<?> viewClass);

    /**
     * Returns the names of all currently registered views.
     *
     * @return list of registered view names
     */
    List<String> getRegisteredViewNames();

    /**
     * Validates and processes a form submission.
     *
     * @param request the submission payload containing the view name and field values
     * @return a {@link FormSubmitResponse} indicating success or field-level errors
     */
    FormSubmitResponse submit(FormSubmitRequest request);

    /**
     * Evicts the cached metadata for the given view, forcing re-generation on next access.
     *
     * @param viewName the view whose cache entry should be removed
     */
    void invalidateCache(String viewName);

    /**
     * Evicts all cached metadata entries.
     */
    void clearCache();
}