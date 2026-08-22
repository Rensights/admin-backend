package com.rensights.admin.service;

import com.rensights.admin.model.Area;
import com.rensights.admin.repository.AreaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Imports the area / district list from a CSV uploaded in the admin app.
 *
 * <p>File handling lives in {@link NameCsvReader} — see it for which shapes are accepted. Only
 * the name is stored; any other column in the file is ignored.
 *
 * <p>The name matters more here than for buildings: it is submitted to the analysis module as
 * the property's community, so it has to match that module's naming (DLD community names).
 *
 * <p>Re-running an import is safe: names already present are skipped case-insensitively, as are
 * duplicates within the file itself.
 */
@Service
public class AreaImportService {

    private static final Logger logger = LoggerFactory.getLogger(AreaImportService.class);

    private final AreaRepository areaRepository;
    private final NameCsvReader csvReader;

    public AreaImportService(AreaRepository areaRepository, NameCsvReader csvReader) {
        this.areaRepository = areaRepository;
        this.csvReader = csvReader;
    }

    /** What the import did, for the admin to see rather than a bare "done". */
    public record ImportResult(int created, int skipped, List<String> problems) {}

    /**
     * @param replaceExisting when true the catalogue is emptied first — for when the CSV is the
     *                        new full truth rather than an addition
     */
    @Transactional
    public ImportResult importCsv(MultipartFile file, boolean replaceExisting) throws IOException {
        List<String> names = csvReader.readNames(file);

        if (replaceExisting) {
            areaRepository.deleteAllInBatch();
            logger.info("Area catalogue cleared before import");
        }

        Set<String> seen = new HashSet<>();
        int created = 0;
        int skipped = 0;
        List<String> problems = new ArrayList<>();

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if (name.isEmpty() || !seen.add(name.toLowerCase(Locale.ROOT))) {
                skipped++;
                continue;
            }

            try {
                if (!replaceExisting
                    && areaRepository.findByNameIgnoringCase(name.toLowerCase(Locale.ROOT)).isPresent()) {
                    skipped++;
                    continue;
                }
                areaRepository.save(Area.builder().name(name).build());
                created++;
            } catch (Exception e) {
                skipped++;
                // Cap the report: a badly formed file should not return 4,000 error lines.
                if (problems.size() < 20) {
                    problems.add("Line " + (i + 1) + ": " + e.getMessage());
                }
            }
        }

        logger.info("Area import finished: {} added, {} skipped", created, skipped);
        return new ImportResult(created, skipped, problems);
    }
}
