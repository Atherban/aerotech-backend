package com.aerotech.ced_ops_backend.attachment.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class StorageService {

    @Value("${app.storage.upload-dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadPath, e);
        }
    }

    public String store(MultipartFile file, String storedFileName) throws IOException {
        Path targetPath = resolvePath(storedFileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return targetPath.toString();
    }

    public Resource loadAsResource(String storedFileName) throws MalformedURLException {
        Path filePath = resolvePath(storedFileName);
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File not found: " + storedFileName);
        }
        return resource;
    }

    private Path resolvePath(String storedFileName) {
        Path resolved = uploadPath.resolve(storedFileName).normalize();
        if (!resolved.startsWith(uploadPath)) {
            throw new SecurityException("Path traversal attempt detected: " + storedFileName);
        }
        return resolved;
    }

}
