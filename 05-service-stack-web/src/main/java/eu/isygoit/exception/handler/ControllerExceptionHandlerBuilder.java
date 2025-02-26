package eu.isygoit.exception.handler;

import eu.isygoit.annotation.MsgLocale;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.helper.SpringClassScanner;
import jakarta.persistence.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Data
@Component
public abstract class ControllerExceptionHandlerBuilder {

    // Constants for message formatting
    private static final String TRANSLATED_MESSAGES = "translatedMessages";
    private static final String NON_TRANSLATED_MESSAGES = "nonTranslatedMessages";
    private static final String CONSTRAINT_VIOLATED = ".constraint.violated";
    private static final String A_LINK_BETWEEN = "A link between ";
    private static final String CAN_T_BE_VIOLATED = " can't be violated";
    private static final String FK = "FK:{}";

    private final Map<String, Class<?>> entityMap = new HashMap<>();

    @Nullable
    @Autowired(required = false)
    private EntityManager entityManager;

    @Autowired
    private Environment environment;

    @Autowired
    private SpringClassScanner springClassScanner;

    private final Map<String, String> excepMessage = new HashMap<>();

    private final Map<String, List<String>> messages = new HashMap<>() {
        @Override
        public List<String> get(Object key) {
            return super.computeIfAbsent((String) key, k -> new ArrayList<>());
        }
    };

    @PostConstruct
    private void generateConstraintMap() {
        processManagedException();

        if (entityManager != null) {
            log.info("BEGIN PROCESSING FULL ENTITY CONSTRAINTS");

            Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
            for (EntityType<?> entity : entities) {
                processEntity(entity);
            }

            log.info("END PROCESSING FULL ENTITY CONSTRAINTS");

            log.error("<Error>: BEGIN PROCESSING ERROR ENTITY CONSTRAINTS");
            messages.get(NON_TRANSLATED_MESSAGES).forEach(msg -> log.error("<Error>: {}", msg));
            log.error("<Error>: END PROCESSING ERROR ENTITY CONSTRAINTS");
        }
    }

    private void processEntity(EntityType<?> entity) {
        entityMap.put(entity.getJavaType().getName(), entity.getJavaType());
        processEntityName(entity);

        Set<Attribute<?, ?>> attributes = (Set<Attribute<?, ?>>) entity.getAttributes();
        for (Attribute<?, ?> attribute : attributes) {
            Field field = (Field) entity.getAttribute(attribute.getName()).getJavaMember();
            processFieldName(field);
            processNotNullConstraint(entity, attribute.getName(), field);
            processFkConstraints(field);
        }

        processUcConstraints(entity);
    }

    private void processEntityName(EntityType<?> entity) {
        String stringMsg = entity.getName();
        String translation = getTranslationMessage(stringMsg, stringMsg);
        excepMessage.put(stringMsg, stringMsg);
        log.info("###:########################################################################################################");
        log.info("ENT: {}", translation);
    }

    private void processUcConstraints(EntityType<?> entity) {
        Table table = entity.getJavaType().getAnnotation(Table.class);
        if (table != null) {
            processUniqueConstraints(table.uniqueConstraints(), table.name());
            processSecondaryTables(entity, table.name());
        }
    }

    private void processSecondaryTables(EntityType<?> entity, String tableName) {
        SecondaryTables secondaryTables = entity.getJavaType().getAnnotation(SecondaryTables.class);
        if (secondaryTables != null) {
            for (SecondaryTable secondaryTable : secondaryTables.value()) {
                processUniqueConstraints(secondaryTable.uniqueConstraints(), tableName);
            }
        }
    }

    private void processUniqueConstraints(UniqueConstraint[] uniqueConstraints, String tableName) {
        for (UniqueConstraint uniqueConstraint : uniqueConstraints) {
            if (StringUtils.hasText(uniqueConstraint.name())) {
                String stringMsg = uniqueConstraint.name().toLowerCase().replace("_", ".") + CONSTRAINT_VIOLATED;
                String translation = getTranslationMessage(stringMsg, "The value "
                        + Arrays.toString(uniqueConstraint.columnNames())
                        + " of "
                        + tableName
                        + " is already used");
                excepMessage.put(uniqueConstraint.name().toLowerCase(), stringMsg);
                log.info("UC: {}", translation);
            }
        }
    }

    private void processFkConstraints(Field field) {
        processJoinColumn(field.getAnnotation(JoinColumn.class));
        processJoinTable(field.getAnnotation(JoinTable.class));
    }

    private void processJoinColumn(JoinColumn joinColumn) {
        if (joinColumn != null) {
            ForeignKey foreignKey = joinColumn.foreignKey();
            if (foreignKey != null && StringUtils.hasText(foreignKey.name())) {
                String stringMsg = foreignKey.name().toLowerCase().replace("_", ".") + CONSTRAINT_VIOLATED;
                String translation = getTranslationMessage(stringMsg, A_LINK_BETWEEN
                        + foreignKey.name()
                        + CAN_T_BE_VIOLATED);
                excepMessage.put(foreignKey.name().toLowerCase(), stringMsg);
                log.info(FK, translation);
            }
        }
    }

    private void processJoinTable(JoinTable joinTable) {
        if (joinTable != null) {
            for (JoinColumn jc : joinTable.joinColumns()) {
                ForeignKey foreignKey = jc.foreignKey();
                if (foreignKey != null && StringUtils.hasText(foreignKey.name())) {
                    String stringMsg = foreignKey.name().toLowerCase().replace("_", ".") + CONSTRAINT_VIOLATED;
                    String translation = getTranslationMessage(stringMsg, A_LINK_BETWEEN
                            + foreignKey.name()
                            + CAN_T_BE_VIOLATED);
                    excepMessage.put(foreignKey.name().toLowerCase(), stringMsg);
                    log.info(FK, translation);
                }
            }
        }
    }

    private void processNotNullConstraint(EntityType<?> entity, String attributeName, Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.nullable()) {
            String stringMsg = "not.null." + entity.getName().toLowerCase() + "." + attributeName.toLowerCase() + CONSTRAINT_VIOLATED;
            Table table = entity.getJavaType().getAnnotation(Table.class);
            String translation = getTranslationMessage(stringMsg, "The value "
                    + column.name()
                    + " of "
                    + table.name()
                    + " is required");
            excepMessage.put(column.name().toLowerCase(), stringMsg);
            log.info("NNU: {}", translation);
        }
    }

    private void processFieldName(Field field) {
        String translation = getTranslationMessage(field.getName(), field.getName());
        log.info("FLD: {}", translation);
    }

    private void processManagedException() {
        Set<BeanDefinition> managedExceptionBeans = springClassScanner.findAnnotatedClasses(MsgLocale.class, "eu.isygoit.exception");

        for (BeanDefinition exceptionBean : managedExceptionBeans) {
            try {
                Class<?> cl = Class.forName(exceptionBean.getBeanClassName());
                MsgLocale msgLocale = cl.getAnnotation(MsgLocale.class);
                if (msgLocale != null && StringUtils.hasText(msgLocale.value())) {
                    String translation = getTranslationMessage(msgLocale.value(), msgLocale.value());
                    log.info("EXCPT: {}", translation);
                } else {
                    log.error("<Error>: msgLocale annotation not defined for class type {}", exceptionBean.getBeanClassName());
                }
            } catch (ClassNotFoundException e) {
                log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            }
        }
    }

    private String getTranslationMessage(String stringMsg, String defaultTranslation) {
        // Fetch translation from environment or use default
        String translation = environment.getProperty(stringMsg);
        StringBuilder translatedMessage = new StringBuilder()
                .append(stringMsg)
                .append("=")
                .append(translation != null ? translation : defaultTranslation);

        // Categorize messages as translated or not
        if (translation != null) {
            messages.get(TRANSLATED_MESSAGES).add(translatedMessage.toString());
        } else {
            messages.get(NON_TRANSLATED_MESSAGES).add(translatedMessage.toString());
        }

        return translatedMessage.toString();
    }
}