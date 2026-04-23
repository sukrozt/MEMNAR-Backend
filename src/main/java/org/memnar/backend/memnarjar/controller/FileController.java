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

    @MessageMapping("/memnarjar/datainput") //listens from
    @SendTo("/memnarjar/status") //send to 
    public MemnarJarStatus handleFileUpload(FileDTO data) {
        System.out.println("\nUPLOAD RECEIVED");

        try {
            Path savedZip = fileService.saveFile(data.getName(), data.getBase64(), data.getChunkIndex(), data.getTotalChunks());
            
            if (data.getChunkIndex() < data.getTotalChunks() - 1) {
                return new MemnarJarStatus("Uploading", "Chunk " + (data.getChunkIndex() + 1) + " of " + data.getTotalChunks() + " received.");
            }
            fileService.unzip(savedZip, "."); 
            fileService.enforceMutationDataFolder(data.getName());
            if (data.isUnformatted()) {
                System.out.println(" User marked data as unformatted. Running DataConverter...");

                String rawInputPath = fileService.findValidRawDataFile("mutation_data");
                System.out.println("Found raw data file for conversion: " + rawInputPath);
                String formattedOutputPath = "mutation_data/formatted_dataset.txt";
                
                fileService.runDataConverter(rawInputPath, formattedOutputPath);

                ConfigData config = configService.getConfig();
                config.setDatasetName(formattedOutputPath);
                configService.updateConfig(config);

                return new MemnarJarStatus("Success", "Zip uploaded, unzipped, formatted, and background files secured.");
            } else {
            System.out.println("✅ Data is already formatted. Skipping DataConverter.");
            
            ConfigData config = configService.getConfig();
            String exactTextFilePath = fileService.findTextFileInDirectory("mutation_data"); 
            config.setDatasetName(exactTextFilePath); 
            configService.updateConfig(config);
            return new MemnarJarStatus("Success", "Zip uploaded, unzipped, and ready for MEMNAR.");
        }

        } catch (Exception e) {
            e.printStackTrace();
            return new MemnarJarStatus("Error", "Upload failed: " + e.getMessage());
        }
    }
}