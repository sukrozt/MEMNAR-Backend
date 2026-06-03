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

import java.io.File;
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

    @MessageMapping("/api/memnarjar/datainput") //listens from
    @SendTo("/memnarjar/status") //send to 
    public MemnarJarStatus handleFileUpload(FileDTO data) {
        System.out.println("\nUPLOAD RECEIVED");

        try {
            Path savedTxt = fileService.saveFile(data.getName(), data.getBase64(), data.getChunkIndex(), data.getTotalChunks());
            
            if (data.getChunkIndex() < data.getTotalChunks() - 1) {
                return new MemnarJarStatus("Uploading", "Chunk " + (data.getChunkIndex() + 1) + " of " + data.getTotalChunks() + " received.");
            }

            // Set the saved text file path directly in the config
            ConfigData config = configService.getConfig();
            String exactTextFilePath = savedTxt.toString(); 
            config.setDatasetName(exactTextFilePath); 
            // System.out.println("[DEBUG - FileController] Set DatasetName in config to: " + exactTextFilePath);
            configService.updateConfig(config);
            
            return new MemnarJarStatus("Success", "Txt uploaded and ready for MEMNAR.");

        } catch (Exception e) {
            e.printStackTrace();
            return new MemnarJarStatus("Error", "Upload failed: " + e.getMessage());
        }
    }
}