package com.rensights.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ReportStorageService {

    @Value("${reports.storage.path:/data/reports}")
    private String storagePath;

    public String storeReportFile(String sectionKey, UUID documentId, MultipartFile file) throws IOException {
        String safeSection = sectionKey.replaceAll("[^a-zA-Z0-9_-]", "-");
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf";
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }
        String filename = documentId + extension;

        Path baseDir = Paths.get(storagePath, "city-analysis", safeSection);
        Files.createDirectories(baseDir);
        Path destination = baseDir.resolve(filename);
        Files.copy(file.getInputStream(), destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return Paths.get("city-analysis", safeSection, filename).toString().replace("\\", "/");
    }

    public Path resolvePath(String relativePath) {
        return Paths.get(storagePath).resolve(relativePath).normalize();
    }

    public void deleteFile(String relativePath) {
        try {
            Path path = resolvePath(relativePath);
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
