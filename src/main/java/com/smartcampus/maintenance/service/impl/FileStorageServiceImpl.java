package com.smartcampus.maintenance.service.impl;

import com.smartcampus.maintenance.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }

        String newFileName = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = Paths.get(uploadDir, subDirectory).toAbsolutePath().normalize();
            Files.createDirectories(targetLocation);

            Path filePath = targetLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subDirectory + "/" + newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    @Override
    public String storeBase64Image(String dataUri, String subDirectory) {
        if (dataUri == null || dataUri.isBlank()) {
            return null;
        }

        String extension = ".jpg";
        String base64Data = dataUri;
        if (dataUri.contains(",")) {
            String header = dataUri.substring(0, dataUri.indexOf(",")).toLowerCase();
            base64Data = dataUri.substring(dataUri.indexOf(",") + 1);
            if (header.contains("image/png")) {
                extension = ".png";
            } else if (header.contains("image/webp")) {
                extension = ".webp";
            } else if (header.contains("image/heic")) {
                extension = ".heic";
            } else if (header.contains("image/heif")) {
                extension = ".heif";
            } else if (header.contains("image/jpeg") || header.contains("image/jpg")) {
                extension = ".jpg";
            }
        }

        try {
            byte[] bytes = java.util.Base64.getDecoder().decode(base64Data.trim());
            String newFileName = UUID.randomUUID().toString() + extension;
            Path targetLocation = Paths.get(uploadDir, subDirectory).toAbsolutePath().normalize();
            Files.createDirectories(targetLocation);

            Path filePath = targetLocation.resolve(newFileName);
            Files.write(filePath, bytes);

            return "/uploads/" + subDirectory + "/" + newFileName;
        } catch (Exception ex) {
            System.err.println("Failed to store base64 image: " + ex.getMessage());
            return null;
        }
    }

    @Override
    public String loadAsDataUri(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        try {
            String cleanPath = relativePath.trim();
            if (cleanPath.startsWith("/uploads/")) {
                cleanPath = cleanPath.substring("/uploads/".length());
            } else if (cleanPath.startsWith("uploads/")) {
                cleanPath = cleanPath.substring("uploads/".length());
            }
            Path filePath = Paths.get(uploadDir, cleanPath).toAbsolutePath().normalize();
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return null;
            }
            byte[] bytes = Files.readAllBytes(filePath);
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                String lower = filePath.getFileName().toString().toLowerCase();
                if (lower.endsWith(".png")) mimeType = "image/png";
                else if (lower.endsWith(".webp")) mimeType = "image/webp";
                else if (lower.endsWith(".heic")) mimeType = "image/heic";
                else if (lower.endsWith(".heif")) mimeType = "image/heif";
                else mimeType = "image/jpeg";
            }
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            return "data:" + mimeType + ";base64," + base64;
        } catch (Exception ex) {
            System.err.println("Could not load image as data URI: " + ex.getMessage());
            return null;
        }
    }
}
