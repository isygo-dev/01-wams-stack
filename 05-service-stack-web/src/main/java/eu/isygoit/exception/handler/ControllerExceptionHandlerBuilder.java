package eu.isygoit.exception.handler;

import eu.isygoit.annotation.MsgLocale;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.*;

/**
 * The type Controller exception handler builder.
 */
@Slf4j
@Data
@Component
public abstract class ControllerExceptionHandlerBuilder {

    /**
     * The constant TRANSLATED_MESSAGES.
     */
    public static final String TRANSLATED_MESSAGES = "translatedMessages";
    /**
     * The constant NON_TRANSLATED_MESSAGES.
     */
    public static final String NON_TRANSLATED_MESSAGES = "nonTranslatedMessages";
    /**
     * The constant CONSTRAINT_VIOLATED.
     */
    public static final String CONSTRAINT_VIOLATED = ".constraint.violated";
    /**
     * The constant A_LINK_BETWEEN.
     */
    public static final String A_LINK_BETWEEN = "A link between ";
    /**
     * The constant CAN_T_BE_VIOLATED.
     */
    public static final String CAN_T_BE_VIOLATED = " can't be violated";
    /**
     * The constant FK.
     */
    public static final String FK = "FK:{}";

    private final Map<String, Class<?>> entityMap = new HashMap<>();

    @Nullable
    @Autowired(required = false)
    private EntityManager entityManager;

    @Autowired
    private Environment environment;

    @Autowired
    private SpringClassScanner springClassScanner;

    private Map<String, String> excepMessage = new HashMap<>();

    private Map<String, List<String>> messages = new HashMap<String, List<String>>() {
        @Override
        public List<String> get(Object key) {
            if (!this.containsKey(key)) {
                this.put((String) key, new ArrayList<>());
            }
            return super.get(key);
        }
    };

    @PostConstruct
    private void generateConstraintMap() {
        processManagedException();
        if (Objects.nonNull(this.entityManager)) {
            log.info("BEGIN PROCESSING FULL ENTITY CONSTRAINTS");
            Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
            Iterator<EntityType<?>> entityTypeIterator = entities.iterator();
            while (entityTypeIterator.hasNext()) {
                EntityType entity = entityTypeIterator.next();

                //collect entity map
                entityMap.put(entity.getJavaType().getName(), entity.getJavaType());
                // process entity name
                processEntityName(entity);

                /*Set<Attribute> attributes = entity.getAttributes();
                Iterator<Attribute> attributeIterator = attributes.iterator();
                while (attributeIterator.hasNext()) {
                    String attributeName = attributeIterator.next().getName();
                    Field field = (Field) entity.getAttribute(attributeName).getJavaMember();
                    // process field name
                    processFieldName(field);

                    // process not null constraints
                    processNotNullConstraint(entity, attributeName, field);

                    // process fk constraints
                    processFkConstraints(field);
                }*/

                // process uc constraints
                processUcConstraints(entity);
            }
            log.info("END PROCESSING FULL ENTITY CONSTRAINTS");

            log.error("<Error>: BEGIN PROCESSING ERROR ENTITY CONSTRAINTS");
            for (String message : messages.get(NON_TRANSLATED_MESSAGES)) {
                log.error("<Error>: {}", message);
            }
            log.error("<Error>: END PROCESSING ERROR ENTITY CONSTRAINTS");
        }
    }

    private void processEntityName(EntityType entity) {
        String stringMsg = entity.getName();
        String translation = getTranslationMessage(stringMsg, stringMsg);
        excepMessage.put(stringMsg, stringMsg);
        log.info("###:########################################################################################################");
        log.info("ENT:{}", translation);
    }

    private void processUcConstraints(EntityType entity) {
        String stringMsg;
        String translation;
        Table table = (Table) entity.getJavaType().getAnnotation(Table.class);
        if (Objects.nonNull(table)) {
            for (int i = 0; i < table.uniqueConstraints().length; i++) {
                UniqueConstraint uniqueConstraint = table.uniqueConstraints()[i];
                if (StringUtils.hasText(uniqueConstraint.name())) {
                    stringMsg = uniqueConstraint.name().toLowerCase().replace("_", ".") + CONSTRAINT_VIOLATED;
                    translation = getTranslationMessage(stringMsg, "The value "
                            + Arrays.toString(uniqueConstraint.columnNames())
                            + " of "
                            + table.name()
                            + " is already used");
                    excepMessage.put(uniqueConstraint.name().toLowerCase(), stringMsg);
                    log.info("UC:{}", translation);
                }
            }

            SecondaryTables secondaryTables = (SecondaryTables) entity.getJavaType().getAnnotation(SecondaryTables.class);
            if (Objects.nonNull(secondaryTables)) {
                for (int i = 0; i < secondaryTables.value().length; i++) {
                    SecondaryTable secondaryTable = secondaryTables.value()[i];
                    for (int j = 0; j < secondaryTable.uniqueConstraints().length; j++) {
                        UniqueConstraint uniqueConstraint = secondaryTable.uniqueConstraints()[j];
                        if (StringUtils.hasText(uniqueConstraint.name())) {
                            stringMsg = uniqueConstraint.name().toLowerCase().replace("_", ".") + CONSTRAINT_VIOLATED;
                            translation = getTranslationMessage(stringMsg, "The value "
                                    + Arrays.toString(uniqueConstraint.columnNames())
                                    + " of "
                                    + table.name()
                                    + " is already used ");
                            excepMessage.put(uniqueConstraint.name().toLowerCase(), stringMsg);
                            log.info("UC:{}", translation);
                        }
                    }
                }
            }
        }
    }

    private void processFkConstraints(Field field) {
        String stringMsg;
        String translation;
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (Objects.nonNull(joinColumn)) {
            ForeignKey foreignKey = joinColumn.foreignKey();
            if (Objects.nonNull(foreignKey) && StringUtils.hasText(foreignKey.name())) {
                stringMsg = foreignKey.name().toLowerCase().replace("_", ".") + CONSTRAINT_VIOLATED;
                translation = getTranslationMessage(stringMsg, A_LINK_BETWEEN
                        + foreignKey.name()
                        + CAN_T_BE_VIOLATED);
                excepMessage.put(foreignKey.name().toLowerCase(), stringMsg);
                log.info(FK, translation);
            }
        }

        JoinTable joinTable = field.getAnnotation(JoinTable.class);
        if (Objects.nonNull(joinTable)) {
            List<JoinColumn> joinColumnArray = Arrays.asList(joinTable.joinColumns());
            if (!CollectionUtils.isEmpty(joinColumnArray)) {
                for (JoinColumn jc : joinColumnArray) {
                    ForeignKey foreignKey = jc.foreignKey();
                    if (Objects.nonNull(foreignKey) && StringUtils.hasText(foreignKey.name())) {
                        stringMsg = foreignKey.name().toLowerCase().replace("_", ".") + CONSTRAINT_VIOLATED;
                        translation = getTranslationMessage(stringMsg, A_LINK_BETWEEN
                                + foreignKey.name()
                                + CAN_T_BE_VIOLATED);
                        excepMessage.put(foreignKey.name().toLowerCase(), stringMsg);
                        log.info(FK, translation);
                    }
                }
            }

            joinColumnArray = Arrays.asList(joinTable.inverseJoinColumns());
            if (!CollectionUtils.isEmpty(joinColumnArray)) {
                for (JoinColumn jc : joinColumnArray) {
                    ForeignKey foreignKey = jc.foreignKey();
                    if (Objects.nonNull(foreignKey) && StringUtils.hasText(foreignKey.name())) {
                        stringMsg = foreignKey.name().toLowerCase().replace("_", ".") + CONSTRAINT_VIOLATED;
                        translation = getTranslationMessage(stringMsg, A_LINK_BETWEEN
                                + foreignKey.name()
                                + CAN_T_BE_VIOLATED);
                        excepMessage.put(foreignKey.name().toLowerCase(), stringMsg);
                        log.info(FK, translation);
                    }
                }
            }
        }
    }

    private void processNotNullConstraint(EntityType entity, String attributeName, Field field) {
        String stringMsg;
        String translation;
        Table table = (Table) entity.getJavaType().getAnnotation(Table.class);
        Column column = field.getAnnotation(Column.class);
        if (Objects.nonNull(column) && !column.nullable()) {
            stringMsg = "not.null." + entity.getName().toLowerCase() + "." + attributeName.toLowerCase() + CONSTRAINT_VIOLATED;
            translation = getTranslationMessage(stringMsg, "The value "
                    + column.name()
                    + " of "
                    + table.name()
                    + " is required");
            excepMessage.put(column.name().toLowerCase(), stringMsg);
            log.info("NNU:{}", translation);
        }
    }

    private void processFieldName(Field field) {
        String translation = getTranslationMessage(field.getName(), field.getName());
        log.info("FLD:{}", translation);
    }

    private void processManagedException() {
        Set<BeanDefinition> managedExceptionBeans = springClassScanner.findAnnotatedClasses(MsgLocale.class, "eu.isygoit.exception");
        managedExceptionBeans.stream().forEach(beanDefinition -> {
            Class<?> cl = null;
            try {
                cl = Class.forName(beanDefinition.getBeanClassName());
                MsgLocale msgLocale = cl.getAnnotation(MsgLocale.class);
                if (Objects.nonNull(msgLocale) && StringUtils.hasText(msgLocale.value())) {
                    String translation = getTranslationMessage(msgLocale.value(), msgLocale.value());
                    log.info("EXCPT:{}", translation);
                } else {
                    log.error("<Error>: msgLocale annotation not defined for class type {}", beanDefinition.getBeanClassName());
                }
            } catch (ClassNotFoundException e) {
                log.error(CtrlConstants.ERROR_API_EXCEPTION, e);
            }
        });
    }

    private String getTranslationMessage(String stringMsg, String defaultTranslation) {
        String translation = this.environment.getProperty(stringMsg);
        StringBuilder translatedMessage = (new StringBuilder()).append(stringMsg).append("=")
                .append(!StringUtils.hasText(translation) ? translation : defaultTranslation);
        if (!StringUtils.hasText(translation)) {
            messages.get(TRANSLATED_MESSAGES).add(translatedMessage.toString());
        } else {
            messages.get(NON_TRANSLATED_MESSAGES).add(translatedMessage.toString());
        }
        return translatedMessage.toString();
    }
}
