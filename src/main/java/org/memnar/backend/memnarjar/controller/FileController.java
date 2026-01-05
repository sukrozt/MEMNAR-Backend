package org.memnar.backend.memnarjar.controller;

import org.memnar.backend.memnarjar.dto.FileDTO; // Or your specific DTO
import org.memnar.backend.memnarjar.model.ConfigData;
import org.memnar.backend.memnarjar.model.MemnarJarStatus;
import org.memnar.backend.memnarjar.service.ConfigService;
import org.memnar.backend.memnarjar.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller; // Use @Controller for WebSocket

import java.nio.file.Path;

@Controller
public class FileController {

    private final FileService fileService;
    private final ConfigService configService;

    @Autowired
    public FileController(FileService fileService, ConfigService configService) {
        this.fileService = fileService;
        this.configService = configService;
    }

    @MessageMapping("/memnarjar/datainput") //listens from
    @SendTo("/memnarjar/status") //send to 
    public MemnarJarStatus handleFileUpload(FileDTO data) {
        System.out.println("\n🚀 UPLOAD RECEIVED (Handled by FileController)");

        try {
            Path zipPath = fileService.saveFile(data.getName(), data.getBase64(), data.getChunkIndex(), data.getTotalChunks());
            fileService.unzip(zipPath, "."); 
            ConfigData config = configService.getConfig();
            config.setDatasetName("mutation_data");
            configService.updateConfig(config);

            return new MemnarJarStatus("Success", "File updated and extracted.");
        } catch (Exception e) {
            e.printStackTrace();
            return new MemnarJarStatus("Error", "Upload failed: " + e.getMessage());
        }
    }
}