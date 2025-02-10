package eu.isygoit.exception.handler;

import eu.isygoit.annotation.MsgLocale;
import eu.isygoit.helper.SpringClassScanner;
import jakarta.persistence.*;
import jakarta.persistence.metamodel.EntityType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.*;

/**
 * The type Controller exception handler builder.
 * <p>
 * This class is responsible for processing various constraints (like foreign keys, unique constraints, not-null constraints)
 * in JPA entities and logging the corresponding messages based on the entity's annotations.
 */
@Slf4j
@Data
@Component
public abstract class ControllerExceptionHandlerBuilder {

    public static final String CONSTRAINT_VIOLATED = ".constraint.violated";
    public static final String A_LINK_BETWEEN = "A link between ";
    public static final String CAN_T_BE_VIOLATED = " can't be violated";
    public static final String FK = "FK:{}";

    // A map to store the Java type (entity class) for each entity name
    private final Map<String, Class<?>> entityClasses = new HashMap<>();
    private final Map<String, String> exceptionMessages = new HashMap<>(); // Store the exception messages
    // A map to hold messages categorized into TRANSLATED and NON_TRANSLATED
    private final Map<MessageType, List<String>> categorizedMessages = new EnumMap<>(MessageType.class);
    // Cache for storing fields to avoid redundant reflection calls
    private final Map<String, Field[]> fieldCache = new HashMap<>();
    @Nullable
    @Autowired(required = false)
    private EntityManager entityManager; // EntityManager for accessing metadata
    @Autowired
    private Environment environment; // To fetch translation messages from properties
    @Autowired
    private SpringClassScanner springClassScanner; // Used for scanning classes annotated with @MsgLocale

    @PostConstruct
    private void initializeEntityConstraintMappings() {
        processExceptionMessages(); // Processes managed exceptions based on MsgLocale annotations

        Optional.ofNullable(entityManager).ifPresent(em -> {
            log.info("BEGIN PROCESSING ENTITY CONSTRAINTS");

            // Get all entities from the EntityManager
            Set<EntityType<?>> entities = em.getMetamodel().getEntities();
            entities.forEach(entity -> {
                // Collect entity metadata (name and Java class)
                entityClasses.put(entity.getJavaType().getName(), entity.getJavaType());

                // Process the entity name and log the translation (or default message)
                logEntityNameTranslation(entity);

                // Process each attribute (field) of the entity
                entity.getAttributes().forEach(attribute -> {
                    Optional.ofNullable(getField(entity, attribute.getName()))
                            .ifPresent(optional -> {
                                Field field = optional.get();
                                logFieldNameTranslation(field); // Process and log the field name

                                // Process constraints (NotNull, ForeignKey, etc.)
                                logNotNullConstraint(entity, attribute.getName(), field);
                                logForeignKeyConstraints(field);
                            });
                });

                // Process unique constraints on the entity
                logUniqueConstraints(entity);
            });

            log.info("END PROCESSING ENTITY CONSTRAINTS");

            log.error("<Error>: BEGIN LOGGING ERROR MESSAGES");
            // Log the non-translated messages as errors
            categorizedMessages.getOrDefault(MessageType.NON_TRANSLATED, Collections.emptyList())
                    .forEach(message -> log.error("<Error>: {}", message));
            log.error("<Error>: END LOGGING ERROR MESSAGES");
        });
    }

    // Get field from the entity by attribute name with improved reflection handling using Optional
    private Optional<Field> getField(EntityType<?> entity, String attributeName) {
        String cacheKey = entity.getName() + "." + attributeName;
        return Optional.ofNullable(fieldCache.get(cacheKey))
                .map(fields -> fields[0]) // return cached field if available
                .or(() -> {
                    try {
                        // Try to find the field in the declared fields
                        Field field = Arrays.stream(entity.getJavaType().getDeclaredFields())
                                .filter(f -> f.getName().equals(attributeName))
                                .findFirst()
                                .orElseThrow(() -> new NoSuchFieldException(attributeName));

                        fieldCache.put(cacheKey, new Field[]{field});
                        return Optional.of(field);
                    } catch (NoSuchFieldException e) {
                        // Handle the exception gracefully, log it, and return an empty Optional
                        log.warn("Field '{}' not found in entity '{}'. Exception: {}", attributeName, entity.getName(), e.getMessage());
                        return Optional.empty(); // Return an empty Optional
                    }
                });
    }

    // Log and process the entity name
    private void logEntityNameTranslation(EntityType<?> entity) {
        String entityName = entity.getName();
        String translation = getTranslationMessage(entityName, entityName);
        exceptionMessages.put(entityName, entityName); // Store the entity name message
        log.info("###:########################################################################################################");
        log.info("Entity: {}", translation);
    }

    // Log and process unique constraints on the entity
    private void logUniqueConstraints(EntityType<?> entity) {
        Optional.ofNullable(entity.getJavaType().getAnnotation(Table.class))
                .ifPresent(table -> {
                    processMainTableUniqueConstraints(table); // Process unique constraints for the main table
                    processSecondaryTableUniqueConstraints(entity); // Process unique constraints for secondary tables
                });
    }

    // Log unique constraints of the main table
    private void processMainTableUniqueConstraints(Table table) {
        Arrays.stream(table.uniqueConstraints())
                .filter(uc -> StringUtils.hasText(uc.name())) // Ensure the unique constraint has a name
                .forEach(uc -> {
                    String constraintMessage = uc.name().toLowerCase().replace("_", ".") + CONSTRAINT_VIOLATED;
                    String translation = getTranslationMessage(constraintMessage, "The value " +
                            Arrays.toString(uc.columnNames()) + " of " + table.name() + " is already used");
                    exceptionMessages.put(uc.name().toLowerCase(), constraintMessage);
                    log.info("UC:{}", translation); // Log the unique constraint violation
                });
    }

    // Log unique constraints for secondary tables
    private void processSecondaryTableUniqueConstraints(EntityType<?> entity) {
        Optional.ofNullable(entity.getJavaType().getAnnotation(SecondaryTables.class))
                .ifPresent(secondaryTables -> Arrays.stream(secondaryTables.value())
                        .forEach(st -> Arrays.stream(st.uniqueConstraints())
                                .filter(uc -> StringUtils.hasText(uc.name())) // Ensure the unique constraint has a name
                                .forEach(uc -> {
                                    String constraintMessage = uc.name().toLowerCase().replace("_", ".") + CONSTRAINT_VIOLATED;
                                    String translation = getTranslationMessage(constraintMessage,
                                            "The value " + Arrays.toString(uc.columnNames()) + " of " + st.name() + " is already used");
                                    exceptionMessages.put(uc.name().toLowerCase(), constraintMessage);
                                    log.info("UC:{}", translation); // Log the unique constraint violation for secondary tables
                                })));
    }

    // Log and process foreign key constraints on the field
    private void logForeignKeyConstraints(Field field) {
        Optional.ofNullable(field.getAnnotation(JoinColumn.class))
                .ifPresent(joinColumn -> processForeignKeyConstraint(joinColumn.foreignKey()));

        Optional.ofNullable(field.getAnnotation(JoinTable.class))
                .ifPresent(joinTable -> {
                    Arrays.stream(joinTable.joinColumns())
                            .map(JoinColumn::foreignKey)
                            .filter(foreignKey -> StringUtils.hasText(foreignKey.name()))
                            .forEach(foreignKey -> processForeignKeyConstraint(foreignKey));
                    Arrays.stream(joinTable.inverseJoinColumns())
                            .map(JoinColumn::foreignKey)
                            .filter(foreignKey -> StringUtils.hasText(foreignKey.name()))
                            .forEach(foreignKey -> processForeignKeyConstraint(foreignKey));
                });
    }

    // Process and log a single foreign key constraint
    private void processForeignKeyConstraint(ForeignKey foreignKey) {
        String constraintMessage = foreignKey.name().toLowerCase().replace("_", ".") + CONSTRAINT_VIOLATED;
        String translation = getTranslationMessage(constraintMessage, A_LINK_BETWEEN + foreignKey.name() + CAN_T_BE_VIOLATED);
        exceptionMessages.put(foreignKey.name().toLowerCase(), constraintMessage);
        log.info(FK, translation); // Log the foreign key constraint violation
    }

    // Log and process NotNull constraints on the field
    private void logNotNullConstraint(EntityType<?> entity, String attributeName, Field field) {
        Optional.ofNullable(field.getAnnotation(Column.class))
                .filter(column -> !column.nullable()) // Only process if the column is not nullable
                .ifPresent(column -> {
                    String constraintMessage = "not.null." + entity.getName().toLowerCase() + "." + attributeName.toLowerCase() + CONSTRAINT_VIOLATED;
                    String translation = getTranslationMessage(constraintMessage, "The value " + column.name() + " of " + entity.getJavaType().getAnnotation(Table.class).name() + " is required");
                    exceptionMessages.put(column.name().toLowerCase(), constraintMessage);
                    log.info("NNU:{}", translation); // Log the NotNull constraint violation
                });
    }

    // Log and process field names
    private void logFieldNameTranslation(Field field) {
        String translation = getTranslationMessage(field.getName(), field.getName());
        log.info("FLD:{}", translation); // Log the field name translation
    }

    // Log and process managed exceptions based on the MsgLocale annotations
    private void processExceptionMessages() {
        Set<BeanDefinition> managedExceptionBeans = springClassScanner.findAnnotatedClasses(MsgLocale.class, "eu.isygoit.exception");
        managedExceptionBeans.forEach(beanDefinition -> {
            try {
                MsgLocale msgLocale = Class.forName(beanDefinition.getBeanClassName()).getAnnotation(MsgLocale.class);
                if (msgLocale != null && StringUtils.hasText(msgLocale.value())) {
                    String translation = getTranslationMessage(msgLocale.value(), msgLocale.value());
                    log.info("EXCPT:{}", translation); // Log the exception translation
                } else {
                    log.error("<Error>: msgLocale annotation not defined for class type {}", beanDefinition.getBeanClassName());
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e); // Handle the exception gracefully
            }
        });
    }

    // Fetch the translation message from properties or use the default value if not found
    private String getTranslationMessage(String messageKey, String defaultTranslation) {
        return Optional.ofNullable(environment.getProperty(messageKey)) // Try to get the translation from properties
                .map(translation -> messageKey + "=" + translation)
                .orElseGet(() -> messageKey + "=" + defaultTranslation); // If not found, use default
    }

    // Enum to represent message types: TRANSLATED or NON_TRANSLATED
    public enum MessageType {
        TRANSLATED, // Messages that have been successfully translated
        NON_TRANSLATED // Messages that couldn't be translated and are using default values
    }
}