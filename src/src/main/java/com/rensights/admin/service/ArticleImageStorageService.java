package com.rensights.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores article images (cover + inline content images) as files on the shared
 * reports volume instead of embedding them as base64 in the article payload -
 * base64 embedding was making article creation/update slow and bloating every
 * read of the article (list, public page) with the full image bytes.
 */
@Service
public class ArticleImageStorageService {

    // Matches a base64 image data URI, e.g. "data:image/png;base64,iVBOR...".
    // Group 1 = mime subtype (png/jpeg/...), group 2 = base64 payload.
    private static final Pattern DATA_URI_PATTERN =
        Pattern.compile("^data:image/([A-Za-z0-9.+-]+);base64,(.+)$", Pattern.DOTALL);

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
     * Persists a base64 image data URI (e.g. "data:image/png;base64,iVBOR...")
     * as a real file on the shared reports volume and returns the stored
     * filename, or {@code null} if the input is not a base64 image data URI.
     *
     * <p>Used to keep article cover/inline images off the article row: a data
     * URI submitted with the article is decoded here and replaced with a
     * "/api/articles/images/{filename}" URL so reads stay small.
     */
    public String storeDataUri(String dataUri) throws IOException {
        if (dataUri == null) {
            return null;
        }
        Matcher matcher = DATA_URI_PATTERN.matcher(dataUri.trim());
        if (!matcher.matches()) {
            return null;
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(matcher.group(2).replaceAll("\\s", ""));
        } catch (IllegalArgumentException invalidBase64) {
            return null;
        }
        String filename = UUID.randomUUID() + extensionForMime(matcher.group(1));

        Path baseDir = Paths.get(storagePath, "article-images");
        Files.createDirectories(baseDir);
        Files.write(baseDir.resolve(filename), bytes);

        return filename;
    }

    private String extensionForMime(String mimeSubtype) {
        switch (mimeSubtype.toLowerCase()) {
            case "png":     return ".png";
            case "jpeg":
            case "jpg":     return ".jpg";
            case "gif":     return ".gif";
            case "webp":    return ".webp";
            case "avif":    return ".avif";
            case "bmp":     return ".bmp";
            case "svg+xml": return ".svg";
            default:         return "";
        }
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
