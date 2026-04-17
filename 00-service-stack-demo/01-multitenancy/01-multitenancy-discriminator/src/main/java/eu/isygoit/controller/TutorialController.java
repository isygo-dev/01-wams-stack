package eu.isygoit.controller;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.MappedCrudController;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedCrudTenantController;
import eu.isygoit.com.rest.tenant.filter.TenantFilterable;
import eu.isygoit.dto.TutorialDto;
import eu.isygoit.mapper.TutorialMapper;
import eu.isygoit.model.Tutorial;
import eu.isygoit.repository.TutorialRepository;
import eu.isygoit.service.TutorialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = TutorialMapper.class, minMapper = TutorialMapper.class)
@InjectService(TutorialService.class)
@RestController
@RequestMapping("/api/v1/tutorials")
public class TutorialController extends MappedCrudController<Long, Tutorial,
        TutorialDto, TutorialDto, TutorialService> {

    private final TutorialRepository tutorialRepository;
    private final TutorialMapper tutorialMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public TutorialController(TutorialRepository tutorialRepository, TutorialMapper tutorialMapper) {
        this.tutorialRepository = tutorialRepository;
        this.tutorialMapper = tutorialMapper;
    }

    @TenantFilterable
    @Operation(summary = "Get all tutorials", description = "Retrieve all tutorials or filter by title (contains, case-insensitive).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found tutorials"),
            @ApiResponse(responseCode = "204", description = "No tutorials found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/title")
    public ResponseEntity<List<TutorialDto>> getAllTutorials(
            @Parameter(description = "Filter tutorials by title") @RequestParam(required = false) String title) {
        try {
            var tutorials = (title == null)
                    ? crudService().findAll()
                    : tutorialRepository.findByTitleContainingIgnoreCase(title);

            var dtos = tutorialMapper.listEntityToDto(tutorials);
            return dtos.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(dtos);
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

    @TenantFilterable
    @Operation(summary = "Get all published tutorials")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Published tutorials found"),
            @ApiResponse(responseCode = "204", description = "No published tutorials found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/published")
    public ResponseEntity<List<TutorialDto>> findByPublished() {
        try {
            var tutorials = tutorialRepository.findByPublished(true);
            return tutorials.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(tutorialMapper.listEntityToDto(tutorials));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
