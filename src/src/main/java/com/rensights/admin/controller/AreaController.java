package com.rensights.admin.controller;

import com.rensights.admin.model.Area;
import com.rensights.admin.repository.AreaRepository;
import com.rensights.admin.service.AreaImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the area catalogue that feeds the analysis request form's type-ahead.
 */
@RestController
@RequestMapping("/api/admin/areas")
public class AreaController {

    private static final Logger logger = LoggerFactory.getLogger(AreaController.class);

    private final AreaRepository areaRepository;
    private final AreaImportService areaImportService;

    public AreaController(AreaRepository areaRepository,
                              AreaImportService areaImportService) {
        this.areaRepository = areaRepository;
        this.areaImportService = areaImportService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "25") int size,
                                  @RequestParam(defaultValue = "") String search) {
        Page<Area> areas = areaRepository.search(
            search.trim().toLowerCase(Locale.ROOT),
            // Ordering lives in the query (case-insensitive); no Sort here.
            PageRequest.of(page, size));
        return ResponseEntity.ok(areas);
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
            AreaImportService.ImportResult result =
                areaImportService.importCsv(file, replaceExisting);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Area import failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Could not import the file: " + e.getMessage()));
        }
    }

    /** Add a single area by hand, for the one-off that is not worth a CSV. */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> request) {
        String name = request.getOrDefault("name", "").trim();
        if (name.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }

        if (areaRepository.findByNameIgnoringCase(name.toLowerCase(Locale.ROOT)).isPresent()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "\"" + name + "\" is already in the catalogue"));
        }

        Area saved = areaRepository.save(Area.builder().name(name).build());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{areaId}")
    public ResponseEntity<?> delete(@PathVariable UUID areaId) {
        areaRepository.deleteById(areaId);
        return ResponseEntity.ok(Map.of("message", "Area deleted"));
    }
}
