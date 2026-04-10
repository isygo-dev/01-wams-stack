package eu.isygoit.form.service;

import eu.isygoit.form.annotation.FormView;
import eu.isygoit.form.core.MetaDataGenerator;
import eu.isygoit.form.domain.FieldMetaData;
import eu.isygoit.form.domain.ViewMetaData;
import eu.isygoit.form.dto.FormSubmitRequest;
import eu.isygoit.form.dto.FormSubmitResponse;
import eu.isygoit.form.exception.MetaDataGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * Abstract base class for form metadata services in Spring Boot applications.
 * <p>
 * Subclasses must implement {@link #configuredViewClasses()} to declare which
 * {@link FormView}-annotated classes are managed by this service instance, and may
 * optionally override {@link #basePackagesToScan()} to enable classpath scanning.
 *
 * <h3>Lifecycle</h3>
 * On Spring context startup ({@link InitializingBean#afterPropertiesSet()}),
 * this class auto-registers all views returned by {@link #configuredViewClasses()}
 * and all views found via {@link #basePackagesToScan()}.
 *
 * <h3>Submission hooks</h3>
 * Override {@link #beforeSubmit(FormSubmitRequest)} and {@link #afterSubmit(FormSubmitRequest, FormSubmitResponse)}
 * to add custom pre-/post-processing logic around form submissions without rewriting validation.
 *
 * <h3>Example usage</h3>
 * <pre>{@code
 * @Service
 * public class HrFormMetaDataService extends AbstractFormMetaDataService {
 *
 *     @Override
 *     protected List<Class<?>> configuredViewClasses() {
 *         return List.of(EmployeeCreateForm.class, EmployeeEditForm.class);
 *     }
 *
 *     @Override
 *     protected FormSubmitResponse doSubmit(FormSubmitRequest request, ViewMetaData meta) {
 *         // persist or delegate to domain service
 *         return FormSubmitResponse.ok("Employee saved");
 *     }
 * }
 * }</pre>
 *
 * @see IFormMetaDataService
 * @see MetaDataGenerator
 */
@Slf4j
public abstract class AbstractFormMetaDataService implements IFormMetaDataService, InitializingBean {

    private final MetaDataGenerator generator = new MetaDataGenerator();
    private final Set<String> registeredViewNames = new LinkedHashSet<>();

    // ─────────────────────────────────────────────────────────────────────────
    // Spring lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called automatically by Spring after dependency injection.
     * Registers all views declared by {@link #configuredViewClasses()} and
     * performs classpath scanning for packages returned by {@link #basePackagesToScan()}.
     */
    @Override
    public final void afterPropertiesSet() {
        log.info("[FormMetaData] Initialising service: {}", getClass().getSimpleName());

        configuredViewClasses().forEach(this::registerView);

        String[] packages = basePackagesToScan();
        if (packages != null && packages.length > 0) {
            scanAndRegister(packages);
        }

        log.info("[FormMetaData] Registered {} view(s): {}", registeredViewNames.size(), registeredViewNames);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Extension points (override in subclasses)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the explicit list of {@link FormView}-annotated classes managed by this service.
     * <p>
     * The default implementation returns an empty list — override to declare views statically.
     *
     * @return list of view classes to register at startup
     */
    protected List<Class<?>> configuredViewClasses() {
        return List.of();
    }

    /**
     * Returns additional packages to scan at startup for {@link FormView}-annotated classes.
     * <p>
     * The default implementation returns {@code null} (no scanning).
     * Override to enable classpath scanning, e.g. {@code return new String[]{"com.myapp.forms"};}.
     *
     * @return array of package names to scan, or {@code null} / empty to skip scanning
     */
    protected String[] basePackagesToScan() {
        return null;
    }

    /**
     * Hook called before any submission is processed.
     * Override to add pre-validation logic (e.g. JWT-based tenant resolution, CSRF checks).
     *
     * @param request the raw submission payload
     */
    protected void beforeSubmit(FormSubmitRequest request) {
        log.debug("[FormMetaData] beforeSubmit hook for view '{}'", request.viewName());
    }

    /**
     * Hook called after a submission has been processed, regardless of outcome.
     * Override to add audit logging, event publishing, etc.
     *
     * @param request  the original submission payload
     * @param response the result produced by {@link #doSubmit(FormSubmitRequest, ViewMetaData)}
     */
    protected void afterSubmit(FormSubmitRequest request, FormSubmitResponse response) {
        log.debug("[FormMetaData] afterSubmit hook for view '{}', success={}", request.viewName(), response.success());
    }

    /**
     * Performs the actual submission logic after built-in annotation-driven validation passes.
     * <p>
     * Subclasses <strong>must</strong> override this method to persist, delegate,
     * or otherwise process the submitted data.
     *
     * @param request the submission payload, guaranteed non-null with a known view name
     * @param meta    the resolved {@link ViewMetaData} for the submitted view
     * @return a {@link FormSubmitResponse} — use {@link FormSubmitResponse#ok(String)} on success
     */
    protected FormSubmitResponse doSubmit(FormSubmitRequest request, ViewMetaData meta) {
        log.warn("[FormMetaData] doSubmit not overridden in {}; returning default OK", getClass().getSimpleName());
        return FormSubmitResponse.ok("Submitted");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IFormMetaDataService implementation
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void registerView(Class<?> viewClass) {
        Objects.requireNonNull(viewClass, "viewClass must not be null");
        FormView annotation = viewClass.getAnnotation(FormView.class);
        if (annotation == null) {
            log.warn("[FormMetaData] Skipping registration — {} is not annotated with @FormView", viewClass.getName());
            return;
        }
        generator.registerView(viewClass);
        registeredViewNames.add(annotation.name());
        log.debug("[FormMetaData] Registered view '{}' from {}", annotation.name(), viewClass.getSimpleName());
    }

    @Override
    public void scanAndRegister(String... basePackages) {
        Objects.requireNonNull(basePackages, "basePackages must not be null");
        for (String pkg : basePackages) {
            log.info("[FormMetaData] Scanning package '{}' for @FormView classes", pkg);
            new Reflections(pkg)
                    .getTypesAnnotatedWith(FormView.class)
                    .forEach(viewClass -> {
                        registerView(viewClass);
                        log.info("[FormMetaData] Auto-registered view class: {}", viewClass.getSimpleName());
                    });
        }
    }

    @Override
    public ViewMetaData getMetaData(String viewName) {
        validateViewName(viewName);
        log.info("[FormMetaData] Resolving metadata for view '{}'", viewName);
        return generator.generate(viewName);
    }

    @Override
    public ViewMetaData getMetaData(String viewName, String version) {
        validateViewName(viewName);
        Objects.requireNonNull(version, "version must not be null");
        log.info("[FormMetaData] Resolving metadata for view '{}' v{}", viewName, version);
        return generator.generate(viewName, version);
    }

    @Override
    public ViewMetaData getMetaDataFromClass(Class<?> viewClass) {
        Objects.requireNonNull(viewClass, "viewClass must not be null");
        log.info("[FormMetaData] Generating metadata directly from class {}", viewClass.getSimpleName());
        return generator.generateFromClass(viewClass);
    }

    @Override
    public List<String> getRegisteredViewNames() {
        return List.copyOf(registeredViewNames);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Execution flow:
     * <ol>
     *   <li>Input validation</li>
     *   <li>{@link #beforeSubmit(FormSubmitRequest)} hook</li>
     *   <li>Annotation-driven field validation via {@link #validateFields(FormSubmitRequest, ViewMetaData)}</li>
     *   <li>{@link #doSubmit(FormSubmitRequest, ViewMetaData)} — subclass business logic</li>
     *   <li>{@link #afterSubmit(FormSubmitRequest, FormSubmitResponse)} hook</li>
     * </ol>
     */
    @Override
    public final FormSubmitResponse submit(FormSubmitRequest request) {
        Objects.requireNonNull(request, "FormSubmitRequest must not be null");
        validateViewName(request.viewName());

        log.info("[FormMetaData] Processing submission for view '{}'", request.viewName());
        beforeSubmit(request);

        ViewMetaData meta = getMetaData(request.viewName());

        Map<String, List<String>> errors = validateFields(request, meta);
        if (!errors.isEmpty()) {
            log.warn("[FormMetaData] Submission for view '{}' failed validation — {} field error(s)",
                    request.viewName(), errors.size());
            FormSubmitResponse failResponse = FormSubmitResponse.invalid(errors);
            afterSubmit(request, failResponse);
            return failResponse;
        }

        FormSubmitResponse response = doSubmit(request, meta);
        afterSubmit(request, response);
        log.info("[FormMetaData] Submission for view '{}' completed, success={}", request.viewName(), response.success());
        return response;
    }

    @Override
    public void invalidateCache(String viewName) {
        validateViewName(viewName);
        log.info("[FormMetaData] Invalidating cache for view '{}'", viewName);
        generator.invalidate(viewName);
    }

    @Override
    public void clearCache() {
        log.info("[FormMetaData] Clearing entire metadata cache");
        generator.clearCache();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Built-in annotation-driven field validation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Applies annotation-driven validation rules from the view's {@link ViewMetaData}
     * to the submitted field values.
     * <p>
     * Rules evaluated per field:
     * <ul>
     *   <li>{@code required} — field must be present and non-blank</li>
     *   <li>{@code minLength} / {@code maxLength} — string length constraints</li>
     *   <li>{@code minValue} / {@code maxValue} — numeric range constraints</li>
     *   <li>{@code pattern} — regex constraint</li>
     * </ul>
     * Override this method to add custom validation logic or integrate with
     * Jakarta Bean Validation (Hibernate Validator).
     *
     * @param request the submission payload
     * @param meta    the view metadata containing field rules
     * @return map of fieldKey → list of error messages; empty if all fields pass
     */
    protected Map<String, List<String>> validateFields(FormSubmitRequest request, ViewMetaData meta) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        Map<String, Object> fields = request.fields() != null ? request.fields() : Map.of();

        for (FieldMetaData field : meta.fields()) {
            validateSingleField(field, fields, errors);
        }
        return errors;
    }

    /**
     * Validates a single {@link FieldMetaData} entry against the submitted values map.
     * Recursively validates nested {@code OBJECT} fields.
     *
     * @param field  the field descriptor from the view metadata
     * @param fields the submitted key-value pairs
     * @param errors accumulator for field-level error messages
     */
    protected void validateSingleField(FieldMetaData field,
                                       Map<String, Object> fields,
                                       Map<String, List<String>> errors) {
        String key = field.key();
        Object value = fields.get(key);
        List<String> fieldErrors = new ArrayList<>();

        // Required check
        if (field.required() && isBlankValue(value)) {
            String msg = !field.validation().containsKey("requiredMessage")
                    ? key + " is required"
                    : field.validation().get("requiredMessage").toString();
            fieldErrors.add(msg);
        }

        if (value instanceof String strValue) {
            // minLength
            Integer minLen = field.minLength();
            if (minLen != null && strValue.length() < minLen) {
                fieldErrors.add(key + " must be at least " + minLen + " characters");
            }
            // maxLength
            Integer maxLen = field.maxLength();
            if (maxLen != null && strValue.length() > maxLen) {
                fieldErrors.add(key + " must be at most " + maxLen + " characters");
            }
            // pattern
            String pattern = (String) field.validation().get("pattern");
            if (pattern != null && !pattern.isBlank() && !strValue.matches(pattern)) {
                fieldErrors.add(key + " does not match the required format");
            }
        }

        if (value instanceof Number numValue) {
            double dval = numValue.doubleValue();
            Double min = field.minValue();
            Double max = field.maxValue();
            if (min != null && dval < min) {
                fieldErrors.add(key + " must be ≥ " + min);
            }
            if (max != null && dval > max) {
                fieldErrors.add(key + " must be ≤ " + max);
            }
        }

        if (!fieldErrors.isEmpty()) {
            errors.put(key, fieldErrors);
        }

        // Recurse into nested OBJECT children
        if (!field.children().isEmpty() && value instanceof Map<?, ?> nested) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedFields = (Map<String, Object>) nested;
            for (FieldMetaData child : field.children()) {
                validateSingleField(child, nestedFields, errors);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void validateViewName(String viewName) {
        if (viewName == null || viewName.isBlank()) {
            throw new MetaDataGenerationException("viewName must not be null or blank");
        }
    }

    private boolean isBlankValue(Object value) {
        if (value == null) return true;
        if (value instanceof String s) return s.isBlank();
        if (value instanceof Collection<?> c) return c.isEmpty();
        return false;
    }
}