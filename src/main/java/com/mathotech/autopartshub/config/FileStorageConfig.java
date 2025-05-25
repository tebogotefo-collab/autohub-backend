package com.mathotech.autopartshub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    @Value("${app.storage.location}")
    private String uploadDir;

    @Bean
    public Path fileStorageLocation() {
        Path fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(fileStorageLocation);
            return fileStorageLocation;
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored", ex);
        }
    }
}
