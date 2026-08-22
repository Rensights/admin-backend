package com.rensights.admin.controller;

import com.rensights.admin.model.Building;
import com.rensights.admin.repository.BuildingRepository;
import com.rensights.admin.service.BuildingImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the building catalogue that feeds the analysis request form's type-ahead.
 */
@RestController
@RequestMapping("/api/admin/buildings")
public class BuildingController {

    private static final Logger logger = LoggerFactory.getLogger(BuildingController.class);

    private final BuildingRepository buildingRepository;
    private final BuildingImportService buildingImportService;

    public BuildingController(BuildingRepository buildingRepository,
                              BuildingImportService buildingImportService) {
        this.buildingRepository = buildingRepository;
        this.buildingImportService = buildingImportService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "25") int size,
                                  @RequestParam(defaultValue = "") String search) {
        Page<Building> buildings = buildingRepository.search(
            search.trim().toLowerCase(Locale.ROOT),
            PageRequest.of(page, size, Sort.by("name").ascending()));
        return ResponseEntity.ok(buildings);
    }

    /**
     * Import a CSV.
     *
     * @param replaceExisting when true the current catalogue is wiped first; otherwise rows are
     *                        merged, so re-importing a corrected file updates in place
     */
    @PostMapping("/import")
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file,
                                       @RequestParam(defaultValue = "false") boolean replaceExisting) {
        try {
            BuildingImportService.ImportResult result =
                buildingImportService.importCsv(file, replaceExisting);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Building import failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Could not import the file: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{buildingId}")
    public ResponseEntity<?> delete(@PathVariable UUID buildingId) {
        buildingRepository.deleteById(buildingId);
        return ResponseEntity.ok(Map.of("message", "Building deleted"));
    }
}
