package eu.isygoit.controller;

import eu.isygoit.audit.TenantContext;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.dto.TutorialDto;
import eu.isygoit.mapper.TutorialMapper;
import eu.isygoit.repository.TutorialRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api/v1/tutorials")
public class TutorialController extends AbstractTenantController {

    private final TutorialRepository tutorialRepository;
    private final TutorialMapper tutorialMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public TutorialController(TutorialRepository tutorialRepository, TutorialMapper tutorialMapper) {
        this.tutorialRepository = tutorialRepository;
        this.tutorialMapper = tutorialMapper;
    }

    @Operation(summary = "Get all tutorials", description = "Retrieve all tutorials or filter by title (contains, case-insensitive).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found tutorials"),
            @ApiResponse(responseCode = "204", description = "No tutorials found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<TutorialDto>> getAllTutorials(
            @Parameter(description = "Filter tutorials by title") @RequestParam(required = false) String title) {
        try {
            String tenantId = TenantContext.getTenantId();
            if (tenantId != null && !tenantId.equals(TenantConstants.SUPER_TENANT_NAME)) {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
            }

            var tutorials = (title == null)
                    ? tutorialRepository.findAll()
                    : tutorialRepository.findByTitleContainingIgnoreCase(title);

            var dtos = tutorialMapper.listEntityToDto(tutorials);
            return dtos.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get tutorial by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tutorial found"),
            @ApiResponse(responseCode = "404", description = "Tutorial not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TutorialDto> getTutorialById(@PathVariable long id) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null && !tenantId.equals(TenantConstants.SUPER_TENANT_NAME)) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        }

        return tutorialRepository.findOneById(id)
                .map(tutorialMapper::entityToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new tutorial")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tutorial created"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<TutorialDto> createTutorial(@Valid @RequestBody TutorialDto TutorialDto) {
        try {
            var saved = tutorialRepository.save(tutorialMapper.dtoToEntity(TutorialDto));
            return ResponseEntity.status(HttpStatus.CREATED).body(tutorialMapper.entityToDto(saved));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update tutorial by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tutorial updated"),
            @ApiResponse(responseCode = "404", description = "Tutorial not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TutorialDto> updateTutorial(@PathVariable long id, @RequestBody TutorialDto TutorialDto) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null && !tenantId.equals(TenantConstants.SUPER_TENANT_NAME)) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        }

        return tutorialRepository.findOneById(id)
                .map(existing -> {
                    existing.setTitle(TutorialDto.getTitle());
                    existing.setDescription(TutorialDto.getDescription());
                    existing.setPublished(TutorialDto.isPublished());
                    return ResponseEntity.ok(tutorialMapper.entityToDto(tutorialRepository.save(existing)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete tutorial by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tutorial deleted"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTutorial(@PathVariable long id) {
        try {
            tutorialRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Delete all tutorials")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "All tutorials deleted"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteAllTutorials() {
        try {
            tutorialRepository.deleteAll();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get all published tutorials")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Published tutorials found"),
            @ApiResponse(responseCode = "204", description = "No published tutorials found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/published")
    public ResponseEntity<List<TutorialDto>> findByPublished() {
        try {
            String tenantId = TenantContext.getTenantId();
            if (tenantId != null && !tenantId.equals(TenantConstants.SUPER_TENANT_NAME)) {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
            }

            var tutorials = tutorialRepository.findByPublished(true);
            return tutorials.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(tutorialMapper.listEntityToDto(tutorials));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Bulk create tutorials")
    @PostMapping("/batch")
    public ResponseEntity<List<TutorialDto>> createTutorials(@Valid @RequestBody List<TutorialDto> dtos) {
        try {
            var saved = tutorialRepository.saveAll(tutorialMapper.listDtoToEntity(dtos));
            return ResponseEntity.status(HttpStatus.OK).body(tutorialMapper.listEntityToDto(saved));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
