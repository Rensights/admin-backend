package com.rensights.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Stores article images (cover + inline content images) as files on the shared
 * reports volume instead of embedding them as base64 in the article payload -
 * base64 embedding was making article creation/update slow and bloating every
 * read of the article (list, public page) with the full image bytes.
 */
@Service
public class ArticleImageStorageService {

    @Value("${reports.storage.path:/data/reports}")
    private String storagePath;

    public String storeImage(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image";
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }
        String filename = UUID.randomUUID() + extension;

        Path baseDir = Paths.get(storagePath, "article-images");
        Files.createDirectories(baseDir);
        Path destination = baseDir.resolve(filename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    /**
     * Deletes a previously uploaded image by filename. Silently ignores
     * missing files - cleanup is best-effort and shouldn't block article
     * deletion if the file is already gone.
     */
    public void deleteImage(String filename) {
        if (filename == null || filename.isBlank()
                || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            return;
        }
        try {
            Path path = Paths.get(storagePath, "article-images", filename);
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best-effort cleanup - an orphaned file is not worth failing the delete over.
        }
    }
}
