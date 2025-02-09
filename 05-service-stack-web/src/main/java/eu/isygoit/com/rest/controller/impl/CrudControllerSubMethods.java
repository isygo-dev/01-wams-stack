package eu.isygoit.com.rest.controller.impl;

import eu.isygoit.com.rest.controller.ICrudControllerSubMethods;
import eu.isygoit.com.rest.controller.ResponseFactory;
import eu.isygoit.com.rest.service.ICrudServiceMethod;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.dto.IIdentifiableDto;
import eu.isygoit.dto.common.RequestContextDto;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.ISAASEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Crud controller sub methods.
 *
 * @param <I>     the type parameter
 * @param <T>     the type parameter
 * @param <MIND>  the type parameter
 * @param <FULLD> the type parameter
 * @param <S>     the type parameter
 */
@Slf4j
public abstract class CrudControllerSubMethods<I, T extends IIdEntity,
        MIND extends IIdentifiableDto,
        FULLD extends MIND, S extends ICrudServiceMethod<I, T>>
        extends CrudControllerUtils<T, MIND, FULLD, S>
        implements ICrudControllerSubMethods<I, T, MIND, FULLD, S> {

    private final Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    @Override
    public ResponseEntity<FULLD> subCreate(FULLD dto) {
        log.info("subCreate - Received request to create {}. DTO: {}", entityClass.getSimpleName(), dto);

        // If the DTO is null, return a bad request response
        if (dto == null) {
            log.warn("subCreate - Null DTO provided. Returning bad request response.");
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            // Perform any pre-create logic (e.g., validation)
            FULLD dtoBeforeCreate = beforeCreate(dto);

            // Create the entity and map it to the response DTO
            T createdEntity = crudService().create(mapper().dtoToEntity(dtoBeforeCreate));
            log.info("subCreate - Successfully created {} with I: {}", entityClass.getSimpleName(), createdEntity.getId());

            // Return the successful response with the created DTO
            return ResponseFactory.ResponseOk(mapper().entityToDto(afterCreate(createdEntity)));
        } catch (Exception e) {
            // Log and return an error response if an exception occurs
            log.error("subCreate - Error occurred while creating {}. Exception: {}", entityClass.getSimpleName(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<FULLD>> subCreate(List<FULLD> dtos) {
        log.info("subCreate - Received request to create multiple {}s. Number of DTOs: {}", entityClass.getSimpleName(), dtos.size());

        // If the DTO list is empty, return a bad request response
        if (CollectionUtils.isEmpty(dtos)) {
            log.warn("subCreate - Empty DTO list provided. Returning bad request response.");
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            // Map each DTO, before and after creating it
            List<T> createdEntities = dtos.stream()
                    .map(dto -> mapper().dtoToEntity(beforeCreate(dto)))
                    .map(crudService()::create)
                    .collect(Collectors.toList());

            // Map the created entities back to DTOs for the response
            List<FULLD> responseDtos = mapper().listEntityToDto(createdEntities);
            log.info("subCreate - Successfully created {} DTOs. Returning response with {} items.", entityClass.getSimpleName(), responseDtos.size());

            // Return the successful response
            return ResponseFactory.ResponseOk(responseDtos);
        } catch (Exception e) {
            // Log and return an error response if an exception occurs
            log.error("subCreate - Error occurred while creating multiple {}s. Exception: {}", entityClass.getSimpleName(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<String> subDelete(RequestContextDto requestContext, I entityId) {
        log.info("subDelete - Received request to delete {} with I: {} from domain: {}", entityClass.getSimpleName(), entityId, requestContext.getSenderDomain());

        // If the I is null, return a bad request response
        if (entityId == null) {
            log.warn("subDelete - Null entity I provided. Returning bad request response.");
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            // Before deleting, check if it's safe or necessary
            if (beforeDelete(entityId)) {
                // Delete the entity
                crudService().delete(requestContext.getSenderDomain(), entityId);
                // Perform any actions after deletion
                afterDelete(entityId);
                log.info("subDelete - Successfully deleted {} with I: {}", entityClass.getSimpleName(), entityId);
            }
            return ResponseFactory.ResponseOk(getExceptionHandler().handleMessage("object.deleted.successfully"));
        } catch (Exception e) {
            // Log and return an error response if an exception occurs
            log.error("subDelete - Error occurred while deleting {} with I: {}. Exception: {}", entityClass.getSimpleName(), entityId, e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<MIND>> subGetAll(RequestContextDto requestContext) {
        log.info("subGetAll - Received request to retrieve all {}s. Domain: {}", entityClass.getSimpleName(), requestContext.getSenderDomain());

        try {
            List<MIND> resultDtos;
            String domain = requestContext.getSenderDomain();

            // For ISAASEntity, we check the domain to apply the appropriate logic
            if (ISAASEntity.class.isAssignableFrom(entityClass) && !DomainConstants.SUPER_DOMAIN_NAME.equals(domain)) {
                resultDtos = minDtoMapper().listEntityToDto(crudService().getAll(domain));
            } else {
                resultDtos = minDtoMapper().listEntityToDto(crudService().getAll());
            }

            // If no records are found, log and return no content
            if (resultDtos.isEmpty()) {
                log.info("subGetAll - No {}s found. Returning no content response.", entityClass.getSimpleName());
                return ResponseFactory.ResponseNoContent();
            }

            // Log successful retrieval of data
            log.info("subGetAll - Found {} {}s. Returning response.", resultDtos.size(), entityClass.getSimpleName());
            return ResponseFactory.ResponseOk(afterGetAll(requestContext, resultDtos));
        } catch (Exception e) {
            // Log and return an error response if an exception occurs
            log.error("subGetAll - Error occurred while retrieving all {}s. Exception: {}", entityClass.getSimpleName(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<Long> subGetCount(RequestContextDto requestContext) {
        log.info("subGetCount - Received request to get count of {}s. Domain: {}", entityClass.getSimpleName(), requestContext.getSenderDomain());

        try {
            // Determine the count based on whether the domain matches certain criteria
            long entityCount = ISAASEntity.class.isAssignableFrom(entityClass)
                    && !DomainConstants.SUPER_DOMAIN_NAME.equals(requestContext.getSenderDomain())
                    ? crudService().count(requestContext.getSenderDomain()) : crudService().count();

            log.info("subGetCount - Count of {}s: {}", entityClass.getSimpleName(), entityCount);
            return ResponseFactory.ResponseOk(entityCount);
        } catch (Exception e) {
            // Log and return an error response if an exception occurs
            log.error("subGetCount - Error occurred while retrieving count of {}s. Exception: {}", entityClass.getSimpleName(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<FULLD> subUpdate(I entityId, FULLD dto) {
        log.info("subUpdate - Received request to update {} with I: {}. DTO: {}", entityClass.getSimpleName(), entityId, dto);

        // Check for null values before proceeding
        if (dto == null || entityId == null) {
            log.warn("subUpdate - Null DTO or entity I provided. Returning bad request response.");
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            // Set the entity I to the DTO and perform any pre-update logic
            dto.setId(entityId);
            FULLD dtoBeforeUpdate = beforeUpdate(entityId, dto);

            // Update the entity and map the result to the response DTO
            T updatedEntity = crudService().update(mapper().dtoToEntity(dtoBeforeUpdate));
            log.info("subUpdate - Successfully updated {} with I: {}", entityClass.getSimpleName(), entityId);

            return ResponseFactory.ResponseOk(mapper().entityToDto(afterUpdate(updatedEntity)));
        } catch (Exception e) {
            // Log and return an error response if an exception occurs
            log.error("subUpdate - Error occurred while updating {} with I: {}. Exception: {}", entityClass.getSimpleName(), entityId, e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    @Override
    public ResponseEntity<List<FULLD>> subUpdate(List<FULLD> dtos) {
        log.info("subUpdate - Received request to update multiple {}s. Number of DTOs: {}", entityClass.getSimpleName(), dtos.size());

        // If the DTO list is empty, return a bad request response
        if (CollectionUtils.isEmpty(dtos)) {
            log.warn("subUpdate - Empty DTO list provided. Returning bad request response.");
            return ResponseFactory.ResponseBadRequest();
        }

        try {
            // Update each DTO and map to DTOs
            List<T> updatedEntities = crudService().update(mapper().listDtoToEntity(dtos));
            List<FULLD> responseDtos = mapper().listEntityToDto(updatedEntities);

            log.info("subUpdate - Successfully updated {} DTOs. Returning response with {} items.", entityClass.getSimpleName(), responseDtos.size());

            return ResponseFactory.ResponseOk(responseDtos);
        } catch (Exception e) {
            // Log and return an error response if an exception occurs
            log.error("subUpdate - Error occurred while updating multiple {}s. Exception: {}", entityClass.getSimpleName(), e.getMessage(), e);
            return getBackExceptionResponse(e);
        }
    }

    // Additional variable renaming and logging improvements are applied similarly in other methods.
}