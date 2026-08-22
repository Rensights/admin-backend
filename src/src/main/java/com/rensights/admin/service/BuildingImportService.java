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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Imports the building catalogue from a CSV uploaded in the admin app.
 *
 * <h2>Accepted shape</h2>
 * The catalogue holds names and nothing else, so only one column is read. If the first line
 * looks like a header naming that column ({@code name}, {@code building}, {@code building_name},
 * {@code project}, {@code tower}) it is skipped and that column is used; otherwise the file is
 * treated as a bare list of names and the first column is read from line one. Extra columns are
 * ignored rather than rejected — a file exported with area and developer still imports, those
 * values are simply dropped.
 *
 * <p>Both comma and semicolon delimiters are handled — Excel writes semicolons in several
 * locales — and quoted fields containing the delimiter are parsed correctly.
 *
 * <h2>Re-running an import</h2>
 * Names already in the catalogue are skipped, case-insensitively, so importing a file twice does
 * not duplicate it. Blank lines are skipped rather than failing the file: a trailing empty line
 * should not cost the admin the other 4,000 rows.
 */
@Service
public class BuildingImportService {

    private static final Logger logger = LoggerFactory.getLogger(BuildingImportService.class);

    private static final List<String> NAME_HEADERS = List.of(
        "name", "building", "building_name", "buildingname", "project", "project_name", "tower");

    private final BuildingRepository buildingRepository;

    public BuildingImportService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    /** What the import did, for the admin to see rather than a bare "done". */
    public record ImportResult(int created, int skipped, List<String> problems) {}

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

        int nameColumn = 0;
        int firstDataRow = 0;
        int headerColumn = findNameColumn(rows.get(0));
        if (headerColumn >= 0) {
            nameColumn = headerColumn;
            firstDataRow = 1;
        }

        if (replaceExisting) {
            buildingRepository.deleteAllInBatch();
            logger.info("Building catalogue cleared before import");
        }

        // Tracks names seen in this file too, so a CSV with its own duplicates only lands once.
        Set<String> seen = new HashSet<>();
        int created = 0;
        int skipped = 0;
        List<String> problems = new ArrayList<>();

        for (int i = firstDataRow; i < rows.size(); i++) {
            String name = value(rows.get(i), nameColumn);
            if (name.isEmpty()) {
                skipped++;
                continue;
            }

            String key = name.toLowerCase(Locale.ROOT);
            if (!seen.add(key)) {
                skipped++;
                continue;
            }

            try {
                if (!replaceExisting && buildingRepository.findByNameIgnoringCase(key).isPresent()) {
                    skipped++;
                    continue;
                }
                buildingRepository.save(Building.builder().name(name).build());
                created++;
            } catch (Exception e) {
                skipped++;
                // Cap the report: a badly formed file should not return 4,000 error lines.
                if (problems.size() < 20) {
                    problems.add("Line " + (i + 1) + ": " + e.getMessage());
                }
            }
        }

        logger.info("Building import finished: {} added, {} skipped", created, skipped);
        return new ImportResult(created, skipped, problems);
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

    /** Index of the name column if the first row is a header, or -1 when it is data already. */
    private int findNameColumn(String[] header) {
        for (int i = 0; i < header.length; i++) {
            String cell = header[i].trim().toLowerCase(Locale.ROOT).replace(' ', '_');
            if (NAME_HEADERS.contains(cell)) {
                return i;
            }
        }
        return -1;
    }

    private String value(String[] row, int index) {
        if (index >= row.length || row[index] == null) {
            return "";
        }
        return row[index].trim();
    }
}
