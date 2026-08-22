package com.rensights.admin.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads a one-column-of-names CSV, whatever shape it arrives in.
 *
 * <p>Shared by the building and area catalogues: both hold names and nothing else, and both are
 * fed by files exported from assorted tools. Keeping the parsing in one place means a fix for
 * one catalogue is a fix for both.
 *
 * <h2>What it copes with</h2>
 * <ul>
 *   <li>a header naming the column, in which case that column is read and the header skipped;
 *       otherwise the file is treated as a bare list and the first column is read from line one</li>
 *   <li>comma or semicolon delimiters — Excel writes semicolons in several locales</li>
 *   <li>quoted fields containing the delimiter, and {@code ""} escaped quotes</li>
 *   <li>a UTF-8 BOM, which Excel adds and which would otherwise corrupt the first header name</li>
 *   <li>blank lines, which are skipped rather than failing the file</li>
 * </ul>
 * Extra columns are ignored rather than rejected: a file exported with more detail still imports.
 */
@Component
public class NameCsvReader {

    private final List<String> headerAliases;

    public NameCsvReader() {
        this(List.of("name", "building", "building_name", "buildingname", "project",
            "project_name", "tower", "area", "community", "district"));
    }

    NameCsvReader(List<String> headerAliases) {
        this.headerAliases = headerAliases;
    }

    /** Every non-empty name in the file, in file order, untrimmed of duplicates. */
    public List<String> readNames(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded.");
        }

        List<String[]> rows = readRows(file);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("The file is empty.");
        }

        int column = 0;
        int firstDataRow = 0;
        int headerColumn = findNameColumn(rows.get(0));
        if (headerColumn >= 0) {
            column = headerColumn;
            firstDataRow = 1;
        }

        List<String> names = new ArrayList<>();
        for (int i = firstDataRow; i < rows.size(); i++) {
            String[] row = rows.get(i);
            String value = column < row.length && row[column] != null ? row[column].trim() : "";
            names.add(value);
        }
        return names;
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
            if (headerAliases.contains(cell)) {
                return i;
            }
        }
        return -1;
    }
}
