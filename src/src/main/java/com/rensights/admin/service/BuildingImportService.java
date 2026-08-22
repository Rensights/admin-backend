package com.rensights.admin.service;

import com.rensights.admin.model.Building;
import com.rensights.admin.repository.BuildingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Imports the building catalogue from a CSV uploaded in the admin app.
 *
 * <h2>Accepted shape</h2>
 * A header row is expected and column names are matched loosely, because the file comes from
 * whatever tool produced it: the name column may be called {@code name}, {@code building},
 * {@code building_name} or {@code project}; the rest ({@code area}/{@code community}/
 * {@code district}, {@code city}, {@code developer}) are optional. A file with no recognisable
 * header is read as a single column of names, which is the other common export.
 *
 * <p>Both comma and semicolon delimiters are handled — Excel writes semicolons in several
 * locales — and quoted fields containing the delimiter are parsed correctly.
 *
 * <h2>Re-running an import</h2>
 * Rows are matched on name + area and updated in place, so importing a corrected file does not
 * duplicate the catalogue. Blank names are skipped rather than failing the whole file: a
 * trailing empty line should not cost the admin the other 4,000 rows.
 */
@Service
public class BuildingImportService {

    private static final Logger logger = LoggerFactory.getLogger(BuildingImportService.class);

    private static final List<String> NAME_HEADERS =
        List.of("name", "building", "building_name", "buildingname", "project", "project_name", "tower");
    private static final List<String> AREA_HEADERS =
        List.of("area", "community", "district", "location", "sub_community");
    private static final List<String> CITY_HEADERS = List.of("city", "emirate");
    private static final List<String> DEVELOPER_HEADERS = List.of("developer", "builder");

    private final BuildingRepository buildingRepository;

    public BuildingImportService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    /** What the import did, for the admin to see rather than a bare "done". */
    public record ImportResult(int created, int updated, int skipped, List<String> problems) {}

    /**
     * @param replaceExisting when true the catalogue is emptied first — for when the CSV is the
     *                        new full truth rather than an addition
     */
    @Transactional
    public ImportResult importCsv(MultipartFile file, boolean replaceExisting) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded.");
        }

        List<String[]> rows = readRows(file);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("The file is empty.");
        }

        Map<String, Integer> columns = mapHeader(rows.get(0));
        // No recognisable header means the file is a bare list of names, and the first line is
        // data rather than a header.
        int firstDataRow = columns.isEmpty() ? 0 : 1;
        if (columns.isEmpty()) {
            columns = Map.of("name", 0);
        }

        if (replaceExisting) {
            buildingRepository.deleteAllInBatch();
            logger.info("Building catalogue cleared before import");
        }

        int created = 0;
        int updated = 0;
        int skipped = 0;
        List<String> problems = new ArrayList<>();

        for (int i = firstDataRow; i < rows.size(); i++) {
            String[] row = rows.get(i);
            String name = value(row, columns.get("name"));

            if (name.isEmpty()) {
                skipped++;
                continue;
            }

            String area = value(row, columns.get("area"));
            String city = value(row, columns.get("city"));
            String developer = value(row, columns.get("developer"));

            try {
                Optional<Building> existing = replaceExisting
                    ? Optional.empty()
                    : buildingRepository.findByNameAndArea(
                        name.toLowerCase(Locale.ROOT), area.toLowerCase(Locale.ROOT));

                if (existing.isPresent()) {
                    Building building = existing.get();
                    building.setCity(city.isEmpty() ? building.getCity() : city);
                    building.setDeveloper(developer.isEmpty() ? building.getDeveloper() : developer);
                    buildingRepository.save(building);
                    updated++;
                } else {
                    buildingRepository.save(Building.builder()
                        .name(name)
                        .area(area.isEmpty() ? null : area)
                        .city(city.isEmpty() ? null : city)
                        .developer(developer.isEmpty() ? null : developer)
                        .build());
                    created++;
                }
            } catch (Exception e) {
                skipped++;
                // Cap the report: a badly formed file should not return 4,000 error lines.
                if (problems.size() < 20) {
                    problems.add("Line " + (i + 1) + ": " + e.getMessage());
                }
            }
        }

        logger.info("Building import finished: {} created, {} updated, {} skipped",
            created, updated, skipped);
        return new ImportResult(created, updated, skipped, problems);
    }

    private List<String[]> readRows(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            char delimiter = ',';
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    // Strip a UTF-8 BOM, which Excel adds and which would otherwise become part
                    // of the first header name.
                    line = line.replace("﻿", "");
                    delimiter = detectDelimiter(line);
                    firstLine = false;
                }
                if (line.isBlank()) {
                    continue;
                }
                rows.add(splitCsvLine(line, delimiter));
            }
        }
        return rows;
    }

    private char detectDelimiter(String headerLine) {
        long semicolons = headerLine.chars().filter(c -> c == ';').count();
        long commas = headerLine.chars().filter(c -> c == ',').count();
        return semicolons > commas ? ';' : ',';
    }

    /** Minimal CSV split: honours double quotes and escaped ("") quotes inside a field. */
    private String[] splitCsvLine(String line, char delimiter) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == delimiter && !inQuotes) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString().trim());
        return fields.toArray(new String[0]);
    }

    /** Column name -> index, for whichever of the known aliases the file happens to use. */
    private Map<String, Integer> mapHeader(String[] header) {
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            String cell = header[i].trim().toLowerCase(Locale.ROOT).replace(' ', '_');
            if (NAME_HEADERS.contains(cell)) {
                columns.putIfAbsent("name", i);
            } else if (AREA_HEADERS.contains(cell)) {
                columns.putIfAbsent("area", i);
            } else if (CITY_HEADERS.contains(cell)) {
                columns.putIfAbsent("city", i);
            } else if (DEVELOPER_HEADERS.contains(cell)) {
                columns.putIfAbsent("developer", i);
            }
        }
        // Without a name column there is nothing to import, whatever else was recognised.
        return columns.containsKey("name") ? columns : Map.of();
    }

    private String value(String[] row, Integer index) {
        if (index == null || index >= row.length || row[index] == null) {
            return "";
        }
        return row[index].trim();
    }

    /** Exposed for the header-detection unit check in the admin app. */
    List<String> knownNameHeaders() {
        return Arrays.asList(NAME_HEADERS.toArray(new String[0]));
    }
}
